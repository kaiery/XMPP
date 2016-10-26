package kaiery.csrs.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import java.util.HashMap;
import java.util.Map;

import fr.ganfra.materialspinner.MaterialSpinner;
import kaiery.csrs.R;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class LoginActivity extends AppCompatActivity {
    /**
     * 系统需要开启的敏感权限键值对
     */
    private final static Map<String,String> permissionMap = new HashMap<>();
    /**
     * Id来标识权限请求许可
     */
    private static final int REQUEST_PERMISS_ID = 0;

    private static final String TAG = "LoginActivity";

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

        mAccountView.setText("testtest");
        mPasswordView.setText("testtest");

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

            goMain();
        }


    }

    private void goMain() {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
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

}

