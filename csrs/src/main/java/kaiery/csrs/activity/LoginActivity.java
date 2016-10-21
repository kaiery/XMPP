package kaiery.csrs.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ComponentName;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ganfra.materialspinner.MaterialSpinner;
import kaiery.csrs.R;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    /**
     * 系统需要开启的敏感权限键值对
     */
    private final static Map<String,String> permissionMap = new HashMap<>();
    /**
     * Id来标识权限请求许可
     */
    private static final int REQUEST_PERMISS_ID = 0;

    /**
     * 一个虚拟身份验证存储包含用户名和密码。
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    private static final String TAG = "LoginActivity";
    /**
     * 用户登录Task
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mAccountView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;


    private static final String[] ITEMS = {"参保个人", "参保企业", "其他"};
    private ArrayAdapter<String> adapter;
    private MaterialSpinner mSpinner1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_login);
        mAccountView = (AutoCompleteTextView) findViewById(R.id.act_account);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.et_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id==666 || id == EditorInfo.IME_ACTION_GO) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mAccountView.setNextFocusForwardId(R.id.et_password);


        Button mBtnSignIn = (Button) findViewById(R.id.btn_sign_in);
        mBtnSignIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        //
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ITEMS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner1 = (MaterialSpinner) findViewById(R.id.sp_usertype);
        mSpinner1.setAdapter(adapter);
        mSpinner1.setSelection(1,true);
        mSpinner1.setPaddingSafe(0, 0, 0, 0);

    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }
        getLoaderManager().initLoader(0, null, this);
    }




    /**
     * 可能请求必须权限
     * @return -
     */
    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        permissionMap.put(READ_CONTACTS,"需要允许开启读取联系人的权限");
        permissionMap.put(WRITE_EXTERNAL_STORAGE,"需要允许开启可写外部存储的权限");
        int granted = 0;

        for (Map.Entry<String, String> entry : permissionMap.entrySet()) {
            final String permiss = entry.getKey();
            String permisstt = entry.getValue();
            if(checkSelfPermission(permiss)!=PackageManager.PERMISSION_GRANTED){
                if (shouldShowRequestPermissionRationale(permiss)) {
                    Snackbar.make(mAccountView, permisstt, Snackbar.LENGTH_INDEFINITE)
                            .setAction(android.R.string.ok, new View.OnClickListener() {
                                @Override
                                @TargetApi(Build.VERSION_CODES.M)
                                public void onClick(View v) {
                                    requestPermissions(new String[]{permiss}, REQUEST_PERMISS_ID);
                                }
                            }).show();
                } else {
                    //权限请求
                    requestPermissions(new String[]{permiss}, REQUEST_PERMISS_ID);
                }
            }else{
                granted++;
            }
        }
        //如果权限全部授权
        if(granted==permissionMap.size()){
            return true;
        }
        return false;
    }

    /**
     * 回调收到权限请求时已经完成.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISS_ID) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }else{
                if(permissions.length>0){
                    final String _permissions = permissions[0];
                    Snackbar.make(mAccountView, permissionMap.get(_permissions), Snackbar.LENGTH_INDEFINITE)
                            .setAction(android.R.string.ok, new View.OnClickListener() {
                                @Override
                                @TargetApi(Build.VERSION_CODES.M)
                                public void onClick(View v) {
                                    //判断用户是否勾选了 不再提醒
                                    if (!shouldShowRequestPermissionRationale(_permissions)) {
                                        showMessageOKCancel(permissionMap.get(_permissions),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        requestPermissions(new String[] {_permissions}, REQUEST_PERMISS_ID);
                                                    }
                                                },
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        //直接进入系统应用
                                                        Intent i = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                                                        String pkg = "com.android.settings";
                                                        String cls = "com.android.settings.applications.InstalledAppDetails";
                                                        i.setComponent(new ComponentName(pkg, cls));
                                                        i.setData(Uri.parse("package:" + getPackageName()));
                                                        startActivity(i);
                                                    }
                                                });
                                    }else{
                                        //权限请求
                                        requestPermissions(new String[]{_permissions}, REQUEST_PERMISS_ID);
                                    }
                                }
                            }).show();
                }
            }
        }
    }

    /**
     * 当用户勾选了不再提醒权限弹出框后，弹出对话框提示用户去系统应用手动设置权限
     * @param message -
     * @param okListener-
     * @param cancelListener-
     */
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("我知道了", okListener)
                .setNegativeButton("去设置",cancelListener)
                .create()
                .show();
    }


    /**
     * 尝试登录
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mAccountView.setError(null);
        mPasswordView.setError(null);

        String account = mAccountView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(account)) {
            mAccountView.setError(getString(R.string.error_field_required));
            focusView = mAccountView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);


            retrofit();
//            mAuthTask = new UserLoginTask(account, password);
//            mAuthTask.execute((Void) null);
        }
    }

    /**
     * retrofit操作掩饰
     */
    private void retrofit() {
        String BASE_URL = "http://192.168.0.112:8080/softfun/";

    }



    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mAccountView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

