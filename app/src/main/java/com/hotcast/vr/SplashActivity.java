package com.hotcast.vr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hotcast.vr.asynctask.LocalVideosAsynctask;
import com.hotcast.vr.bean.Classify;
import com.hotcast.vr.bean.Update;
import com.hotcast.vr.services.DownLoadingService;
import com.hotcast.vr.tools.Constants;
import com.hotcast.vr.tools.L;
import com.hotcast.vr.tools.Utils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.umeng.analytics.AnalyticsConfig;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.InjectView;

/**
 * Created by joey on 8/6/15.
 */
public class SplashActivity extends BaseActivity {
    @InjectView(R.id.vp_guide)
    ViewPager vp_guide;
    @InjectView(R.id.iv_start)
    ImageView iv_start;

//    显示下载更新对话框
    protected static final int SHOW_UPDATE_DIALOG = 1;
//    加载主UI界面
    private static final int LOAD_MAINUI = 2;
//    包管理器
    private PackageManager packageManager;

    private String requestUrl;
    //下载路径
    private String spec ;
    //更新日志
    private String newFeatures;
    //是否强制更新
    private String is_force;
    private int[] images = {R.mipmap.guide_1,R.mipmap.guide_2,R.mipmap.guide_3,R.mipmap.guide_4};

    private Timer timer;
    boolean  isFrist ;
    @Override
    public int getLayoutId() {
        return R.layout.activity_splash;
    }
    private PackageInfo info;
    @Override
    public void init() {
        AnalyticsConfig.enableEncrypt(true);
        requestUrl = Constants.URL_UPDATE;
        packageManager = this.getPackageManager();
        try {
            info = packageManager.getPackageInfo(this.getPackageName(), 0);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        new LocalVideosAsynctask(this).execute();
        L.e("PackageName:" + getPackageName());
        getNetDate();
//        getNetDate2();
            System.out.println("***sp=" + sp);

    }


    private void getNetDate() {
        RequestParams params = new RequestParams();
        params.addBodyParameter("token", "123");
        params.addBodyParameter("version", BaseApplication.version);
        params.addBodyParameter("platform", BaseApplication.platform);
        this.httpPost(requestUrl, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                L.e("DetailActivity responseInfo:" + responseInfo.result);
                setViewData(responseInfo.result);
            }

            @Override
            public void onFailure(HttpException e, String s) {
            }
        });
    }
   private String version;
    private void setViewData(String json){
        if (Utils.textIsNull(json)) {
            return;
        }
        Update update = new Gson().fromJson(json, Update.class);
        spec = update.getUrl();
        is_force = update.getIs_force();
        version = update.getVersion();
        System.out.println("--version = " + version);
        newFeatures = update.getLog();
//        System.out.println("***SplashActivity spec:" + spec + ",force:" + force);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void getIntentData(Intent intent) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        startJmp();
    }

    Handler shanler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            System.out.println("---第一次运行，显示引导页");
            vp_guide.setAdapter(new MyAdapter());
            vp_guide.setVisibility(View.VISIBLE);
        }
    };

    private void startJmp() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                isFrist = sp.select("isFrist", true);
                if (isFrist) {
                    shanler.sendEmptyMessage(0);

                }else {
                    vp_guide.setVisibility(View.GONE);
                    pageJump();
                }
            }
        }, 3000);
    }
    private void pageJump() {
        boolean isFrist1 = true;
        Intent intent = new Intent(this, MainActivity_new.class);
        if (isFrist) {
//            sp.getBooleanData(SplashActivity.this,"isFrist",false);
            L.e("***第一次运行" + isFrist + "显示声明");
            sp.add("isFrist", false);
            isFrist1 = true;
            if (!BaseApplication.version.equals(version)) {
                BaseApplication.isUpdate = true;
                intent.putExtra("spec", spec);
                intent.putExtra("is_force", is_force);
                intent.putExtra("newFeatures",newFeatures);
            }

        } else {
            L.e("***不是第一次运行" + isFrist + "不显示");
           isFrist1 = false;
            if (!BaseApplication.version.equals(version)) {

                BaseApplication.isUpdate = true;
                intent.putExtra("spec", spec);
                intent.putExtra("is_force", is_force);
                intent.putExtra("newFeatures",newFeatures);
            }
        }
        intent.putExtra("isFrist1",isFrist1);
        startActivity(intent);
        finish();
    }
    class MyAdapter extends PagerAdapter{

        @Override
        public int getCount() {
            return images.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView iv = (ImageView) View.inflate(SplashActivity.this,R.layout.splash_item,null);
            System.out.println("--创建iv");
            iv.setBackgroundResource(images[position]);
            vp_guide.addView(iv);
            if(position == images.length-1){
              iv.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                      pageJump();
                  }
              });
            }
            return iv;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            object = null;

        }
    }

}
