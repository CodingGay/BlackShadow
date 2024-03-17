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

import java.util.Objects;
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

    private PluginConfig mConfig;
    private final AtomicBoolean isBind = new AtomicBoolean(false);
    private boolean bindOk = false;
    private BSPluginLoader mPluginLoader;

    public static BlackShadowClient get() {
        return sClient;
    }

    public void initConfig(PluginConfig pluginConfig) {
        if (this.mConfig != null && !Objects.equals(this.mConfig.getPluginKey(), pluginConfig.getPluginKey())) {
            return;
        }
        this.mConfig = pluginConfig;
        mLogger.debug("initConfig: " + mConfig);
    }

    public PluginConfig getConfig() {
        return mConfig;
    }

    public String getDelegateProviderKey() {
        if (this.mConfig != null) {
            return mConfig.getPluginKey();
        }
        return "DEFAULT-KEY";
    }

    @Override
    public synchronized boolean bindApplication() {
        if (isBind.getAndSet(true)) {
            return bindOk;
        }
        mLogger.debug("bindApplication: " + mConfig);
        mPluginLoader = new BSPluginLoader(BlackShadow.getContext(), mConfig);
        DelegateProviderHolder.setDelegateProvider(mPluginLoader.getDelegateProviderKey(), mPluginLoader);
        ContentProviderDelegateProviderHolder.setContentProviderDelegateProvider(mPluginLoader);

        try {
            mPluginLoader.onCreate();
            mPluginLoader.loadPlugin(mConfig.getInstalledApk()).get();
            hackShadowApplicationInfo();
            mPluginLoader.callApplicationOnCreate(mConfig.getPluginKey());
            this.bindOk = true;
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

    private void hackShadowApplicationInfo() {
        try {
            PluginParts pluginParts = mPluginLoader.getPluginParts(mConfig.getPluginKey());
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
}
