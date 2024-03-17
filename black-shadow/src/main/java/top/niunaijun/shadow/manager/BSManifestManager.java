package top.niunaijun.shadow.manager;

import top.niunaijun.shadow.BlackShadow;
import top.niunaijun.shadow.container.PluginContainerContentProvider;
import top.niunaijun.shadow.container.PluginProxyActivity;

/**
 * Created by Milk on 2024/3/9.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class BSManifestManager {
    public static final int MAX_PROCESS = 10;

    public static String getPluginContentProvider(int bPid) {
        return BlackShadow.getContext().getPackageName() + ".blackshadow.provider" + bPid;
    }

    public static String getPluginProcessName(int bPid) {
        return BlackShadow.getContext().getPackageName() + ":plugin" + bPid;
    }

    public static String getPluginContainerContentProviderClassName(int bPid) {
        return PluginContainerContentProvider.class.getName() + "$P" + bPid;
    }

    public static String getPluginContainerContentProviderAuth(int bPid) {
        return BlackShadow.getContext().getPackageName() + ".blackshadow.dynamic" + bPid;
    }

    public static String getPluginProxyActivity(int bPid) {
        return PluginProxyActivity.class.getName();
    }
}
