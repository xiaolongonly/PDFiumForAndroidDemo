package cn.xiaolong.pdfiumpdfviewer.pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author xiaolong
 * @version v1.0
 * @function <描述功能>
 * @date 2017/2/10-11:02
 */
public class CircleProgressBar extends View {
    private int width;// 控件的宽度
    private int height;// 控件的高度
    private int radius;// 圆形的半径
    private int socktwidth = dp2px(8);// 圆环进度条的宽度
    private Paint paint = new Paint();
    private Rect rec = new Rect();
    private int value = 70;// 百分比0~100;
    private int textSize = dp2px(18);// 文字大小
    private Bitmap bitmap;
    @Deprecated
    float scale = 0.15f;// 中间背景图片相对圆环的大小的比例
    private int preColor = Color.parseColor("#2c2200");// 进度条未完成的颜色
    private int progressColor = Color.parseColor("#6bb849");// 进度条颜色
    private float paddingscale = 0.8f;// 控件内偏距占空间本身的比例
    private int CircleColor = Color.parseColor("#CCCCCC");// 圆中间的背景颜色
    private int textColor = progressColor;// 文字颜色
    private onProgressListener monProgress;// 进度时间监听
    private int startAngle = 270;
    RectF rectf = new RectF();

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        width = getWidth();
        int size = height = getHeight();
        if (height > width)
            size = width;
        radius = (int) (size * paddingscale / 2f);
        paint.setAntiAlias(true);
        paint.setColor(progressColor);
        // 绘制最大的圆 进度条圆环的背景颜色（未走到的进度）就是这个哦
        canvas.drawCircle(width / 2, height / 2, radius, paint);
        paint.setColor(preColor);
        canvas.drawCircle(width / 2, height / 2, radius-1, paint);
        rectf.set((width - radius * 2) / 2f, (height - radius * 2) / 2f,
                ((width - radius * 2) / 2f) + (2 * radius),
                ((height - radius * 2) / 2f) + (2 * radius));
        paint.setColor(progressColor);
        canvas.drawArc(rectf, startAngle, value * 3.6f, true, paint);
        paint.setColor(CircleColor);
        // 绘制用于遮住伞形两个边的小圆
        canvas.drawCircle(width / 2, height / 2, radius - socktwidth, paint);
        if (bitmap != null) {// 绘制中间的图片
            int width2 = (int) (rectf.width() * scale);
            int height2 = (int) (rectf.height() * scale);
            rectf.set(rectf.left + width2, rectf.top + height2, rectf.right
                    - width2, rectf.bottom - height2);
            canvas.drawBitmap(bitmap, null, rectf, null);
        }
        String v = value + "%";
        paint.setColor(textColor);
        paint.setTextSize(textSize);
        paint.getTextBounds(v, 0, v.length(), rec);
        int textwidth = rec.width();
        int textheight = rec.height();
        // 绘制中间文字
        canvas.drawText(v, (width - textwidth) / 2,
                ((height + textheight) / 2), paint);
        super.onDraw(canvas);
    }

    public int dp2px(int dp) {
        return (int) ((getResources().getDisplayMetrics().density * dp) + 0.5);
    }

    /**
     * 设置进度
     *
     * @param value <p>
     *              ps: 百分比 0~100;
     */
    public void setValue(int value) {
        if (value > 100)
            return;
        this.value = value;
        invalidate();
        if (monProgress != null)
            monProgress.onProgress(value);
    }

    /**
     * 设置圆环进度条的宽度 px
     */
    public CircleProgressBar setProdressWidth(int width) {
        this.socktwidth = width;
        return this;
    }

    /**
     * 设置文字大小
     *
     * @param value
     */
    public CircleProgressBar setTextSize(int value) {
        textSize = value;
        return this;
    }

    /**
     * 设置文字大小
     */
    public CircleProgressBar setTextColor(int color) {
        this.textColor = color;
        return this;
    }

    /**
     * 设置进度条之前的颜色
     */
    public CircleProgressBar setPreProgress(int precolor) {
        this.preColor = precolor;
        return this;
    }

    /**
     * 设置进度颜色
     *
     * @param color
     */
    public CircleProgressBar setProgress(int color) {
        this.progressColor = color;
        return this;
    }

    /**
     * 设置圆心中间的背景颜色
     *
     * @param color
     * @return
     */
    public CircleProgressBar setCircleBackgroud(int color) {
        this.CircleColor = color;
        return this;
    }

    /**
     * 设置圆相对整个控件的宽度或者高度的占用比例
     *
     * @param scale
     */
    public CircleProgressBar setPaddingscale(float scale) {
        this.paddingscale = scale;
        return this;
    }

    /**
     * 设置开始的位置
     *
     * @param startAngle 0~360
     *                   <p>
     *                   ps 0代表在最右边 90 最下方 按照然后顺时针旋转
     */
    public CircleProgressBar setStartAngle(int startAngle) {
        this.startAngle = startAngle;
        return this;
    }

    public interface onProgressListener {
        void onProgress(int value);
    }
}
