package com.wudy.locationwudy.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.allen.library.SuperButton;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hss01248.dialog.StyledDialog;

import com.wudy.locationwudy.R;
import com.wudy.locationwudy.bean.User;
import com.wudy.locationwudy.utils.MyApplication;
import com.wudy.locationwudy.utils.StatusBarUtil;
import com.xw.repo.XEditText;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.allen.library.SuperButton.LEFT_RIGHT;


/**
 * Created by xgx on 2019/4/8 for facesign
 */
public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.et_userName)
    XEditText etUserName;
    @BindView(R.id.et_password)
    XEditText etPassword;
    @BindView(R.id.sb_login)
    SuperButton sbLogin;
    @BindView(R.id.input)
    LinearLayout input;
    @BindView(R.id.tv_registration)
    TextView tvRegistration;
    @BindView(R.id.l_f)
    LinearLayout lF;
    @BindView(R.id.iv_bg)
    ImageView ivBg;
    private String userName;
    private String password;

    @Override
    @AfterPermissionGranted(1)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "图片选择需要以下权限:\n\n1.访问设备上的照片\n\n2.拍照", 1, perms);
        }
        ButterKnife.bind(this);
        Bmob.initialize(this, MyApplication.APPID);

        StatusBarUtil.immersive(this);
        StatusBarUtil.darkMode(this);
        etUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                userName = editable.toString();
                if (!TextUtils.isEmpty(password) && !TextUtils.isEmpty(userName)) {
                    sbLogin.setShapeGradientOrientation(LEFT_RIGHT).setUseShape();
                } else {

                    sbLogin.setShapeGradientOrientation(-1).setUseShape();
                }
            }
        });
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                password = editable.toString();
                if (!TextUtils.isEmpty(password) && !TextUtils.isEmpty(userName)) {
                    sbLogin.setShapeGradientOrientation(LEFT_RIGHT).setShapeUseSelector(true)
                            .setShapeSelectorPressedColor(getResources().getColor(R.color.sGradientEndColor)
                            ).setUseShape();
                } else {
                    sbLogin.setShapeGradientOrientation(-1).setUseShape();
                }
            }
        });
        if (SPUtils.getInstance().getBoolean("isLogin", false)) {
            startActivity(new Intent(LoginActivity.this, IndoorLocationActivity.class));
            finish();
        }
    }


    private void startSearchUser(final String username, final String password) {
        BmobQuery<User> query = new BmobQuery<>();
        query.addWhereEqualTo("username", username);
        query.setLimit(1);
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                StyledDialog.dismissLoading(LoginActivity.this);
                if (e == null && !list.isEmpty()) {
                    startLogin(list.get(0));
                } else if (e == null) {
                    ToastUtils.showShort("你输入的用户名没有注册");
                } else {
                    ToastUtils.showShort("登录失败！请检查网络连接");
                }
            }
        });
    }


    private void startLogin(User user) {

        user.setUsername(etUserName.getText().toString());
        user.setPassword(etPassword.getText().toString());
        user.login(new SaveListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                StyledDialog.dismissLoading(LoginActivity.this);

                if (e == null) {
                    SPUtils.getInstance().put("username", user.getUsername());
                    SPUtils.getInstance().put("isLogin", true);
                    startActivity(new Intent(LoginActivity.this, IndoorLocationActivity.class));
                    finish();
                } else {
                    ToastUtils.showShort(e.getMessage());

                }
            }
        });
    }

    @OnClick(R.id.sb_login)
    public void onViewClicked() {
        if (StringUtils.isEmpty(etUserName.getText().toString())) {
            ToastUtils.showShort("请输入用户名");
            return;
        }
        if (StringUtils.isEmpty(etPassword.getText().toString())) {
            ToastUtils.showShort("请输入密码");
            return;
        }
        StyledDialog.buildLoading().show();
        startSearchUser(etUserName.getText().toString(), etPassword.getText().toString());
    }

    @OnClick(R.id.tv_registration)
    public void onRegistrClicked() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
