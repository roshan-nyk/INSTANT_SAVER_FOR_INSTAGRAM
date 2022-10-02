package instant.saver.for_instagram.api;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import instant.saver.for_instagram.model.PhotosFeedModel;
import instant.saver.for_instagram.model.ResponseModel;
import instant.saver.for_instagram.model.UserInfoForSingleStoryDownload;
import instant.saver.for_instagram.model.album_gallery.Album_Data;
import instant.saver.for_instagram.model.story.FullDetailModel;
import instant.saver.for_instagram.model.story.StoryModel;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class GetDataFromServer extends Application {
    private static final int NUMBER_OF_THREADS = 5;
    private static volatile GetDataFromServer INSTANCE;
    private final ScheduledExecutorService dataBaseWriteExecutor = Executors.newScheduledThreadPool(NUMBER_OF_THREADS);
    private final HashMap<Album_Data, List<Long>> storeDataAfterDownloadCompletion = new HashMap<>();
    private ArrayList<String> browserData = (ArrayList<String>) Stream.of(
//"\"Mozilla/5.0 (Linux; Android 11; GM1901 Build/RKQ1.201022.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/105.0.5195.79 Mobile Safari/537.36\"",
//          "1217981644879628",
            "\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36\"",
            "936619743392459",
            "*/*",
            "en-US,en;q=0.5",
            "198387",
            "hmac.AR0RiVMdZR3OBMQbj0C2mnJc8cDcVdbTQdcHyaroQcXU6Jem",
//          "hmac.AR3W7OZrdhF6CuDt5_Ruz6njJ7ytITG9nF0_j5Enuxpo3iiF",
            "https://www.instagram.com",
            "1",
            "keep-alive",
            "https://www.instagram.com/",
            "empty",
            "cors",
            "same-site",
            "1",
            "trailers"
//            ,
//            "i.instagram.com"
//            "4b5f8c8eb791"
    ).collect(Collectors.toList());
    private String csrfToken = "";

    public static GetDataFromServer getInstance() {
        if (INSTANCE == null) {
            synchronized (GetDataFromServer.class) {
                INSTANCE = new GetDataFromServer();
            }
        }
        return INSTANCE;
    }

    //    to make orientation portrait
    @Override
    public void onCreate() {
        super.onCreate();
        // register to be informed of activities starting up
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                // new activity created; force its orientation to portrait
                if (android.os.Build.VERSION.SDK_INT != Build.VERSION_CODES.O)
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
               /* StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                        .detectDiskReads()
                        .detectDiskWrites()
                        .detectNetwork()   // or .detectAll() for all detectable problems
                        .penaltyLog()
                        .build());*/
                /*StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                        .detectLeakedSqlLiteObjects()
                        .detectLeakedClosableObjects()
                        .detectNonSdkApiUsage()
                        .penaltyLog()
                        .build());*/
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }
        });
    }
    
    @SuppressLint("SuspiciousIndentation")
    String getCsrfTokenValue(String Cookie){
        if(csrfToken.isEmpty())
//         csrfToken = Cookie.split(";")[3].substring(11);
        csrfToken = "z8MYADZ3mUJbwsCQFUsiAhC2XhIKOoT7";
        Log.d("CSRF_TOKEN","===========================" + csrfToken + "===================");
        return csrfToken;
    }

    public HashMap<Album_Data, List<Long>> getStoreDataAfterDownloadCompletion() {
        return storeDataAfterDownloadCompletion;
    }

    public ScheduledExecutorService getDataBaseWriteExecutor() {
        return dataBaseWriteExecutor;
    }

    public void getStories(DisposableObserver<StoryModel> observer, String Cookie) {
        RestClient.getInstance().getService()
                .getStoriesApi(
                        "https://i.instagram.com/api/v1/feed/reels_tray/",
                        Cookie,
                        browserData.get(0),browserData.get(1),browserData.get(2),browserData.get(3),browserData.get(4),browserData.get(5),browserData.get(6),browserData.get(7),browserData.get(8),browserData.get(9),browserData.get(10),browserData.get(11),browserData.get(12),browserData.get(13),browserData.get(14)//,browserData.get(15),getCsrfTokenValue(Cookie)
                )
                .subscribeOn(Schedulers.from(dataBaseWriteExecutor))
                .observeOn(AndroidSchedulers.mainThread(), true)
                .subscribe(new Observer<StoryModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(StoryModel o) {
                        observer.onNext(o);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                });
    }

    public void getStoriesFullDetailFeed(DisposableObserver<FullDetailModel> observer, String UserId, String Cookie) {
        
        RestClient.getInstance().getService()
//                .getStoriesFullDetailInfoApi("https://i.instagram.com/api/v1/users/" + UserId + "/full_detail_info?max_id=", Cookie, "\"Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 XL Build/OPD1.170816.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.87 Mobile Safari/537.36 Instagram 182.0.0.29.124\"")
                .getStoriesFullDetailInfoApi(
                            "https://i.instagram.com/api/v1/feed/reels_media/",
                        UserId,
                        Cookie,
                        browserData.get(0),browserData.get(1),browserData.get(2),browserData.get(3),browserData.get(4),browserData.get(5),browserData.get(6),browserData.get(7),browserData.get(8),browserData.get(9),browserData.get(10),browserData.get(11),browserData.get(12),browserData.get(13),browserData.get(14)//,browserData.get(15),getCsrfTokenValue(Cookie)
                )
                .subscribeOn(Schedulers.from(dataBaseWriteExecutor))
                .observeOn(AndroidSchedulers.mainThread(), true)
                .subscribe(new Observer<FullDetailModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(FullDetailModel o) {
                        observer.onNext(o);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                });
    }

    public void getPhotoFullDetailFeed(DisposableObserver<PhotosFeedModel> photoDetailObserver, String userId, String Cookie, String end_cursor, String query_hash) {
        String variables;
        if (query_hash.equals("d4d88dc1500312af6f937f7b804c68c3"))
            variables = "{\"user_id\":\"" + userId + "\",\"include_highlight_reels\":true}";
        else {
            if (end_cursor == null)
                variables = "{\"id\":\"" + userId + "\",\"first\":18}";
            else
                variables = "{\"id\":\"" + userId + "\",\"first\":9,\"after\":\"" + end_cursor + "\"}";
        }
        
        RestClient.getInstance().getService()
                .getPhotoFullDetailInfoApi(
                        "https://www.instagram.com/graphql/query/",
                        Cookie,
                        query_hash,
                        variables,
                        browserData.get(0),browserData.get(1),browserData.get(2),browserData.get(3),browserData.get(4),browserData.get(5),browserData.get(6),browserData.get(7),browserData.get(8),browserData.get(9),browserData.get(10),browserData.get(11),browserData.get(12),browserData.get(13),browserData.get(14)//,browserData.get(15),getCsrfTokenValue(Cookie)
                )
                .subscribeOn(Schedulers.from(dataBaseWriteExecutor))
                .observeOn(AndroidSchedulers.mainThread(), true)
                .subscribe(new Observer<PhotosFeedModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(PhotosFeedModel o) {
                        photoDetailObserver.onNext(o);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("TAG", "onError: " + "photodetailobserver" + e.getMessage());
                        photoDetailObserver.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        photoDetailObserver.onComplete();
                    }
                });
    }

    public void callResult(DisposableObserver<ResponseModel> observer, String URL, String Cookie) {
        
        RestClient.getInstance().getService()
                .callResult(
                          URL,
                        Cookie,
                        browserData.get(0),browserData.get(1),browserData.get(2),browserData.get(3),browserData.get(4),browserData.get(5),browserData.get(6),browserData.get(7),browserData.get(8),browserData.get(9),browserData.get(10),browserData.get(11),browserData.get(12),browserData.get(13),browserData.get(14)//,browserData.get(15),getCsrfTokenValue(Cookie)
                )
                .subscribeOn(Schedulers.from(dataBaseWriteExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(ResponseModel o) {
                        Log.d("TAG", "onNext: callResult entered in getDataServer class.");
                        observer.onNext(o);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                });
    }

    public void addUserSaveProfile(DisposableObserver<ResponseModel> observer, String URL, String Cookie) {
        
        RestClient.getInstance().getService()
                .callResult(
                         URL,
                        Cookie,
                        browserData.get(0),browserData.get(1),browserData.get(2),browserData.get(3),browserData.get(4),browserData.get(5),browserData.get(6),browserData.get(7),browserData.get(8),browserData.get(9),browserData.get(10),browserData.get(11),browserData.get(12),browserData.get(13),browserData.get(14)//,browserData.get(15),getCsrfTokenValue(Cookie)
                )
                .subscribeOn(Schedulers.from(dataBaseWriteExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(ResponseModel o) {
                        observer.onNext(o);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("addUserSaveProfile onerror:-", e.getLocalizedMessage());
                        observer.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                });
    }

    public void getStoryUserIdForDownload(DisposableObserver<UserInfoForSingleStoryDownload> observer, String url, String Cookie) {
        
        RestClient.getInstance().getService()
                .getUserIdForStoryDownload(
                         url,
                        Cookie,
                        browserData.get(0),browserData.get(1),browserData.get(2),browserData.get(3),browserData.get(4),browserData.get(5),browserData.get(6),browserData.get(7),browserData.get(8),browserData.get(9),browserData.get(10),browserData.get(11),browserData.get(12),browserData.get(13),browserData.get(14)//,browserData.get(15),getCsrfTokenValue(Cookie)
                )
                .subscribeOn(Schedulers.from(dataBaseWriteExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<UserInfoForSingleStoryDownload>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(UserInfoForSingleStoryDownload o) {
                        observer.onNext(o);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                });
    }

    public void getStoryToDownload(DisposableObserver<FullDetailModel> observer, String userId, String Cookie) {
        
        RestClient.getInstance().getService()
                .getStoryToDownload(
                          "https://i.instagram.com/api/v1/feed/reels_media/",
                        userId,
                        Cookie,
                        browserData.get(0),browserData.get(1),browserData.get(2),browserData.get(3),browserData.get(4),browserData.get(5),browserData.get(6),browserData.get(7),browserData.get(8),browserData.get(9),browserData.get(10),browserData.get(11),browserData.get(12),browserData.get(13),browserData.get(14)//,browserData.get(15),getCsrfTokenValue(Cookie)
                )
                .subscribeOn(Schedulers.from(dataBaseWriteExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<FullDetailModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(FullDetailModel o) {
                        observer.onNext(o);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                });
    }

    public void checkUserNameProfilePic(DisposableObserver<ResponseModel> observer, String URL, String Cookie) {
        
        RestClient.getInstance().getService()
                .callResult(
                        URL,
                        Cookie,
                        browserData.get(0),browserData.get(1),browserData.get(2),browserData.get(3),browserData.get(4),browserData.get(5),browserData.get(6),browserData.get(7),browserData.get(8),browserData.get(9),browserData.get(10),browserData.get(11),browserData.get(12),browserData.get(13),browserData.get(14)//,browserData.get(15),getCsrfTokenValue(Cookie)
                )
                .subscribeOn(Schedulers.from(dataBaseWriteExecutor))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(ResponseModel o) {
                        observer.onNext(o);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                });
    }

    public void getSavedItems(DisposableObserver<StoryModel> observer, String URL, String Cookie) {
        RestClient.getInstance().getService()
                .getStoriesApi(
                        "https://i.instagram.com/api/v1/feed/reels_tray/",
                        Cookie,
                        browserData.get(0),browserData.get(1),browserData.get(2),browserData.get(3),browserData.get(4),browserData.get(5),browserData.get(6),browserData.get(7),browserData.get(8),browserData.get(9),browserData.get(10),browserData.get(11),browserData.get(12),browserData.get(13),browserData.get(14)//,browserData.get(15),getCsrfTokenValue(Cookie)
                )
                .subscribeOn(Schedulers.from(dataBaseWriteExecutor))
                .observeOn(AndroidSchedulers.mainThread(), true)
                .subscribe(new Observer<StoryModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(StoryModel o) {
                        observer.onNext(o);
                    }

                    @Override
                    public void onError(Throwable e) {
                        observer.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                });
    }
}
