package hesso.smartvideoplayer;//package hesso.smartvideoplayer;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import java.util.Arrays;

/**
 * Created by flavio on 08.05.17.
 */


public class VolumeControl extends AsyncTask<Integer, Float, Integer> {

    private MediaRecorder mRecorder = null;
    private EasyVideoPlayer mPlayer;
    private Activity mContext;

    private float maxVol=1.0f;
    private float minVol=0.1f;

    public static final int HEADPHONES_UNPLUGGED = 0;
    public static final int CRASH = 1;
    public static final int CANCELLED = 2;

    public VolumeControl(Activity context, EasyVideoPlayer player, MediaRecorder recorder) {
        mContext = context;
        mPlayer = player;
        mRecorder = recorder;
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        Log.i("FCCVolCtrl","Background task started");

        AudioManager am = (AudioManager)mContext.getSystemService(mContext.AUDIO_SERVICE);
        if (!am.isWiredHeadsetOn())
            return HEADPHONES_UNPLUGGED;

        float firstMed, medVal;
        int nbSamples=params[1];
        int samplesTime=params[0];

        Log.i("FCCVolCtrl","nbSamples="+nbSamples+" ; samplesTime="+samplesTime);

        // Get first median value
        try {
            firstMed = getMed(samplesTime,nbSamples); // blocking function
        } catch (InterruptedException e) {
            return CRASH;
        }

        Log.i("FCCVolCtrl","firstMed="+firstMed);

        while (!isCancelled() && am.isWiredHeadsetOn()) {
            try {
                medVal = getMed(samplesTime, nbSamples); // blocking function
            } catch (InterruptedException e) {
                return CRASH;
            }
            if (medVal!=0)
                publishProgress(firstMed, medVal);
        }
        if (!am.isWiredHeadsetOn())
            return HEADPHONES_UNPLUGGED;
        else
            return CANCELLED;
    }

    // get median value - blocking function
    public float getMed(int sampleDelay, int nbSamples) throws InterruptedException {
        float amp[] = new float[nbSamples];
        for (int currMeas=0;currMeas<nbSamples;currMeas++) {

            // get microphone maximum value from last call
            amp[currMeas] = (float) getAmplitude();

            // some times "getAmplitude" give 0 error values
            // so we wait to have a valid value
            while (amp[currMeas] == 0){
                Thread.sleep(1);
                amp[currMeas] = (float) getAmplitude();
            }

            Thread.sleep(sampleDelay);
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
        Log.i("FCCVolCtrl","Volume control stopped");
        mPlayer.setVolume(1.0F, 1.0F);
        Toast.makeText(mContext, "Volume control stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute (Integer result){
        mPlayer.setVolume(1.0F, 1.0F);
        if (result==HEADPHONES_UNPLUGGED)
        {
            ((MainActivity) mContext).editVolCtrlEn(false);
            Toast.makeText(mContext, "Headphones unplugged : volume control stopped", Toast.LENGTH_SHORT).show();

        }
        else
            Toast.makeText(mContext, "Volume control stopped", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i("FCCVolCtrl","Volume control started");
        AudioManager am = (AudioManager)mContext.getSystemService(mContext.AUDIO_SERVICE);
        if (am.isWiredHeadsetOn())
            Toast.makeText(mContext, "Volume control started", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onProgressUpdate(Float... values) {
        super.onProgressUpdate(values);
        float firstMed = values[0];
        float medVal = values[1];
        float newVol = 0.5f+0.5f*(float)Math.log10(medVal/firstMed);
        if (newVol > maxVol)
            newVol = maxVol;
        if (newVol < minVol)
            newVol = minVol;
        mPlayer.setVolume(newVol, newVol);
        Log.i("FCCVolCtrl","Updated"+
                " vol="+String.valueOf(newVol) +
                " (med="+String.valueOf(medVal)+")"+
                "");

    }

}