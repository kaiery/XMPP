package com.softfun_xmpp.network;

import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.softfun_xmpp.application.GlobalContext;
import com.softfun_xmpp.notification.NotificationUtil;
import com.softfun_xmpp.utils.AppUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 下载apk文件
 */
public class DownloadAPK extends AsyncTask<String,Long,Boolean> {
    private long mUpdatefilesize;
    private File file;

    public DownloadAPK(long mUpdatefilesize) {
        this.mUpdatefilesize = mUpdatefilesize;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        NotificationUtil.notification_download_updatefile(100, 0);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        //软件包名
        String packageName = AppUtils.getPackageName(GlobalContext.getInstance());
        String url = params[0];
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = HttpUtil.mOkHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                return false;
            }else if (response.isSuccessful()) {
                ResponseBody body = response.body();

                is = new BufferedInputStream(body.byteStream());
                file = new File(Environment.getExternalStorageDirectory(),packageName+".apk");
                fos = new FileOutputStream(file);

                long downloaded = 0;
                long target = mUpdatefilesize;
                //publishProgress(0L, target);//这里不发布进度值
                byte[] buffer = new byte[1024];
                int len = 0;
                int times = 0;
                while (( len = is.read(buffer))!=-1){
                    fos.write(buffer, 0, len);

                    downloaded += len;
                    //publishProgress(downloaded, target);//这里不发布进度值
                    long tempFileLength = file.length();
                    //!!防止刷新过度。
                    if((times % 1000 ==0)||(tempFileLength == mUpdatefilesize)){
                        //更新通知，更新进度
                        NotificationUtil.notification_download_updatefile_progress(100, Math.round(100 * downloaded / mUpdatefilesize));
                    }
                    if (isCancelled()) {
                        //通知下载失败
                        NotificationUtil.notification_download_failure();
                        return false;
                    }

                    times++;
                }
                return downloaded == target ;
            }
        } catch (Exception e) {
            e.printStackTrace();
            //通知下载失败
            NotificationUtil.notification_download_failure();
            //System.out.println("网络访问异常");
            return false;
        } finally {
            try {
                if(fos != null){
                    fos.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                NotificationUtil.notification_download_failure();
                return false;
            }
        }
        return false;
    }


    @Override
    protected void onProgressUpdate(Long... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        ////System.out.println("下载"+ (result ? "成功" : "失败" ));
        if(result){
            NotificationUtil.notification_download_cancel();
            AppUtils.installApk(GlobalContext.getInstance(),file);
        }else{
            NotificationUtil.notification_download_failure();
            Toast.makeText(GlobalContext.getInstance(),"下载失败",Toast.LENGTH_SHORT).show();
        }
    }
}
