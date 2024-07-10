package com.example.mogu.retrofit;

import com.example.mogu.object.CreateGroupRequest;
import com.example.mogu.object.UserInfo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/signup")
    Call<String> signup(@Body UserInfo user);

    @POST("/login")
    Call<UserInfo> login(@Body UserInfo user);

    @POST("/group-create")
    Call<UserInfo> createGroup(@Body CreateGroupRequest request);
}
