package project.ece496.emotionrecogspeechgui;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    AppCompatTextView mTranscriptionView, mResultView;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTranscriptionView = (AppCompatTextView) getActivity().findViewById(R.id.transcription);
        mResultView = (AppCompatTextView) getActivity().findViewById(R.id.display);

    }

    public void updateTranscriptionView(String transcribedText) {
        mTranscriptionView.setMovementMethod(new ScrollingMovementMethod());
        mTranscriptionView.setText("Transcription: \n" + transcribedText);
    }

    public void updateResultView(String result) {
        String emoji = "";
        switch (result) {
            case "happy":
                emoji = "\uD83D\uDE04" ;
                break;
            case "sad":
                emoji = "\uD83D\uDE22" ;
                break;
            case "angry":
                emoji = "\uD83D\uDE20";
                break;
            case "fearful":
                emoji = "\uD83D\uDE28";
                break;
            case "disgust":
                emoji = "";
                break;
            case "surprise":
                emoji = "";
                break;
            default:
                break;
        }
        String text = "second";

        SpannableString emoji_span = new SpannableString(emoji);
        SpannableString text_span = new SpannableString(result);
        emoji_span.setSpan(new RelativeSizeSpan(4f), 0,emoji.length(), 0);
        text_span.setSpan(new RelativeSizeSpan(1f), 0,result.length(), 0);

        mResultView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        mResultView.setText( emoji_span );
        mResultView.append("\n");
        mResultView.append( text_span );


    }

}
