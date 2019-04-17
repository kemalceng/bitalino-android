package com.acoustic.bitalino;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

// TODO : delete
public interface RecordService {

    @POST("/records")
    Call<ServerResponse<CreateRecordResponse>> createRecord(@Body BitalinoDeviceInfo deviceInfo);

    @PUT("/records/{recordId}")
    Call<Void> uploadReading(@Path("recordId") String recordId, @Body BITalinoReading reading);

    @PUT("/records/{recordId}/stop")
    Call<SimpleMessage> stopRecord(@Path("recordId") String recordId);

}