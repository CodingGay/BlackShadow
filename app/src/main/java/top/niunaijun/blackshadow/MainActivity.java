package top.niunaijun.blackshadow;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

import top.niunaijun.blackshadow.utils.FileUtils;
import top.niunaijun.shadow.BlackShadow;
import top.niunaijun.shadow.common.InstallResult;
import top.niunaijun.shadow.common.InstalledPlugin;
import top.niunaijun.shadow.host.R;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_start).setOnClickListener(v -> {
            Toast.makeText(this, "启动中", Toast.LENGTH_SHORT).show();
            new Thread(this::handlePlugin).start();
        });
    }

    private void handlePlugin() {
        File plugin = new File(getFilesDir(), "plugin.apk");
        copyAssetsPlugin(plugin);

        // 允许访问宿主的白名单类
        String[] hostWhiteList = new String[]{
                "com.tencent.*",
                "okhttp3",
                "okhttp3.*",
                "okhttp3.**",
                "com.google.**"
        };

        Intent launcher = new Intent();
        launcher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        launcher.setClassName("top.niunaijun.shadow.host", "top.niunaijun.shadow.plugin.MainActivity");

        String key = "app-plugin";
        InstalledPlugin installedPlugin = BlackShadow.get().getInstalledPlugin(key);
        if (installedPlugin == null) {
            InstallResult installResult = BlackShadow.get().installPlugin(key, plugin, hostWhiteList, launcher);
            Log.d(TAG, "installPlugin: " + installResult);
            installedPlugin = BlackShadow.get().getInstalledPlugin(key);
        }
        BlackShadow.get().launchPlugin(installedPlugin.pluginKey, installedPlugin.launcher);
    }

    private void copyAssetsPlugin(File target) {
        try {
            if (target.exists()) {
                return;
            }
            FileUtils.copyFile(this.getAssets().open("plugin.apk"), target);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}