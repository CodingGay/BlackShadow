// IBlackShadowClient.aidl
package top.niunaijun.shadow;

// Declare any non-default types here with import statements
import android.content.Intent;

interface IBlackShadowClient {
    boolean bindApplication(in String key);
    boolean startActivity(in Intent intent);
}
