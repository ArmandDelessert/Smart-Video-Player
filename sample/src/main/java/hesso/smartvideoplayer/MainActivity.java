package hesso.smartvideoplayer;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.afollestad.easyvideoplayersample.R;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.IOException;

/*

TODO :

VolCtrl que activer si ecoueturs branchées (sinon c'est faussé par les hp du portable)
faire tache background ? ou juste handler...
 */


public class MainActivity extends AppCompatActivity implements EasyVideoCallback {

    private EasyVideoPlayer player;
    private SoundMeter soundMeter = null;
    private boolean volCtrlEn = false;
    private int volCtrlSR = 10;
    private float maxVol=1.0f;
    private float minVol=0.1f;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (permissionToRecordAccepted ) {
            soundMeter = new SoundMeter(1000); // MagicNumber : number of median samples
            if (volCtrlEn && !soundMeter.isRunning()) {
                //Toast.makeText(this, "Starting volume control" , Toast.LENGTH_SHORT).show();
                try {
                    soundMeter.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);


        player = (EasyVideoPlayer) findViewById(R.id.player);
        assert player != null;
        player.setCallback(this);
        // All further configuration is done from the XML layout.

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }


    @Override
    protected void onPause() {
        super.onPause();
//        player.pause();
    }

    @Override
    public void onStarted(EasyVideoPlayer player) {
    }

    @Override
    public void onPaused(EasyVideoPlayer player) {
    }

    @Override
    public void onPreparing(EasyVideoPlayer player) {
        Log.d("EVP-Sample", "onPreparing()");
    }

    @Override
    public void onPrepared(EasyVideoPlayer player) {
        Log.d("EVP-Sample", "onPrepared()");
    }

    @Override
    public void onBuffering(int percent) {
        Log.d("EVP-Sample", "onBuffering(): " + percent + "%");
    }

    @Override
    public void onError(EasyVideoPlayer player, Exception e) {
        Log.d("EVP-Sample", "onError(): " + e.getMessage());
        new MaterialDialog.Builder(this)
                .title(R.string.error)
                .content(e.getMessage())
                .positiveText(android.R.string.ok)
                .show();
    }

    @Override
    public void onCompletion(EasyVideoPlayer player) {
        Log.d("EVP-Sample", "onCompletion()");
        Toast.makeText(this, "onCompletion", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRetry(EasyVideoPlayer player, Uri source) {
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
            Toast.makeText(this, "vol=" + String.valueOf(newVol) + " ; " +
                    "med=" + String.valueOf(medVal) + " ; " +
                    "fmed=" + String.valueOf(soundMeter.getFirstMed()), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSubmit(EasyVideoPlayer player, Uri source) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClickVideoFrame(EasyVideoPlayer player) {
        //Toast.makeText(this, "Click video frame.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume(){
        super.onResume();
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        volCtrlEn = SP.getBoolean("pref_volctrl_switch", volCtrlEn);
        volCtrlSR = Integer.parseInt(SP.getString("pref_volctrl_sample_time", String.valueOf(volCtrlSR)));
        if (soundMeter!=null && volCtrlEn && !soundMeter.isRunning()) {
            try {
                soundMeter.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(soundMeter!=null && !volCtrlEn)
            soundMeter.stop();
        //Toast.makeText(this, ""+volCtrlSR , Toast.LENGTH_SHORT).show();
    }

}
