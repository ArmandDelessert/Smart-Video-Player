package hesso.smartvideoplayer;//package hesso.smartvideoplayer;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by flavio on 08.05.17.
 */


public class VolumeControl extends AsyncTask<Integer, Float, Void> {

    private MediaRecorder mRecorder = null;
    private EasyVideoPlayer mPlayer;
    private Context mContext;

    private float maxVol=1.0f;
    private float minVol=0.1f;

    public VolumeControl(Context context,EasyVideoPlayer player, MediaRecorder recorder) {
        mContext = context;
        mPlayer = player;
        mRecorder = recorder;
    }

    @Override
    protected Void doInBackground(Integer... params) {
        Log.i("FCCVolCtrl","Background task started");

        float firstMed=0, medVal=0;
        int nbSamples=params[1];
        int samplesTime=params[0];

        Log.i("FCCVolCtrl","nbSamples="+nbSamples+" ; samplesTime="+samplesTime);

        // Get first median value
        try {
            firstMed = getMed(samplesTime,nbSamples); // blocking function
        } catch (InterruptedException e) {
            return null;
        }

        Log.i("FCCVolCtrl","firstMed="+firstMed);

        while (!isCancelled()) {
            try {
                medVal = getMed(samplesTime, nbSamples); // blocking function
            } catch (InterruptedException e) {
                return null;
            }
            if (medVal!=0)
                publishProgress(firstMed, medVal);
        }
        return null;
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
        //mRecorder.stop();
        Toast.makeText(mContext, "Volume control stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute (Void result){
        Log.i("FCCVolCtrl","onPostExecute() ERROR !!");
        mPlayer.setVolume(1.0F, 1.0F);
        //mRecorder.stop();
        Toast.makeText(mContext, "Volume control stopped due an unknown error", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i("FCCVolCtrl","Volume control started");
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