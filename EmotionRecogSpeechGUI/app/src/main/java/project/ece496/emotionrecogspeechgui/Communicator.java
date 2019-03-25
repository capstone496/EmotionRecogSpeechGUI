package project.ece496.emotionrecogspeechgui;

public interface Communicator {
    void updateTranscription(String transcribedText);
    void updateResult(String result);
}
