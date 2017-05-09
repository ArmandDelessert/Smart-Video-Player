package hesso.smartvideoplayer;//package hesso.smartvideoplayer;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.AsyncTask;
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
    private ProgressDialog dialog;

    // TODO :
    private float maxVol=1.0f;
    private float minVol=0.1f;
    private int volctrlDelay = 5000;
    private int sensitivity = 12345;

    public VolumeControl(Context context,EasyVideoPlayer player) {
        mContext = context;
        dialog = new ProgressDialog(mContext);
        mPlayer = player;
    }

    private void startRecorder() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mRecorder.start();
        }
    }

    @Override
    protected Void doInBackground(Integer... params) {

        //float firstMed;
        //int nbSamples=params[1];
        //int samplesTime=params[0];

        //startRecorder(); // TODO reprendre ici ...

        // First run
        //firstMed = getMed(samplesTime,nbSamples); // blocking function

        while (true) {
            //for (int i = 0; i < 2 ; i++) {
            //    publishProgress(getMed(samplesTime,nbSamples));
            try {
                Thread.sleep(5000L);
                publishProgress(0.0F);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // get median value - blocking function
    public float getMed(int sampleDelay, int nbSamples) {
        float amp[] = new float[nbSamples];
        for (int currMeas=0;currMeas<nbSamples;currMeas++)
        {
            amp[currMeas++] = (float) getAmplitude();
            try {
                Thread.sleep(sampleDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Arrays.sort(amp);
        return amp[amp.length/2];
    }


    // get microphone maximum value from last call
    private double getAmplitude() {
        if (mRecorder != null)
            return  mRecorder.getMaxAmplitude();
        else
            return 0;
    }



    @Override
    protected void onCancelled() {
        Toast.makeText(mContext, "Volume control stopped", Toast.LENGTH_SHORT).show();
    }




    /*for (Long i = 0L; i < 3L; i++) {
        Thread.sleep(5000);
        publishProgress((Long) i);
    }

    if (soundMeter!=null && soundMeter.isRunning()) {
        float medVal = soundMeter.getMed();
        float newVol = 0.5f+0.5f*(float)Math.log10(medVal/soundMeter.getFirstMed());
        if (medVal!=0 && soundMeter.getFirstMed()!=0) {
            if (newVol > maxVol)
                newVol = maxVol;
            if (newVol < minVol)
                newVol = minVol;
            player.setVolume(newVol, newVol);
        }
        Toast.makeText(getApplicationContext(), "vol=" + String.valueOf(newVol) + " ; " +
                "med=" + String.valueOf(medVal) + " ; " +
                "fmed=" + String.valueOf(soundMeter.getFirstMed()), Toast.LENGTH_LONG).show();
    }
    if (volCtrlEn)
        handler.postDelayed(this, volctrlDelay);

    */


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(mContext, "Volume control started", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onProgressUpdate(Float... values) {
        super.onProgressUpdate(values);
        Toast.makeText(mContext, "volume : "+values[0], Toast.LENGTH_SHORT).show();
    }

}