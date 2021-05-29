package cyee.forcetouch;

public enum CyeeForceTouchState {
    NULL(-1), PRESS(0), LIGHT(1), MID(2), FORCE(3);

    private int mValue = 0;

    CyeeForceTouchState(int value) {
        this.mValue = value;
    }

    public int getValue() {
        return this.mValue;
    }
}
