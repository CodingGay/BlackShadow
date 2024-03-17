package top.niunaijun.shadow.container;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import top.niunaijun.shadow.BlackShadowClient;
import top.niunaijun.shadow.common.PluginConfig;

/**
 * Created by Milk on 2024/3/9.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class PluginContentProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Bundle call(@NotNull String method, String arg, Bundle extras) {
        Bundle bundle = new Bundle();
        if ("startClient".equals(method) && extras != null) {
            extras.setClassLoader(PluginConfig.class.getClassLoader());
            PluginConfig config = extras.getParcelable("pluginConfig");
            BlackShadowClient.get().initConfig(config);
        } else if ("getConfig".equals(method)) {
            bundle.putParcelable("pluginConfig", BlackShadowClient.get().getConfig());
        }
        bundle.putBinder("client", BlackShadowClient.get());
        return bundle;
    }

    @Override
    public Cursor query(@NotNull Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
    }

    @Override
    public String getType(@NotNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NotNull Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NotNull Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(@NotNull Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    public static class P0 extends PluginContentProvider {
    }

    public static class P1 extends PluginContentProvider {
    }

    public static class P2 extends PluginContentProvider {
    }

    public static class P3 extends PluginContentProvider {
    }

    public static class P4 extends PluginContentProvider {
    }

    public static class P5 extends PluginContentProvider {
    }

    public static class P6 extends PluginContentProvider {
    }

    public static class P7 extends PluginContentProvider {
    }

    public static class P8 extends PluginContentProvider {
    }

    public static class P9 extends PluginContentProvider {
    }
}
