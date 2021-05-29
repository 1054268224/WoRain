package cyee.widget;

public class CyeeWidgetVersion {
	

    private static final String mVersionNum = "V9.0.0.ar";

    private static final VersionType sVersionType = VersionType.FULLSCREEN_VER;

    public enum VersionType {
        NORMAL_VER, BUSINESS_VER, FULLSCREEN_VER
    }

    public static String getVersion() {
        return mVersionNum;
    }

    public static VersionType getVersionType() {
        return sVersionType;
    }
}
