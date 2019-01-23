package project.ece496.emotionrecogspeechgui;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;


public class MainActivity extends RecorderActivity {

    //private BottomNavigationView mMainNav;
    private FrameLayout mMainFrame;
    //private Record mRecordFragment;
    //private HomeFragment mHomeFragment;
    private static final String LOG_TAG = "AudioRecordTest";
    private AppCompatButton mRecordButton, mPlayButton, mUploadButton, mAnalyzeButton;
    private AppCompatTextView mResultView;
    private AudioRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private static String mFileName = null;
    boolean mStartPlaying = true, mStartRecording = true;
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
        mFileName = getExternalCacheDir().getAbsolutePath();
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
        file = new File(mFileName);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    client = new Socket("100.65.202.107", 5000); //connect to server
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

                        /*
                        while (dataInputStream.read(received) == -1) {
                        }
                        s = new String(received);
                        System.out.println(s);
                        */
                        fileInputStream.close();
                    }

                    mUploadButton.setText("Upload Success");
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

    private void analyzeRecording() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client = new Socket("100.65.202.107", 5000); //connect to server
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
                mResultView.append("\nResult:" + emotionResult);

            }

        }).start();


            //System.out.println(emotionResult);
    }

    class TranscriptionTask extends AsyncTask<File, Void, String> {

        @Override

        protected String doInBackground(File... files) {

            transcriber = new WatsonSpeechTranscriber();

            return transcriber.transcribe(files[0]);

        }
        @Override
        protected void onPostExecute(String s) {
            if(s != null)
                mResultView.setText(s);
                transcribedText = new String(s);
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mMainFrame = (FrameLayout) findViewById(R.id.main_frame);
        storage = FirebaseStorage.getInstance();
        mRecordButton = (AppCompatButton) findViewById(R.id.record_button);
        mPlayButton = (AppCompatButton) findViewById(R.id.play_button);
        mUploadButton = (AppCompatButton) findViewById(R.id.upload_button);
        mAnalyzeButton = (AppCompatButton) findViewById(R.id.analyze_button);
        mResultView = (AppCompatTextView) findViewById(R.id.display);

/*      new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client = new Socket("100.65.202.107", 5000);
                } catch (UnknownHostException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();*/
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
        mAnalyzeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                analyzeRecording();
            }
        });

        /*
        mMainNav = (BottomNavigationView) findViewById(R.id.main_nav);

        mRecordFragment = new Record();
        mHomeFragment = new HomeFragment();
        setFragment(mHomeFragment);

        mMainNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {

                    case R.id.Emo_Recog_Home:
//                        mMainNav.setItemBackgroundResource(R.color.colorPrimary);
                        setFragment(mHomeFragment);
                        return true;
                    case R.id.Emo_Recog_New_Audio:
                        //mMainNav.setItemBackgroundResource(R.color.colorPrimary);
                        setFragment(mRecordFragment);
                        return true;
                    default:
                        return false;
                }
            }
        });*/
        }

    /*
    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }*/

    }
