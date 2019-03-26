package project.ece496.emotionrecogspeechgui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.database.Cursor;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import java.io.IOException;
import java.lang.String;

import java.io.File;
import java.util.UUID;


import retrofit.model.ResultObject;
import retrofit.service.AudioInterface;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.Call;
import retrofit2.converter.gson.GsonConverterFactory;


public class Record extends Fragment {
    private static final String LOG_TAG = "AudioRecordTest";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // The request code must be 0 or greater.
    private static final int PLUS_ONE_REQUEST_CODE = 0;
    // The URL to +1.  Must be a valid URL.
    private final String PLUS_ONE_URL = "http://developer.android.com";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private AppCompatButton mRecordButton, mPlayButton, mUploadButton;
    private AppCompatTextView mRecordButtonText, mPlayButtonText, mUploadButtonText;
    private AudioRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private static String mFileName = null;
    boolean mStartPlaying = true, mStartRecording = true;
    private MainActivity main;
    private ProgressDialog dialog, analyzeDialog;
    private AlertDialog.Builder analyze_success_dialog;
    private WatsonSpeechTranscriber transcriber;
    private String transcribedText, emotionResult;
    public Communicator comm;

    private static final String TAG = MainActivity.class.getSimpleName();

    public Record() {
        // Required empty public constructor
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            public void onCompletion(MediaPlayer mp) {
                mPlayer.release();
                mPlayer = null;
                mPlayButtonText.setText("Play");
                mRecordButton.setEnabled(true);
                mUploadButton.setEnabled(true);
                mStartPlaying = !mStartPlaying;
            }
        });
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mFileName = getActivity().getExternalCacheDir().getAbsolutePath();
        mFileName += UUID.randomUUID().toString();
        mRecorder = new AudioRecorder(mFileName, main);
        mRecorder.startRecording();
    }

    private void stopRecording() {
        mRecorder.stopRecording();
        mRecorder = null;

        transcribedText = "";
        transcriber = new WatsonSpeechTranscriber();
        new Thread(new Runnable() {
            @Override
            public void run() {
                transcribedText = transcriber.transcribe(new File(mFileName));

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        comm.updateTranscription(transcribedText);
                        if (transcribedText == "") {
                            analyze_success_dialog.setTitle("Transcription Error");
                            analyze_success_dialog.setMessage("Please analyze or record again.");
                            analyze_success_dialog.show();
                        } else {
                            analyze_success_dialog.setTitle("Transcription Success");
                            analyze_success_dialog.setMessage("Please see result from transcription.");
                            analyze_success_dialog.show();
                        }

                    }
                });
            }
        }).start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = (MainActivity)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRecordButton = (AppCompatButton) getActivity().findViewById(R.id.record_button);
        mPlayButton = (AppCompatButton) getActivity().findViewById(R.id.play_button);
        mUploadButton = (AppCompatButton) getActivity().findViewById(R.id.upload_button);
        mRecordButtonText = (AppCompatTextView) getActivity().findViewById(R.id.record_button_text);
        mPlayButtonText = (AppCompatTextView) getActivity().findViewById(R.id.play_button_text);
        mUploadButtonText = (AppCompatTextView) getActivity().findViewById(R.id.upload_button_text);
        dialog = new ProgressDialog(getActivity()); // this = YourActivity
        analyzeDialog = new ProgressDialog(getActivity());
        analyze_success_dialog = new AlertDialog.Builder(getActivity());
        comm = (Communicator)getActivity();

        mRecordButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //call recording api
                onRecord(mStartRecording);
                if (mStartRecording) {
                    mRecordButtonText.setText("Stop");
                    mPlayButton.setEnabled(false);
                    mUploadButton.setEnabled(false);
                } else {
                    mRecordButtonText.setText("Record");
                    //Log.e(LOG_TAG, "calling comm in record");
                    mPlayButton.setEnabled(true);
                    mUploadButton.setEnabled(true);
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setTitle("Transcribing");
                    dialog.setMessage("Please wait...");
                    dialog.setIndeterminate(true);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                }
                mStartRecording = !mStartRecording;
                Log.e(LOG_TAG, "calling comm in record");
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //call playing api
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    mPlayButtonText.setText("Stop");
                    mRecordButton.setEnabled(false);
                    mUploadButton.setEnabled(false);

                } else {
                    mPlayButtonText.setText("Play");
                    mRecordButton.setEnabled(true);
                    mUploadButton.setEnabled(true);

                }
                mStartPlaying = !mStartPlaying;

            }
        });

        mUploadButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (mFileName != null) {
                    analyzeDialog.setTitle("Analyzing");
                    analyzeDialog.setMessage("Please wait...");
                    analyzeDialog.setIndeterminate(true);
                    analyzeDialog.setCanceledOnTouchOutside(false);
                    analyzeDialog.show();
                    emotionResult = null;
                    uploadAudioToServer();

                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            if (emotionResult == null) {
                                analyzeDialog.dismiss();
                                analyze_success_dialog.setTitle("Timed out");
                                analyze_success_dialog.setMessage("Please try analyze again!");
                                analyze_success_dialog.show();
                                emotionResult = "";
                                comm.updateResult(emotionResult);
                            }
                        }
                    }, 15000);
                } else {
                    analyze_success_dialog.setTitle("Analyze Failed");
                    analyze_success_dialog.setMessage("Please record first!");
                    analyze_success_dialog.show();
                }

            }
        });

    }


    //https://inducesmile.com/android/android-record-and-upload-video-to-server-using-retrofit-2/
    private void uploadAudioToServer() {

        File audioFile = new File(mFileName);

        RequestBody audioBody = RequestBody.create(MediaType.parse("audio/*"), audioFile);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
        MultipartBody.Part aFile = MultipartBody.Part.createFormData("file", audioFile.getName(), audioBody);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://100.65.81.177:5001")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        Log.e("upload audio to server", "before enqueue");

        AudioInterface aInterface = retrofit.create(AudioInterface.class);
        Call<ResultObject>  serverCom = aInterface.uploadAudioToServer(aFile);
        Log.e("upload audio to server", "upload success");

        serverCom.enqueue(new Callback<ResultObject>() {
            @Override
            public void onResponse(Call<ResultObject> call, Response<ResultObject> response) {
                Log.d(TAG, "getting a response...");
                //String result = response.body().toString();
                emotionResult = response.body().toString();
                Log.e(TAG, "Result " + emotionResult);
                comm.updateResult(emotionResult);
                analyzeDialog.dismiss();
                //add a pop up window
                analyze_success_dialog.setTitle("Analyze Success");
                analyze_success_dialog.setMessage("Please see result from analysis!");
                analyze_success_dialog.show();
            }
            @Override
            public void onFailure(Call<ResultObject> call, Throwable t) {
                Log.d(TAG, "Error message " + t.getMessage());
            }
        });

    }

    private String getRealPathFromURIPath(Uri contentURI, Activity activity) {
        Cursor cursor = activity.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

}

