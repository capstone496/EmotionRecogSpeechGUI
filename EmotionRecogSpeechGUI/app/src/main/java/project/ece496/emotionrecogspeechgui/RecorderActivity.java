package project.ece496.emotionrecogspeechgui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;


import omrecorder.AudioChunk;
import omrecorder.PullTransport;

import static java.lang.Thread.sleep;

/**
 * Activity template that conforms to the OmRecorder API.
 */

public class RecorderActivity extends AppCompatActivity
        implements PullTransport.OnAudioChunkPulledListener, MediaPlayer.OnCompletionListener{
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            default:
                break;

        }
        if (!permissionToRecordAccepted ) finish();

    }
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public void onAudioChunkPulled(AudioChunk audioChunk) {

    }
}
