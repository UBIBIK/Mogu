package com.example.mogu.retrofit;

import com.example.mogu.object.CreateGroupRequest;
import com.example.mogu.object.CreateTripScheduleRequest;
import com.example.mogu.object.DeleteGroupMemberRequest;
import com.example.mogu.object.JoinGroupRequest;
import com.example.mogu.object.UserInfo;
import com.example.mogu.object.DeleteGroupRequest;

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

    @POST("/api/joinGroup")
    Call<UserInfo> joinGroup(@Body JoinGroupRequest request);

    @POST("/api/DeleteGroup")
    Call<UserInfo> deleteGroup(@Body DeleteGroupRequest request);

    @POST("/api/DeleteGroupMember")
    Call<UserInfo> deleteGroupMember(@Body DeleteGroupMemberRequest request);

    @POST("/api/TripScheduleCreate")
    Call<UserInfo> createTripSchedule(@Body CreateTripScheduleRequest request);
}
