package instant.saver.for_instagram;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import instant.saver.for_instagram.R;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import instant.saver.for_instagram.api.GetDataFromServer;
import instant.saver.for_instagram.databinding.ActivitySavedCollectionInstagramBinding;
import instant.saver.for_instagram.fragments.SavedItemFragment;
import instant.saver.for_instagram.model.PhotosFeedModel;
import instant.saver.for_instagram.model.story.FullDetailModel;
import instant.saver.for_instagram.model.story.ReelFeedModel;
import instant.saver.for_instagram.util.Utils;
import io.reactivex.observers.DisposableObserver;

public class Saved_Collection_Instagram_Activity extends AppCompatActivity {

    private static boolean isMediaFragmentOpened = false;
    private ActivitySavedCollectionInstagramBinding binding;
    private SavedItemFragment savedItemFragment;
    private static PhotosFeedModel photosFeedModel;
    private static ReelFeedModel reelFeedModel;
    private Activity activity;
    private Utils utils;
    private String userId, storyHighLight;
    private WindowInsetsController insetsController;

    public static PhotosFeedModel getPhotosFeedModel() {
        return photosFeedModel;
    }

    public static ReelFeedModel getReelFeedModel() { return reelFeedModel; }

    private final DisposableObserver<PhotosFeedModel> photoDetailObserver = new DisposableObserver<PhotosFeedModel>() {
        @Override
        public void onNext(@NotNull PhotosFeedModel response) {
            photosFeedModel = response;
            binding.savedItemActivityProgressBarLayout.setVisibility(View.GONE);
            if (savedItemFragment == null) {
//                TransactionTooLargeException when trying to switch from Fragment to Activity so not sending photoFeedModel
//                savedItemFragment = SavedItemFragment.newInstance(photosFeedModel, userId, "2ce1d673055b99250e93b6f88f878fde");
                savedItemFragment = SavedItemFragment.newInstance(userId, "2ce1d673055b99250e93b6f88f878fde");
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.saved_collection_Constraint_layout, savedItemFragment)
                    .addToBackStack(null)
                    .commit();
        }

        @Override
        public void onError(@NotNull Throwable throwable) {
            binding.savedItemActivityProgressBarLayout.setVisibility(View.GONE);
            Log.e("TAG", "onError: Saved_Collection_Inst_Act_PhotoDetailObserver" + throwable.getCause() + "        " + throwable.getMessage() + "      " + throwable.getLocalizedMessage());
            Toast.makeText(activity, "Error TimeOut. Try Again", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onComplete() {
        }
    };

    private final DisposableObserver<FullDetailModel> allHighlights = new DisposableObserver<FullDetailModel>() {
        @Override
        public void onNext(@NotNull FullDetailModel allStories) {
            try {
                Log.d("TAG", "onNext: " + allStories.getReels_media().get(0).getUser().getUsername());
                reelFeedModel = allStories.getReels_media().get(0);
                binding.savedItemActivityProgressBarLayout.setVisibility(View.GONE);
                if (savedItemFragment == null)
                    savedItemFragment = SavedItemFragment.newInstance();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.saved_collection_Constraint_layout, savedItemFragment)
                        .addToBackStack(null)
                        .commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(@NotNull Throwable e) {
            binding.savedItemActivityProgressBarLayout.setVisibility(View.GONE);
            Log.e("TAG", "onError: Saved_Collection_Inst_Act_allHighlights" + e.getCause() + "        " + e.getMessage() + "      " + e.getLocalizedMessage());
            Toast.makeText(activity, "Error TimeOut. Try Again", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onComplete() {
        }
    };

    public static boolean isIsMediaFragmentOpened() {
        return isMediaFragmentOpened;
    }

    public static void setIsMediaFragmentOpened(boolean isMediaFragmentOpened) {
        Saved_Collection_Instagram_Activity.isMediaFragmentOpened = isMediaFragmentOpened;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySavedCollectionInstagramBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activity = this;
        utils = new Utils(activity);

        userId = getIntent().getStringExtra("USER_ID");
        storyHighLight = getIntent().getStringExtra("STORY_HIGHLIGHT");

//        setTranslucent()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            insetsController = activity.getWindow().getInsetsController();

        if(storyHighLight == null)
            binding.savedCollectionUserName.setText("saved items");
        else  binding.savedCollectionUserName.setText("story highlights");
        binding.savedCollectionBackButton.setOnClickListener(v -> finish());

        Objects.requireNonNull(getSupportActionBar()).hide();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (savedItemFragment == null) {
            if (storyHighLight == null)
                callPhotosDetailApi(userId);
            else if (reelFeedModel == null)
                callStoryHighLightsApi(userId);
            binding.savedItemActivityProgressBarLayout.setVisibility(View.VISIBLE);
        }
    }

    private void callStoryHighLightsApi(String userId) {
        try {
            if (utils.isNetworkAvailable()) {
                if (GetDataFromServer.getInstance() != null && utils.getCookies() != null)
                    GetDataFromServer.getInstance().getStoryToDownload(allHighlights, "highlight:"+userId, utils.getCookies());
            } else {
                Toast.makeText(this, "NO Internet Connection.", Toast.LENGTH_SHORT).show();
                binding.savedItemActivityProgressBarLayout.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callPhotosDetailApi(String userId) {
        try {
            if (utils.isNetworkAvailable()) {
                if (GetDataFromServer.getInstance() != null && utils.getCookies() != null)
                    GetDataFromServer.getInstance().getPhotoFullDetailFeed(photoDetailObserver, userId, utils.getCookies(), null, "2ce1d673055b99250e93b6f88f878fde");
            } else {
                Toast.makeText(this, "NO Internet Connection.", Toast.LENGTH_SHORT).show();
                binding.savedItemActivityProgressBarLayout.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        int fullscreenFlags = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        setSystemUiFlags(fullscreenFlags, value);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        photosFeedModel = null;
        reelFeedModel = null;
        isMediaFragmentOpened = false;
    }

    @Override
    public void onBackPressed() {
        if (isIsMediaFragmentOpened()) {
            isMediaFragmentOpened = false;
            /*setSystemUiFlags(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN, false);
            setFullscreenFlags(false);*/

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && insetsController != null)
                insetsController.show(WindowInsets.Type.systemBars());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//                getWindow().clearFlags(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS);
            }

            getWindow().getDecorView().setSystemUiVisibility( getWindow().getDecorView().getSystemUiVisibility() & ~(View.SYSTEM_UI_FLAG_FULLSCREEN));
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(activity.getResources().getColor(R.color.purple_700));
        }
        else super.onBackPressed();
        super.onBackPressed();
    }
}

