package mobi.intuitit.android.p.launcher;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;

/**
 * 
 * @author bo
 * 
 */
public class ScreenPrefActivity extends PreferenceActivity implements OnPreferenceChangeListener {

    private SharedPreferences mDefaultPrefs;
    private ListPreference mScreenNumberPref;
    private ListPreference mDefaultScreenPref;

    static final String PREFIX = "Current: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.screens_prefs);

        mDefaultPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        mScreenNumberPref = (ListPreference) findPreference(getString(R.string.key_screen_number));
        mScreenNumberPref.setOnPreferenceChangeListener(this);

        String total = mDefaultPrefs.getString(getString(R.string.key_screen_number), "4");
        mScreenNumberPref.setSummary(PREFIX + total);

        CharSequence[] screens = newSeq(total);
        mDefaultScreenPref = (ListPreference) findPreference(getString(R.string.key_default_screen));
        mDefaultScreenPref.setOnPreferenceChangeListener(this);
        mDefaultScreenPref.setEntries(screens);
        mDefaultScreenPref.setEntryValues(screens);
        mDefaultScreenPref.setSummary(PREFIX + mDefaultScreenPref.getValue());

    }

    /**
     * 
     */
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        preference.setSummary(PREFIX + (CharSequence) newValue);
        if (preference == mScreenNumberPref) {
            try {
                String v = (String) newValue;
                int total = Integer.parseInt(v);
                int defaults = Integer.parseInt(mDefaultScreenPref.getValue());
                if (total < defaults) {
                    mDefaultScreenPref.setValue(v);
                    mDefaultScreenPref.setSummary(PREFIX + v);
                }

                CharSequence[] screens = newSeq(v);
                mDefaultScreenPref.setEntries(screens);
                mDefaultScreenPref.setEntryValues(screens);

            } catch (Exception e) {
            }
        } else if (preference == mDefaultScreenPref) {

        }
        return true;
    }

    /**
     * 
     * @param v
     * @return
     */
    private CharSequence[] newSeq(String v) {
        int total = Integer.parseInt(v);
        CharSequence[] screens = new CharSequence[total];
        for (int i = 1; i <= total; i++) {
            screens[i - 1] = i + "";
        }
        return screens;
    }

}
