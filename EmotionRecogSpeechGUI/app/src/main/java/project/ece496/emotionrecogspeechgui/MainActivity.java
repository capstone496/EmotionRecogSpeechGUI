package project.ece496.emotionrecogspeechgui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;



public class MainActivity extends RecorderActivity implements Communicator {

    private BottomNavigationView mMainNav;
    FragmentManager fragmentManager;
    Record mRecordFragment;
    HomeFragment mHomeFragment;
    Fragment active;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainNav = (BottomNavigationView) findViewById(R.id.main_nav);
        mRecordFragment = new Record();
        mHomeFragment = new HomeFragment();
        active = mHomeFragment;

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.main_frame, mRecordFragment, "2").hide(mRecordFragment).commit();
        fragmentManager.beginTransaction().add(R.id.main_frame, mHomeFragment, "1").commit();

        mMainNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.Emo_Recog_Home:
                        fragmentManager.beginTransaction().hide(active).show(mHomeFragment).commit();
                        active = mHomeFragment;
                        return true;
                    case R.id.Emo_Recog_New_Audio:
                        fragmentManager.beginTransaction().hide(active).show(mRecordFragment).commit();
                        active = mRecordFragment;
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    @Override
    public void updateTranscription(String transcribedText){
        mHomeFragment.updateTranscriptionView(transcribedText);
    }

    @Override
    public void updateResult(String result) {
        mHomeFragment.updateResultView(result);
    }
}
