package cyee.widget;

/**
 * @author huming
 * @description the interface is implemented in the widget which contain complex
 *              view ,which can be called by launcher
 */
public interface ICyeeWidget {

	// allows you to add the maximum number of widget
    int getPermittedCount();

	// set the upper-left corner of coordinates of widget
    int[] setWigetPoi(int[] pos);

	// when widget is removed from launcher,call the method
    void onDestroy();

	// whether the widget can add onto launcher
	boolean canAddToGioneeLauncher();

	// whether the widget upto maxinum number
	void showUpToLimit();

	// add widget to launcher,call the method
	void onAddToGioneeLauncher();

	// set the screen which the widget display
    void setScreen(int curScreen);

	// when the widget start drag,call the method
    void onStartDrag();

	// when the widget stop drag,call the method
    void onStopDrag();

	// when the launcher screen start scroll,call the method
    boolean onScrollStart();

	// when the launcher screen stop scroll,call the method
    void onScrollEnd(int curScreen);

	// when the launcher screen is scrolling,call the method
    void onScroll(float factor);

	// when the floader is opening,call the method
    void startCovered(int screenConvered);

	// when the floader is closing,call the method
    void stopCovered(int screenConvered);

	// when the launcher activity onPause,call the method
    void onPauseWhenShown(int curScreen);

	// when the launcher activity onResume,call the method
    void onResumeWhenShown(int curScreen);

	// bind date on bundle object,update the view
    void updateView(android.os.Bundle arg0);
}
