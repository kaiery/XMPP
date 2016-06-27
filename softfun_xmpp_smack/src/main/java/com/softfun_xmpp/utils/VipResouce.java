package com.softfun_xmpp.utils;


import com.softfun_xmpp.R;

/**
 * Created by 范张 on 2016-02-15.
 */
public class VipResouce {

    public static int getVipResouce(String vip) {
        int resouce = 0;
        switch (vip) {
            case "1": {
                resouce = R.mipmap.vip1;
                break;
            }
            case "2": {
                resouce = R.mipmap.vip2;
                break;
            }
            case "3": {
                resouce = R.mipmap.vip3;
                break;
            }
            case "4": {
                resouce = R.mipmap.vip4;
                break;
            }
            case "5": {
                resouce = R.mipmap.vip5;
                break;
            }
            case "6": {
                resouce = R.mipmap.vip6;
                break;
            }
            case "7": {
                resouce = R.mipmap.vip7;
                break;
            }
            case "8": {
                resouce = R.mipmap.vip8;
                break;
            }
            case "9": {
                resouce = R.mipmap.vip9;
                break;
            }
            case "10": {
                resouce = R.mipmap.vip10;
                break;
            }
        }
        return resouce;
    }
}
