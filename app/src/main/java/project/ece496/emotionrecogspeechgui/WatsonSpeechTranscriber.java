
package project.ece496.emotionrecogspeechgui;

import com.ibm.watson.developer_cloud.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.developer_cloud.speech_to_text.v1.model.SpeechRecognitionResults;
import java.io.File;
import java.io.FileNotFoundException;



    /**

     * Wraps Watson's Speech to Text API

*/



    public class WatsonSpeechTranscriber {



        SpeechToText speechToText;



        public WatsonSpeechTranscriber(){

            speechToText = new SpeechToText(

                    BuildConfig.Speech2TextUsername,

                    BuildConfig.Speech2TextPassword);

        }



        public String transcribe(File file){

            try {

                RecognizeOptions recognizeOptions = new RecognizeOptions.Builder()

                        .audio(file)

                        .contentType("audio/wav")

                        .timestamps(true)

                        .build();



                SpeechRecognitionResults speechRecognitionResults =

                        speechToText.recognize(recognizeOptions).execute();



                return speechRecognitionResults

                        .getResults()

                        .get(0)

                        .getAlternatives()

                        .get(0)

                        .getTranscript();



            } catch (FileNotFoundException e) {

                e.printStackTrace();

            }

            return "Transcription has failed";

        }

    }
