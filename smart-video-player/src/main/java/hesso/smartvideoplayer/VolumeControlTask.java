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


public class VolumeControlTask extends AsyncTask<Integer, Float, Integer> {

    private EasyVideoPlayer mPlayer;
    private Activity mContext;
    private VolumeControl mParent;
    private MediaRecorder mRecorder = null;
    private AudioManager mAudioManager;

    private float maxVol=1.0f;
    private float minVol=0.1f;

    public static final int CANCELLED = 0;
    public static final int HEADPHONES_UNPLUGGED = 1;
    public static final int CRASH = 2;

    public VolumeControlTask(Activity context, VolumeControl parent, EasyVideoPlayer player,MediaRecorder recorder, AudioManager audioManager) {
        mContext = context;
        mPlayer = player;
        mAudioManager = audioManager;
        mParent = parent;
        mRecorder = recorder;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        Log.i("FCCVolumeControlTask","Background task started");

        if (!mAudioManager.isWiredHeadsetOn())
            return HEADPHONES_UNPLUGGED;

        float firstMed, medVal;
        int nbSamples=params[1];
        int samplesTime=params[0];

        Log.i("FCCVolumeControlTask","nbSamples="+nbSamples+" ; samplesTime="+samplesTime);

        // Get first median value
        try {
            firstMed = getMed(samplesTime,nbSamples); // blocking function
        } catch (InterruptedException e) {
            return CRASH;
        }

        Log.i("FCCVolumeControlTask","firstMed="+firstMed);

        while (!isCancelled() && mAudioManager.isWiredHeadsetOn()) {
            try {
                medVal = getMed(samplesTime, nbSamples); // blocking function
            } catch (InterruptedException e) {
                return CRASH;
            }
            if (medVal!=0)
                publishProgress(firstMed, medVal);

        }
        if (!mAudioManager.isWiredHeadsetOn())
            return HEADPHONES_UNPLUGGED;
        else
            return CANCELLED;
    }

    // get median value - blocking function
    public float getMed(int sampleDelay, int nbSamples) throws InterruptedException {
        int delay;
        float amp[] = new float[nbSamples];
        for (int currMeas=0;currMeas<nbSamples;currMeas++) {

            // get microphone maximum value from last call
            amp[currMeas] = (float) getAmplitude();

            // some times "getAmplitude" give 0 error values
            // so we wait to have a valid value
            delay = sampleDelay;
            while (amp[currMeas] == 0){
                Thread.sleep(1);
                amp[currMeas] = (float) getAmplitude();
                if (delay>0)
                    delay--;
                else if (delay==0) {
                    Log.e("FCCVolumeControlTask", "sampleDelay ("+sampleDelay+") too small");
                    delay--;
                }
            }

            if (delay>0)
                Thread.sleep(delay);
        }
        Arrays.sort(amp);
        return amp[amp.length/2];
    }


    // get microphone maximum value from last call
    private double getAmplitude() throws InterruptedException {
        if (mRecorder != null)
            return  mRecorder.getMaxAmplitude();
        else
            throw new InterruptedException();
    }


    @Override
    protected void onCancelled() {
        Log.i("FCCVolumeControlTask","onCancelled");
        mPlayer.setVolume(1.0F, 1.0F);
        Toast.makeText(mContext, R.string.vol_ctrl_stop, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute (Integer result){
        Log.i("FCCVolumeControlTask","onPostExecute");

        mPlayer.setVolume(1.0F, 1.0F);
        if (result==HEADPHONES_UNPLUGGED)
        {
            mParent.editVolCtrlEn(false);
            Toast.makeText(mContext, R.string.hp_unplugged + R.string.vol_ctrl_stop, Toast.LENGTH_SHORT).show();

        }
        else
            Toast.makeText(mContext, R.string.vol_ctrl_stop, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i("FCCVolumeControlTask","onPreExecute");
        AudioManager am = (AudioManager)mContext.getSystemService(mContext.AUDIO_SERVICE);
        if (am.isWiredHeadsetOn())
            Toast.makeText(mContext, R.string.vol_ctrl_start, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onProgressUpdate(Float... values) {
        super.onProgressUpdate(values);
        float firstMed = values[0] ;
        float medVal = values[1];
        float newVol = 0.5f+0.5f*(float)Math.log10(medVal/firstMed);
        if (newVol > maxVol)
            newVol = maxVol;
        if (newVol < minVol)
            newVol = minVol;
        mPlayer.setVolume(newVol, newVol);
        Log.i("FCCVolumeControlTask","Updated"+
                " vol="+String.valueOf(newVol) +
                " (med="+String.valueOf(medVal)+")"+
                "");

        // TODO : remove this after demo
        Toast.makeText(mContext, "DemoMsg : Updated " + " vol="+String.valueOf(newVol) , Toast.LENGTH_SHORT).show();
    }

}