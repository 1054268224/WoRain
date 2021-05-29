package gn.com.android.facerecog.aidl;
import gn.com.android.facerecog.aidl.IServiceCallback;

interface IFaceRecogService {
     void  startFaceRecognition(int timeout, IServiceCallback callback);  
} 