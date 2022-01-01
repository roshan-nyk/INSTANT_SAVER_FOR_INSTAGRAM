package instant.saver.for_instagram.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestClient {
    private static volatile Retrofit retrofit = null;
    private static RestClient restClient = null ;

    public static RestClient getInstance() {
        if(restClient == null) {
            synchronized (RestClient.class) {
                restClient = new RestClient();
            }
        }
        return restClient;
    }


    Gson gson = new GsonBuilder()
            .setLenient()
            .create();

    private RestClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://www.instagram.com/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
    }
    public APISERVICES getService() {
        return retrofit.create(APISERVICES.class);
    }
}
