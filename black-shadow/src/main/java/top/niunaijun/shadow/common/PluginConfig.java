package top.niunaijun.shadow.common;

import android.os.Parcel;
import android.os.Parcelable;

import com.tencent.shadow.core.common.InstalledApk;

/**
 * Created by Milk on 2024/3/9.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class PluginConfig implements Parcelable {
    private final String pluginKey;
    private final InstalledApk installedApk;
    private int bPid;

    public PluginConfig(String pluginKey, InstalledApk installedApk, int bPid) {
        this.pluginKey = pluginKey;
        this.installedApk = installedApk;
        this.bPid = bPid;
    }

    public PluginConfig(Parcel in) {
        pluginKey = in.readString();
        installedApk = in.readParcelable(InstalledApk.class.getClassLoader());
        bPid = in.readInt();
    }

    public String getPluginKey() {
        return pluginKey;
    }

    public InstalledApk getInstalledApk() {
        return installedApk;
    }

    public int getBPid() {
        return bPid;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pluginKey);
        dest.writeParcelable(installedApk, flags);
        dest.writeInt(bPid);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PluginConfig> CREATOR = new Creator<PluginConfig>() {
        @Override
        public PluginConfig createFromParcel(Parcel in) {
            return new PluginConfig(in);
        }

        @Override
        public PluginConfig[] newArray(int size) {
            return new PluginConfig[size];
        }
    };

    @Override
    public String toString() {
        return "PluginConfig{" +
                "pluginKey='" + pluginKey + '\'' +
                ", installedApk=" + installedApk +
                ", bPid=" + bPid +
                '}';
    }
}
