package cn.xiaolong.pdfiumpdfviewer.download;


/**
 * @author xiaolong
 * @version v1.0
 * @function <描述功能>
 * @date 2017/2/10-9:53
 */
public class DownLoadInfo {
    /*存储位置*/
    public String savePath;
    /*下载url*/
    public String url;
    /*文件总长度*/
    public long countLength;
    /*下载长度*/
    public long readLength;
    /*下载唯一的HttpService*/
    public ApiService service;
    /*回调监听*/
    public HttpProgressOnNextListener listener;
    /*下载状态*/
    public DownLoadManager.DownState state;


    public String getBaseUrl() {
        String baseUrl = url;
        String head = "";
        int index = baseUrl.indexOf("://");
        if (index != -1) {
            head = baseUrl.substring(0, index + 3);
            baseUrl = baseUrl.substring(index + 3);
        }
        index = baseUrl.indexOf("/");
        if (index != -1) {
            baseUrl = baseUrl.substring(0, index + 1);
        }
        return head + baseUrl;
    }
}
