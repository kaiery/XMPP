package com.softfun.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 范张 on 2016-05-24.
 */
public class MatherListUtil {

    public static List<ListOPBean> compare(List<String> list1, List<String> list2) {
        List<ListOPBean> newList = new ArrayList<ListOPBean>();
        for (String t : list2) {
            if (!list1.contains(t)) {
                ListOPBean bean1 = new ListOPBean();
                bean1.str = t;
                bean1.op = "+";
                newList.add(bean1);
            }
        }
        for (String t : list1) {
            if (!list2.contains(t)) {
                ListOPBean bean = new ListOPBean();
                bean.str = t;
                bean.op = "-";
                newList.add(bean);
            }
        }
        return newList;
    }



    public static  class ListOPBean {
        private String op;
        private String str;

        public String getOp() {
            return op;
        }
        public void setOp(String op) {
            this.op = op;
        }
        public String getStr() {
            return str;
        }
        public void setStr(String str) {
            this.str = str;
        }
    }
}
