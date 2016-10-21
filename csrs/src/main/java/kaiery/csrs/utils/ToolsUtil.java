package kaiery.csrs.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import kaiery.csrs.application.GlobalContext;

public class ToolsUtil {

    /**
     * 从身份证获取男女
     * @param value
     * @return
     */
    public static String getSexFromIdCard(String value) {
        value = value.trim();
        if (value == null || (value.length() != 15 && value.length() != 18)) {
            return "";
        }
        if (value.length() == 15 || value.length() == 18) {
            String lastValue = value.substring(value.length() - 1, value.length());
            int sex;
            if (lastValue.trim().toLowerCase().equals("x") || lastValue.trim().toLowerCase().equals("e")) {
                return "先生";
            } else {
                sex = Integer.parseInt(lastValue) % 2;
                return sex == 0 ? "女士" : "先生";
            }
        } else {
            return "";
        }
    }


    /**
     * 获取当前时间
     * @return
     */
    public static String getCurrentStamp() {
        SimpleDateFormat formDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String datestr = "";
        try {
            datestr = formDate.format(new Date());
        } catch (Exception ignored) {
        }
        return datestr;
    }

    /**
     * 获取当前时间
     * @param str
     * @return
     */
    public static Date getDateFromString(String str) {
        SimpleDateFormat formDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.SIMPLIFIED_CHINESE);
        Date date;
        try {
            date = formDate.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
            return new Date();
        }
        return date;
    }


    /**
     * @param value
     * @param scale;精度位数(保留的小数位数) ,BigDecimal.ROUND_DOWN,BigDecimal.ROUND_CEILING,BigDecimal.ROUND_FLOOR
     * @param roundingMode;精度取值方式
     * @return
     */
    public static float roundFloat(float value, int scale, int roundingMode) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(scale, roundingMode);
        float d = bd.floatValue();
        bd = null;
        return d;
    }

    public static double roundDouble(float value, int scale, int roundingMode) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(scale, roundingMode);
        double d = bd.doubleValue();
        bd = null;
        return d;
    }


    // 对包含中文的字符串进行转码，此为UTF-8。服务器那边要进行一次解码
    public String decode(String value) throws Exception {
        return URLDecoder.decode(value, "UTF-8");
    }


    // 对包含中文的字符串进行转码，此为UTF-8。服务器那边要进行一次解码
    public static String encode(String value) throws Exception {
        if (value == null) {
            return "";
        }
        return URLEncoder.encode(value, "UTF-8");
    }


    /**
     * 获取键盘高度
     * @param paramActivity
     * @return
     */
    public static int getKeyboardHeight(Activity paramActivity) {

        int height = getScreenHeight(paramActivity) - getStatusBarHeight(paramActivity)
                - getAppHeight(paramActivity);
        if (height == 0) {
            height = SharedPreferencesUtils.getIntShareData("KeyboardHeight", 787);//787为默认软键盘高度 基本差不离
        } else {
            SharedPreferencesUtils.putIntShareData("KeyboardHeight", height);
        }
        return height;
    }

    /**
     * 屏幕分辨率高
     **/
    public static int getScreenHeight(Activity paramActivity) {
        Display display = paramActivity.getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.heightPixels;
    }

    /**
     * statusBar高度
     **/
    public static int getStatusBarHeight(Activity paramActivity) {
        Rect localRect = new Rect();
        paramActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        return localRect.top;

    }

    /**
     * 可见屏幕高度
     **/
    public static int getAppHeight(Activity paramActivity) {
        Rect localRect = new Rect();
        paramActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        return localRect.height();
    }

    /**
     * 关闭键盘
     **/
    public static void hideSoftInput(View paramEditText) {
        ((InputMethodManager) GlobalContext.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(paramEditText.getWindowToken(), 0);
    }

    // below actionbar, above softkeyboard
    public static int getAppContentHeight(Activity paramActivity) {
        return getScreenHeight(paramActivity) - getStatusBarHeight(paramActivity)
                - getActionBarHeight(paramActivity) - getKeyboardHeight(paramActivity);
    }

    /**
     * 获取actiobar高度
     **/
    public static int getActionBarHeight(Activity paramActivity) {
        if (true) {
            return dip2px(56);
        }
        int[] attrs = new int[]{android.R.attr.actionBarSize};
        TypedArray ta = paramActivity.obtainStyledAttributes(attrs);
        return ta.getDimensionPixelSize(0, dip2px(56));
    }

    /**
     * dp转px
     **/
    public static int dip2px(int dipValue) {
        float reSize = GlobalContext.getInstance().getResources().getDisplayMetrics().density;
        return (int) ((dipValue * reSize) + 0.5);
    }

    /**
     * 键盘是否在显示
     **/
    public static boolean isKeyBoardShow(Activity paramActivity) {
        int height = getScreenHeight(paramActivity) - getStatusBarHeight(paramActivity)
                - getAppHeight(paramActivity);
        return height != 0;
    }

    /**
     * 显示键盘
     **/
    public static void showKeyBoard(final View paramEditText) {
        paramEditText.requestFocus();
        paramEditText.post(new Runnable() {
            @Override
            public void run() {
                ((InputMethodManager) GlobalContext.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(paramEditText, 0);
            }
        });
    }


    public static String arrayListToString(ArrayList<String> arrayList) {
        String result = "";
        for (int i = 0; i < arrayList.size(); i++) {
            if (i + 1 == arrayList.size()) {
                result += arrayList.get(i);
            } else {
                result += arrayList.get(i) + ",";
            }
        }
        return result;
    }


    /**
     * 截断群类型字符串，获得 需要展示群类型的文字
     * @param list
     * @return
     */
    public static List<String> fiterList(List<String> list) {
        List<String> ml = new ArrayList<>();
        if (list == null) {
            return ml;
        } else if (list.size() > 0) {
            for (String s : list) {
                ml.add(s.substring(0,s.indexOf("@:")));
            }
        }
        return  ml;
    }


    /**
     * 得到群类型的类型代码
     * @param list
     * @param position
     * @return
     */
    public static String getFiterListId(List<String> list,int position){
        if(list!=null){
            String id = list.get(position);
            id = id.substring(id.indexOf("@:")+"@:".length());
            return id;
        }else{
            return "";
        }
    }


    /**
     * 生成指定范围的随机数
     * @param min
     * @param max
     * @return
     */
    public static String getRandomRect(int min,int max){
        Random random = new Random();
        int s = random.nextInt(max)%(max-min+1) + min;
        if(s<10){
            return "0"+String.valueOf(s);
        }else{
            return String.valueOf(s);
        }
    }




}
