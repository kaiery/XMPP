package softfun.softfun_xmpp;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.softfun_xmpp.dbhelper.ContactsDbHelper;
import com.softfun_xmpp.provider.ContactsProvider;

import opensource.jpinyin.PinyinFormat;
import opensource.jpinyin.PinyinHelper;

/**
 * Created by 范张 on 2016-04-15.
 */
public class TestContactsProvider extends AndroidTestCase {

    public void testInsert() {
        ContentValues values = new ContentValues();
        values.put(ContactsDbHelper.ContactTable.ACCOUNT, "kaiery@softfun.com");
        values.put(ContactsDbHelper.ContactTable.NICKNAME, "超人");
        values.put(ContactsDbHelper.ContactTable.AVATARURL, "0");
        values.put(ContactsDbHelper.ContactTable.PINYIN, "chaoren");
        getContext().getContentResolver().insert(ContactsProvider.URI_CONTACT, values);
    }

    public void testDelete() {
        getContext().getContentResolver().delete(ContactsProvider.URI_CONTACT, ContactsDbHelper.ContactTable.ACCOUNT + "=?", new String[]{"kaiery@softfun.com"});
    }

    public void testUpdate() {
        ContentValues values = new ContentValues();
        values.put(ContactsDbHelper.ContactTable.ACCOUNT, "kaiery1@softfun.com");
        values.put(ContactsDbHelper.ContactTable.NICKNAME, "超人1");
        values.put(ContactsDbHelper.ContactTable.AVATARURL, "0");
        values.put(ContactsDbHelper.ContactTable.PINYIN, "chaoren1");
        getContext().getContentResolver().update(ContactsProvider.URI_CONTACT,values,ContactsDbHelper.ContactTable.ACCOUNT + "=?",new String[]{"kaiery@softfun.com"});
    }

    public void testQuery() {
        try {
            Cursor query = getContext().getContentResolver().query(ContactsProvider.URI_CONTACT, null, null, null, null);
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


    public void testpinyin(){
        String txt = "我好想你";
        System.out.println(PinyinHelper.getShortPinyin(txt));
        System.out.println(PinyinHelper.convertToPinyinString(txt, ",", PinyinFormat.WITHOUT_TONE) );
    }
}

























