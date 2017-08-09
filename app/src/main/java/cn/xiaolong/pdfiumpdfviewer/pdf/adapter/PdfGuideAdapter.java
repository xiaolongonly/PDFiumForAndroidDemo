package cn.xiaolong.pdfiumpdfviewer.pdf.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.concurrent.ConcurrentHashMap;

import cn.xiaolong.pdfiumpdfviewer.R;
import cn.xiaolong.pdfiumpdfviewer.pdf.PDFManager;
import cn.xiaolong.pdfiumpdfviewer.pdf.list.ViewHolder;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;


/**
 * @author xiaolong
 * @version v1.0
 * @function <左侧列表适配器>
 * @date 2017/2/27-14:50
 */
public class PdfGuideAdapter extends BaseAdapter {

    private PDFManager mPdfManager;
    private Context mContext;
    private ConcurrentHashMap<Integer, Bitmap> mConcurrentHashMap;
    private View.OnClickListener mOnItemClickListener;
    private int[] mStates;
    private int count;

    public PdfGuideAdapter(Context context, PDFManager pdfManager) {
        this.mPdfManager = pdfManager;
        this.mContext = context;
        mConcurrentHashMap = new ConcurrentHashMap();
        count = mPdfManager.pageCount();
        mStates = new int[count];
    }

    @Override
    public int getCount() {
        return mPdfManager.pageCount();
    }

    @Override
    public Object getItem(int position) {
//        return mPdfManager.getPdfBitmapNormalSize(position);
        return "sb";
    }

    public void setOnItemClickListener(View.OnClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = ViewHolder.get(mContext, convertView, parent,
                R.layout.listitem_guide, position);
        TextView tvGuideName = viewHolder.getView(R.id.tvLocate);
        final ImageView imageView = viewHolder.getView(R.id.imageView);
        getBitmap(position).observeOn(AndroidSchedulers.mainThread()).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Bitmap>() {
                    @Override
                    public void onStart() {
                        super.onStart();
//                        imageView.setImageResource(R.drawable.ic_delete);
                    }

                    @Override
                    public void onCompleted() {
//                Log.d("adapterloadCompleted","completed");

                    }

                    @Override
                    public void onError(Throwable e) {
//                Log.d("adapterError",e.getMessage());
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        imageView.setImageBitmap(bitmap);
                    }
                });
        tvGuideName.setText(position + 1 + "");
        if (mStates[position] == 1) {
            imageView.setSelected(true);
        } else {
            imageView.setSelected(false);
        }
        if (mOnItemClickListener != null) {
            imageView.setOnClickListener(v -> {
                v.setTag(position);
                mOnItemClickListener.onClick(v);
                viewHolder.getConvertView().setSelected(false);
                setStatePosition(position);
            });
        }
        return viewHolder.getConvertView();
    }

    public Observable<Bitmap> getBitmap(final int position) {
        return mConcurrentHashMap.get(position) == null ?
                mPdfManager.getPdfBitmapCustomSize(position, 160)
                        .doOnNext(bitmap -> mConcurrentHashMap.put(position, bitmap)) : Observable.just(mConcurrentHashMap.get(position));
    }

    public void setStatePosition(int position) {
        mStates = new int[count];
        mStates[position] = 1;
        notifyDataSetChanged();
    }
}
