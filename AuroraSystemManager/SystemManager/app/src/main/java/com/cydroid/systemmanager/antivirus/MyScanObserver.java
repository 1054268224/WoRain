package com.cydroid.systemmanager.antivirus;

import com.intel.security.vsm.ScanObserver;
import com.intel.security.vsm.ScanResult;
import com.intel.security.vsm.Threat;
import com.intel.security.vsm.content.ScanSource;

import com.cydroid.systemmanager.utils.Log;

public class MyScanObserver implements ScanObserver
    {
     private static final String TAG = "MyScanObserver";

        private String mFrom;
        private long mOldTime = 0;
        private int count = 0;

        public MyScanObserver(String str)
        {
            mFrom = str;
        }

        @Override
        public void onStarted()
        {
            Log.d(TAG, "[" + mFrom + "] start scan.");
            mOldTime = System.currentTimeMillis();
            count = 0;
        }

        @Override
        public void onCompleted(int i)
        {
             Log.d(TAG, "========batchScan.scanCount=" + count);


        }

        @Override
        public void onScanned(ScanSource source, ScanResult scanResult)
        {
if (scanResult.getThreat() != null)
            {
                Threat threat = scanResult.getThreat();

                Log.d(TAG, "[" + mFrom + "] " + source.toString() + " is a threat!");
                Log.d(TAG,
                        "[" + mFrom + "] " + "ThreatName = " + threat.getName() + ", ThreatLevel = " +
                                threat.getRiskLevel() + ", ThreatType = " + threat.getType() +
                                ", ThreatVariant = " + threat.getVariant());

            }
           
        }
    }



