package com.cydroid.systemmanager.rubbishcleaner;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Formatter;

import com.cydroid.softmanager.R;

import java.io.File;

import cyee.app.CyeeActivity;
import cyee.app.CyeeAlertDialog;
import com.cydroid.systemmanager.utils.FileUtil;
import com.cydroid.systemmanager.utils.Log;

public class ResidualRubbishAlertActivity extends CyeeActivity {
	private String[] mPaths;
	private static final boolean DEBUG = true;
	private static final String TAG = "CyeeRubbishCleaner/ResidualRubbishAlertActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.ResidualDialogStyle);
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String pkgName = intent.getStringExtra("package_name");
		long size = intent.getLongExtra("residual_size", 0);
		mPaths = intent.getStringArrayExtra("files_path");
		String msg = getResources().getString(R.string.residual_alert_content)
				+ Formatter.formatShortFileSize(this, size);
		Log.d(DEBUG, TAG, "ResidualRubbishAlertActivity, pkgName = "
				+ pkgName + ", msg = " + msg);
		showDialog(msg);
	}

	private void showDialog(String msg) {
		CyeeAlertDialog.Builder builder = new CyeeAlertDialog.Builder(this);
		builder.setTitle(R.string.residual_alert_title);
		builder.setMessage(msg);
		builder.setPositiveButton(R.string.go_clean, listener);
		builder.setNegativeButton(R.string.go_cancel, listener);
		// Gionee <xuwen> <2015-08-25> add for CR01547240 begin
		builder.setOnDismissListener(mDismissListener);
		// Gionee <xuwen> <2015-08-25> add for CR01547240 end
		builder.show();
	}

	private DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case CyeeAlertDialog.BUTTON_NEGATIVE:
				dismissAlertActivity();
				break;
			case CyeeAlertDialog.BUTTON_POSITIVE:
				goCleanResidualRubbish();
				break;
			}

			// Gionee <xuwen> <2015-08-25> add for CR01547240 begin
			dialog.dismiss();
			// Gionee <xuwen> <2015-08-25> add for CR01547240 end

		}
	};

	private void dismissAlertActivity() {
	}

	private void goCleanResidualRubbish() {
		if (mPaths == null) {
			return;
		}
		deleteResidual(mPaths);
	}

	private void deleteResidual(final String[] path) {
		new Thread() {
			public void run() {
				Log.d(DEBUG, TAG,
						"ResidualRubbishAlertActivity, deleteResidual, path = "
								+ path.toString());
				for (int i = 0; i < path.length; i++) {
					FileUtil.recurDelete(new File(path[i]));
				}
			}
		}.start();
	}

	// Gionee <xuwen> <2015-08-25> add for CR01547240 begin
	private DialogInterface.OnDismissListener mDismissListener = new DialogInterface.OnDismissListener() {
		@Override
		public void onDismiss(DialogInterface arg0) {
			finish();
		}
	};
	// Gionee <xuwen> <2015-08-25> add for CR01547240 end
}
