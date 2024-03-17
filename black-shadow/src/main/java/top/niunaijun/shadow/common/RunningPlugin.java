package top.niunaijun.shadow.common;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Milk on 2024/3/11.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class RunningPlugin implements Parcelable {
    public String pluginKey;
    public InstalledPlugin installedPlugin;
    public int pid;

    public RunningPlugin(String pluginKey, int pid) {
        this.pluginKey = pluginKey;
        this.pid = pid;
    }

    protected RunningPlugin(Parcel in) {
        pluginKey = in.readString();
        installedPlugin = in.readParcelable(InstalledPlugin.class.getClassLoader());
        pid = in.readInt();
    }

    public static final Creator<RunningPlugin> CREATOR = new Creator<RunningPlugin>() {
        @Override
        public RunningPlugin createFromParcel(Parcel in) {
            return new RunningPlugin(in);
        }

        @Override
        public RunningPlugin[] newArray(int size) {
            return new RunningPlugin[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pluginKey);
        dest.writeParcelable(installedPlugin, flags);
        dest.writeInt(pid);
    }
}
