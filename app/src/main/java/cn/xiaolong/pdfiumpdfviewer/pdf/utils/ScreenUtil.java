package cn.xiaolong.pdfiumpdfviewer.pdf.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * @author xiaolong
 * @version v1.0
 * @function <描述功能>
 * @date 2017/3/1-16:55
 */
public class ScreenUtil {

    public static int[] getScreenSize(Context context) {

        int[] size = new int[2];

        WindowManager w = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display d = w.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        d.getMetrics(metrics);
// since SDK_INT = 1;
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        size[0] = widthPixels;
        size[1] = heightPixels /*- getStatusBarHeight(context)*/;
        return size;
    }
}
