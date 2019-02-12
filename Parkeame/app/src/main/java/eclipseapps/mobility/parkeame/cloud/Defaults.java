package eclipseapps.mobility.parkeame.cloud;

import android.os.Build;

/**
 * Created by usuario on 26/08/17.
 */
public class Defaults
{
    public static final String APPLICATION_ID = "AF5AA00D-C423-A9D1-FFFB-EDFBC25BBF00";
    public static final String API_KEY = "48769467-C2F0-7D8F-FF60-948DFE835A00";
    public static final String SERVER_URL = "http://localhost:8080/api";
    public static final String SERVER_URL_EMULATOR = "http://10.0.3.2:8080/api";
    public static final String getServer_URL(){
        return isEmulator()?SERVER_URL_EMULATOR:SERVER_URL;
    }
    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }
}
