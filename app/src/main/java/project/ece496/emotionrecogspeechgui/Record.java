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
    boolean mStartPlaying = true, mStartRecording = true;
    private MainActivity main;
    private Uri file;
    private FirebaseStorage storage;
    private StorageReference riversRef, storageRef;
    private UploadTask uploadTask;
    // Get a reference to our posts
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private SpeechRecognizer speech;
    private WatsonSpeechTranscriber transcriber;
    //private OnFragmentInteractionListener mListener;

    public Record() {
        // Required empty public constructor
    }


/*
    public static Record newInstance(String param1, String param2) {
        Record fragment = new Record();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
*/
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

        /*
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");

        speech.startListening(intent);
        if(intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        }*/
        mFileName = getActivity().getExternalCacheDir().getAbsolutePath();
        mFileName += UUID.randomUUID().toString();

        mRecorder = new AudioRecorder(mFileName, main);
        mRecorder.startRecording();
    }

    private void stopRecording() {
        mRecorder.stopRecording();
        mRecorder = null;

        new TranscriptionTask().execute(new File(mFileName));
        Log.d("Stop recording", "trying to transcribe");
    }

    private void uploadRecording(){
        file = Uri.fromFile(new File(mFileName));
        storageRef = storage.getReference();
        riversRef  = storageRef.child("audio/"+file.getLastPathSegment());
        uploadTask = riversRef.putFile(file);

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                System.out.println("Upload is " + progress + "% done");
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("Upload is paused");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                System.out.println("Upload failed");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Handle successful uploads on complete
                mUploadButton.setText("Upload Success");
                System.out.println("Upload is successful");
            }
        });
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
            }
        });


/*
        mAnalyzeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //call playing api
                analyzeRecording();
            }
        });
*/

        return view;
    }

}
