package hesso.smartvideoplayer;

/**
 * Created by flavio on 07.04.17.
 */

import android.media.MediaRecorder;
import android.os.Handler;


import java.io.IOException;
import java.util.Arrays;


public class SoundMeter {

    private MediaRecorder mRecorder = null;
    private boolean isRunning = false;
    private boolean initDone = false;
    private int currMeas;
    private float amp[];
    private int medSamples;
    private int sampleDelay=10;
    private int thr=10;
    private float firstMed;


    public SoundMeter (int medSamples){
        this.medSamples=medSamples;
        amp = new float[medSamples];
    }

    Handler handler = new Handler();
    final Runnable r = new Runnable() {
        public void run() {
            amp[currMeas++] = (float) getAmplitude();
            if (currMeas>=medSamples) {
                currMeas = 0;
                if (!initDone) {
                    initDone = true;
                    firstMed = getMed();// update first median value
                }
            }
            if (isRunning) {
                handler.postDelayed(this, sampleDelay);
            }
            else {
                currMeas = 0;
                initDone=false;
            }
        }
    };

    public void start() throws IOException {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            mRecorder.prepare();
            mRecorder.start();
        }
        if (!isRunning) {
            isRunning = true;
            handler.postDelayed(r, sampleDelay);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void stop() {
        if (mRecorder != null) {
            mRecorder.stop();
        }
        isRunning = false;
    }

    public void release() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
        isRunning = false;
    }

    public void setSampleDelay(int sampleDelay) {
        this.sampleDelay=sampleDelay;
    }
    public int getSampleDelay() {
        return this.sampleDelay;
    }
    public void setThreshold(int thr) {
        this.thr=thr;
    }
    public int getThreshold() {
        return this.thr;
    }
    public float getFirstMed() {
        return this.firstMed;
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return  mRecorder.getMaxAmplitude();
        else
            return 0;
    }

    public float getMed() {
        float[] sAmp = amp.clone();
        if ((mRecorder != null) && initDone)
        {
            Arrays.sort(sAmp);
            return sAmp[medSamples/2];
        }
        else
            return 0;
    }
}
