package com.cydroid.systemmanager.antivirus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.cydroid.softmanager.R;

import java.util.ArrayList;

import com.cydroid.systemmanager.BaseActivity;
import com.cydroid.systemmanager.utils.UiUtils;

public class ScanningExceptionActivity extends BaseActivity {
	private TextView textView = null;
	private ArrayList<String> scanResultList = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UiUtils.setElevation(getCyeeActionBar(), 0);
		setContentView(R.layout.virusscan_exception_layout);
		textView = (TextView) findViewById(R.id.info2);
		Intent intent = getIntent();
		scanResultList = intent
				.getStringArrayListExtra("com.qvssdk.demo.SCANRESULT");
		updateTextView();
	}

	private void updateTextView() {
		StringBuilder builder = new StringBuilder();
		for (String s : scanResultList) {
			builder.append(s);
			builder.append("\n");
		}
		textView.setText(builder.toString());
	}
}
