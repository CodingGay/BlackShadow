package top.niunaijun.shadow.common;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Milk on 2024/3/8.
 * * ∧＿∧
 * (`･ω･∥
 * 丶　つ０
 * しーＪ
 * 此处无Bug
 */
public class InstallResult implements Parcelable {
    private final boolean success;
    private final String msg;
    private String pluginKey;

    public InstallResult(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public InstallResult(boolean success, String msg, String pluginKey) {
        this.success = success;
        this.msg = msg;
        this.pluginKey = pluginKey;
    }

    protected InstallResult(Parcel in) {
        success = in.readByte() != 0;
        msg = in.readString();
        pluginKey = in.readString();
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMsg() {
        return msg;
    }

    public String getPluginKey() {
        return pluginKey;
    }

    public static final Creator<InstallResult> CREATOR = new Creator<InstallResult>() {
        @Override
        public InstallResult createFromParcel(Parcel in) {
            return new InstallResult(in);
        }

        @Override
        public InstallResult[] newArray(int size) {
            return new InstallResult[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (success ? 1 : 0));
        parcel.writeString(msg);
        parcel.writeString(pluginKey);
    }

    @Override
    public String toString() {
        return "InstallResult{" +
                "success=" + success +
                ", msg='" + msg + '\'' +
                ", pluginKey='" + pluginKey + '\'' +
                '}';
    }
}
