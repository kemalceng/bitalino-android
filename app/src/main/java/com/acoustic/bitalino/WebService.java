package com.acoustic.bitalino;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface WebService {
    @GET("/ping")
    Call<SimpleMessage> ping();

    @POST("/start-test")
    Call<SimpleMessage> startTest(@Body TestInfo testInfo);

    @POST("/records/{test}")
    Call<ServerResponse<CreateRecordResponse>> createRecord(@Body BitalinoDeviceInfo deviceInfo);

    @PUT("/records/{test}/{recordId}")
    Call<Void> uploadReading(@Path("test") String test, @Path("recordId") String recordId, @Body BITalinoReading reading);

    @PUT("/records/{test}/{recordId}/stop")
    Call<SimpleMessage> stopRecord(@Path("test") String test, @Path("recordId") String recordId);

}