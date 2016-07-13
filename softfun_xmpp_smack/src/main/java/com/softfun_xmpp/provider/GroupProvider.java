package com.softfun_xmpp.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.softfun_xmpp.dbhelper.GroupDbHelper;

/**
 *  群组内容提供者
 */
public class GroupProvider extends ContentProvider {
    //得到本类的完整路径
    //主机地址的常量
    //跟清单文件中暴露的android:authorities="com.softfun_xmpp.provider.GroupProvider"  一致
    public static final String AUTHORITIES = "com.softfun_xmpp.provider.GroupProvider";//GroupProvider.class.getCanonicalName();


    //地址匹配对象
    static UriMatcher mUriMatcher;
    //对应群组表的一个常量
    //固定写法
    public static Uri URI_GROUP = Uri.parse("content://" + AUTHORITIES + "/group");
    public static final int GROUP = 1;

    static {
        //固定写法，当与传递进来的uri进行匹配，看有没有正确的暗号
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        //添加了一条匹配规则：AUTHORITIES是授权字符串，"/group"：是暗号，如果授权匹配&&暗号匹配，就匹配成功，返回GROUP的代码1，否则就返回UriMatcher.NO_MATCH的代码-1
        mUriMatcher.addURI(AUTHORITIES, "/group", GROUP);
        //外部只要访问 --> content://softfun.softfun_xmpp.provider.ContactsProvider/contact  就可以得到CONTACTD
        //                content://com.softfun_xmpp.provider.ContactsProvider/contact
        //                content://com.softfun_xmpp.provider.ContactsProvider/contact
    }

    private GroupDbHelper mHelper;

    @Override
    public boolean onCreate() {
        mHelper = new GroupDbHelper(getContext());
        if (mHelper != null) {
            return true;
        }
        return false;
    }
    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }












    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int code = mUriMatcher.match(uri);
        switch( code )
        {
            case GROUP:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                long id = db.insert(GroupDbHelper.TABLE_GROUP,"",values);
                if(id>0){
                    //System.out.println("群组插入成功");
                    //拼接最新的uri,重新赋值 ,比如拼接成：   content://softfun.softfun_xmpp.provider.ContactsProvider/contact/id
                    uri = ContentUris.withAppendedId(uri, id);
                    /*====================  通知 内容观察者，数据改变了  =====================*/
                    getContext().getContentResolver().notifyChange(GroupProvider.URI_GROUP,null);//null 为所有的观察者都可以收到
                }
                break;
        }
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int code = mUriMatcher.match(uri);
        int deleteCount = 0;
        switch (code) {
            case GROUP:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                deleteCount = db.delete(GroupDbHelper.TABLE_GROUP, selection, selectionArgs);
                if (deleteCount > 0) {
                    //System.out.println("----------群组删除成功--------");
                    //***通知 ContactContentObserver  数据改变了
                    getContext().getContentResolver().notifyChange(GroupProvider.URI_GROUP,null);//null 为所有的观察者都可以收到
                }
                //db.close();
                break;
        }
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int code = mUriMatcher.match(uri);
        int updateCount = 0;
        switch (code) {
            case GROUP:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                updateCount = db.update(GroupDbHelper.TABLE_GROUP, values, selection, selectionArgs);
                if (updateCount > 0) {
                    //System.out.println("----------群组更新成功--------");
                    //***通知 ContactContentObserver  数据改变了
                    getContext().getContentResolver().notifyChange(GroupProvider.URI_GROUP,null);//null 为所有的观察者都可以收到
                }
                //db.close();
                break;
        }
        return updateCount;
    }
    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int code = mUriMatcher.match(uri);
        Cursor cursor = null;
        switch (code) {
            case GROUP:
                SQLiteDatabase db = mHelper.getReadableDatabase();
                cursor = db.query(GroupDbHelper.TABLE_GROUP, projection, selection, selectionArgs, null, null, sortOrder);
                //System.out.println("----------群组查询成功--------");
                //db.close();
                break;
        }
        return cursor;
    }




}
