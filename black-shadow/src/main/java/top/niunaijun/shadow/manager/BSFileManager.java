package top.niunaijun.shadow.manager;

import java.io.File;

import top.niunaijun.shadow.BlackShadow;
import top.niunaijun.shadow.utils.FileUtils;

/**
 * Created by Milk on 2024/3/8.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class BSFileManager {
    private static final BSFileManager S_BS_FILE_MANAGER = new BSFileManager();
    private final File mOdexDir;

    public static BSFileManager get() {
        return S_BS_FILE_MANAGER;
    }

    public BSFileManager() {
        mOdexDir = new File(BlackShadow.getContext().getFilesDir(), "black_shadow_plugins");
        FileUtils.mkdirs(mOdexDir.getAbsolutePath());
    }

    public File getPluginDir(String pluginKey, long version) {
        File file = new File(mOdexDir, pluginKey);
        FileUtils.mkdirs(file.getAbsolutePath());
        return file;
    }

    public File getPluginOdexDir(String pluginKey, long version) {
        File file = new File(getPluginDir(pluginKey, version), "odex");
        FileUtils.mkdirs(file.getAbsolutePath());
        return file;
    }

    public File getPluginLibsDir(String pluginKey, long version) {
        File file = new File(getPluginDir(pluginKey, version), "libs");
        FileUtils.mkdirs(file.getAbsolutePath());
        return file;
    }

    public File getPluginApk(String pluginKey, long version) {
        File file = new File(getPluginDir(pluginKey, version), "app.apk");
        return file;
    }
}
