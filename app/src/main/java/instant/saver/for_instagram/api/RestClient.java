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

    /*OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor((Interceptor) new LoggingInterceptor()).build();

    static class LoggingInterceptor implements Interceptor {
        @NonNull
        @SuppressLint("DefaultLocale")
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request request = chain.request();

            long t1 = System.nanoTime();
            Log.v("Request ",String.format("Sending request %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            Log.v("Request ",String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers()));


            assert response.body() != null;
            final String responseString = new String(response.body().bytes());

            Log.v("Request ","Response: " + responseString);

            return  response.newBuilder()
                    .body(ResponseBody.create(response.body().contentType(), responseString))
                    .build();
        }}*/

    Gson gson = new GsonBuilder()
            .setLenient()
//          .setPrettyPrinting() there will not be any whitespace in between field names and its value, object fields, and objects within arrays in the JSON output
        	.setPrettyPrinting()
            .create();

    private RestClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://www.instagram.com/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                    .client(client)
                    .build();
        }
    }
    public APISERVICES getService() {
        return retrofit.create(APISERVICES.class);
    }
}
