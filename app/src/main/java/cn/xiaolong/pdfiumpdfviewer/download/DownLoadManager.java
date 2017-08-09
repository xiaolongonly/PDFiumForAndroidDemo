package cn.xiaolong.pdfiumpdfviewer.download;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import cn.xiaolong.pdfiumpdfviewer.download.exception.HttpTimeException;
import cn.xiaolong.pdfiumpdfviewer.download.exception.RetryWhenNetworkException;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author xiaolong
 * @version v1.0
 * @function <描述功能>
 * @date 2017/2/10-9:51
 */
public class DownLoadManager {
    /*记录下载数据*/
    private Set<DownLoadInfo> downInfos;
    /*回调sub队列*/
    private HashMap<String, ProgressDownSubscriber> subMap;

    private static DownLoadManager sDownLoadManager;


    private DownLoadManager() {
        downInfos = new HashSet<>();
        subMap = new HashMap<>();
    }

    public static DownLoadManager getDownLoadManager() {
        if (sDownLoadManager == null) {
            synchronized (DownLoadManager.class) {
                if (sDownLoadManager == null) {
                    sDownLoadManager = new DownLoadManager();
                }
            }
        }
        return sDownLoadManager;
    }

    /**
     * 开始下载
     */
    public void startDown(DownLoadInfo info) {
        /*正在下载不处理*/
        if (info == null || subMap.get(info.url) != null) {
            return;
        }
        /*添加回调处理类*/
        ProgressDownSubscriber subscriber = new ProgressDownSubscriber(info);
        /*记录回调sub*/
        subMap.put(info.url, subscriber);
        /*获取service，多次请求公用一个service*/
        ApiService httpService;
        if (downInfos.contains(info)) {
            httpService = info.service;
        } else {
            DownloadInterceptor interceptor = new DownloadInterceptor(subscriber);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            //手动创建一个OkHttpClient并设置超时时间
            builder.connectTimeout(30, TimeUnit.SECONDS);
            builder.addInterceptor(interceptor);
            builder.readTimeout(30, TimeUnit.SECONDS);
            Retrofit retrofit = new Retrofit.Builder()
                    .client(builder.build())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .baseUrl(info.getBaseUrl())
                    .build();
            httpService = retrofit.create(ApiService.class);
            info.service = httpService;
        }
        /*得到rx对象-上一次下載的位置開始下載*/
        httpService.download("bytes=" + info.readLength + "-", info.url)
                /*指定线程*/
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                /*失败后的retry配置*/
                .retryWhen(new RetryWhenNetworkException())
                /*读取下载写入文件*/
                .map(responseBody -> {
                    try {
                        writeCache(responseBody, new File(info.savePath), info);
                    } catch (IOException e) {
                        /*失败抛出异常*/
                        throw new HttpTimeException(e.getMessage());
                    }
                    return info;
                })
                /*回调线程*/
                .observeOn(AndroidSchedulers.mainThread())
                /*数据回调*/
                .subscribe(subscriber);

    }


    /**
     * 停止下载
     */
    public void stopDown(DownLoadInfo info) {
        if (info == null) return;
        info.state = DownState.STOP;
        info.listener.onLoadStop();
        if (subMap.containsKey(info.url)) {
            ProgressDownSubscriber subscriber = subMap.get(info.url);
            subscriber.unsubscribe();
            subMap.remove(info.url);
        }
        /*同步数据库*/
    }


    /**
     * 删除
     *
     * @param info
     */
    public void deleteDown(DownLoadInfo info) {
        stopDown(info);
         /*删除数据库信息和本地文件*/
    }


    /**
     * 暂停下载
     *
     * @param info
     */
    public void pause(DownLoadInfo info) {
        if (info == null) return;
        info.state = DownState.PAUSE;
        info.listener.onLoadPause();
        if (subMap.containsKey(info.url)) {
            ProgressDownSubscriber subscriber = subMap.get(info.url);
            subscriber.unsubscribe();
            subMap.remove(info.url);
        }
        /*这里需要讲info信息写入到数据中，可自由扩展，用自己项目的数据库*/
    }

    /**
     * 停止全部下载
     */
    public void stopAllDown() {
        for (DownLoadInfo downInfo : downInfos) {
            stopDown(downInfo);
        }
        subMap.clear();
        downInfos.clear();
    }

    /**
     * 暂停全部下载
     */
    public void pauseAll() {
        for (DownLoadInfo downInfo : downInfos) {
            pause(downInfo);
        }
        subMap.clear();
        downInfos.clear();
    }


    /**
     * 返回全部正在下载的数据
     *
     * @return
     */
    public Set<DownLoadInfo> getDownInfos() {
        return downInfos;
    }


    /**
     * 写入文件
     *
     * @param file
     * @param info
     * @throws IOException
     */
    public void writeCache(ResponseBody responseBody, File file, DownLoadInfo info) throws IOException {
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        long allLength;
        if (info.countLength == 0) {
            allLength = responseBody.contentLength();
        } else {
            allLength = info.countLength;
        }
        FileChannel channelOut = null;
        RandomAccessFile randomAccessFile = null;
        randomAccessFile = new RandomAccessFile(file, "rwd");
        channelOut = randomAccessFile.getChannel();
        MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE,
                info.readLength, allLength - info.readLength);
        byte[] buffer = new byte[1024 * 8];
        int len;
        int record = 0;
        while ((len = responseBody.byteStream().read(buffer)) != -1) {
            mappedBuffer.put(buffer, 0, len);
            record += len;
        }
        responseBody.byteStream().close();
        if (channelOut != null) {
            channelOut.close();
        }
        if (randomAccessFile != null) {
            randomAccessFile.close();
        }
    }

    public enum DownState {
        START,
        DOWN,
        PAUSE,
        STOP,
        ERROR,
        FINISH,
    }
}
