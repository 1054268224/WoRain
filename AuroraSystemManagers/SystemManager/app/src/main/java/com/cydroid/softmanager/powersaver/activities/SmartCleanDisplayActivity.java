package com.cydroid.softmanager.powersaver.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.powersaver.utils.SmartCleanInfoWriter;
import com.cydroid.softmanager.utils.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/*
 * 供"拨号*#837504#>系统信息统计>智能内存清理"查看用的界面
 */
public class SmartCleanDisplayActivity extends AppCompatActivity {
    private final static String TAG = "SmartCleanDisplayActivity";

    private TextView infoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.powersaver_auto_clean_info_layout);

        infoTextView = (TextView) findViewById(R.id.info_text);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        upDataActivity();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void upDataActivity() {
        String info = readInfoFile();

        infoTextView.setText(info);
    }

    private String readInfoFile() {
        String strFile = SmartCleanInfoWriter.getDataFilePath();
        StringBuffer sb = new StringBuffer();

        try {
            File file = new File(strFile);

            if (!file.exists()) {
                sb.append("没有智能清理信息");
                return sb.toString();
            }

            InputStream input = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(input, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            reader.close();
            inputStreamReader.close();
            input.close();
        } catch (Exception e) {
            Log.d(TAG, "error : " + e.getMessage());

            sb.append("没有智能清理信息");
            return sb.toString();
        }
        return sb.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItemCompat.setShowAsAction(menu.add(Menu.NONE, Menu.FIRST, 0, "清空"), MenuItem.SHOW_AS_ACTION_ALWAYS);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case Menu.FIRST:
                clearLogFile();

                upDataActivity();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearLogFile() {
        try {
            File file = new File(SmartCleanInfoWriter.getDataFilePath());

            if (!file.exists()) {
                return;
            }

            if (!file.isFile()) {
                return;
            }
            file.delete();
        } catch (Exception e) {
            Log.d(TAG, "error : " + e.getMessage());
        }
    }
}
