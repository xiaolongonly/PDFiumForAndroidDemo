package cn.xiaolong.pdfiumpdfviewer.pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import cn.xiaolong.pdfiumpdfviewer.pdf.source.AssetSource;
import cn.xiaolong.pdfiumpdfviewer.pdf.source.ByteArraySource;
import cn.xiaolong.pdfiumpdfviewer.pdf.source.DocumentSource;
import cn.xiaolong.pdfiumpdfviewer.pdf.source.FileSource;
import cn.xiaolong.pdfiumpdfviewer.pdf.source.InputStreamSource;
import cn.xiaolong.pdfiumpdfviewer.pdf.source.UriSource;
import rx.Observable;
import rx.schedulers.Schedulers;
//参照着读取文件流的封装。如果想要把manager这个类变得可扩展，
// 比如说后面我们添加了DocManager..等等图片生成的Manager
//这个时候就应该把用到的方法抽出来，图片生成的方法，资源回收的方法等。
// 获取总数的方法抽到某个interface/父类中去，通过继承还有接口实现来达到目的。
// 到那时候就可以直接使用父类的Manager来指定自己需要实例化的对象。
//这也正是封装，继承和多态的妙处

/**
 * @author xiaolong
 * @version v1.0
 * @function <描述功能>
 * @date 2017/2/27-17:15
 */
public class PDFManager {
    private static String TAG = "PDF";
    private Context mContext;
    private PdfiumCore mPdfiumCore;
    private PdfDocument mPdfDocument;
    private int pageCount;
    private int realWidth = 0;
    private int realHeight = 0;

    private PDFManager(Context context, DocumentSource documentSource, String password,
                       OnOpenErrorListener onErrorListener, OnOpenSuccessListener onOpenSuccessListener) {
        mContext = context;
        mPdfiumCore = new PdfiumCore(context);
        try {
            mPdfDocument = documentSource.createDocument(context, mPdfiumCore, password);
            pageCount = mPdfiumCore.getPageCount(mPdfDocument);
            if (onOpenSuccessListener != null)
                onOpenSuccessListener.onSuccess();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "PDF Couldn't Open");
            if (onErrorListener != null)
                onErrorListener.onError(e.getCause());
        }

    }

    public Observable<Bitmap> getPdfBitmapNormalSize(final int pageIndex) {
        return Observable.create((Observable.OnSubscribe<Bitmap>) subscriber -> {
            mPdfiumCore.openPage(mPdfDocument, pageIndex);
            if (realWidth == 0) {
                realWidth = mPdfiumCore.getPageWidthPoint(mPdfDocument, pageIndex);
                realHeight = mPdfiumCore.getPageHeightPoint(mPdfDocument, pageIndex);
            }
            Bitmap bitmap = Bitmap.createBitmap(realWidth * 2, realHeight * 2,
                    Bitmap.Config.ARGB_8888);
            mPdfiumCore.renderPageBitmap(mPdfDocument, bitmap, pageIndex, 0, 0,
                    realWidth, realWidth);
            subscriber.onNext(bitmap);
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io());


//        printInfo(mPdfiumCore, mPdfDocument);
//        return Observable.just(bitmap);
    }

    public Observable<Bitmap> getPdfBitmapCustomSize(final int pageIndex, final int width) {
        return Observable.create((Observable.OnSubscribe<Bitmap>) subscriber -> {
            mPdfiumCore.openPage(mPdfDocument, pageIndex);
            if (realWidth == 0) {
                realWidth = mPdfiumCore.getPageWidthPoint(mPdfDocument, pageIndex);
                realHeight = mPdfiumCore.getPageHeightPoint(mPdfDocument, pageIndex);
            }
            int height = (realHeight * width) / realWidth;
            Bitmap bitmap = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ARGB_8888);
            mPdfiumCore.renderPageBitmap(mPdfDocument, bitmap, pageIndex, 0, 0,
                    width, height);
            subscriber.onNext(bitmap);
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io());

//        printInfo(mPdfiumCore, mPdfDocument);
    }

    public int pageCount() {
        return pageCount;
    }

    /**
     * this must do when all work have done, to recycle the memory
     */
    public void recycle() {
        if (mPdfDocument != null) {
            mPdfiumCore.closeDocument(mPdfDocument); // important!
        }
    }

    public void printInfo(PdfiumCore core, PdfDocument doc) {
        PdfDocument.Meta meta = core.getDocumentMeta(doc);
        Log.e(TAG, "title = " + meta.getTitle());
        Log.e(TAG, "author = " + meta.getAuthor());
        Log.e(TAG, "subject = " + meta.getSubject());
        Log.e(TAG, "keywords = " + meta.getKeywords());
        Log.e(TAG, "creator = " + meta.getCreator());
        Log.e(TAG, "producer = " + meta.getProducer());
        Log.e(TAG, "creationDate = " + meta.getCreationDate());
        Log.e(TAG, "modDate = " + meta.getModDate());

        printBookmarksTree(core.getTableOfContents(doc), "-");

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    public static class Builder {
        private DocumentSource mDocumentSource;
        private Context context;
        private String password;
        private OnOpenErrorListener mOnErrorListener;
        private OnOpenSuccessListener mOnOpenSuccessListener;
        private static final int KILOBYTE = 1024;
        private static final int BUFFER_LEN = 1 * KILOBYTE;
        private static final int NOTIFY_PERIOD = 150 * KILOBYTE;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder pdfFromFile(File file) {
            mDocumentSource = new FileSource(file);
            return this;
        }

        public Builder pdfFromStream(InputStream inputStream) {
            mDocumentSource = new InputStreamSource(inputStream);
            return this;
        }

        public Builder pdfFromUri(Uri uri) {
            mDocumentSource = new UriSource(uri);
            return this;
        }

        public Builder pdfFormAsset(String assetName) {
            mDocumentSource = new AssetSource(assetName);
            return this;
        }

        public Builder pdfFromByte(byte[] data) {
            mDocumentSource = new ByteArraySource(data);
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setOnOpenErrorListener(OnOpenErrorListener onErrorListener) {
            this.mOnErrorListener = onErrorListener;
            return this;
        }

        public Builder setOnOpenSuccessListener(OnOpenSuccessListener onOpenSuccessListener) {
            this.mOnOpenSuccessListener = onOpenSuccessListener;
            return this;
        }

        public PDFManager build() {
            return new PDFManager(context, mDocumentSource, password, mOnErrorListener, mOnOpenSuccessListener);
        }
    }

    public interface OnOpenErrorListener {
        /**
         * Called if error occurred while opening PDF
         *
         * @param t Throwable with error
         */
        void onError(Throwable t);
    }

    public interface OnOpenSuccessListener {
        void onSuccess();
    }
}
