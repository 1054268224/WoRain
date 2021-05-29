/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyee.internal.app;

import com.cyee.internal.R;
import com.android.internal.content.PackageMonitor;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.cyee.utils.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import cyee.widget.CyeeButton;
import android.widget.GridView;
import android.widget.ImageView;
import cyee.widget.CyeeListView;
import android.widget.TextView;
import android.view.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import java.lang.reflect.Method;



//Gionee:zhang_xin 2012-10-23 add for CR00717539 start
import android.os.SystemProperties;
//Gionee:zhang_xin 2012-10-23 add for CR00717539 end

// Gionee fengjianyi 2012-12-26 add for CR00751916 start

//import com.mediatek.common.featureoption.FeatureOption;
// Gionee fengjianyi 2012-12-26 add for CR00751916 end


/**
 * This activity is displayed when the system attempts to start an Intent for
 * which there is more than one matching activity, allowing the user to decide
 * which to go to.  It is not normally used directly by application developers.
 */
public class CyeeDualInstanceActivity extends CyeeAlertActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "CyeeDualInstanceActivity";

    private DisplayListAdapter mAdapter;
    private PackageManager mPM;
    private GridView mGrid;
    private CyeeButton mAlwaysButton;
    private CyeeButton mOnceButton;
    
    private int mIconDpi;
    private int mIconSize;
    
    private final boolean mIsSupportAppSort = SystemProperties.get("ro.gn.appsort.default").equals("yes");
    private boolean mRegistered;
    
    private final List<DisplayInfo> mDispInfoList = new ArrayList<DisplayInfo>();
    private Intent mOrigIntent;
    
    private final PackageMonitor mPackageMonitor = new PackageMonitor() {
        @Override public void onSomePackagesChanged() {
//            mAdapter.handlePackagesChanged();
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
    	CharSequence title = getResources().getText(R.string.cyee_whichApplication);
    	setTheme(com.cyee.internal.R.style.Theme_Cyee_Light_Dialog_Alert);

        super.onCreate(savedInstanceState);
        
        mPM = getPackageManager();
        
        CyeeAlertController.AlertParams ap = mAlertParams;

        ap.mTitle = title;

        mPackageMonitor.register(this, getMainLooper(), false);
        mRegistered = true;

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        
        mIconDpi = am.getLauncherLargeIconDensity();
        mIconSize = am.getLauncherLargeIconSize();
        
        Intent intent = getIntent();
        
        String clonePkg = intent.getStringExtra("CLONE_PKG_NAME");
        
        setUpData(clonePkg);
        
        mAdapter = new DisplayListAdapter(this, mDispInfoList);
        
        ap.mView = getLayoutInflater().inflate(R.layout.cyee_resolver_grid, null);
        mGrid = (GridView) ap.mView.findViewById(R.id.cyee_resolver_grid);
        mGrid.setAdapter(mAdapter);

        mGrid.setOnItemClickListener(this);
        mGrid.setChoiceMode(CyeeListView.CHOICE_MODE_SINGLE);

        mGrid.setNumColumns(2);
        mGrid.setItemChecked(0, true);

        setupAlert();

        final ViewGroup buttonPanelLayout = (ViewGroup) findViewById(R.id.cyee_buttonPanel);
		final ViewGroup buttonLayout = (ViewGroup) findViewById(R.id.cyee_button_bar);
        if (buttonPanelLayout != null && buttonLayout != null) {
            buttonPanelLayout.setVisibility(View.GONE);
			buttonLayout.setVisibility(View.GONE);
        }
			/*
            mAlwaysButton = (CyeeButton) buttonLayout.findViewById(R.id.cyee_button_always);
            mOnceButton = (CyeeButton) buttonLayout.findViewById(R.id.cyee_button_once);
            if (count > 1) {
                mAlwaysButton.setEnabled(true);
                mOnceButton.setEnabled(true);
            }
            */
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!mRegistered) {
            mPackageMonitor.register(this, getMainLooper(), false);
            mRegistered = true;
        }
//        mAdapter.handlePackagesChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mRegistered) {
            mPackageMonitor.unregister();
            mRegistered = false;
        }
        
        if ((getIntent().getFlags()&Intent.FLAG_ACTIVITY_NEW_TASK) != 0) {
            // This resolver is in the unusual situation where it has been
            // launched at the top of a new task.  We don't let it be added
            // to the recent tasks shown to the user, and we need to make sure
            // that each time we are launched we get the correct launching
            // uid (not re-using the same resolver from an old launching uid),
            // so we will now finish ourself since being no longer visible,
            // the user probably can't get back to us.
            if (!isChangingConfigurations()) {
                finish();
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    	
    	DisplayInfo di = mDispInfoList.get(position);
    	
    	Intent result = new Intent();
    	result.putExtra("KEY_SEL_CLONE", di.isClone);
    	setResult(RESULT_OK, result);
    	
    	Intent extraIntent = getIntent().getParcelableExtra("EXTRA_ORG_INTENT");
    	if(extraIntent != null){
            if(di.isClone){
    	        extraIntent.addCategory("android.intent.category.CLONED");
            }
            try {
                startActivity(extraIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    	
    	finish();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
       if (keyCode == KeyEvent.KEYCODE_BACK) {
           setResult(RESULT_CANCELED);
           finish();
           return true;
       }
       return super.onKeyUp(keyCode, event);   
    }

    public void onButtonClick(View v) {
        final int id = v.getId();
//        startSelected(mGrid.getCheckedItemPosition(), id == R.id.cyee_button_always);
        dismiss();
    }

    private final class DisplayInfo {
        ResolveInfo ri;
        CharSequence displayLabel;
        Drawable displayIcon;
        CharSequence extendedInfo;
        Intent origIntent;
        boolean isClone;
        DisplayInfo(ResolveInfo pri, CharSequence pLabel,
                CharSequence pInfo, Intent pOrigIntent) {
            ri = pri;
            displayLabel = pLabel;
            extendedInfo = pInfo;
            origIntent = pOrigIntent;
        }
        
        DisplayInfo(CharSequence label,
        		Drawable icon,boolean clone) {
            displayLabel = label;
            displayIcon = icon;
            isClone = clone;
        }
    }

    
    private void setUpData(String pkg){
    	ApplicationInfo ai = null;
    	
    	try {
    		ai = mPM.getApplicationInfo(pkg, 0);
		} catch (NameNotFoundException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
    	
		if (ai != null && !isSecretAPP(pkg, this.getContentResolver())) {
			String label = ai.loadLabel(mPM).toString();
			Drawable icon = ai.loadIcon(mPM);

			DisplayInfo di = new DisplayInfo(label, icon, false);
		        mDispInfoList.add(di);

			/*
			String cloneLabel = label + getResources().getString(R.string.clone_suffix);
			DisplayInfo cloneDi = new DisplayInfo(cloneLabel, icon, true);
			*/
		}
		
		ApplicationInfo cloneAi = null;
    	try {
    		//Ex,we must have an app named com.tencent.mm.clone
    		String clonePkgName = pkg + ".clone";
    		cloneAi = mPM.getApplicationInfo(clonePkgName, 0);
		} catch (NameNotFoundException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		if(cloneAi != null && !isSecretAPP(pkg + ".clone", this.getContentResolver())){
			
			String cloneLabel = cloneAi.loadLabel(mPM).toString();
			Drawable cloneIcon = cloneAi.loadIcon(mPM);
			
			DisplayInfo cloneDi = new DisplayInfo(cloneLabel, cloneIcon, true);
                        mDispInfoList.add(cloneDi);
		}
    }
   
    private boolean isSecretAPP(String pkg, ContentResolver resolver) {

        boolean result = false;

        try {
            Class cls = Class.forName("com.android.server.pm.EncryptFramworkUtils");
            Method method = cls.getMethod("isEncryptApp", String.class, ContentResolver.class);
            result = (boolean) method.invoke(null, pkg, resolver);
            Log.v("ARA, result ", pkg + ":" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result; 

    }
 
    private final class DisplayListAdapter extends BaseAdapter {
    	
        private final LayoutInflater mInflater;
        private final List<DisplayInfo> mDiList;
        
		public DisplayListAdapter(Context context, List<DisplayInfo> data) {
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mDiList = data;
		}

        public int getCount() {
            return mDiList != null ? mDiList.size() : 0;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				
				view = mInflater.inflate(R.layout.cyee_resolve_list_item_light, parent, false);
			
				ImageView icon = (ImageView) view.findViewById(com.android.internal.R.id.icon);
				ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) icon.getLayoutParams();
				lp.width = lp.height = mIconSize;
				
			} else {
				view = convertView;
			}
			
			bindView(view, mDiList.get(position));
			
			return view;
		}

        private final void bindView(View view, DisplayInfo info) {
        	
            TextView labelTv = (TextView)view.findViewById(com.android.internal.R.id.text1);
            labelTv.setText(info.displayLabel);
            
            TextView text2 = (TextView)view.findViewById(com.android.internal.R.id.text2);
            text2.setVisibility(View.GONE);
            
            ImageView icon = (ImageView)view.findViewById(com.android.internal.R.id.icon);
            icon.setImageDrawable(info.displayIcon);
            
        }
        
    }
}

