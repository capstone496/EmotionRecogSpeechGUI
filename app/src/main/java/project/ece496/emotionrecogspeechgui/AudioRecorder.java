package project.ece496.emotionrecogspeechgui;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import java.io.File;

import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.Recorder;
import project.ece496.emotionrecogspeechgui.MainActivity;
import project.ece496.emotionrecogspeechgui.RecorderActivity;

/**
 * A wrapper class for OmRecorder
 */

public class AudioRecorder {
    private Recorder recorder;

    public AudioRecorder(String filePath, RecorderActivity activity){
        recorder = OmRecorder.wav(
                new PullTransport.Default(mic(), activity),
                new File(filePath));
    }

    @NonNull
    private omrecorder.AudioSource mic(){
        int RECORDING_FREQ = 44100;
        return new omrecorder.AudioSource.Smart(
                MediaRecorder.AudioSource.MIC,
                AudioFormat.ENCODING_PCM_16BIT,
                AudioFormat.CHANNEL_IN_MONO,
                RECORDING_FREQ);
    }

    public void startRecording(){
        recorder.startRecording();
    }

    public void stopRecording(){
        recorder.stopRecording();
    }
}
