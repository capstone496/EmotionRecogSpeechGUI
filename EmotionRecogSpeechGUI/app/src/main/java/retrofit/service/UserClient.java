package retrofit.service;
import retrofit.model.User;
import okhttp3.MultipartBody;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;


public interface UserClient {

    @POST("user")
    Call<User> creatAccount(@Body User user);

    @Multipart
    @POST("upload")
    Call<ResponseBody> uploadAudio(
            @Part("description") RequestBody description,
            @Part MultipartBody.Part audio
    );
}
