package top.niunaijun.blackshadow;

import android.app.Application;
import android.content.Context;

import top.niunaijun.blackshadow.utils.FileUtils;
import top.niunaijun.shadow.BlackShadow;

/**
 * Created by Milk on 2024/3/17.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class BlackShadowApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        BlackShadow.get().init(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
