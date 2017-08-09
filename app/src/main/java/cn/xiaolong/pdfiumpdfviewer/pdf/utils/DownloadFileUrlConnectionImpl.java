/*
 * Copyright (C) 2016 Olmo Gallegos Hernández.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.xiaolong.pdfiumpdfviewer.pdf.utils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadFileUrlConnectionImpl implements DownloadFile {
    private static final int KILOBYTE = 1024;

    private static final int BUFFER_LEN = 1 * KILOBYTE;
    private static final int NOTIFY_PERIOD = 150 * KILOBYTE;

    Context context;
    Handler uiThread;
    DownloadListener listener;
    private String mUrl = "";
    private String mFilePath = "";

    public DownloadFileUrlConnectionImpl(Context context, Handler uiThread, DownloadListener listener) {
        this.context = context;
        this.uiThread = uiThread;
        this.listener = listener;
    }

    @Override
    public void download(final String url, final String destinationPath) {
        if (mUrl.equals("") || mFilePath.equals("")) {
            this.mUrl = url;
            this.mFilePath = destinationPath;
        }
        new Thread(() -> {
            try {
                File file = new File(destinationPath);
                FileOutputStream fileOutput = new FileOutputStream(file);
                HttpURLConnection urlConnection = null;
                URL urlObj = new URL(url);
                urlConnection = (HttpURLConnection) urlObj.openConnection();
                int totalSize = urlConnection.getContentLength();
                int downloadedSize = 0;
                int counter = 0;
                byte[] buffer = new byte[BUFFER_LEN];
                int bufferLength = 0;
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                while ((bufferLength = in.read(buffer)) > 0) {
                    fileOutput.write(buffer, 0, bufferLength);
                    downloadedSize += bufferLength;
                    notifyProgressOnUiThread(downloadedSize, totalSize);
                }

                urlConnection.disconnect();
                fileOutput.close();
                notifySuccessOnUiThread(url, destinationPath);
            } catch (MalformedURLException e) {
                notifyFailureOnUiThread(e);
            } catch (IOException e) {
                notifyFailureOnUiThread(e);
            }

        }).start();
    }

    @Override
    public void reload() {
        download(mUrl, mFilePath);
    }

    protected void notifySuccessOnUiThread(final String url, final String destinationPath) {
        if (uiThread == null) {
            return;
        }
        Log.e("DownLoad", "Success:" + url + " " + destinationPath);
        uiThread.post(() -> listener.onSuccess(url, destinationPath));
    }

    protected void notifyFailureOnUiThread(final Exception e) {
        if (uiThread == null) {
            return;
        }
        Log.e("DownLoad", "Fail:" + e.toString());
        uiThread.post(() -> listener.onFailure(e));
    }

    private void notifyProgressOnUiThread(final int downloadedSize, final int totalSize) {
        if (uiThread == null) {
            return;
        }

        uiThread.post(() -> listener.onProgressUpdate(downloadedSize, totalSize));
    }


}
