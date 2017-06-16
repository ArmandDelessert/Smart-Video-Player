package hesso.smartvideoplayer;

import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by flavio on 6/15/17.
 */

public class BlueFilter {

    private View filterView = null;
    private boolean filterEn = false;
    private long filterColor = 0x20FFFF00;
    private SharedPreferences SP;

    private Handler handler = new Handler();

    BlueFilter (View _filterView, SharedPreferences _SP) {
        filterView = _filterView;
        filterView.setVisibility(View.INVISIBLE);
        SP = _SP;
    }


    private void setVisible (boolean visible) {
        if (visible) {
            Log.i("FCCBlueFilter", "Visible");
            filterView.setVisibility(View.VISIBLE);
        } else {
            Log.i("FCCBlueFilter", "Invisible");
            filterView.setVisibility(View.INVISIBLE);
        }
    }

    private Runnable enableFilter = new Runnable(){
        public void run() {
            setVisible(true);
            handler.postDelayed(enableFilter, 1000*3600*24);// repeat after 24h ...
            // TODO : repeat usefull ? Certainly that the app will be restarted until this time ...
        }
    };
    private Runnable disableFilter = new Runnable(){
        public void run() {
            setVisible(false);
            handler.postDelayed(disableFilter, 1000*3600*24);// repeat after 24h ...
            // TODO : repeat usefull ? Certainly that the app will be restarted until this time ...
        }
    };


    void updateState(){
        // onResume blue filter, get shared preferences
        filterColor =  Long.parseLong(SP.getString("pref_bluefilter_color", Long.toHexString(filterColor)), 16);
        filterView.setBackgroundColor((int)filterColor);
        Log.i("FCCBlueFilter" , "blue filter color : "+Long.toHexString(filterColor));

        // Get timings
        Calendar cal = Calendar.getInstance();
        long now = cal.getTime().getTime();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long startDay = cal.getTime().getTime();
        long oneHour = 1000*3600;
        long oneDay = oneHour*24;

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

        String strEn = SP.getString("pref_bluefilter_enable", Long.toHexString(filterColor));
        long millsEn, todayMillsEn;
        try {
            todayMillsEn = dateFormat.parse(strEn).getTime()+oneHour+startDay;
            if (todayMillsEn>now)
                millsEn = todayMillsEn;
            else
                millsEn = todayMillsEn+oneDay;
        } catch (ParseException e) {
            Log.e("FCCBlueFilter" , "Wrong date format for pref_bluefilter_enable");
            return; // TODO : correct SP and tell it to user
        }

        String strDis= SP.getString("pref_bluefilter_disable", Long.toHexString(filterColor));
        long millsDis, todayMillsDis;
        try {
            todayMillsDis = dateFormat.parse(strDis).getTime()+oneHour+startDay;
            if (todayMillsDis>now)
                millsDis = todayMillsDis;
            else
                millsDis = todayMillsDis+oneDay;
        } catch (ParseException e) {
            Log.e("FCCBlueFilter" , "Wrong date format for pref_bluefilter_disable");
            return; // TODO : correct SP and tell it to user
        }

        SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Log.i("FCCBlueFilter" , "now :" + dateFormat2.format(new Date(now)));
        Log.i("FCCBlueFilter" , "todayMillsEn :" + dateFormat2.format(new Date(todayMillsEn)));
        Log.i("FCCBlueFilter" , "millsEn :" + dateFormat2.format(new Date(millsEn)));
        Log.i("FCCBlueFilter" , "todayMillsDis :" + dateFormat2.format(new Date(todayMillsDis)));
        Log.i("FCCBlueFilter" , "millsDis :" + dateFormat2.format(new Date(millsDis)));

        filterEn =  SP.getBoolean("pref_bluefilter_switch", filterEn);

        handler.removeCallbacks(disableFilter);
        handler.removeCallbacks(enableFilter);
        if (filterEn) {
            handler.postDelayed(disableFilter, millsDis-now);
            handler.postDelayed(enableFilter, millsEn-now);

            if (todayMillsDis<todayMillsEn) {
                if ((now<todayMillsDis) || (now>todayMillsEn)){
                    setVisible(true);
                    //Log.e("FCCBlueFilter" , "1");
                }
                else{
                    setVisible(false);
                    //Log.e("FCCBlueFilter" , "2");
                }
            } else {
                if ((now<todayMillsEn) && (now<todayMillsDis)){
                    setVisible(true);
                    //Log.e("FCCBlueFilter" , "3");
                }
                else{
                    setVisible(false);
                    //Log.e("FCCBlueFilter" , "4");
                }
            }
        }
        else {
            Log.i("FCCBlueFilter" , "Invisible");
            filterView.setVisibility(View.INVISIBLE);
        }


    }
}
