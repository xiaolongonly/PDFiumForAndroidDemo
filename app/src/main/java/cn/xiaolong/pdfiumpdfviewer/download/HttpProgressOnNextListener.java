package cn.xiaolong.pdfiumpdfviewer.download;

/**
 * 下载过程中的回调处理
 *
 * @author xiaolong
 * @version v1.0
 * @function <描述功能>
 */
public interface HttpProgressOnNextListener<T> {
    /**
     * 成功后回调方法
     *
     * @param t
     */
    void onNext(T t);

    /**
     * 开始下载
     */
    void onLoadStart();

    /**
     * 完成下载
     */
    void onLoadComplete();


    /**
     * 下载进度
     *
     * @param readLength
     * @param countLength
     */
    void updateProgress(long readLength, long countLength);

    /**
     * 失败或者错误方法
     * 主动调用，更加灵活
     *
     * @param e
     */
    void onLoadError(Throwable e);

    /**
     * 暂停下载
     */
    void onLoadPause();

    /**
     * 停止下载销毁
     */
    void onLoadStop();
}
