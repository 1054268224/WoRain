package gn.com.android.facerecog.aidl;
oneway interface IServiceCallback {
    void onAuthenticationError(int errorCode, CharSequence errString);
	void onAuthenticationSucceeded(int resultCode);
}