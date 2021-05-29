package com.odm.tool.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class CameraHelper {
    private static final String TAG = "WtkToolBox/CameraHelper";

    public static final String CAMERA_BACK = "0";
    public static final String CAMERA_FRONT = "1";

    private String mCameraId = CAMERA_BACK; // Default back camera

    private final Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private final Object mCameraStateLock = new Object();

    private CameraManager mCameraManager;
    private Context mContext;
    private TextureView mTextureView;
    private SurfaceView mSurfaceView;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mCaptureSession;
    private CameraDevice mCameraDevice;
    private CameraCharacteristics mCameraCharacteristics;

    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    private Callback mCallback;

    public interface Callback {
        public void refreshUI();
    }

    public  CameraHelper(Context context, SurfaceView surfaceView, TextureView textureView, Callback callback ){
        mContext = context;
        mTextureView = textureView;
        mSurfaceView = surfaceView;
        mCallback = callback;
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        if (mSurfaceView != null) {
            mSurfaceView.getHolder().addCallback(mSurfaceHolderCallback);
        }
        if (mTextureView != null) {
            mTextureView.setSurfaceTextureListener(mTextureListener);
        }
    }

    public void openCamera() {
        openCamera(mCameraId);
    }


    @SuppressLint("MissingPermission")
    public void openCamera(String cameraId) {
        try {
            // Attempt to open the camera. mStateCallback will be called on the background handler's
            // thread when this succeeds or fails.

            mCameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);
            mCameraManager.openCamera(cameraId, mCameraDeviceStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            synchronized (mCameraStateLock) {
                if (null != mCaptureSession) {
                    mCaptureSession.close();
                    mCaptureSession = null;
                }
                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    public void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        synchronized (mCameraStateLock) {
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        }
    }

    public void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            synchronized (mCameraStateLock) {
                mBackgroundHandler = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startPreview() throws CameraAccessException {
        mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getRealMetrics(dm);
        int width = Math.max(dm.widthPixels, dm.heightPixels);
        int height = Math.min(dm.widthPixels, dm.heightPixels);
        Size previewSize = null;
        Surface surface = null;
        StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map != null) {
            previewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);
        }

        if (mTextureView != null) {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();

            if(previewSize != null){
                texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            }
            surface = new Surface(texture);
        }

        if (mSurfaceView != null) {
            if(previewSize != null) {
                final Size previewSize1 = previewSize;
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSurfaceView.getHolder().setFixedSize(previewSize1.getWidth(), previewSize1.getHeight());
                    }
                });
            }
            surface = mSurfaceView.getHolder().getSurface();
        }
		
        mPreviewBuilder.addTarget(surface);
        mCameraDevice.createCaptureSession(Arrays.asList(surface), mSessionStateCallback, mBackgroundHandler);
        if (mCallback != null) {
            mCallback.refreshUI();
        }
    }

    private final SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            openCamera(mCameraId);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    };

    // A: Bug_id:EWSY-489 chenchunyong 20181204 {
    private TextureView.SurfaceTextureListener mTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera(mCameraId);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };
	// A: }

    private final CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here if
            // the TextureView displaying this has been set up.
            synchronized (mCameraStateLock) {
                mCameraOpenCloseLock.release();
                mCameraDevice = cameraDevice;
                try {
                    startPreview();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            synchronized (mCameraStateLock) {
                mCameraOpenCloseLock.release();
                cameraDevice.close();
                mCameraDevice = null;
            }
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            synchronized (mCameraStateLock) {
                mCameraOpenCloseLock.release();
                cameraDevice.close();
                mCameraDevice = null;
            }
            ((Activity) mContext).finish();
        }
    };

    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                session.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, mBackgroundHandler);
                mCaptureSession = session;
            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                //CameraDevice was already closed
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
        }
    };


    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                       TotalCaptureResult result) {
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
                                        CaptureResult partialResult) {
        }
    };

    public <T> void setPreviewBuilder(CaptureRequest.Key<T> key, T value) {
        if (mPreviewBuilder == null || mCaptureSession == null) {
            return;
        }
        mPreviewBuilder.set(key, value);
        try {
            mCaptureSession.setRepeatingRequest(
                    mPreviewBuilder.build(),
                    mSessionCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return;
        } catch (IllegalStateException e) {
            //CameraDevice was already closed
            e.printStackTrace();
        }
    }

    public <T> T getPreviewBuilder(CaptureRequest.Key<T> key) {
        return mPreviewBuilder.get(key);
    }

    public <T> T getCameraCharacteristics(CameraCharacteristics.Key<T> key) {
        //A: hongqian YWSW-1662 20190918 start
        if(mCameraCharacteristics == null){
            try {
                mCameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        //A: hongqian YWSW-1662 20190918 end
        return mCameraCharacteristics.get(key);
    }

    public int getNumberOfCameras() {
        String[] cameraIdList = null;
        try {
            cameraIdList = mCameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return cameraIdList != null ? cameraIdList.length : 0;
    }

	// A: Bug_id:EWSY-489 chenchunyong 20181204 {
    private Size getOptimalSize(Size[] sizeMap, int width, int height) {
        double displayRatio = (double) width / (double) height;
        if(displayRatio > (18d / 9)){
            displayRatio = 18d / 9;
        }
        double ASPECT_TOLERANCE = 0.02;
        double minDiff;
        double minDiffMax = Double.MAX_VALUE;
        Size bestMatchSize = null;
        for (Size size : sizeMap) {
            double ratio = (double) size.getWidth() / size.getHeight();
            if (Math.abs(ratio -displayRatio) <= ASPECT_TOLERANCE) {
                minDiff = Math.abs(size.getHeight() - height);
                if (minDiff <= minDiffMax) {
                    minDiffMax = minDiff;
                    bestMatchSize = size;
                }
            }
        }
        if(bestMatchSize != null){
             Log.d(TAG, " getOptimalPreviewSize size: "
                + bestMatchSize.getWidth() + " X " + bestMatchSize.getHeight());
        }
        return bestMatchSize;
    }
	// A: }
}
