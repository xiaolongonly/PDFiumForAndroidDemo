package cn.xiaolong.pdfiumpdfviewer.download;



import java.lang.ref.WeakReference;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * 用于在Http请求开始时，自动显示一个ProgressDialog
 * 在Http请求结束是，关闭ProgressDialog
 * 调用者自己对请求数据进行处理
 *
 * @author xiaolong
 * @version v1.0
 * @function <描述功能>
 */
public class ProgressDownSubscriber<T> extends Subscriber<T> implements DownloadProgressListener {
    //弱引用结果回调
    private WeakReference<HttpProgressOnNextListener> mSubscriberOnNextListener;
    /*下载数据*/
    private DownLoadInfo downInfo;


    public ProgressDownSubscriber(DownLoadInfo downInfo) {
        this.mSubscriberOnNextListener = new WeakReference<>(downInfo.listener);
        this.downInfo = downInfo;
    }

    /**
     * 订阅开始时调用
     * 显示ProgressDialog
     */
    @Override
    public void onStart() {
        if (mSubscriberOnNextListener.get() != null) {
            mSubscriberOnNextListener.get().onLoadStart();
        }
        downInfo.state = DownLoadManager.DownState.START;
    }

    /**
     * 完成，隐藏ProgressDialog
     */
    @Override
    public void onCompleted() {
        if (mSubscriberOnNextListener.get() != null) {
            mSubscriberOnNextListener.get().onLoadComplete();
        }
        downInfo.state = DownLoadManager.DownState.FINISH;
    }

    /**
     * 对错误进行统一处理
     * 隐藏ProgressDialog
     *
     * @param e
     */
    @Override
    public void onError(Throwable e) {
        /*停止下载*/
        DownLoadManager.getDownLoadManager().stopDown(downInfo);
        if (mSubscriberOnNextListener.get() != null) {
            mSubscriberOnNextListener.get().onLoadError(e);
        }
        downInfo.state = DownLoadManager.DownState.ERROR;
    }

    /**
     * 将onNext方法中的返回结果交给Activity或Fragment自己处理
     *
     * @param t 创建Subscriber时的泛型类型
     */
    @Override
    public void onNext(T t) {
        if (mSubscriberOnNextListener.get() != null) {
            mSubscriberOnNextListener.get().onNext(t);
        }
    }

    @Override
    public void update(long read, long count, boolean done) {
        if (downInfo.countLength > count) {
            read = downInfo.countLength - count + read;
        } else {
            downInfo.countLength = count;
        }
        downInfo.readLength = read;
        if (mSubscriberOnNextListener.get() != null) {
            /*接受进度消息，造成UI阻塞，如果不需要显示进度可去掉实现逻辑，减少压力*/
            rx.Observable.just(read).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                      /*如果暂停或者停止状态延迟，不需要继续发送回调，影响显示*/
                            if (downInfo.state == DownLoadManager.DownState.PAUSE || downInfo.state == DownLoadManager.DownState.STOP)
                                return;
                            downInfo.state = DownLoadManager.DownState.DOWN;
                            mSubscriberOnNextListener.get().updateProgress(aLong, downInfo.countLength);
                        }
                    });
        }
    }

}