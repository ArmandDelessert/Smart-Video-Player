package hesso.smartvideoplayer;//package hesso.smartvideoplayer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.afollestad.easyvideoplayersample.R;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by flavio on 08.05.17.
 */


public class VolumeControl {

    private EasyVideoPlayer mPlayer;
    private Activity mContext;
    private AudioManager mAudioManager;
    private MediaRecorder recorder = null;
    private VolumeControlTask volCtrlTask = null;
    private SharedPreferences SP;

    private boolean volCtrlEn = false;
    private int volCtrlSR = 10;
    private int volCtrlNbSamples = 100;

    private static final int NO_RESPONSE = 0;
    private static final int ACCEPTED = 1;
    private static final int REFUSED = 2;
    private int permissionToRecord = NO_RESPONSE;


    public VolumeControl(Activity context, EasyVideoPlayer player, AudioManager audioManager, SharedPreferences _SP) {
        mContext = context;
        mPlayer = player;
        mAudioManager = audioManager;
        SP = _SP;

        try {
            startRecorder();// Always active !
            // If we stop and start again it will crash the app ...
        } catch (IOException e) {
            Log.i("FCCVolumeControl", "recorder.prepare() FAIL !");
            e.printStackTrace();
        }
    }

    private boolean isWiredHeadsetOn (){
        return mAudioManager.isWiredHeadsetOn();
    }

    public void setPermissionToRecord (boolean accepted){
        if (accepted)
            permissionToRecord = ACCEPTED;
        else
            permissionToRecord = REFUSED;
    }

    private void startRecorder () throws IOException {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile("/dev/null");
        recorder.prepare(); // throws IOException
        recorder.start();
    }


    void updateState () {

        if (permissionToRecord==NO_RESPONSE)
            return; // do nothing

        // Get volCtrlEn shared preference
        volCtrlEn = SP.getBoolean("pref_volctrl_switch", volCtrlEn);

        if (!volCtrlEn) {
            if (volCtrlTask!=null) {
                Log.i("FCCVolumeControl", "Stop task");
                volCtrlTask.cancel(true);
                volCtrlTask = null;
            }
            return; // exit
        }

        if (!isWiredHeadsetOn()) {
            editVolCtrlEn(false);// If headphones unplugged disable volume control
            // Inform user that volume control is disabled
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(R.string.hp_unplugged + R.string.volctrl_not_started)
                    .setCancelable(false)
                    .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

            return; // exit
        }

        if (permissionToRecord==REFUSED) {
            editVolCtrlEn(false);// disable vol control if permission to record not accepted
            // Inform user that volume control is disabled
            // TODO : maybe ask permission again ?
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(R.string.volctrl_rec_perm)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

            return; // exit
        }

        // Get others shared preferences
        int newVolCtrlSR = Integer.parseInt(SP.getString("pref_volctrl_sample_time", String.valueOf(volCtrlSR)));
        int newVolCtrlNbSamples = Integer.parseInt(SP.getString("pref_volctrl_nb_samples", String.valueOf(volCtrlNbSamples)));

        if (volCtrlTask==null) {
            Log.i("FCCVolumeControl" , "Create task");
            volCtrlTask = new VolumeControlTask(mContext, this, mPlayer,recorder, mAudioManager);
            volCtrlTask.execute(newVolCtrlSR, newVolCtrlNbSamples); // start volume control
        } else if (newVolCtrlSR!=volCtrlSR || newVolCtrlNbSamples!=volCtrlNbSamples){
            Log.i("FCCVolumeControl" , "Restart task");
            // restart volume control if preferences changed
            volCtrlTask.cancel(true);
            volCtrlTask = new VolumeControlTask(mContext, this,mPlayer,recorder, mAudioManager);
            volCtrlTask.execute(newVolCtrlSR, newVolCtrlNbSamples);
            Log.i("FCCVolumeControl" , "param updated : "+newVolCtrlSR+" "+newVolCtrlNbSamples);
        }

        // update values
        volCtrlSR=newVolCtrlSR;
        volCtrlNbSamples=newVolCtrlNbSamples;
    }

    public void editVolCtrlEn(boolean newVal) {
        SharedPreferences.Editor editor = SP.edit();
        editor.putBoolean("pref_volctrl_switch",newVal);
        editor.commit();
        volCtrlEn = newVal;
    }

}