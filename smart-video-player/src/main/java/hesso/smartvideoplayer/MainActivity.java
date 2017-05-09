package hesso.smartvideoplayer;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.afollestad.easyvideoplayersample.R;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.IOException;


/*
TODO : VolCtrl que activer si ecoueturs branchées (sinon c'est faussé par les hp du portable)
 */


public class MainActivity extends AppCompatActivity implements EasyVideoCallback {


    private EasyVideoPlayer player;
    public boolean startingApp = true;


    // Volume control (default values at first start app) :
    private VolumeControl volCtrl = null;
    public boolean volCtrlEn = false;
    private int volCtrlSR = 10;
    private int volCtrlNbSamples = 100;



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
        if (permissionToRecordAccepted) {
            // if permission to record accepted and vol ctrl enable
            // create volume control instance and start execute
            if (volCtrlEn) {
                volCtrl = new VolumeControl(MainActivity.this,player);
                volCtrl.execute(volCtrlSR, volCtrlNbSamples);
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

        TextureView v = (TextureView) findViewById(R.id.textureView);
        v.setOpaque(false);


        Log.i("FCCdebug" , "Starting app");

        //v.setBackgroundColor(0xFF00FF00);
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
    }

    @Override
    public void onRetry(EasyVideoPlayer player, Uri source) {

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

        // Get shared preferences
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        // On resume volume control :
        volCtrlEn = SP.getBoolean("pref_volctrl_switch", volCtrlEn);
        volCtrlSR = Integer.parseInt(SP.getString("pref_volctrl_sample_time", String.valueOf(volCtrlSR)));
        volCtrlNbSamples = Integer.parseInt(SP.getString("pref_volctrl_nb_samples", String.valueOf(volCtrlNbSamples)));

        if (!permissionToRecordAccepted) {
            if (!startingApp)
                Toast.makeText(this, "Permission to record not accepted", Toast.LENGTH_LONG).show();
        }
        if (volCtrlEn && volCtrl==null) {
            volCtrl = new VolumeControl(MainActivity.this, player);
            volCtrl.execute(volCtrlSR, volCtrlNbSamples); // start volume control
        }
        else if (!volCtrlEn && volCtrl!=null) {
            volCtrl.cancel(true);
            volCtrl = null;
            // Flavio : Je ne sait pas pourquoi la toute promiere fois que ca passe par la
            // le message de "onCancelled" de volCtrl n'est pas afficher...
        }
        else
            Toast.makeText(this, "else", Toast.LENGTH_SHORT).show();

        startingApp=false;
    }

    /*
        Control volume timer

    Handler handler = new Handler();
    final Runnable r = new Runnable() {
        public void run() {
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
        }
    };*/
}
