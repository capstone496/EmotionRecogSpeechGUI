package project.ece496.emotionrecogspeechgui;

public interface Communicator {
     void updateTransription(String transcribedText);

    void updateResult(String result);
}
