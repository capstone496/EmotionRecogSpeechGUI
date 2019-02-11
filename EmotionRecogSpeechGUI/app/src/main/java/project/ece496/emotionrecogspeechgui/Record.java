package project.ece496.emotionrecogspeechgui;


import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.String;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;


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
    private AppCompatTextView mResultView;
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
    //private OnFragmentInteractionListener mListener;

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
                mPlayButton.setText("Play");
                mRecordButton.setEnabled(true);
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
        //new TranscriptionTask().execute(new File(mFileName));
        new Thread(new Runnable() {
            @Override
            public void run() {
                transcribedText =  transcriber.transcribe(new File(mFileName));
            }
        }).start();

        Log.d("Stop recording", "trying to transcribe");
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

        //System.out.println(emotionResult);

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

/*
    private void analyzeRecording() {
        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                EmotionResult emotionResult = dataSnapshot.getValue(EmotionResult.class);
                String emoStr = "";
                int max = 0;
                for(int i = 1; i < emotionResult.emotion.length; i ++) {
                    if (emotionResult.emotion[max] < emotionResult.emotion[i]) {
                        max = i;
                    }
                }
                switch (max) {
                    case 0:
                        emoStr = "happy";
                        break;
                    case 1:
                        emoStr = "sad";
                        break;
                    case 2:
                        emoStr = "angry";
                        break;
                    case 3:
                        emoStr = "fearful";
                        break;
                    case 4:
                        emoStr = "disgust";
                        break;
                    case 5:
                        emoStr = "surprise";
                        break;
                    default:
                        break;
                }
                mResultView.setText("Result:\n"+emoStr);
                //System.out.println(emotionResult);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }*/
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
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        mRecordButton = (AppCompatButton) view.findViewById(R.id.record_button);
        mPlayButton = (AppCompatButton) view.findViewById(R.id.play_button);
        mUploadButton = (AppCompatButton) view.findViewById(R.id.upload_button);
        mAnalyzeButton = (AppCompatButton) view.findViewById(R.id.analyze_button);
        mResultView = (AppCompatTextView) view.findViewById(R.id.display);


        mRecordButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //call recording api
                onRecord(mStartRecording);
                if (mStartRecording) {
                    mRecordButton.setText("Stop");
                    mPlayButton.setEnabled(false);

                } else {
                    mRecordButton.setText("Record");
                    mPlayButton.setEnabled(true);
                    while (transcribedText == null) {
                    }
                    mResultView.setText(transcribedText);
                }
                mUploadButton.setText("UPLOAD");
                mStartRecording = !mStartRecording;
            }
        });

        mPlayButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //call playing api
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    mPlayButton.setText("Stop");
                    mRecordButton.setEnabled(false);
                } else {
                    mPlayButton.setText("Play");
                    mRecordButton.setEnabled(true);
                }
                mStartPlaying = !mStartPlaying;

            }
        });

        mUploadButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //call playing api
                uploadRecording();
                while(mUploadSuccess != true){
                }
                mUploadButton.setText("Upload Success");
            }
        });



        mAnalyzeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //call playing api
                analyzeRecording();
                while (emotionResult == null) {

                }
                mResultView.append("\nResult:" + emotionResult);
            }
        });


        return view;
    }

}
