package learning.nitish.loktralocationtracker.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Nitish Singh Rathore on 6/8/17.
 */

public class SharedPrefHelperClass {

    private static final String KEY = "SERVICE_STATE";
    Context mContext;

    public SharedPrefHelperClass(Context mContext) {
        this.mContext = mContext;
    }

    public void setServiceState(boolean value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        sp.edit().putBoolean(KEY, value).apply();
    }


    public boolean getServiceState() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sp.getBoolean(KEY, true);
    }
}
