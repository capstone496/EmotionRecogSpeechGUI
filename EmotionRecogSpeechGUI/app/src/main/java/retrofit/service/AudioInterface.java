package retrofit.service;


import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit.model.ResultObject;

import retrofit2.Response;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
public interface AudioInterface {
    @Multipart
    @POST("api/predict") //index.php
    // @POST("/imagefolder/index.php")
    Call<ResultObject> uploadAudioToServer(@Part MultipartBody.Part audio);
}