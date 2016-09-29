package com.softfun_xmpp.log;

import android.os.Environment;

import com.softfun_xmpp.utils.ToolsUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by 范张 on 2016-07-08.
 */
public class SoftFunLog {

    private File file;

    private SoftFunLog() {
        if(file==null){
            file = new File(Environment.getExternalStorageDirectory(),"softfun_logs.txt");
        }
    }

    private static class SingletonHolder {
        private final static SoftFunLog INSTANCE = new SoftFunLog();
    }
    public static SoftFunLog getInstance() {
        return SingletonHolder.INSTANCE;
    }


    /**
     * 追加写日志
     * @param conent
     */
    public void writeLog(String conent,String type){
        if(type==null){
            type = "info";
        }else if(type.contains("i")){
            type = "info";
        }else if(type.contains("e")){
            type = "err ";
        }else if(type.contains("w")){
            type = "warn";
        }


        BufferedWriter out = null ;
        try  {
            out = new  BufferedWriter( new OutputStreamWriter(new  FileOutputStream(file,  true )));
            out.write(type+"  "+ ToolsUtil.getCurrentStamp()+":  "+conent);
            out.newLine();
        } catch  (Exception e) {
            //e.printStackTrace();
        } finally  {
            try  {
                if (out != null) {
                    out.close();
                }
            } catch  (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
