package top.niunaijun.shadow.common;

import com.tencent.shadow.core.common.InstalledApk;

import top.niunaijun.shadow.IBlackShadowClient;

/**
 * Created by Milk on 2024/3/9.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class ProcessConfig {
    private String pluginKey;
    private InstalledApk installedApk;
    private int pid;
    private int bPid;
    private IBlackShadowClient client;

    public String getPluginKey() {
        return pluginKey;
    }

    public void setPluginKey(String pluginKey) {
        this.pluginKey = pluginKey;
    }

    public InstalledApk getInstalledApk() {
        return installedApk;
    }

    public void setInstalledApk(InstalledApk installedApk) {
        this.installedApk = installedApk;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getBPid() {
        return bPid;
    }

    public void setBPid(int bPid) {
        this.bPid = bPid;
    }

    public IBlackShadowClient getClient() {
        return client;
    }

    public void setClient(IBlackShadowClient client) {
        this.client = client;
    }

    @Override
    public String toString() {
        return "ProcessConfig{" +
                "pluginKey='" + pluginKey + '\'' +
                ", installedApk=" + installedApk +
                ", pid=" + pid +
                ", bPid=" + bPid +
                ", client=" + client +
                '}';
    }
}
