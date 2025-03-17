package top.niunaijun.shadow;

import android.content.Intent;
import android.content.pm.ApplicationInfo;

import com.tencent.shadow.core.common.Logger;
import com.tencent.shadow.core.common.LoggerFactory;
import com.tencent.shadow.core.loader.infos.PluginParts;
import com.tencent.shadow.core.loader.managers.PluginPackageManagerImpl;
import com.tencent.shadow.core.runtime.ShadowApplication;
import com.tencent.shadow.core.runtime.container.ContentProviderDelegateProviderHolder;
import com.tencent.shadow.core.runtime.container.DelegateProviderHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import top.niunaijun.shadow.common.PluginConfig;
import top.niunaijun.shadow.loader.BSPluginLoader;
import top.niunaijun.shadow.utils.Reflector;

/**
 * Created by Milk on 2024/3/9.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class BlackShadowClient extends IBlackShadowClient.Stub {
    private static final BlackShadowClient sClient = new BlackShadowClient();
    private final Logger mLogger = LoggerFactory.getLogger(BlackShadowClient.class);

    private final Map<String, PluginConfig> mConfigs = new HashMap<>();
    private final Map<String, BlackShadowClientConfig> mClientConfig = new HashMap<>();
    private BSPluginLoader mPluginLoader;

    public static BlackShadowClient get() {
        return sClient;
    }

    public void initConfig(PluginConfig pluginConfig) {
        if (pluginConfig == null || mConfigs.containsKey(pluginConfig.getPluginKey())) {
            return;
        }
        this.mClientConfig.put(pluginConfig.getPluginKey(), new BlackShadowClientConfig());
        this.mConfigs.put(pluginConfig.getPluginKey(), pluginConfig);

        // 任意一个就可以了
        if (this.mPluginLoader == null) {
            this.mPluginLoader = new BSPluginLoader(BlackShadow.getContext(), pluginConfig.getBPid());
            this.mPluginLoader.onCreate();
            DelegateProviderHolder.setDelegateProvider(mPluginLoader.getDelegateProviderKey(), mPluginLoader);
            ContentProviderDelegateProviderHolder.setContentProviderDelegateProvider(mPluginLoader);
        }
        mLogger.debug("initConfig: " + pluginConfig);
    }

    public ArrayList<PluginConfig> getConfig() {
        return new ArrayList<>(mConfigs.values());
    }

    @Override
    public synchronized boolean bindApplication(String key) {
        BlackShadowClientConfig blackShadowClientConfig = mClientConfig.get(key);
        if (blackShadowClientConfig == null) {
            mLogger.debug("bindApplication mClientConfig: " + key + " not found");
            return false;
        }
        if (blackShadowClientConfig.isBind.getAndSet(true)) {
            return blackShadowClientConfig.bindOk;
        }
        mLogger.debug("bindApplication: " + key);
        PluginConfig mConfig = mConfigs.get(key);
        if (mConfig == null) {
            mLogger.debug("bindApplication mConfigs: " + key + " not found");
            return false;
        }
        try {
            mPluginLoader.loadPlugin(mConfig.getInstalledApk()).get();
            hackShadowApplicationInfo(key);
            mPluginLoader.callApplicationOnCreate(key);
            blackShadowClientConfig.bindOk = true;
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean startActivity(Intent intent) {
        try {
            Intent proxy = mPluginLoader.getComponentManager().convertPluginActivityIntent(intent);
            mLogger.debug("startActivity: " + intent + ", proxy: " + proxy);
            BlackShadow.getContext().startActivity(proxy);
            return true;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return false;
        }
    }

    private void hackShadowApplicationInfo(String key) {
        try {
            PluginParts pluginParts = mPluginLoader.getPluginParts(key);
            ShadowApplication shadowApplication = pluginParts.getApplication();
            shadowApplication.getApplicationInfo().packageName = BlackShadow.getContext().getPackageName();

            ApplicationInfo pluginApplicationInfoFromPluginManifest = Reflector.on(PluginPackageManagerImpl.class)
                    .field("pluginApplicationInfoFromPluginManifest")
                    .get(pluginParts.getPluginPackageManager());
            pluginApplicationInfoFromPluginManifest.packageName = BlackShadow.getContext().getPackageName();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static class BlackShadowClientConfig {
        final AtomicBoolean isBind = new AtomicBoolean(false);
        boolean bindOk = false;
    }
}
