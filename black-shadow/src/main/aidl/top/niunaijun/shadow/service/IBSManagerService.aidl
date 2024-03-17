// IBSManagerService.aidl
package top.niunaijun.shadow.service;

// Declare any non-default types here with import statements

import top.niunaijun.shadow.common.InstallResult;
import top.niunaijun.shadow.common.InstalledPlugin;
import top.niunaijun.shadow.common.RunningPlugin;
import android.content.Intent;
import java.util.List;

interface IBSManagerService {
    InstallResult installPlugin(String pluginPath, String pluginKey, in String[] hostWhiteList, in Intent launcher);

    boolean launchPlugin(String pluginKey, in Intent launcher);
    boolean callApplication(String pluginKey);
    List<RunningPlugin> getRunningPlugins();
    void stopPlugin(String pluginKey);
    void stopAllPlugin();

    List<InstalledPlugin> getInstalledPlugins();
    InstalledPlugin getInstalledPlugin(String pluginKey);
    void uninstallPlugin(String pluginKey);
}
