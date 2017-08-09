package cn.xiaolong.pdfiumpdfviewer.pdf.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;


import cn.xiaolong.pdfiumpdfviewer.R;
import cn.xiaolong.pdfiumpdfviewer.pdf.PDFManager;
import cn.xiaolong.pdfiumpdfviewer.pdf.utils.ScreenUtil;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import uk.co.senab.photoview.PhotoView;

/**
 * @author xiaolong
 * @version v1.0
 * @function <ViewPager适配器>
 * @date 2016/7/18-16:57
 */
public class PdfImageAdapter extends PagerAdapter {
    private Context context;
    private PDFManager mPDFManager;

    public PdfImageAdapter(Context context, PDFManager pdfManager) {
        this.context = context;
        mPDFManager = pdfManager;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        Log.d("msg", "page" + (position + 1) + "destory");
    }


    @Override
    public int getCount() {
        return mPDFManager.pageCount();
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View contentView = View.inflate(context, R.layout.item_viewpage_image, null);
        final PhotoView ivLargeImage = (PhotoView) contentView.findViewById(R.id.ivLargeImage);
        mPDFManager.getPdfBitmapCustomSize(position, ScreenUtil.getScreenSize(context)[0] * 7 / 8)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Bitmap>() {
                    @Override
                    public void onStart() {
                        ivLargeImage.setImageResource(R.mipmap.ic_launcher);
                    }

                    @Override
                    public void onCompleted() {
//                        Log.d("adapterloadCompleted", "completed");
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        ivLargeImage.setImageBitmap(bitmap);
                    }
                });
        container.addView(contentView);
        return contentView;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }
}
