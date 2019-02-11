package project.ece496.emotionrecogspeechgui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import android.view.MenuItem;
import android.widget.FrameLayout;



public class MainActivity extends RecorderActivity {

    private BottomNavigationView mMainNav;
    private FrameLayout mMainFrame;
    private Record mRecordFragment;
    private HomeFragment mHomeFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        });
        }


    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }

    }
