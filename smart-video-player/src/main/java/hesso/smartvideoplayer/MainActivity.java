package hesso.smartvideoplayer;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.afollestad.easyvideoplayersample.R;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.IOException;

/*

TODO : pourquoi il faut redemarrer l'app pour que les valeurs dans les params prennent effet ? Truc bizare JAVA ?

TODO : Voir pourquoi quand on quitte l'app en appuiant sur "retour" ca crache...
 */

public class MainActivity extends AppCompatActivity implements EasyVideoCallback {

    private EasyVideoPlayer player;
    private MediaRecorder recorder = null;
    public int startingApp = 0;

    // Shared preferences
    SharedPreferences SP;

    // Volume control (default values at first start app) :
    private VolumeControl volCtrl = null;
    public boolean volCtrlEn = false;
    private int volCtrlSR = 10;
    private int volCtrlNbSamples = 100;

    // Blue filter
    private BlueFilter blueFilter;



    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (permissionToRecordAccepted) {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile("/dev/null");
            try {
                recorder.prepare();
            } catch (IOException e) {
                Log.i("FCCMainActivity", "recorder.prepare() FAIL !");
                e.printStackTrace();
            }
            recorder.start(); // Always active !
            // If we stop and start again it will crash the app ...

            // if permission to record accepted and vol ctrl enable
            // create volume control instance and start execute
            AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE); // test also if the headphones are connected
            if (volCtrlEn && am.isWiredHeadsetOn()) {
                volCtrl = new VolumeControl(MainActivity.this, player, recorder);
                volCtrl.execute(volCtrlSR, volCtrlNbSamples);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("FCCMainActivity", "#######################\nStarting app");

        // Shared preferences
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        setContentView(R.layout.main_activity_layout);

        player = (EasyVideoPlayer) findViewById(R.id.player);
        assert player != null;
        player.setCallback(this);
        // All further configuration is done from the XML layout.

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        blueFilter = new BlueFilter(findViewById(R.id.filter_view));
    }


    @Override
    protected void onPause() {
        super.onPause();
        //player.pause();
    }

    @Override
    public void onStarted(EasyVideoPlayer player) {
    }

    @Override
    public void onPaused(EasyVideoPlayer player) {
    }

    @Override
    public void onPreparing(EasyVideoPlayer player) {
        //Log.d("EVP-Sample", "onPreparing()");
    }

    @Override
    public void onPrepared(EasyVideoPlayer player) {
        //Log.d("EVP-Sample", "onPrepared()");
    }

    @Override
    public void onBuffering(int percent) {
        //Log.d("EVP-Sample", "onBuffering(): " + percent + "%");
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
        //Log.d("EVP-Sample", "onCompletion()");
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
        Log.i("FCCMainActivity" , "onResume() 0");

        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        // On resume volume control, get shared preferences
        volCtrlEn = SP.getBoolean("pref_volctrl_switch", volCtrlEn);
        if (!am.isWiredHeadsetOn() && volCtrlEn) {
            editVolCtrlEn(false);// If headphones unplugged disable volume control
            // Inform user that volume control is disabled
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Headphones unplugged : volume control not started")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
        int newVolCtrlSR = Integer.parseInt(SP.getString("pref_volctrl_sample_time", String.valueOf(volCtrlSR)));
        int newVolCtrlNbSamples = Integer.parseInt(SP.getString("pref_volctrl_nb_samples", String.valueOf(volCtrlNbSamples)));


        if (!permissionToRecordAccepted && volCtrlEn) {
            Log.i("FCCMainActivity" , "onResume() 1.1");

            // disable vol control if permission to record not accepted

            if (startingApp>1) {
                Log.i("FCCMainActivity" , "onResume() 1.2");
                // first 2 onResume() are at app startup, we want to skip this startup

                editVolCtrlEn(false);

                Log.i("FCCMainActivity" , "onResume() 1.3");

                // Inform user that volume control is disabled
                // TODO : maybe ask permition again ?
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Can't enable volume control !\nPlease accept record permission")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }

        } else if (volCtrlEn && volCtrl==null) {
            Log.i("FCCMainActivity" , "onResume() 2.1");
            volCtrl = new VolumeControl(this, player,recorder);
            Log.i("FCCMainActivity" , "onResume() 2.2");
            volCtrl.execute(newVolCtrlSR, newVolCtrlNbSamples); // start volume control
        } else if (!volCtrlEn && volCtrl!=null) {
            Log.i("FCCMainActivity" , "onResume() 3");
            volCtrl.cancel(true);
            volCtrl = null;
        } else if (volCtrlEn && (newVolCtrlSR!=volCtrlSR || newVolCtrlNbSamples!=volCtrlNbSamples)){
            Log.i("FCCMainActivity" , "onResume() 4");
            // restart volume control if preferences changed
            volCtrl.cancel(true);
            volCtrl = new VolumeControl(this,player,recorder);
            volCtrl.execute(newVolCtrlSR, newVolCtrlNbSamples);
            Log.i("FCCVolCtrl" , "param changed : "+newVolCtrlSR+" "+newVolCtrlNbSamples);
        }

        Log.i("FCCMainActivity" , "onResume() 5");

        // update values
        volCtrlSR=newVolCtrlSR;
        volCtrlNbSamples=newVolCtrlNbSamples;

        blueFilter.updateState(SP);

        if (startingApp<=1)
            startingApp++;
        Log.i("FCCMainActivity" , "onResume() end");
    }

    public void editVolCtrlEn(Boolean newVal) {
        SharedPreferences.Editor editor = SP.edit();
        editor.putBoolean("pref_volctrl_switch",newVal);
        editor.commit();
    }

}
