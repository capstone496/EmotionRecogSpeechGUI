package retrofit.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResultObject {

    @SerializedName("predictedEmotion")
    @Expose
    private String predictedEmotion;

    public String getPredictedEmotion() {
        return predictedEmotion;
    }

    public void setPredictedEmotion(String predictedEmotion) {
        this.predictedEmotion = predictedEmotion;
    }

}