package com.cydroid.softmanager.softmanager;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.cydroid.softmanager.R;
import com.cydroid.softmanager.interfaces.StateChangeCallback;
import com.cydroid.softmanager.softmanager.adapter.DefaultSoftDetailAdapter;
import com.cydroid.softmanager.softmanager.defaultsoft.DefMrgSoftIntent;
import com.cydroid.softmanager.softmanager.defaultsoft.DefaultSoftResolveInfo;
import com.cydroid.softmanager.softmanager.defaultsoft.DefaultSoftSettingsManager;
import com.cydroid.softmanager.softmanager.loader.SoftManagerLoader;
import com.cydroid.softmanager.utils.Log;

import java.util.ArrayList;
import java.util.List;

import cyee.widget.CyeeEditModeView;
import cyee.widget.CyeeRadioButton;

public class DefaultSoftDetailActivity extends BaseActivity
        implements OnItemClickListener, LoaderCallbacks<Object>, StateChangeCallback {
    private static final String TAG = "DefaultSoftDetailActivity";
    public static final String DEFAULT_SOFT_ITEM_INDEX = "default_soft_item_index";

    private int mDefaultSoftItemIndex;
    private int mCurrentDefaultSoftIndex;
    List<DefaultSoftResolveInfo> mData = new ArrayList<>();

    private CyeeEditModeView mEditModeView;
    private ListView mListView;
    private DefaultSoftDetailAdapter mAdapter;

    private DefaultSoftSettingsManager mDefaultSoftSettingsManager;
    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.softmanager_activity_default_soft_detail);
        mDefaultSoftSettingsManager = DefaultSoftSettingsManager.getInstance();
        try {
            parseIntent();
            initView();
            mDefaultSoftSettingsManager.setAppsChangeCallBack(String.valueOf(this.hashCode()), this);
            getLoaderManager().initLoader(SoftManagerLoader.ID_LOADER_DEFAULTSOFT, null, this);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "DEFAULT_SOFT_ITEM_INDEX is invalid", e);
            finish();
        }
    }

    private void parseIntent() throws IllegalArgumentException {
        mDefaultSoftItemIndex = getIntent().getIntExtra(DEFAULT_SOFT_ITEM_INDEX, -1);
        if (-1 == mDefaultSoftItemIndex) {
            throw new IllegalArgumentException();
        }
    }

    private void initView() {
        Resources res = getResources();
        getCyeeActionBar().hide();
        // Chenyee xionghg 20171012 add for 212890 begin
        TextView title = (TextView) findViewById(R.id.select_default);
        title.setText(getResources().getString(R.string.select_default_text, getTitle(mDefaultSoftItemIndex)));
        // Chenyee xionghg 20171012 add for 212890 end
        mListView = (ListView) findViewById(R.id.listview);
        mListView.setVisibility(View.GONE);
        mListView.setOnItemClickListener(this);
        mAdapter = new DefaultSoftDetailAdapter(this, mData);
        mListView.setAdapter(mAdapter);

        mEditModeView = (CyeeEditModeView) findViewById(R.id.editmodeview);
        mEditModeView.setEditModeBtnTxt(res.getString(R.string.freeze_cancel),
                res.getString(R.string.freeze_continue));
    }

    private String getTitle(int index) {
        return mDefaultSoftSettingsManager.getDefaultSoftItemNameByIndex(index);
    }

    private void setEditModeBtnClickListener() {
        mEditModeView.setEditModeBtnClickListener(new CyeeEditModeView.EditModeClickListener() {
            @Override
            public void rightBtnClick() {
                if (mListView.getVisibility() == View.GONE) {
                    return;
                }
                int adapterPos = mAdapter.getDefaultPos();
                if (adapterPos == mCurrentDefaultSoftIndex) {
                    finish();
                    return;
                }

                switch (mDefaultSoftItemIndex) {
                    case DefMrgSoftIntent.DEF_INPUT_METHOD:
                        mDefaultSoftSettingsManager.setDefInputMethod(adapterPos);
                        break;
                    //fengpeipei modify for 58595 start
                    case DefMrgSoftIntent.DEF_BROWSER:
                        mDefaultSoftSettingsManager.setDefBrowserMethod(adapterPos);
                        break;
                    //fengpeipei modify for 58595 end
                    default:
                        mDefaultSoftSettingsManager.setDefSoft(adapterPos, mDefaultSoftItemIndex);
                        break;
                }

                finish();
            }

            @Override
            public void leftBtnClick() {
                if (mListView.getVisibility() == View.GONE) {
                    return;
                }
                finish();
            }
        });
    }

    @Override
    public Loader<Object> onCreateLoader(int id, Bundle args) {
        return new SoftManagerLoader(this);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        mDefaultSoftSettingsManager.unsetAppsChangeCallBack(String.valueOf(this.hashCode()));
        mData.clear();
    }

    @Override
    public void onLoadFinished(Loader<Object> arg0, Object arg1) {
        // Chenyee xionghg 20171012 delete for 212890 begin
        // TextView title = (TextView) findViewById(R.id.select_default);
        // title.setText(getResources().getString(R.string.select_default_text, getTitle(mDefaultSoftItemIndex)));
        // Chenyee xionghg 20171012 delete for 212890 end
        setEditModeBtnClickListener();
        updateList(mDefaultSoftItemIndex);
    }

    @Override
    public void onLoaderReset(Loader<Object> arg0) {
    }

    private void updateList(int pos) {
        List<DefaultSoftResolveInfo> data = mDefaultSoftSettingsManager.
                getDefaultSoftMatchesByItemIndex(mDefaultSoftItemIndex);

        mData.clear();
        mData.addAll(data);
        mCurrentDefaultSoftIndex = 0;
        for (int i = 0; i < mData.size(); ++i) {
            DefaultSoftResolveInfo ri = mData.get(i);
            if (ri.isBestMatched()) {
                mCurrentDefaultSoftIndex = i;
                break;
            }
        }
        mAdapter.setDefaultPos(mCurrentDefaultSoftIndex);
        mAdapter.notifyDataSetChanged();
        mListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStateChange() {
        mHandler.post(new Runnable() {
            public void run() {
                updateList(mDefaultSoftItemIndex);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
        CyeeRadioButton radio = (CyeeRadioButton) view.findViewById(R.id.radio);
        radio.setChecked(!radio.isChecked());
    }

    @Override
    public void onBackPressed() {
        mListView.setVisibility(View.GONE);
        finish();
    }
}
