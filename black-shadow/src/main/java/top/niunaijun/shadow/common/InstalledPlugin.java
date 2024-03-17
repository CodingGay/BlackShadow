package top.niunaijun.shadow.common;

import android.content.Intent;
import android.content.pm.PackageInfo;
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
public class InstalledPlugin implements Parcelable {
    public transient PackageInfo packageInfo;

    public String pluginKey;
    public long version;
    public final InstalledApk installedApk;
    public Intent launcher;

    public InstalledPlugin(String pluginKey, InstalledApk installedApk, PackageInfo packageInfo, Intent launcher) {
        this.packageInfo = packageInfo;

        this.pluginKey = pluginKey;
        this.version = packageInfo.versionCode;
        this.installedApk = installedApk;
        this.launcher = launcher;
    }

    protected InstalledPlugin(Parcel in) {
        pluginKey = in.readString();
        version = in.readLong();
        installedApk = in.readParcelable(InstalledApk.class.getClassLoader());
        launcher = in.readParcelable(Intent.class.getClassLoader());
        packageInfo = in.readParcelable(PackageInfo.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pluginKey);
        dest.writeLong(version);
        dest.writeParcelable(installedApk, flags);
        dest.writeParcelable(launcher, flags);
        dest.writeParcelable(packageInfo, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<InstalledPlugin> CREATOR = new Creator<InstalledPlugin>() {
        @Override
        public InstalledPlugin createFromParcel(Parcel in) {
            return new InstalledPlugin(in);
        }

        @Override
        public InstalledPlugin[] newArray(int size) {
            return new InstalledPlugin[size];
        }
    };

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
        this.version = packageInfo.versionCode;
    }

    @Override
    public String toString() {
        return "InstalledPlugin{" +
                "pluginKey='" + pluginKey + '\'' +
                ", version=" + version +
                ", installedApk=" + installedApk +
                ", launcher=" + launcher +
                ", packageInfo=" + packageInfo +
                '}';
    }
}
