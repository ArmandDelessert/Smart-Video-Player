package hesso.smartvideoplayer;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;

/**
 * Created by flavio on 6/15/17.
 */

public class BlueFilter {

    private View filterView = null;
    private boolean filterEn = false;
    private long filterColor = 0x20FFFF00;
    private SharedPreferences SP;

    BlueFilter (View _filterView, SharedPreferences _SP) {
        filterView = _filterView;
        filterView.setVisibility(View.INVISIBLE);
        SP = _SP;
    }

    boolean getEnable () {
        return filterEn;
    }

    void updateState(){
        // onResume blue filter, get shared preferences
        filterEn =  SP.getBoolean("pref_bluefilter_switch", filterEn);
        filterColor =  Long.parseLong(SP.getString("pref_bluefilter_color", Long.toHexString(filterColor)), 16);
        Log.i("FCCBlueFilter" , "blue filter color : "+Long.toHexString(filterColor));
        if (filterEn) {
            Log.i("FCCBlueFilter" , "Visible");
            filterView.setVisibility(View.VISIBLE);
            filterView.setBackgroundColor((int)filterColor);
        }
        else {
            Log.i("FCCBlueFilter" , "Invisible");
            filterView.setVisibility(View.INVISIBLE);
        }
    }
}
