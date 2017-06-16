package hesso.smartvideoplayer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.afollestad.easyvideoplayersample.R;
import com.afollestad.materialdialogs.MaterialDialog;

public class MainActivity extends AppCompatActivity implements EasyVideoCallback {

    private EasyVideoPlayer player;

    // Features
    private VolumeControl volCtrl = null;
    private BlueFilter blueFilter = null;

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

        Log.i("FCCMainActivity" , "permissionToRecordAccepted : " + permissionToRecordAccepted);
        volCtrl.setPermissionToRecord(permissionToRecordAccepted);
        volCtrl.updateState();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("FCCMainActivity", "#######################\nStarting app");

        // Shared preferences
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        setContentView(R.layout.main_activity_layout);

        // Permissions
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        // Player
        player = (EasyVideoPlayer) findViewById(R.id.player);
        assert player != null;
        player.setCallback(this);
        // All further configuration is done from the XML layout.

        // Features
        volCtrl = new VolumeControl(MainActivity.this, player, (AudioManager)getSystemService(Context.AUDIO_SERVICE), SP);
        blueFilter = new BlueFilter(findViewById(R.id.filter_view), SP);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);
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
        Log.i("FCCMainActivity" , "onResume() start");

        volCtrl.updateState();
        blueFilter.updateState();

        Log.i("FCCMainActivity" , "onResume() end");
    }



}
