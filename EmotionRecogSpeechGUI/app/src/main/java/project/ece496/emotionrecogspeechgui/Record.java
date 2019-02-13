package project.ece496.emotionrecogspeechgui;


import android.app.Activity;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.String;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;


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
    private AppCompatButton mRecordButton, mPlayButton, mUploadButton, mAnalyzeButton;
    private AppCompatTextView mRecordButtonText, mPlayButtonText, mUploadButtonText, mResultView;
    private AudioRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private static String mFileName = null;
    boolean mStartPlaying = true, mStartRecording = true, mUploadSuccess = false;
    private MainActivity main;
    private File file;
    private FirebaseStorage storage;
    private StorageReference riversRef, storageRef;
    private UploadTask uploadTask;
    // Get a reference to our posts
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private SpeechRecognizer speech;
    private WatsonSpeechTranscriber transcriber;
    private Socket client;
    private DataInputStream dataInputStream = null;
    private DataOutputStream dataOutputStream = null;
    private String transcribedText;
    private String emotionResult;
    public Communicator comm;

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

        transcribedText = null;
        transcriber = new WatsonSpeechTranscriber();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //new TranscriptionTask().execute(new File(mFileName));
                transcribedText = transcriber.transcribe(new File(mFileName));
            }
        }).start();

    }

    private void analyzeRecording() {
        emotionResult= null;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client = new Socket("100.65.194.121", 7012); //connect to server
                    dataInputStream = new DataInputStream(client.getInputStream());
                    byte[] received = new byte[1024];
                    while (dataInputStream.read(received) == -1) {

                    }
                    emotionResult = new String(received);
                    for (int i = 0; i < 1024; i ++) {
                        if (emotionResult.charAt(i) == '.') {
                            emotionResult = emotionResult.substring(0, i);
                            break;
                        }
                    }
                    System.out.println(emotionResult);
                } catch (UnknownHostException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void uploadRecording(){
        file = new File(mFileName);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    client = new Socket("100.65.194.121", 7012); //connect to server
                    dataOutputStream = new DataOutputStream(client.getOutputStream());
                    dataInputStream = new DataInputStream(client.getInputStream());
                    String s;
                    while (transcribedText == null) {
                    }
                    byte[] received = new byte[2];
                    dataOutputStream.writeBytes("2");
                    dataOutputStream.flush();

                    while (dataInputStream.read(received) == -1){
                    }
                    s = new String(received);
                    System.out.println(s);

                    dataOutputStream.writeUTF(transcribedText+ "\n");
                    dataOutputStream.flush();

                    while (dataInputStream.read(received) == -1) {
                    }
                    s = new String(received);
                    System.out.println(s);

                    if(file.isFile()) {
                        dataOutputStream.writeBytes("1");
                        dataOutputStream.flush();

                        while (dataInputStream.read(received) == -1) {
                        }
                        s = new String(received);
                        System.out.println(s);

                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] buf = new byte[1024];
                        int readSuccess = fileInputStream.read(buf,0,1024);

                        while(readSuccess != -1) {
                            dataOutputStream.write(buf, 0, 1024);
                            dataOutputStream.flush();
                            readSuccess = fileInputStream.read(buf,0,1024);
                        }
                        fileInputStream.close();
                    }
                    mUploadSuccess = true;
                    dataOutputStream.close();
                    dataInputStream.close();
                    client.close();
                } catch (UnknownHostException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    class TranscriptionTask extends AsyncTask<File, Void, String> {
        @Override
        protected String doInBackground(File... files) {
            transcriber = new WatsonSpeechTranscriber();
            return transcriber.transcribe(files[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            // TODO: spaghetti code here...
            mResultView.setText(s);
        }

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = (MainActivity)getActivity();
        storage = FirebaseStorage.getInstance();
        speech = SpeechRecognizer.createSpeechRecognizer(getActivity());
        //database = FirebaseDatabase.getInstance();
        //ref = database.getReference("/results/emotionResult");
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
        mAnalyzeButton = (AppCompatButton) getActivity().findViewById(R.id.analyze_button);
        mRecordButtonText = (AppCompatTextView) getActivity().findViewById(R.id.record_button_text);
        mPlayButtonText = (AppCompatTextView) getActivity().findViewById(R.id.play_button_text);
        mUploadButtonText = (AppCompatTextView) getActivity().findViewById(R.id.upload_button_text);
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
                    while (transcribedText == null) {
                    }
/*
                    transcribedText = "Marginal cost is the additional (incremental) cost required to increase" +
                            "the quantity of  output by one unit. it is the derivative of the cost function with" +
                            "respect to the output quantity. Marginal and average cost values corresponding to " +
                            "a specified output quantity are generally different. If the marginal cost is smaller" +
                            "than the average cost, an increase in output would result in a reduction of unit cost.";
*/
                    comm.updateTransription(transcribedText);
                }
                mUploadButton.setText("UPLOAD");
                mStartRecording = !mStartRecording;
                //Log.e(LOG_TAG, "calling comm in record");
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
                //call playing api
                uploadRecording();
                while(!mUploadSuccess){
                }
                mUploadButtonText.setText("Upload Success");
            }
        });


        mAnalyzeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //call playing api
                analyzeRecording();
                while (emotionResult == null) {
                }
                comm.updateResult(emotionResult);
            }
        });
    }

}
