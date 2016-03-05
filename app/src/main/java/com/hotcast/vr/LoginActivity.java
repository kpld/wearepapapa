package com.hotcast.vr;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.gson.Gson;
import com.hotcast.vr.bean.User1;
import com.hotcast.vr.bean.User2;
import com.hotcast.vr.tools.Constants;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.InjectView;

/**
 * Created by lostnote on 16/3/3.
 */
public class LoginActivity extends BaseActivity {
    boolean islook = false;
    @InjectView(R.id.tv_login)
    TextView tv_login;
    @InjectView(R.id.iv_look)
    ImageView iv_look;
    @InjectView(R.id.et_password)
    EditText et_password;
    @InjectView(R.id.et_username)
    EditText et_username;
    @InjectView(R.id.bt_login)
    Button bt_login;
    @InjectView(R.id.iv_return)
    ImageView iv_return;

    String username;
    String password;


    @Override
    public int getLayoutId() {
        return R.layout.activity_login;
    }

    boolean canLogin = false;
    @Override
    public void init() {
        bt_login.setOnClickListener(this);
        iv_look.setOnClickListener(this);
        iv_return.setOnClickListener(this);
        et_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 11){
                    canLogin = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (canLogin && s.length() >= 6){
                    bt_login.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        tv_login.setText("登录");
    }

    @Override
    public void getIntentData(Intent intent) {

    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_login:
                username = et_username.getText().toString().trim();
                password = et_password.getText().toString().trim();
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
                    showToast("亲，手机号或密码不能为空为空哦^_^");
                }
                login(username, password);
                System.out.println("---点击了登录按钮");

                break;
            case R.id.iv_look:
                if (islook){
                    et_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    iv_look.setImageResource(R.mipmap.look);
                    System.out.println("---点击了密码不可见");
                    islook = false;
                }else {
                    et_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    iv_look.setImageResource(R.mipmap.unlook);
                    System.out.println("---点击了密码可见");
                    islook = true;
                }
                break;
            case R.id.iv_return:
                finish();
                break;
        }
    }

    User2 user2;
    private void login(String username, String password) {
        String mUrl = Constants.LOGIN;
        RequestParams params = new RequestParams();
        params.addBodyParameter("token", "123");
        params.addBodyParameter("version", BaseApplication.version);
        params.addBodyParameter("platform", BaseApplication.platform);
        params.addBodyParameter("phone",username);
        params.addBodyParameter("password",password);
        this.httpPost(mUrl, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                super.onStart();

            }
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                System.out.println("---responseInfo.result = " + responseInfo.result);

                    try {
                        JSONObject j = new JSONObject(responseInfo.result);

                        JSONObject data = j.getJSONObject("data");
                        System.out.println("---1");
                        if (data.length()>5){
                            user2 = new Gson().fromJson(responseInfo.result, User2.class);
                            System.out.println("---user2 = " + user2);
                            if ("success".equals(user2.getMessage()) || 0 <= user2.getCode() && 10 >= user2.getCode()) {
                                BaseApplication.isLogin = true;
                                showToast("亲,登录成功了哟，快去看片儿吧*_*" );
                                sp.add("userData",user2.getData());
                                finish();
                            } else {
                                bt_login.setEnabled(false);
                                showToast("亲," + user2.getMessage() + "^_^");
                                System.out.println("---message=" +user2.getMessage());
                            }
                        }else{
                            JSONObject message = j.getJSONObject("message");
                            showToast("亲," + message + "^_^");
                            System.out.println("---message=" + message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }



            }
            @Override
            public void onFailure(HttpException e, String s) {
                bt_login.setEnabled(false);
                showToast("亲，验证码获取失败T_T，请重新获取");
            }
        });
    }

}
