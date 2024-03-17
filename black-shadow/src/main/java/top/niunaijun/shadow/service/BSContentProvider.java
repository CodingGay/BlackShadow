package top.niunaijun.shadow.service;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Milk on 2024/3/8.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class BSContentProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return BlackShadowService.startup();
    }

    @Override
    public Bundle call(@NotNull String method, String arg, Bundle extras) {
        Bundle bundle = new Bundle();
        bundle.putBinder("service", BSManagerService.get());
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
}
