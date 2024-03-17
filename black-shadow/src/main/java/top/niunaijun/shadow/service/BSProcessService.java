package top.niunaijun.shadow.service;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;

import com.tencent.shadow.core.common.InstalledApk;
import com.tencent.shadow.core.common.Logger;
import com.tencent.shadow.core.common.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import top.niunaijun.shadow.BlackShadow;
import top.niunaijun.shadow.IBlackShadowClient;
import top.niunaijun.shadow.common.PluginConfig;
import top.niunaijun.shadow.common.ProcessConfig;
import top.niunaijun.shadow.common.RunningPlugin;
import top.niunaijun.shadow.manager.BSManifestManager;
import top.niunaijun.shadow.utils.compat.ProviderCallCompat;

/**
 * Created by Milk on 2024/3/8.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class BSProcessService implements IBlackShadowService {
    private static final BSProcessService sBSProcessService = new BSProcessService();

    private final Map<String, ProcessConfig> mProcessConfigMap = new HashMap<>();
    private final Logger mLogger = LoggerFactory.getLogger(BSManagerService.class);

    public static BSProcessService get() {
        return sBSProcessService;
    }

    public ProcessConfig startProcess(String pluginKey, InstalledApk installedApk) {
        synchronized (mProcessConfigMap) {
            ProcessConfig processConfig = mProcessConfigMap.get(pluginKey);
            if (processConfig != null && processConfig.getClient() != null) {
                return processConfig;
            }
            mProcessConfigMap.remove(pluginKey);
            int bPid = getAvailableBPid();
            if (bPid == -1) {
                mLogger.debug("No more available bPid.");
                return null;
            }
            processConfig = new ProcessConfig();
            processConfig.setPluginKey(pluginKey);
            processConfig.setInstalledApk(installedApk);
            processConfig.setBPid(bPid);
            if (!startClient(processConfig)) {
                mLogger.debug("startClient error.");
                processConfig = null;
            } else {
                processConfig.setPid(getPid(BlackShadow.getContext(), BSManifestManager.getPluginProcessName(processConfig.getBPid())));
                mProcessConfigMap.put(pluginKey, processConfig);
            }
            return processConfig;
        }
    }

    private boolean startClient(ProcessConfig processConfig) {
        String provider = BSManifestManager.getPluginContentProvider(processConfig.getBPid());

        Bundle bundle = new Bundle();
        bundle.putParcelable("pluginConfig", new PluginConfig(processConfig.getPluginKey(), processConfig.getInstalledApk(), processConfig.getBPid()));
        Bundle call = ProviderCallCompat.callSafely(provider, "startClient", null, bundle);
        if (call == null) {
            return false;
        }
        IBinder client = call.getBinder("client");
        if (client == null) {
            return false;
        }
        processConfig.setClient(IBlackShadowClient.Stub.asInterface(client));

        try {
            client.linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    client.unlinkToDeath(this, 0);
                    stopProcess(processConfig);
                }
            }, 0);
        } catch (RemoteException ignored) {
        }
        try {
            return processConfig.getClient().bindApplication();
        } catch (RemoteException e) {
            mLogger.debug("bindApplication error.");
            return false;
        }
    }

    private void stopProcess(ProcessConfig processConfig) {
        if (processConfig == null)
            return;
        synchronized (mProcessConfigMap) {
            mProcessConfigMap.remove(processConfig.getPluginKey());
            Process.killProcess(processConfig.getPid());
        }
    }

    public void stopProcess(String pluginKey) {
        synchronized (mProcessConfigMap) {
            stopProcess(mProcessConfigMap.get(pluginKey));
        }
    }

    public void stopAllPlugin() {
        synchronized (mProcessConfigMap) {
            Set<String> pluginKeys = new HashSet<>(mProcessConfigMap.keySet());
            for (String pluginKey : pluginKeys) {
                stopProcess(mProcessConfigMap.get(pluginKey));
            }
        }
    }

    public List<RunningPlugin> getRunningProcess() {
        List<RunningPlugin> runningPlugins = new LinkedList<>();
        synchronized (mProcessConfigMap) {
            for (ProcessConfig value : mProcessConfigMap.values()) {
                runningPlugins.add(new RunningPlugin(value.getPluginKey(), value.getPid()));
            }
            return runningPlugins;
        }
    }

    public boolean isRunning(String pluginKey) {
        synchronized (mProcessConfigMap) {
            return mProcessConfigMap.containsKey(pluginKey);
        }
    }

    private void recoveryProcess() {
        synchronized (mProcessConfigMap) {
            ActivityManager manager = (ActivityManager) BlackShadow.getContext().getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
                int bPid = parseBPid(runningAppProcess.processName);
                if (bPid != -1) {
                    String auth = BSManifestManager.getPluginContentProvider(bPid);
                    Bundle bundle = ProviderCallCompat.callSafely(auth, "getConfig", null, null);
                    if (bundle == null) {
                        Process.killProcess(runningAppProcess.pid);
                        return;
                    }
                    bundle.setClassLoader(PluginConfig.class.getClassLoader());
                    IBinder client = bundle.getBinder("client");
                    PluginConfig pluginConfig = bundle.getParcelable("pluginConfig");
                    if (client == null || pluginConfig == null) {
                        Process.killProcess(runningAppProcess.pid);
                        return;
                    }
                    ProcessConfig processConfig = new ProcessConfig();
                    processConfig.setPluginKey(pluginConfig.getPluginKey());
                    processConfig.setInstalledApk(pluginConfig.getInstalledApk());
                    processConfig.setPid(runningAppProcess.pid);
                    processConfig.setBPid(bPid);
                    processConfig.setClient(IBlackShadowClient.Stub.asInterface(client));
                    try {
                        client.linkToDeath(new IBinder.DeathRecipient() {
                            @Override
                            public void binderDied() {
                                client.unlinkToDeath(this, 0);
                                stopProcess(processConfig);
                            }
                        }, 0);
                    } catch (RemoteException ignored) {
                    }
                    mLogger.debug("recoveryProcess: " + processConfig);
                    mProcessConfigMap.put(processConfig.getPluginKey(), processConfig);
                }
            }
        }
    }

    private int getAvailableBPid() {
        ActivityManager manager = (ActivityManager) BlackShadow.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();
        Set<Integer> usingPs = new HashSet<>();
        for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
            int i = parseBPid(runningAppProcess.processName);
            usingPs.add(i);
        }
        for (int i = 0; i < BSManifestManager.MAX_PROCESS; i++) {
            if (usingPs.contains(i)) {
                continue;
            }
            return i;
        }
        return -1;
    }

    private int parseBPid(String stubProcessName) {
        String prefix;
        if (stubProcessName == null) {
            return -1;
        } else {
            prefix = BlackShadow.getContext().getPackageName() + ":plugin";
        }
        if (stubProcessName.startsWith(prefix)) {
            try {
                return Integer.parseInt(stubProcessName.substring(prefix.length()));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return -1;
    }

    public static int getPid(Context context, String processName) {
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
                if (runningAppProcess.processName.equals(processName)) {
                    return runningAppProcess.pid;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void startup() {
        recoveryProcess();
    }
}
