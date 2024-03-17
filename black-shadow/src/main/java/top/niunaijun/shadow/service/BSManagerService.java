package top.niunaijun.shadow.service;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;

import com.tencent.mmkv.MMKV;
import com.tencent.shadow.core.common.InstalledApk;
import com.tencent.shadow.core.common.Logger;
import com.tencent.shadow.core.common.LoggerFactory;
import com.tencent.shadow.core.load_parameters.LoadParameters;
import com.wind.meditor.ManifestEditorMain;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import top.niunaijun.shadow.BlackShadow;
import top.niunaijun.shadow.common.InstallResult;
import top.niunaijun.shadow.common.InstalledPlugin;
import top.niunaijun.shadow.common.ProcessConfig;
import top.niunaijun.shadow.common.RunningPlugin;
import top.niunaijun.shadow.manager.BSFileManager;
import top.niunaijun.shadow.utils.FileUtils;
import top.niunaijun.shadow.utils.NativeUtils;

/**
 * Created by Milk on 2024/3/8.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class BSManagerService extends IBSManagerService.Stub implements IBlackShadowService {
    private static final BSManagerService sBlackProcessService = new BSManagerService();
    private final Map<String, InstalledPlugin> mInstalledApkMap = new HashMap<>();
    private final Object mLockInstall = new Object();
    private final MMKV mManagerMMKV = MMKV.mmkvWithID("black_shadow_manager");
    private final Logger mLogger = LoggerFactory.getLogger(BSManagerService.class);

    public static BSManagerService get() {
        return sBlackProcessService;
    }

    @Override
    public InstallResult installPlugin(String pluginKey, String pluginPath, String[] hostWhiteList, Intent launcher) {
        boolean deleteSourceFile = false;
        InstallResult installResult = new InstallResult(true, "", pluginKey);
        mLogger.debug("installPlugin: " + pluginKey + " " + pluginPath);
            // 正在运行则不安装，这个根据自己业务需求来决定是否使用
            if (BSProcessService.get().isRunning(pluginKey)) {
                return new InstallResult(false, "plugin is running.", pluginKey);
            }
            try {
                PackageInfo packageArchiveInfo = BlackShadow.getContext().getPackageManager().getPackageArchiveInfo(pluginPath, PackageManager.GET_ACTIVITIES);
                if (packageArchiveInfo == null) {
                    return new InstallResult(false, "getPackageArchiveInfo error.", pluginKey);
                }
                synchronized (mLockInstall) {
                    // 修改包名与宿主相同
                    if (!BlackShadow.getContext().getPackageName().equals(packageArchiveInfo.packageName)) {
                        mLogger.debug("plugin packageName not same host " + packageArchiveInfo.packageName + " " + BlackShadow.getContext().getPackageName());
                        mLogger.debug("use ManifestEditorMain.doMain");
                        String s = ManifestEditorMain.doMain(BlackShadow.getContext(), new File(pluginPath));
                        if (TextUtils.isEmpty(s)) {
                            return new InstallResult(false, "plugin packageName not same host " + packageArchiveInfo.packageName + " " + BlackShadow.getContext().getPackageName());
                        }
                        pluginPath = s;
                        deleteSourceFile = true;
                        mLogger.debug("ManifestEditorMain.doMain: " + s);
                        packageArchiveInfo = BlackShadow.getContext().getPackageManager().getPackageArchiveInfo(pluginPath, PackageManager.GET_ACTIVITIES);
                        if (packageArchiveInfo == null) {
                            return new InstallResult(false, "getPackageArchiveInfo error.", pluginKey);
                        }
                    }
                    int version = packageArchiveInfo.versionCode;
                    // 清除旧版本
                    uninstallPlugin(pluginKey);

                    // 复制插件
                    File pluginApk = BSFileManager.get().getPluginApk(pluginKey, version);
                    FileUtils.copyFile(new File(pluginPath), pluginApk);
                    // 复制so文件
                    NativeUtils.copyNativeLib(new File(pluginPath), BSFileManager.get().getPluginLibsDir(pluginKey, version));

                    LoadParameters loadParameters = new LoadParameters(null, pluginKey, null,
                            hostWhiteList);
                    Parcel parcel = Parcel.obtain();
                    loadParameters.writeToParcel(parcel, 0);
                    InstalledApk installedApk = installPlugin(pluginKey, pluginApk, version);
                    final InstalledApk plugin = new InstalledApk(
                            installedApk.apkFilePath,
                            installedApk.oDexPath,
                            installedApk.libraryPath,
                            parcel.marshall()
                    );
                    parcel.recycle();
                    mInstalledApkMap.put(pluginKey, new InstalledPlugin(pluginKey, plugin, packageArchiveInfo, launcher));
                    save();
                    mLogger.debug("installPlugin OK: " + installedApk);
                }
                if (deleteSourceFile) {
                    FileUtils.deleteDir(pluginPath);
                }
            } catch (Exception e) {
                e.printStackTrace();
                mLogger.debug("installPlugin Error.");
                uninstallPlugin(pluginKey);
            }
        return installResult;
    }

    @Override
    public boolean launchPlugin(String pluginKey, Intent intent) {
        synchronized (mLockInstall) {
            try {
                if (!isInstallPlugin(pluginKey)) {
                    mLogger.debug(pluginKey + " not installed.");
                    return false;
                }
                ProcessConfig processConfig = BSProcessService.get().startProcess(pluginKey, Objects.requireNonNull(mInstalledApkMap.get(pluginKey)).installedApk);
                if (processConfig == null) {
                    throw new RemoteException("startProcess error.");
                }
                return processConfig.getClient().startActivity(intent);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                mLogger.debug("launchPlugin error: " + throwable.getMessage());
                return false;
            }
        }
    }

    @Override
    public boolean callApplication(String pluginKey) throws RemoteException {
        mLogger.debug("callApplication: " + pluginKey);
        // 需要上锁，防止插件这个时间更新或者卸载可能会引起问题
        synchronized (mLockInstall) {
            if (!isInstallPlugin(pluginKey)) {
                mLogger.debug(pluginKey + " not installed.");
                return false;
            }
            ProcessConfig processConfig = BSProcessService.get().startProcess(pluginKey, Objects.requireNonNull(mInstalledApkMap.get(pluginKey)).installedApk);
            if (processConfig == null) {
                throw new RemoteException("startProcess error.");
            }
            return true;
        }
    }

    @Override
    public List<RunningPlugin> getRunningPlugins() {
        synchronized (mInstalledApkMap) {
            List<RunningPlugin> runningProcess = BSProcessService.get().getRunningProcess();
            for (RunningPlugin process : runningProcess) {
                process.installedPlugin = mInstalledApkMap.get(process.pluginKey);
            }
            return runningProcess;
        }
    }

    @Override
    public void stopPlugin(String pluginKey) {
        BSProcessService.get().stopProcess(pluginKey);
    }

    @Override
    public void stopAllPlugin() {
        BSProcessService.get().stopAllPlugin();
    }

    @Override
    public List<InstalledPlugin> getInstalledPlugins() {
        synchronized (mInstalledApkMap) {
            return new ArrayList<>(mInstalledApkMap.values());
        }
    }

    @Override
    public InstalledPlugin getInstalledPlugin(String pluginKey) {
        if (pluginKey == null) {
            return null;
        }
        synchronized (mInstalledApkMap) {
            return mInstalledApkMap.get(pluginKey);
        }
    }

    @Override
    public void uninstallPlugin(String pluginKey) {
        synchronized (mLockInstall) {
            synchronized (mInstalledApkMap) {
                InstalledPlugin remove = mInstalledApkMap.remove(pluginKey);
                if (remove != null) {
                    BSProcessService.get().stopProcess(pluginKey);
                    FileUtils.deleteDir(BSFileManager.get().getPluginDir(pluginKey, remove.version));
                    save();
                }
            }
        }
    }

    public boolean isInstallPlugin(String pluginKey) {
        synchronized (mInstalledApkMap) {
            return mInstalledApkMap.containsKey(pluginKey);
        }
    }

    private void save() {
        synchronized (mInstalledApkMap) {
            Parcel parcel = Parcel.obtain();
            try {
                parcel.writeMap(mInstalledApkMap);
                mManagerMMKV.putBytes("installed_apks", parcel.marshall());
            } finally {
                parcel.recycle();
            }
        }
    }

    private void load() {
        synchronized (mInstalledApkMap) {
            Parcel parcel = Parcel.obtain();
            try {
                byte[] bytes = mManagerMMKV.getBytes("installed_apks", null);
                if (bytes == null) {
                    return;
                }
                parcel.unmarshall(bytes, 0, bytes.length);
                parcel.setDataPosition(0);
                mInstalledApkMap.clear();
                parcel.readMap(mInstalledApkMap, InstalledPlugin.class.getClassLoader());
            } finally {
                parcel.recycle();
            }
            Iterator<InstalledPlugin> iterator = mInstalledApkMap.values().iterator();
            while (iterator.hasNext()) {
                InstalledPlugin next = iterator.next();
                try {
                    if (next.installedApk == null) {
                        iterator.remove();
                        continue;
                    }
                    PackageInfo packageArchiveInfo = BlackShadow.getContext().getPackageManager().getPackageArchiveInfo(next.installedApk.apkFilePath, PackageManager.GET_ACTIVITIES);
                    if (packageArchiveInfo == null) {
                        iterator.remove();
                        continue;
                    }
                    next.setPackageInfo(packageArchiveInfo);
                    mLogger.debug("load plugin: " + next);
                } catch (Throwable t) {
                    t.printStackTrace();
                    iterator.remove();
                }
            }
            save();
        }
    }

    private InstalledApk installPlugin(String pluginKey, File apk, long version) {
        return new InstalledApk(
                apk.getAbsolutePath(),
                BSFileManager.get().getPluginOdexDir(pluginKey, version).getAbsolutePath(),
                BSFileManager.get().getPluginLibsDir(pluginKey, version).getAbsolutePath()
        );
    }

    @Override
    public void startup() {
        load();
    }
}
