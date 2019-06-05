package com.wudy.locationwudy.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.allen.library.SuperButton;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.interfaces.MyItemDialogListener;
import com.wudy.locationwudy.R;
import com.wudy.locationwudy.bean.User;
import com.wudy.locationwudy.utils.MyApplication;
import com.wudy.locationwudy.utils.StatusBarUtil;
import com.xw.repo.XEditText;

import java.util.Arrays;
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
public class RegisterActivity extends AppCompatActivity {
    @BindView(R.id.goBackBtn)
    TextView goBackBtn;
    @BindView(R.id.et_userName)
    XEditText etUserName;
    @BindView(R.id.et_password)
    XEditText etPassword;
    @BindView(R.id.sb_login)
    SuperButton sbLogin;
    @BindView(R.id.input)
    LinearLayout input;
    @BindView(R.id.iv_bg)
    ImageView ivBg;
    @BindView(R.id.et_type)
    TextView etType;
    @BindView(R.id.et_realname)
    XEditText etRealname;
    private String userName;
    private String password;
    private String username;

    @Override
    @AfterPermissionGranted(1)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
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
    }


    private void startLogin(User user) {

        user.setUsername(etUserName.getText().toString());
        user.setPassword(etPassword.getText().toString());
        user.login(new SaveListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                StyledDialog.dismissLoading(RegisterActivity.this);
                if (e == null) {
                    SPUtils.getInstance().put("username", user.getUsername());
                    SPUtils.getInstance().put("isLogin", true);
                    startActivity(new Intent(RegisterActivity.this, IndoorLocationActivity.class));
                    finish();

                } else {
                    ToastUtils.showShort("登录失败！请检查网络连接");

                }
            }
        });
    }

    @OnClick(R.id.sb_login)
    public void onViewClicked() {

    }

    @OnClick({R.id.et_type, R.id.goBackBtn, R.id.sb_login})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.et_type:
                StyledDialog.buildIosSingleChoose(Arrays.asList(new String[]{"教师", "学生"}), new MyItemDialogListener() {
                    @Override
                    public void onItemClick(CharSequence charSequence, int i) {
                        etType.setText(charSequence);
                    }
                }).show();
                break;
            case R.id.goBackBtn:
                finish();
                break;
            case R.id.sb_login:
                if (StringUtils.isEmpty(etUserName.getText().toString())) {
                    ToastUtils.showShort("请输入用户名");
                    return;
                }
                if (StringUtils.isEmpty(etPassword.getText().toString())) {
                    ToastUtils.showShort("请输入密码");
                    return;
                }
                StyledDialog.buildLoading().show();
                startSearchUser(etUserName.getText().toString(), etPassword.getText().toString(), etType.getText().toString());
                break;
        }
    }

    private void startSearchUser(final String username, final String password, final String type) {
        BmobQuery<User> query = new BmobQuery<>();
        query.addWhereEqualTo("username", username);
        query.setLimit(1);
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null && !list.isEmpty()) {
                    ToastUtils.showShort("你输入的用户名已经被注册");
                } else if (e == null) {
                    startRegister(username, password, type);
                } else {
                    ToastUtils.showShort("登录失败！请检查网络连接");
                }
            }
        });
    }

    private void startRegister(final String username, final String password, final String type) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setUsertype(type);
        user.setRealname(etRealname.getText().toString());
        user.signUp(new SaveListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                if (e == null) {
                    ToastUtils.showShort("注册成功");
                    startLogin(user);
                } else {
                    ToastUtils.showShort("注册失败！请检查网络连接");
                    StyledDialog.dismissLoading(RegisterActivity.this);

                }
            }
        });
    }


}
