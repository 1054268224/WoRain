package com.cydroid.powersaver.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cydroid.powersaver.launcher.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cyee.app.CyeeActionBar;
import cyee.app.CyeeActivity;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
//import cyee.widget.CyeeListView;
//import cyee.widget.CyeeTextView;

public class AppPickerActivity extends CyeeActivity implements AdapterView.OnItemClickListener {

    public static final String PICK_DATA = "pick";
    private static final String TAG = "AppPicker";
    // PHONE, CONTACTS, SMS, three fixed components in two packages
    private static final ArrayList<String> FIXED_APP_PACKAGE_NAMES;

    static {
        FIXED_APP_PACKAGE_NAMES = new ArrayList<>(2);
        FIXED_APP_PACKAGE_NAMES.add("com.android.contacts");
        FIXED_APP_PACKAGE_NAMES.add("com.android.mms");
    }

    private PackageManager mPackageManager;
    private MyAsyncTask myAsyncTask;
    private List<Function> mData = new ArrayList<>();
    private ChooseAdapter mChooseAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Chenyee xionghg add for black NavigationBar begin
        getWindow().setNavigationBarColor(getResources().getColor(android.R.color.black));
        // Chenyee xionghg add for black NavigationBar end
        // TODO: add a empty view to display when there were no extra apps
        setContentView(R.layout.listview);
        CyeeActionBar actionBar = getCyeeActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mPackageManager = getPackageManager();
        ListView list = (ListView) findViewById(R.id.list);
        mChooseAdapter = new ChooseAdapter();
        list.setAdapter(mChooseAdapter);
        list.setOnItemClickListener(this);
        queryApps();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        Function function = new Function();
        function.packageName = mData.get(position).packageName;
        function.activityName = mData.get(position).activityName;
        function.functionNull = false;
        intent.putExtra(PICK_DATA, function);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myAsyncTask.cancel(true);
    }

    private void queryApps() {
        myAsyncTask = new MyAsyncTask(this);
        myAsyncTask.execute();
    }

    /**
     * Use static inner class to prevent memory leaks
     */
    static class MyAsyncTask extends AsyncTask<Void, Void, List<ResolveInfo>> {

        private WeakReference<AppPickerActivity> weakReference;

        public MyAsyncTask(AppPickerActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        protected List<ResolveInfo> doInBackground(Void... params) {
            AppPickerActivity activity = weakReference.get();
            if (activity == null) {
                return null;
            }
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            return activity.mPackageManager.queryIntentActivities(mainIntent, 0);
        }

        @Override
        protected void onPostExecute(List<ResolveInfo> resolveInfos) {
            AppPickerActivity activity = weakReference.get();
            if (activity == null) {
                return;
            }
            activity.mData.clear();
            if (resolveInfos != null && resolveInfos.size() > 0) {
                List<String> systemApps = ConfigUtil.getSystemAppList(activity.getApplicationContext());
                List<String> socialApps = ConfigUtil.getSocialAppList(activity.getApplicationContext());
                for (ResolveInfo info : resolveInfos) {
                    String packageName = info.activityInfo.packageName;
                    String activityName = info.activityInfo.name;
                    // add filter
                    if (FIXED_APP_PACKAGE_NAMES.contains(packageName)) {
                        continue;
                    }
                    // if activity has been added, continue
                    if (ConfigUtil.activityAdded(activity, activityName)) {
                        continue;
                    }

                    if (systemApps.contains(packageName) || socialApps.contains(packageName)) {
                        Function function = new Function();
                        function.title = info.loadLabel(activity.mPackageManager).toString();
                        function.packageName = packageName;
                        function.activityName = activityName;
                        function.systemApp = systemApps.contains(packageName);
                        function.icon = info.loadIcon(activity.mPackageManager);
                        //Chenyee guoxt 20180503 modify for begin
                        ColorMatrix cm = new ColorMatrix();
                        cm.setSaturation(1.0f);
                        function.icon.setColorFilter(new ColorMatrixColorFilter(cm));
                        //Chenyee guoxt 20180503 modify for end

                        Log.d(TAG, info.toString());
                        activity.mData.add(function);
                    }
                }
            }
            activity.mChooseAdapter.notifyDataSetChanged();
        }
    }

    private class ChooseAdapter extends BaseAdapter {
        final LayoutInflater mInflater;

        public ChooseAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (null == convertView) {
                viewHolder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.customization_item, parent, false);
                viewHolder.title = (TextView) convertView.findViewById(R.id.title);
                viewHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
                viewHolder.type = (TextView) convertView.findViewById(R.id.type);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Function fun = mData.get(position);
            viewHolder.title.setText(fun.title);
            viewHolder.type.setText(fun.systemApp ? R.string.system_process : R.string.social_app);
            viewHolder.icon.setBackground(fun.icon);

            return convertView;
        }

        public class ViewHolder {
            TextView title;
            TextView type;
            ImageView icon;
        }
    }
}