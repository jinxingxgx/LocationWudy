package com.wudy.locationwudy;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import com.blankj.utilcode.util.Utils;

/**
 * Created by xgx on 2018/12/13 for MusicPlayDemo
 */
public class MyApplication extends Application {
    private static DaoSession daoSession;
    private static MyApplication instance;
    private SharedPreferences sp;
    public static final String SHAREDPREFERENCES_NAME = "wc_preferences";
    private static String dbPath;

    public SharedPreferences getSP() {
        if (sp == null) {
            sp = getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
        }
        return sp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setupDatabase();
        CrashHandler mCustomCrashHandler = CrashHandler.getInstance();
        mCustomCrashHandler.setCustomCrashHanler(getApplicationContext());
        Utils.init(this);
        instance = this;//在Application的oncreate方法里:
        StyledDialog.init(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                ActivityStackManager.getInstance().addActivity(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                ActivityStackManager.getInstance().removeActivity(activity);
            }
        });
    }

    public static MyApplication getInstance() {
        return instance;
    }

    /**
     * 配置数据库
     */
    private void setupDatabase() {
        //创建数据库shop.db"
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "file.db");
        //获取可写数据库
        SQLiteDatabase db = helper.getWritableDatabase();
        Log.e("xgx", db.getPath());
        //获取数据库对象
        DaoMaster daoMaster = new DaoMaster(db);
        //获取Dao对象管理者
        daoSession = daoMaster.newSession();
        dbPath = db.getPath();
    }

    public static String getDbPath() {
        return dbPath;
    }

    public static DaoSession getDaoInstant() {
        return daoSession;
    }
}
