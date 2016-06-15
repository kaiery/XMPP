package softfun.softfun_xmpp;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.softfun_xmpp.dbhelper.SmsDbHelper;
import com.softfun_xmpp.provider.SmsProvider;

/**
 * Created by 范张 on 2016-04-18.
 */
public class TestSmsProvider extends AndroidTestCase {

    public void testInsert() {
        ContentValues values = new ContentValues();
        values.put(SmsDbHelper.SmsTable.FROM_ACCOUNT, "kaiery@softfun.com");
        values.put(SmsDbHelper.SmsTable.TO_ACCOUNT, "admin@softfun.com");
        values.put(SmsDbHelper.SmsTable.BODY, "我是测试消息");
        values.put(SmsDbHelper.SmsTable.STATUS, "offline");
        values.put(SmsDbHelper.SmsTable.TYPE, "chat");
        values.put(SmsDbHelper.SmsTable.TIME, System.currentTimeMillis());
        values.put(SmsDbHelper.SmsTable.SESSION_ACCOUNT, "admin@softfun.com");
        getContext().getContentResolver().insert(SmsProvider.URI_SMS, values);
    }

    public void testDelete() {
        getContext().getContentResolver().delete(SmsProvider.URI_SMS, SmsDbHelper.SmsTable.FROM_ACCOUNT + "=?", new String[]{"kaiery@softfun.com"});
    }

    public void testUpdate() {
        ContentValues values = new ContentValues();
        values.put(SmsDbHelper.SmsTable.FROM_ACCOUNT, "kaiery@softfun.com");
        values.put(SmsDbHelper.SmsTable.TO_ACCOUNT, "admin@softfun.com");
        values.put(SmsDbHelper.SmsTable.BODY, "我是测试消息11111");
        values.put(SmsDbHelper.SmsTable.STATUS, "offline");
        values.put(SmsDbHelper.SmsTable.TYPE, "chat");
        values.put(SmsDbHelper.SmsTable.TIME, System.currentTimeMillis());
        values.put(SmsDbHelper.SmsTable.SESSION_ACCOUNT, "admin@softfun.com");
        getContext().getContentResolver().update(SmsProvider.URI_SMS,values,SmsDbHelper.SmsTable.FROM_ACCOUNT + "=?",new String[]{"kaiery@softfun.com"});
    }

    public void testQuery() {
        try {
            Cursor query = getContext().getContentResolver().query(SmsProvider.URI_SMS, null, null, null, null);
            int columnCount = query.getColumnCount();
            while (query.moveToNext()){
                for (int i = 0; i < columnCount; i++) {
                    System.out.print(query.getString(i)+"          ");
                }
                System.out.println("");
            }
            query.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
