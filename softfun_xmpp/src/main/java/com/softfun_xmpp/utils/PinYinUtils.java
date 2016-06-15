package com.softfun_xmpp.utils;

import opensource.jpinyin.PinyinFormat;
import opensource.jpinyin.PinyinHelper;

/**
 * Created by 范张 on 2016-04-15.
 */
public class PinYinUtils {


    public static String getPinYin(String str){
        return PinyinHelper.convertToPinyinString(str, ",", PinyinFormat.WITHOUT_TONE);
    }

    public static String getShortPinYin(String str){
        return PinyinHelper.getShortPinyin(str);
    }
}
