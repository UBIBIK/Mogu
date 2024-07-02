package com.example.mogu;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/signup")
    Call<String> signup(@Body UserInfo user);
}
