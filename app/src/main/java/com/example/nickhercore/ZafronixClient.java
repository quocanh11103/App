package com.example.nickhercore;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ZafronixClient {

    private static Retrofit retrofit = null;

    public static final String BASE_URL = "https://api.zafronix.com/fifa/worldcup/v1/";

    // Dán key Zafronix của bạn vào đây
    public static final String API_KEY = "zwc_free_2a0cd7db1f9b0fef878df96b";

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(12, TimeUnit.SECONDS)
                    .readTimeout(12, TimeUnit.SECONDS)
                    .writeTimeout(12, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request original = chain.request();

                        Request request = original.newBuilder()
                                .header("X-API-Key", API_KEY)
                                .header("Accept", "application/json")
                                .method(original.method(), original.body())
                                .build();

                        return chain.proceed(request);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

    public static ZafronixApiService getApiService() {
        return getClient().create(ZafronixApiService.class);
    }
}