package com.alaythiaproductions.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService  {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAm8LBKe8:APA91bEEuH9pLgPzE-LwayNREqT3_6pcii1YphIL_TeBuM3GF5HSV9VcHb6P2Z7uu013fyVI-bI5JMybhlev8DT0ZwqX0DH35or7RIaH8j5aMdSQ252i-dcBCJC1ShJDx3vq-A2_ciM9"
    })

    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
