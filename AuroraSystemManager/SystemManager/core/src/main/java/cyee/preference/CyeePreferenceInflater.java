package cyee.preference;

import java.io.IOException;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.cyee.internal.util.CyeeXmlUtils;

import android.app.AliasActivity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import com.cyee.utils.Log;

/**
 * The {@link PreferenceInflater} is used to inflate preference hierarchies from
 * XML files.
 * <p>
 * Do not construct this directly, instead use
 * {@link Context#getSystemService(String)} with
 * {@link Context#PREFERENCE_INFLATER_SERVICE}.
 */
class CyeePreferenceInflater extends CyeeGenericInflater<CyeePreference, CyeePreferenceGroup> {
    private static final String TAG = "PreferenceInflater";
    private static final String INTENT_TAG_NAME = "intent";
    private static final String EXTRA_TAG_NAME = "extra";

    private CyeePreferenceManager mPreferenceManager;
    
    public CyeePreferenceInflater(Context context, CyeePreferenceManager preferenceManager) {
        super(context);
        init(preferenceManager);
    }

    CyeePreferenceInflater(CyeeGenericInflater<CyeePreference, CyeePreferenceGroup> original, CyeePreferenceManager preferenceManager, Context newContext) {
        super(original, newContext);
        init(preferenceManager);
    }

    @Override
    public CyeeGenericInflater<CyeePreference, CyeePreferenceGroup> cloneInContext(Context newContext) {
        return new CyeePreferenceInflater(this, mPreferenceManager, newContext);
    }
    
    private void init(CyeePreferenceManager preferenceManager) {
        mPreferenceManager = preferenceManager;
        setDefaultPackage("cyee.preference.");
    }

    @Override
    protected boolean onCreateCustomFromTag(XmlPullParser parser, CyeePreference parentPreference,
            AttributeSet attrs) throws XmlPullParserException {
        final String tag = parser.getName();
        
        if (tag.equals(INTENT_TAG_NAME)) {
            Intent intent = null;
            
            try {
                intent = Intent.parseIntent(getContext().getResources(), parser, attrs);
            } catch (IOException e) {
                XmlPullParserException ex = new XmlPullParserException(
                        "Error parsing preference");
                ex.initCause(e);
                throw ex;
            }
            
            if (intent != null) {
                parentPreference.setIntent(intent);
            }
            
            return true;
        } else if (tag.equals(EXTRA_TAG_NAME)) {
            getContext().getResources().parseBundleExtra(EXTRA_TAG_NAME, attrs,
                    parentPreference.getExtras());
            try {
                CyeeXmlUtils.skipCurrentTag(parser);
            } catch (IOException e) {
                XmlPullParserException ex = new XmlPullParserException(
                        "Error parsing preference");
                ex.initCause(e);
                throw ex;
            }
            return true;
        }
        
        return false;
    }

    @Override
    protected CyeePreferenceGroup onMergeRoots(CyeePreferenceGroup givenRoot, boolean attachToGivenRoot,
            CyeePreferenceGroup xmlRoot) {
        // If we were given a Preferences, use it as the root (ignoring the root
        // Preferences from the XML file).
        if (givenRoot == null) {
            xmlRoot.onAttachedToHierarchy(mPreferenceManager);
            return xmlRoot;
        } else {
            return givenRoot;
        }
    }
    
}
