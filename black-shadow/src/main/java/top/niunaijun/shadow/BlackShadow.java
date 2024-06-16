package top.niunaijun.shadow;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import com.tencent.mmkv.MMKV;
import com.tencent.shadow.core.common.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import top.niunaijun.shadow.common.InstallResult;
import top.niunaijun.shadow.common.InstalledPlugin;
import top.niunaijun.shadow.common.RunningPlugin;
import top.niunaijun.shadow.service.IBSManagerService;
import top.niunaijun.shadow.service.StubService;
import top.niunaijun.shadow.utils.AndroidLogLoggerFactory;
import top.niunaijun.shadow.utils.compat.ProviderCallCompat;

/**
 * Created by Milk on 2024/3/8.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
@SuppressLint("StaticFieldLeak")
public class BlackShadow {
    private static final String TAG = "BlackShadow";
    private static final BlackShadow sInstance = new BlackShadow();

    private final AtomicBoolean isInit = new AtomicBoolean(false);
    private Context mContext;
    private ProcessType mProcessType;
    private IBSManagerService mBSManagerService = null;

    public static BlackShadow get() {
        return sInstance;
    }

    public static Context getContext() {
        return get().mContext;
    }

    public static String getHostPackageName() {
        return get().mContext.getPackageName();
    }

    public static boolean isHostProcess() {
        return get().mProcessType == ProcessType.Host;
    }

    public static boolean isPluginProcess() {
        return get().mProcessType == ProcessType.Plugin;
    }

    public static boolean isServerProcess() {
        return get().mProcessType == ProcessType.Server;
    }

    public void init(Application application) {
        if (isInit.getAndSet(true)) {
            return;
        }
        mContext = application;
        MMKV.initialize(mContext, new File(mContext.getFilesDir(), "black_shadow_mmkv").getAbsolutePath());
        LoggerFactory.setILoggerFactory(new AndroidLogLoggerFactory());

        String processName = getProcessName(application);
        if (processName.contains(application.getString(R.string.black_shadow_plugin_name))) {
            mProcessType = ProcessType.Plugin;
        } else if (processName.contains(application.getString(R.string.black_shadow_service_name))) {
            mProcessType = ProcessType.Server;
        } else {
            mProcessType = ProcessType.Host;
        }

        if (isHostProcess()) {
            Intent intent = new Intent(application, StubService.class);
            try {
                application.startService(intent);
            } catch (Exception ignored) {
            }
            IBSManagerService blackShadowService = getBlackShadowService();
            Log.d(TAG, "init: " + blackShadowService);
        }
    }

    private IBSManagerService getBlackShadowService() {
        if (mBSManagerService != null && mBSManagerService.asBinder().isBinderAlive() && mBSManagerService.asBinder().pingBinder()) {
            return mBSManagerService;
        }
        Bundle call = ProviderCallCompat.callSafely(getContext().getPackageName() + ".BlackShadowContentProvider", "", null, null);
        if (call == null) {
            throw new RuntimeException("Start BlackShadow Failed.");
        }
        IBinder service = call.getBinder("service");
        mBSManagerService = IBSManagerService.Stub.asInterface(service);
        if (mBSManagerService != null) {
            try {
                mBSManagerService.asBinder().linkToDeath(() -> mBSManagerService = null, 0);
            } catch (RemoteException ignored) {
            }
        }
        return getBlackShadowService();
    }

    private static String getProcessName(Context context) {
        int pid = Process.myPid();
        String processName = null;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
            if (info.pid == pid) {
                processName = info.processName;
                break;
            }
        }
        if (processName == null) {
            throw new RuntimeException("processName = null");
        }
        return processName;
    }

    public InstallResult installPlugin(String pluginKey, File path, String[] hostWhiteList, Intent launcher) {
        try {
            return getBlackShadowService().installPlugin(pluginKey, path.getAbsolutePath(), hostWhiteList, launcher);
        } catch (RemoteException e) {
            return new InstallResult(false, e.getMessage());
        }
    }

    public InstallResult installPlugin(String pluginKey, File path, String[] hostWhiteList) {
        return installPlugin(pluginKey, path, hostWhiteList, null);
    }

    public boolean launchPlugin(String pluginKey, Intent intent) {
        try {
            return getBlackShadowService().launchPlugin(pluginKey, intent);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean callApplication(String pluginKey) {
        try {
            return getBlackShadowService().callApplication(pluginKey);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<InstalledPlugin> getInstalledPlugins() {
        try {
            return getBlackShadowService().getInstalledPlugins();
        } catch (RemoteException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public InstalledPlugin getInstalledPlugin(String pluginKey) {
        try {
            return getBlackShadowService().getInstalledPlugin(pluginKey);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void uninstallPlugin(String pluginKey) {
        try {
            getBlackShadowService().uninstallPlugin(pluginKey);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void stopPlugin(String pluginKey) {
        try {
            getBlackShadowService().stopPlugin(pluginKey);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void stopAllPlugin() {
        try {
            getBlackShadowService().stopAllPlugin();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public List<RunningPlugin> getRunningPlugins() {
        try {
            return getBlackShadowService().getRunningPlugins();
        } catch (RemoteException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private enum ProcessType {
        /**
         * Server process
         */
        Server,
        /**
         * plugin process
         */
        Plugin,
        /**
         * Host process
         */
        Host,
    }
}
