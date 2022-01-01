package instant.saver.for_instagram;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.insta_saver.R;
import com.example.insta_saver.databinding.ActivityProfileAcitivityBinding;
import com.gu.toolargetool.TooLargeTool;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import instant.saver.for_instagram.api.GetDataFromServer;
import instant.saver.for_instagram.model.PhotosFeedModel;
import instant.saver.for_instagram.model.ResponseModel;
import instant.saver.for_instagram.model.album_gallery.AlbumDataViewModel;
import instant.saver.for_instagram.model.album_gallery.Album_Data;
import instant.saver.for_instagram.model.bookmark_profile.SavedProfileViewModel;
import instant.saver.for_instagram.model.bookmark_profile.Saved_Profile;
import instant.saver.for_instagram.model.story.FullDetailModel;
import instant.saver.for_instagram.util.Utils;
import io.reactivex.observers.DisposableObserver;

public class SingleProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static boolean anyOneButtonTouched = false;
    private ActivityProfileAcitivityBinding binding;
    private String userId, userName, savedProfilePicURL;
    private Utils utils;
    private SingleProfileActivity activity;
    private StoryFragment storyFragment;
    private PhotosFragment photosFragment;
    private boolean isPhotoButtonClicked = false;
    private boolean isStoryButtonClicked = false;
    private FullDetailModel storyDetailModel;
    private static PhotosFeedModel photosFeedModel;
    private PhotosFeedModel storyHighlightModel;
    private WindowInsetsController insetsController;
    private int savedProfilePosition;
    private AlbumDataViewModel albumDataViewModel;
    private static List<Album_Data> albumData;

    public static PhotosFeedModel getPhotosFeedModel() {
        return photosFeedModel;
    }

    private final DisposableObserver<PhotosFeedModel> photoDetailObserver = new DisposableObserver<PhotosFeedModel>() {
        @Override
        public void onNext(@NotNull PhotosFeedModel response) {
            try {
                photosFeedModel = response;
                checkForOtherModel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(@NotNull Throwable throwable) {
            Log.e("TAG", "onError: " + throwable.getCause() + "        " + throwable.getMessage() + "      " + throwable.getLocalizedMessage());
            Toast.makeText(activity, "Error TimeOut. Try Again", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onComplete() {
        }
    };
    private SavedProfileViewModel savedProfileViewModel;
    private final DisposableObserver<ResponseModel> userNameProfilePic = new DisposableObserver<ResponseModel>() {
        @Override
        public void onNext(@NotNull ResponseModel responseModel) {
            String currentProfileUserName = responseModel.getGraphql().getUser().getUsername();
            String currentProfilePicURL = responseModel.getGraphql().getUser().getProfile_pic_url();
            Log.d("TAG", "userNameProfilePic: " + currentProfilePicURL + " " + currentProfileUserName);
            if (!currentProfileUserName.equals(userName) || !currentProfilePicURL.equals(savedProfilePicURL)) {
                Saved_Profile savedProfile = new Saved_Profile(userId, currentProfileUserName, currentProfilePicURL);
                savedProfileViewModel.updateSingleSavedProfile(savedProfile);
            }
        }

        @Override
        public void onError(@NotNull Throwable e) {        }

        @Override
        public void onComplete() {        }
    };
    private final DisposableObserver<FullDetailModel> storyDetailObserver = new DisposableObserver<FullDetailModel>() {
        @Override
        public void onNext(@NotNull FullDetailModel response) {
            try {
                storyDetailModel = response;
                if (savedProfilePosition != -5)
                    // if coming from savedProfile activity to update the saved files profile pic and username, savedProfilePosition number used
                    checkForUpdatingSavedProfile(response);
                checkForOtherModel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(@NotNull Throwable throwable) {
            Log.e("TAG", "onError: " + throwable.getCause() + "        " + throwable.getMessage() + "      " + throwable.getLocalizedMessage());
            Toast.makeText(activity, "Error TimeOut. Try Again", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onComplete() { }
    };

    private final DisposableObserver<PhotosFeedModel> storyHighlightObserver = new DisposableObserver<PhotosFeedModel>() {
        @Override
        public void onNext(@NotNull PhotosFeedModel response) {
            try {
                storyHighlightModel = response;
                checkForOtherModel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(@NotNull Throwable throwable) {
            Log.e("TAG", "onError: " + throwable.getCause() + "        " + throwable.getMessage() + "      " + throwable.getLocalizedMessage());
            Toast.makeText(activity, "Error TimeOut. Try Again", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onComplete() { }
    };

    public static void setAnyOneButtonTouched(boolean anyOneButtonTouched) {
        SingleProfileActivity.anyOneButtonTouched = anyOneButtonTouched;
    }

    private void checkForUpdatingSavedProfile(FullDetailModel response) {
        Log.d("TAG", "checkForUpdatingSavedProfile: " + response.getReels_media().size());
        if (response.getReels_media().size() > 0) {
            String currentProfileUserName = response.getReels_media().get(0).getUser().getUsername();
            String currentProfilePicURL = response.getReels_media().get(0).getUser().getProfile_pic_url();
            if (!currentProfileUserName.equals(userName) || !currentProfilePicURL.equals(savedProfilePicURL)) {
                Saved_Profile savedProfile = new Saved_Profile(userId, currentProfileUserName, currentProfilePicURL);
                savedProfileViewModel.updateSingleSavedProfile(savedProfile);
            }
        } else
            GetDataFromServer.getInstance().checkUserNameProfilePic(userNameProfilePic, "https://instagram.com/" + userName + "?__a=1", utils.getCookies());
    }

    private void checkForOtherModel() {
        if (storyDetailModel != null && photosFeedModel != null && storyHighlightModel != null) {
            if (binding.Stories.getVisibility() == View.GONE && binding.Photos.getVisibility() == View.GONE) {
                Log.d("TAG", "checkForOtherModel: ");
                binding.singleProfileActivityProgressbar.setVisibility(View.GONE);
                binding.Stories.setVisibility(View.VISIBLE);
                binding.Photos.setVisibility(View.VISIBLE);
                if (savedProfilePosition == -5) {
                    savedProfilePicURL = storyDetailModel.getReels_media().get(0).getUser().getProfile_pic_url();
                    userName = storyDetailModel.getReels_media().get(0).getUser().getUsername();
                }
            }
            onClick(binding.Stories);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileAcitivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activity = this;
        utils = new Utils(activity);
//        to check tooLarge Exception
        TooLargeTool.startLogging(getApplication());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            insetsController = activity.getWindow().getInsetsController();

        savedProfileViewModel = new ViewModelProvider.AndroidViewModelFactory(activity.getApplication()).create(SavedProfileViewModel.class);

        albumDataViewModel = new ViewModelProvider.AndroidViewModelFactory(activity.getApplication()).create(AlbumDataViewModel.class);

        Intent intent = getIntent();
        userId = intent.getStringExtra("UserId");
        userName = intent.getStringExtra("UserName");
//         savedProfilePosition  this variable is used to check whether you are accessing this activity from savedProfile database or from the currentStories visible to user
        savedProfilePosition = intent.getIntExtra("Saved_Profile_Position", -5);
        savedProfilePicURL = intent.getStringExtra("Saved_Profile_Profile_URL");

        binding.Photos.setOnClickListener(this);
        binding.Stories.setOnClickListener(this);

        Objects.requireNonNull(getSupportActionBar()).hide();

        binding.singleProfileUserName.setText(userName);
        binding.singleProfileBackButton.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String @NotNull [] permissions, int @NotNull [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 30) {
            if (grantResults.length > 0) {
                boolean reader = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean writer = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (reader && writer) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    if (storyFragment != null) storyFragment.startDownload();
                    if (photosFragment != null) photosFragment.startDownload();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "You Denied Permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (storyFragment == null && photosFragment == null) {
            albumDataViewModel.getAllMedias().observe(activity, album_data -> {
               if(albumData == null) {
                   albumData = album_data;
                   callStoriesDetailApi(userId);
                   callPhotosDetailApi(userId);
                   callStoriesHighlightsApi(userId);
               }
            });
        }
    }

    public static List<Album_Data> getAlbumData() {
        return albumData;
    }

    private void callStoriesHighlightsApi(String userId) {
        try {
            if (utils.isNetworkAvailable()) {
                if (GetDataFromServer.getInstance() != null && utils.getCookies() != null)
                    GetDataFromServer.getInstance().getPhotoFullDetailFeed(storyHighlightObserver, userId, utils.getCookies(), null, "d4d88dc1500312af6f937f7b804c68c3");
            } else {
                binding.singleProfileActivityProgressbar.setVisibility(View.GONE);
                Toast.makeText(this, "NO internet Connection.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callStoriesDetailApi(String UserId) {
        try {
            if (utils.isNetworkAvailable()) {
                if (GetDataFromServer.getInstance() != null && utils.getCookies() != null)
                    GetDataFromServer.getInstance().getStoriesFullDetailFeed(storyDetailObserver, UserId, utils.getCookies());
            } else {
                binding.singleProfileActivityProgressbar.setVisibility(View.GONE);
                Toast.makeText(this, "NO internet Connection.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callPhotosDetailApi(String userId) {
        try {
            if (utils.isNetworkAvailable()) {
                if (GetDataFromServer.getInstance() != null && utils.getCookies() != null)
                    GetDataFromServer.getInstance().getPhotoFullDetailFeed(photoDetailObserver, userId, utils.getCookies(), null, "8c2a529969ee035a5063f2fc8602a0fd");
            } else {
                binding.singleProfileActivityProgressbar.setVisibility(View.GONE);
                Toast.makeText(this, "NO internet Connection.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        anyOneButtonTouched = true;
        if (v == binding.Photos) {
            if (!isPhotoButtonClicked) {
                binding.Photos.setBackgroundColor(Color.LTGRAY);
                binding.Stories.setBackgroundColor(Color.DKGRAY);
                isPhotoButtonClicked = true;
                isStoryButtonClicked = false;
                if (photosFragment == null) {
//                    TransactionTooLargeException when trying to switch from Fragment to Activity so not sending photoFeedModel
//                    photosFragment = PhotosFragment.newInstance(photosFeedModel, userId, savedProfilePicURL, "8c2a529969ee035a5063f2fc8602a0fd");
                    photosFragment = PhotosFragment.newInstance(userId, savedProfilePicURL, "8c2a529969ee035a5063f2fc8602a0fd");
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.empty_constraint_layout, photosFragment)
                            .addToBackStack(null)
                            .commit();
                }
                if (storyFragment != null) {
                    photosFragment.setBackground();
                    storyFragment.setBackground();
                }
            }
        } else if (v == binding.Stories) {
            if (!isStoryButtonClicked) {
                binding.Photos.setBackgroundColor(Color.DKGRAY);
                binding.Stories.setBackgroundColor(Color.LTGRAY);
                isStoryButtonClicked = true;
                isPhotoButtonClicked = false;
                if (storyFragment == null && storyHighlightModel != null) {
                    storyFragment = StoryFragment.newInstance(storyDetailModel, userName, savedProfilePicURL, storyHighlightModel);
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.empty_constraint_layout, storyFragment)
                            .addToBackStack(null)
                            .commit();
                }
                if (photosFragment != null) {
                    photosFragment.setBackground();
                    storyFragment.setBackground();
                }
            }
        } else if (v == binding.singleProfileBackButton)
            finish();
        anyOneButtonTouched = false;
    }

    private void setSystemUiFlags(int flags, boolean value) {
        int systemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
        if (value)
            systemUiVisibility |= flags;
        else
            systemUiVisibility &= ~flags;
        activity.getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);
    }


    private void setFullscreenFlags(boolean value) {
        int fullscreenFlags = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        setSystemUiFlags(fullscreenFlags, value);
    }

    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("TAG", "onSaveInstanceState  activity: " + outState);
    }

    @Override
    public void onBackPressed() {
//        Log.d("TAG", "onBackPressed: "+anyOneButtonTouched);                check this value after coming back from viewing a pic/story in full view

        if (!anyOneButtonTouched)
            finish();
        else {
//          not required if statement
            if (insetsController != null)
                insetsController.show(WindowInsets.Type.statusBars());

           /* setSystemUiFlags(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN, false);
            setFullscreenFlags(false);*/

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//                getWindow().clearFlags(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS);
            }
            getWindow().getDecorView().setSystemUiVisibility( getWindow().getDecorView().getSystemUiVisibility() & ~(View.SYSTEM_UI_FLAG_FULLSCREEN));
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(activity.getResources().getColor(R.color.purple_700));

            anyOneButtonTouched = false;
            super.onBackPressed();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "onDestroy: SingleProfileActivity");
//        as albumData is a static variable we need to make it null before destroying
        albumData = null;
        albumDataViewModel.getAllMedias().removeObservers(activity);
        storyFragment = null;
        photosFragment = null;
        anyOneButtonTouched = false;
        activity = null;
    }
}