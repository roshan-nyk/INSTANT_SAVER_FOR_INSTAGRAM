package instant.saver.for_instagram;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import instant.saver.for_instagram.api.GetDataFromServer;
import instant.saver.for_instagram.databinding.ActivityInstagramBinding;
import instant.saver.for_instagram.fragments.FeedbackFragment;
import instant.saver.for_instagram.fragments.LogInFragment;
import instant.saver.for_instagram.fragments.PrivacyFragment;
import instant.saver.for_instagram.model.Edge;
import instant.saver.for_instagram.model.EdgeSidecarToChildren;
import instant.saver.for_instagram.model.Node;
import instant.saver.for_instagram.model.PhotosFeedModel;
import instant.saver.for_instagram.model.ResponseModel;
import instant.saver.for_instagram.model.ShortcodeMedia;
import instant.saver.for_instagram.model.UserInfoForSingleStoryDownload;
import instant.saver.for_instagram.model.album_gallery.AlbumDataViewModel;
import instant.saver.for_instagram.model.album_gallery.Album_Data;
import instant.saver.for_instagram.model.bookmark_profile.SavedProfileViewModel;
import instant.saver.for_instagram.model.bookmark_profile.Saved_Profile;
import instant.saver.for_instagram.model.story.FullDetailModel;
import instant.saver.for_instagram.model.story.ItemModel;
import instant.saver.for_instagram.model.story.ReelFeedModel;
import instant.saver.for_instagram.model.story.StoryModel;
import instant.saver.for_instagram.model.story.TrayModel;
import instant.saver.for_instagram.model.story.User;
import instant.saver.for_instagram.model.story.UserModel;
import instant.saver.for_instagram.util.Utils;
import io.reactivex.observers.DisposableObserver;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class InstagramActivity extends AppCompatActivity implements View.OnClickListener, TextView.OnEditorActionListener {

    private final List<Long> idsOfDownloadingFiles = new ArrayList<>();
    ActivityResultLauncher<Intent> activityResultLauncher;
    String[] permission = {READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE};
    private ClipboardManager clipboardManager;
    private ActivityInstagramBinding binding;
    private InstagramActivity activity;
    private SavedProfileViewModel savedProfileViewModel;
    private AlbumDataViewModel albumDataViewModel;
    private Utils utils;
    private List<Saved_Profile> savedProfiles = null;
    private List<Album_Data> albumData;
    private ArrayList<TrayModel> trayModelArrayList;
    private int[] storiesCardViewModelIndex = new int[2];
    //    required as after permission dialog disappears  onWindowFocusChanged() is called and without this variable and no permission permission dialog will not stop popping
    private boolean isPermissionGrantedToAccessStorage = true;
    private LogInFragment logInFragment;
    private Intent intent;
    private String intentAction, intentType, COOKIES = null, clipBoardUrl = null, storyIdToDownload, MODIFIED_COOKIES = null;
    private String storyHighlightToDownloadPKId;
    private List<Edge> storyHighlightEdgeList = null;
    private int countStoryHighlightEdgeList = 0;


    private final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long idBroadCast = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            idsOfDownloadingFiles.add(idBroadCast);
            //Checking if the received broadcast is for our enqueued download by matching download id
            HashMap<Album_Data, List<Long>> storeDataAfterDownloadCompletion = GetDataFromServer.getInstance().getStoreDataAfterDownloadCompletion();
            for (Map.Entry<Album_Data, List<Long>> entry : storeDataAfterDownloadCompletion.entrySet()) {
                if (entry != null && entry.getValue().contains(idBroadCast)) {
//                    for (long id : idsOfDownloadingFiles){                   java.util.ConcurrentModificationException
//                    applying following method as my list size will be small to medium always
//                    logic for storing data in roomDB in a ordered manner as it is visible on instagram posts
                    for (int i = 0; i < idsOfDownloadingFiles.size(); i++) {
                        long id = idsOfDownloadingFiles.get(i);
                        if (entry.getValue().contains(id)) {
                            Objects.requireNonNull(storeDataAfterDownloadCompletion.get(entry.getKey())).remove(id);
                            idsOfDownloadingFiles.remove(id);
                            i--;
                        }
                    }
                    if (Objects.requireNonNull(storeDataAfterDownloadCompletion.get(entry.getKey())).size() == 0) {
                        albumDataViewModel.insert(entry.getKey());
                        storeDataAfterDownloadCompletion.remove(entry.getKey());
                        Toast.makeText(context, "Download Completed.", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
        }
    };
    private final DisposableObserver<StoryModel> storyObserver = new DisposableObserver<StoryModel>() {

        @Override
        public void onNext(@NotNull StoryModel response) {
                trayModelArrayList = response.getTray();
                Log.d("TAG", "onNext: storyObserver size:-  "+ trayModelArrayList.size()+"\n"+activity);
                if (activity != null) {
                    try {
                        int count = 0 ;
                        for(int i = 0; i < trayModelArrayList.size(); i++){
                            UserModel userModel = trayModelArrayList.get(i).getUser();
                            if(userModel != null) {
                                if(count ==  0){
                                    storiesCardViewModelIndex[0] = i;
                                    Glide.with(activity).load(userModel.getProfile_pic_url()).error(R.drawable.no_image_available1).into(binding.storyIcon1);
                                    binding.realName1.setText(userModel.getFull_name());
                                    binding.storiesCardview.setVisibility(View.VISIBLE);
                                    binding.storyIcon1.setVisibility(View.VISIBLE);
                                    binding.realName1.setVisibility(View.VISIBLE);
                                    count++;
                                }
                                else if(count == 1){
                                    storiesCardViewModelIndex[1] = i;
                                    Glide.with(activity).load(trayModelArrayList.get(2).getUser().getProfile_pic_url()).error(R.drawable.no_image_available1).into(binding.storyIcon2);
                                    binding.realName2.setText(trayModelArrayList.get(2).getUser().getFull_name());
                                    binding.storyIcon2.setVisibility(View.VISIBLE);
                                    binding.realName2.setVisibility(View.VISIBLE);
                                    count++;
                                }
                                else if(count == 2){
                                    Glide.with(activity).load(R.drawable.ic_baseline_more_vert_24).error(R.drawable.no_image_available1).into(binding.storyIcon3);
                                    binding.realName3.setText("See All");
                                    binding.storyIcon3.setVisibility(View.VISIBLE);
                                    binding.realName3.setVisibility(View.VISIBLE);
                                    count++;
                                    break;
                                }
                            }
                        }
                        if(count == 0) binding.storiesCardview.setVisibility(View.INVISIBLE);
                        else if(count == 1){
                            binding.storyIcon2.setVisibility(View.GONE);
                            binding.realName2.setVisibility(View.GONE);
                        }
                        else if(count == 2){
                            binding.storyIcon3.setVisibility(View.GONE);
                            binding.realName3.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        Log.d("TAG", "onNext try/catch storyObserver:-" + e.getLocalizedMessage()+"\n"+e.getCause()+"\n"+e.getMessage());
                    }
                    GetDataFromServer.getInstance().getPhotoFullDetailFeed(photoDetailObserver, utils.getUserId(), COOKIES, null, "2ce1d673055b99250e93b6f88f878fde");
                }
                dispose();
        }

        @Override
        public void onError(@org.jetbrains.annotations.NotNull Throwable throwable) {
            Log.d("TAG", "storyObserver Error: " + throwable.getLocalizedMessage());
            if (activity != null)
                Toast.makeText(activity, "Error TimeOut. Frequent Access Detected.\nTry Again after 30-40 mins.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onComplete() {
        }
    };
    private final DisposableObserver<ResponseModel> addToSaveProfile = new DisposableObserver<ResponseModel>() {
        @Override
        public void onNext(@NotNull ResponseModel response) {
            try {
                if (utils.isNetworkAvailable()) {
                    String userId = response.getGraphql().getUser().getId();
                    String userName = response.getGraphql().getUser().getUsername();
                    String profilePicUrl = response.getGraphql().getUser().getProfile_pic_url();
                    Saved_Profile savedProfile = new Saved_Profile(userId, userName, profilePicUrl);
                    savedProfileViewModel.insert(savedProfile);
                    binding.bookmarkEditText.setText("");
                    utils.setClipBoardClip(clipBoardUrl);
                    Snackbar.make(activity, binding.edittextCardview, "BOOK MARKED THIS PROFILE", Snackbar.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activity, "No Network Connection.....Try Again", Toast.LENGTH_LONG).show();
                    clipBoardUrl = null;
                }
                binding.ProgressBarConstraintLayout.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("TAG", "onNext try/catch addToSaveProfile:-" + e.getLocalizedMessage()+"\n"+e.getCause()+"\n"+e.getMessage());
            }
        }

        @Override
        public void onError(@org.jetbrains.annotations.NotNull Throwable throwable) {
            clipBoardUrl = null;
            Log.d("TAG", "addToSaveFile Error: " + throwable.getLocalizedMessage());
            binding.ProgressBarConstraintLayout.setVisibility(View.GONE);
            if (utils.isNetworkAvailable()) {
                if (utils.getCookies() != null)
                    Toast.makeText(activity, "Unable To Access Instagram.\nCheck Your Connection And Try Again Later", Toast.LENGTH_LONG).show();
                else
                    callLogINFragment();
            } else
                Toast.makeText(activity, "No Network Connection.....Try Again", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onComplete() {
        }
    };
    private final DisposableObserver<FullDetailModel> singleStoryToDownload = new DisposableObserver<FullDetailModel>() {
        @Override
        public void onNext(@NotNull FullDetailModel allStories) {
            try {
                Log.d("TAG", "onNext: " + allStories.getReels_media().get(0).getUser().getUsername());
                for (ItemModel singleItem : allStories.getReels_media().get(0).getItems()) {
//                    Log.d("TAG", "onNext: " + storyIdToDownload + "    " + singleItem.getPk());
                    if (storyIdToDownload.equals((String.valueOf(singleItem.getPk())))) {
                        if (utils.isNetworkAvailable()) {
                            ArrayList<String> userDetails = new ArrayList<>();
                            userDetails.add(allStories.getReels_media().get(0).getUser().getUsername());
                            userDetails.add(allStories.getReels_media().get(0).getUser().getProfile_pic_url());
                            userDetails.add(null);
                            userDetails.add(null);
                            userDetails.add(null);
                            ArrayList<Boolean> isVideo = new ArrayList<>();
                            ArrayList<String> stringsUrlsToDownload = new ArrayList<>();
                            if (singleItem.getMedia_type() == 2) {
                                stringsUrlsToDownload.add(singleItem.getVideo_versions().get(0).getUrl());
                                isVideo.add(true);
                            } else {
                                stringsUrlsToDownload.add(singleItem.getImage_versions2().getCandidates().get(0).getUrl());
                                isVideo.add(false);
                            }
                            utils.checkAvailableExternalStorage(stringsUrlsToDownload, userDetails, albumData, isVideo);
                        } else {
                            Toast.makeText(activity, "No Network Connection.....Try Again", Toast.LENGTH_LONG).show();
                            clipBoardUrl = null;
                            binding.ProgressBarConstraintLayout.setVisibility(View.GONE);
                        }
                        binding.editText.setText("");
                        utils.setClipBoardClip(clipBoardUrl);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(@NotNull Throwable e) {
            binding.ProgressBarConstraintLayout.setVisibility(View.GONE);
            clipBoardUrl = null;
            Log.d("TAG", "SingleStoryToDownload onError: " + e.toString());
            if (utils.isNetworkAvailable()) {
                if (utils.getCookies() != null)
                    Toast.makeText(activity, "Unable To Download.\nRecheck URL And Try Again", Toast.LENGTH_LONG).show();
                else
                    callLogINFragment();
            } else
                Toast.makeText(activity, "No Network Connection.....Try Again", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onComplete() {
        }
    };
    private final DisposableObserver<UserInfoForSingleStoryDownload> userInfoForSingleStoryDownload = new DisposableObserver<UserInfoForSingleStoryDownload>() {
        @Override
        public void onNext(@NotNull UserInfoForSingleStoryDownload userInfoForSingleStoryDownload) {
            GetDataFromServer.getInstance().getStoryToDownload(singleStoryToDownload, userInfoForSingleStoryDownload.getUser().getId(), utils.getCookies());
        }

        @Override
        public void onError(@NotNull Throwable e) {
            binding.ProgressBarConstraintLayout.setVisibility(View.GONE);
            clipBoardUrl = null;
            e.printStackTrace();
            Log.d("TAG", " userInfoForSingleStoryDownload onError: " + e.toString());
            if (!utils.isNetworkAvailable())
                Toast.makeText(activity, "No Network Connection.....Try Again", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onComplete() {
        }
    };
    private final DisposableObserver<ResponseModel> instaObserver = new DisposableObserver<ResponseModel>() {
        @Override
        public void onNext(@NotNull ResponseModel response) {
            try {
//                Type listType = new TypeToken<ResponseModel>() { }.getType();
//                ResponseModel responseModel = new Gson().fromJson(versionList.toString(), listType);

                ArrayList<String> userDetails = new ArrayList<>();
                ArrayList<Boolean> isVideo = new ArrayList<>();
                ArrayList<String> stringsUrlsToDownload = new ArrayList<>();
                if (response.getGraphql().getShortcode_media() != null) {
                    ShortcodeMedia shortcodeMedia = response.getGraphql().getShortcode_media();
                    String mediaCaption = null, productType;
                    if (shortcodeMedia.getEdgeMediaToCaption().getEdges().size() > 0)
                        mediaCaption = shortcodeMedia.getEdgeMediaToCaption().getEdges().get(0).getNode().getText();
                    if ("igtv".equals(shortcodeMedia.getProduct_type()))
                        productType = "igtv";
//                    for reel even though product type is "clips" but the url to access both posts and reel has /p/ in it
                    else productType = "p";

                    userDetails.add(shortcodeMedia.getOwner().getUsername());
                    userDetails.add(shortcodeMedia.getOwner().getProfile_pic_url());
                    userDetails.add(shortcodeMedia.getShortcode());
                    userDetails.add(mediaCaption);
                    userDetails.add(productType);

                    EdgeSidecarToChildren edgeSidecarToChildren = shortcodeMedia.getEdge_sidecar_to_children();

                    if (utils.isNetworkAvailable()) {
                        if (edgeSidecarToChildren != null) {
                            List<Edge> edgeArrayList = edgeSidecarToChildren.getEdges();
                            for (int i = 0; i < edgeArrayList.size(); i++) {
                                Node node = edgeArrayList.get(i).getNode();
                                if (node.isIs_video())
                                    stringsUrlsToDownload.add(node.getVideo_url());
                                else
                                    stringsUrlsToDownload.add(node.getDisplay_resources().get(node.getDisplay_resources().size() - 1).getSrc());
                            }
                            if (!utils.checkForAlreadyExistedFile(stringsUrlsToDownload, "", albumData)) {
                                for (int i = 0; i < edgeArrayList.size(); i++)
                                    isVideo.add(edgeArrayList.get(i).getNode().isIs_video());
                                utils.checkAvailableExternalStorage(stringsUrlsToDownload, userDetails, albumData, isVideo);
                            } else {
                                Toast.makeText(activity, "File Already Exist", Toast.LENGTH_LONG).show();
                                binding.ProgressBarConstraintLayout.setVisibility(View.GONE);
                            }
                        } else {
                            isVideo.add(shortcodeMedia.isIs_video());
                            if (isVideo.get(0))
                                stringsUrlsToDownload.add(shortcodeMedia.getVideo_url());
                            else
                                stringsUrlsToDownload.add(shortcodeMedia.getDisplay_resources().get(shortcodeMedia.getDisplay_resources().size() - 1).getSrc());
                            utils.checkAvailableExternalStorage(stringsUrlsToDownload, userDetails, albumData, isVideo);
                        }
                        binding.editText.setText("");
                        utils.setClipBoardClip(clipBoardUrl);
                    } else {
                        Toast.makeText(activity, "No Network Connection.....Try Again", Toast.LENGTH_LONG).show();
                        clipBoardUrl = null;
                        binding.ProgressBarConstraintLayout.setVisibility(View.GONE);
                    }
                }
//                condition for userProfile url pic to download
                else if (response.getGraphql().getUser() != null) {
                    User user = response.getGraphql().getUser();
                    userDetails.add(user.getUsername());
                    userDetails.add(user.getProfile_pic_url_hd());
                    userDetails.add(null);
                    userDetails.add(user.getFull_name());
                    userDetails.add(null);
                    stringsUrlsToDownload.add(user.getProfile_pic_url_hd());
                    isVideo.add(false);
                    if (utils.isNetworkAvailable()) {
                        utils.checkAvailableExternalStorage(stringsUrlsToDownload, userDetails, albumData, isVideo);
                        binding.editText.setText("");
                        utils.setClipBoardClip(clipBoardUrl);
                    } else {
                        Toast.makeText(activity, "No Network Connection.....Try Again", Toast.LENGTH_LONG).show();
                        clipBoardUrl = null;
                        binding.ProgressBarConstraintLayout.setVisibility(View.GONE);
                    }
                }

                else{
                    Log.d("TAG", "onNext else condition instaObserver:-" + response +"\n"+ response.getGraphql().getShortcode_media() + "\n"+response.getGraphql().getUser());
                    binding.ProgressBarConstraintLayout.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("TAG", "onNext try/catch instaObserver:-" + response + "\n"+ e.getLocalizedMessage()+"\n"+e.getCause()+"\n"+e.getMessage());
                binding.ProgressBarConstraintLayout.setVisibility(View.GONE);
            }
        }

        @Override
        public void onError(Throwable e) {
            binding.ProgressBarConstraintLayout.setVisibility(View.GONE);
            clipBoardUrl = null;
            e.printStackTrace();
            Log.d("TAG", "instaObserver onError: " + e +"\n"+e.getCause()+"\n"+e.getLocalizedMessage());
            if (utils.isNetworkAvailable()) {
                if (utils.getCookies() != null)
                    Toast.makeText(activity, "Unable To Download.\nRecheck URL And Try Again", Toast.LENGTH_LONG).show();
                else
                    callLogINFragment();
            } else
                Toast.makeText(activity, "No Network Connection.....Try Again", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onComplete() {
        }
    };
    private final DisposableObserver<PhotosFeedModel> photoDetailObserver = new DisposableObserver<PhotosFeedModel>() {

        @Override
        public void onNext(@NotNull PhotosFeedModel response) {
            try {
                ArrayList<Edge> edgeArrayList = (ArrayList<Edge>) response.getData().getUser().getEdgeSavedMedia().getEdgeList();
                if (edgeArrayList.size() > 0 && activity != null) {
                    binding.SavedItemsCardview.setVisibility(View.VISIBLE);
                    binding.savedItemCardView1.setVisibility(View.VISIBLE);
                    Glide.with(activity).load(edgeArrayList.get(0).getNode().getDisplay_url()).error(R.drawable.no_image_available1).placeholder(R.drawable.ic_baseline_photo_24).centerCrop().into(binding.savedItemCardViewImageview1);
                    if (edgeArrayList.size() > 1) {
                        binding.savedItemCardView2.setVisibility(View.VISIBLE);
                        Glide.with(activity).load(edgeArrayList.get(1).getNode().getDisplay_url()).error(R.drawable.no_image_available1).placeholder(R.drawable.ic_baseline_photo_24).centerCrop().into(binding.savedItemCardViewImageview2);
                    } else
                        binding.savedItemCardView2.setVisibility(View.GONE);
                    if (edgeArrayList.size() > 2) {
                        binding.savedItemCardView3.setVisibility(View.VISIBLE);
                        Glide.with(activity).load(edgeArrayList.get(2).getNode().getDisplay_url()).error(R.drawable.no_image_available1).placeholder(R.drawable.ic_baseline_photo_24).centerCrop().into(binding.savedItemCardViewImageview3);
                    } else
                        binding.savedItemCardView3.setVisibility(View.GONE);
                } else
                    binding.SavedItemsCardview.setVisibility(View.INVISIBLE);
                dispose();
            } catch (Exception e) {
                Log.d("TAG", "onNext catch photoObserver:-" + e.getLocalizedMessage());
            }
        }

        @Override
        public void onError(@NotNull Throwable throwable) {
            Log.e("TAG", "onError: photoObserver:-" + throwable.getCause() + "        " + throwable.getMessage() + "      " + throwable.getLocalizedMessage());
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
                ReelFeedModel reelFeedModel = allStories.getReels_media().get(0);
                Log.d("TAG", "onNext: " + reelFeedModel.getUser().getUsername());
                if (reelFeedModel.getMedia_ids().stream().anyMatch(s -> storyHighlightToDownloadPKId.equals(s))) {
                    ArrayList<String> userDetails = new ArrayList<>();
                    ArrayList<Boolean> isVideo = new ArrayList<>();
                    ArrayList<String> stringsUrlsToDownload = new ArrayList<>();
                    userDetails.add(reelFeedModel.getUser().getUsername());
                    userDetails.add(reelFeedModel.getUser().getProfile_pic_url());

                    for (ItemModel itemModel : reelFeedModel.getItems()) {
                        if (String.valueOf(itemModel.getPk()).equals(storyHighlightToDownloadPKId)) {
                            userDetails.add(null);
                            userDetails.add(null);
                            userDetails.add(null);
                            if (itemModel.getMedia_type() == 2) {
                                isVideo.add(true);
                                stringsUrlsToDownload.add(itemModel.getVideo_versions().get(0).getUrl());
                            } else {
                                isVideo.add(false);
                                stringsUrlsToDownload.add(itemModel.getImage_versions2().getCandidates().get(0).getUrl());
                            }
                            if (utils.isNetworkAvailable()) {
                                utils.checkAvailableExternalStorage(stringsUrlsToDownload, userDetails, albumData, isVideo);
                                binding.ProgressBarConstraintLayout.setVisibility(View.GONE);
                                binding.editText.setText("");
                                utils.setClipBoardClip(clipBoardUrl);
                            } else {
                                Toast.makeText(activity, "No Network Connection.....Try Again", Toast.LENGTH_LONG).show();
                                clipBoardUrl = null;
                                binding.ProgressBarConstraintLayout.setVisibility(View.GONE);
                            }
                            storyHighlightEdgeList = null;
                            countStoryHighlightEdgeList = 0;
                            break;
                        }
                    }
                } else
                    callStoryHighlightTODownload(++countStoryHighlightEdgeList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(@NotNull Throwable e) {
            Toast.makeText(activity, "Unable To Download.\nCheck Your Network And Url", Toast.LENGTH_LONG).show();
            binding.ProgressBarConstraintLayout.setVisibility(View.GONE);
            clipBoardUrl = null;
        }

        @Override
        public void onComplete() {
        }
    };
    private final DisposableObserver<PhotosFeedModel> storyHighlightObserver = new DisposableObserver<PhotosFeedModel>() {
        @Override
        public void onNext(@NotNull PhotosFeedModel response) {
            try {
                storyHighlightEdgeList = response.getData().getUser().getEdgeHighlightReels().getEdgeList();
                callStoryHighlightTODownload(countStoryHighlightEdgeList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(@NotNull Throwable throwable) {
            Log.e("TAG", "storyHighlightObserver onError: " + throwable.getCause() + "        " + throwable.getMessage() + "      " + throwable.getLocalizedMessage());
            binding.ProgressBarConstraintLayout.setVisibility(View.GONE);
            clipBoardUrl = null;
            if (utils.isNetworkAvailable()) {
                if (utils.getCookies() != null)
                    Toast.makeText(activity, "Unable To Download.\nRecheck URL And Try Again", Toast.LENGTH_LONG).show();
                else
                    callLogINFragment();
            } else
                Toast.makeText(activity, "No Network Connection.....Try Again", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onComplete() {
        }
    };

    //after download starts
    public void hideProgressBar() {
        binding.ProgressBarConstraintLayout.setVisibility(View.GONE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d("TAG", "onWindowFocusChanged: " + hasFocus);
        if (getSupportFragmentManager().getBackStackEntryCount() == 0 && hasFocus) {
            if (binding.ProgressBarConstraintLayout.getVisibility() == View.GONE && isPermissionGrantedToAccessStorage) {
                if (Intent.ACTION_SEND.equals(intentAction) && "text/plain".equals(intentType)) {
                    String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                    if (binding.bookmarkAdduserConstraintLayout.getVisibility() == View.VISIBLE) {
                        binding.bookmarkEditText.setText(sharedText);
                        onClick(binding.bookMarkUserAddButton);
                    } else {
                        binding.editText.setText(sharedText);
                        onClick(binding.downloadButton);
                    }
                    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, sharedText));
                    intentType = null;
                    intentAction = null;
                } else if (binding.bookmarkAdduserConstraintLayout.getVisibility() == View.VISIBLE)
                    onClick(binding.bookMarkUserAddButton);
                else onClick(binding.pasteButton);
            }
        }
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        binding = ActivityInstagramBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activity = this;
        utils = new Utils(activity);

        savedProfileViewModel = new ViewModelProvider.AndroidViewModelFactory(activity.getApplication()).create(SavedProfileViewModel.class);

        albumDataViewModel = new ViewModelProvider.AndroidViewModelFactory(activity.getApplication()).create(AlbumDataViewModel.class);

        savedProfileViewModel.getAllContacts().observe(activity, saved_profiles -> {
            savedProfiles = saved_profiles;
            if (savedProfiles != null)
                updateBookMarkedUsers(savedProfiles);
        });

        albumDataViewModel.getAllMedias().observe(activity, album_data -> {
            albumData = album_data;
        });

        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        //        not required for version 10 above
      /*  activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });*/

        logInFragment = LogInFragment.newInstance("InstagramActivity");

        Objects.requireNonNull(getSupportActionBar()).setTitle("");

        binding.downloadButton.setOnClickListener(this);
        binding.pasteButton.setOnClickListener(this);
        binding.storyIcon1.setOnClickListener(this);
        binding.storyIcon2.setOnClickListener(this);
        binding.storyIcon3.setOnClickListener(this);
        binding.addButton.setOnClickListener(this);
        binding.bookMarkUserAddButton.setOnClickListener(this);
        binding.bookMarkUserCancelButton.setOnClickListener(this);
        binding.bookmarkEditText.setOnEditorActionListener(this);
        binding.editText.setOnEditorActionListener(this);
        binding.bookmarkIcon1.setOnClickListener(this);
        binding.bookmarkIcon2.setOnClickListener(this);
        binding.bookmarkIcon3.setOnClickListener(this);
        binding.bookmarkIcon4.setOnClickListener(this);
        binding.savedItemsConstraintLayout.setOnClickListener(this);
        binding.ProgressBarConstraintLayout.setOnClickListener(this);

//        If user open this app using ShareTo option
        intent = getIntent();
        intentAction = intent.getAction();
        intentType = intent.getType();


//        Log.d("TAG", "onCreate: "+COOKIES);

        if (!utils.isUserInstructionActivityShown()) {
            startActivity(new Intent(this, UserInstructionActivity.class));
            utils.setUserInstructionActivityShown(true);
        }

        if (utils.getCookies() != null) {
            String[] temp = utils.getCookies().split(" ");
            MODIFIED_COOKIES = temp[2] + " " + temp[0] + " " + temp[1] + " " + temp[3] ;
            MODIFIED_COOKIES = MODIFIED_COOKIES.substring(0,MODIFIED_COOKIES.length()-1);
//            System.out.println("--------------------------------------------------\n" + MODIFIED_COOKIES + "\n--------------------------------------------------------------------------------");
            COOKIES = utils.getCookies();
            if(utils.isNetworkAvailable())
                GetDataFromServer.getInstance().getStories(storyObserver, COOKIES);
        } else {
//            COOKIES = utils.getTempCookies();
            MODIFIED_COOKIES = utils.getTempCookies();
            binding.storiesCardview.setVisibility(View.INVISIBLE);
            binding.SavedItemsCardview.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_statubar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (binding.ProgressBarConstraintLayout.getVisibility() == View.GONE && binding.LogOUTConstraintLayout.getVisibility() == View.GONE) {
            if (item.getItemId() == R.id.actionbar_instagram_icon) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
                if (launchIntent != null)
                    startActivity(launchIntent);
//            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.instagram.com/")).setPackage("com.instagram.android"));
                else
                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=" + "com.instagram.android")));
//   or i can write startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.instagram.com/)));  to open in an browser
            } else if (item.getItemId() == R.id.actionbar_album_icon)
                startActivity(new Intent(activity, GalleryActivity.class));
            else if (item.getItemId() == R.id.actionbar_tutorial_icon)
                startActivity(new Intent(activity, UserInstructionActivity.class));
            else if (item.getItemId() == R.id.actionbar_login_icon) {
                if (utils.getCookies() == null) callLogINFragment();
                else {
                    binding.LogOUTConstraintLayout.setVisibility(View.VISIBLE);
                    binding.LogOUTConstraintLayout.setOnClickListener(this);
                    binding.loggedOptionYesButton.setOnClickListener(this);
                    binding.loggedOptionNoButton.setOnClickListener(this);
                }
            } else if (item.getItemId() == R.id.actionbar_privacy_icon || item.getItemId() == R.id.actionbar_feedback_icon) {
                binding.mainScrollView.setVisibility(View.GONE);
                Objects.requireNonNull(getSupportActionBar()).hide();
                if (item.getItemId() == R.id.actionbar_feedback_icon)
                    getSupportFragmentManager().beginTransaction().add(R.id.mainScrollView_constraint_Layout, FeedbackFragment.newInstance())
                        .addToBackStack(null)
                        .commit();
                else getSupportFragmentManager().beginTransaction().add(R.id.mainScrollView_constraint_Layout, PrivacyFragment.newInstance())
                            .addToBackStack(null)
                            .commit();
            }else if(item.getItemId() == R.id.actionbar_share_option){
                Intent sendIntent = new Intent();
                Intent chooser = Intent.createChooser(sendIntent, "Share Link Using");
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Best story and image downloader for Instagram!\nCheck this out!\n"+"https://play.google.com/store/apps/details?id=instant.saver.for_instagram");
                sendIntent.setType("text/plain");
                startActivity(chooser);
            }
        }
        return (super.onOptionsItemSelected(item));
    }

    @Override
    protected void onResume() {
        super.onResume();
       /* ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#3CE3E3E3"));          #6FBCBCBC       #68CFCFCF
        getSupportActionBar().setBackgroundDrawable(colorDrawable);*/

        if(utils.getCookies() == null)
//            COOKIES = utils.getTempCookies();
              MODIFIED_COOKIES = utils.getTempCookies();
//      check stories onResume dosn't need to destory or Restart application
        if (utils.getCookies() != null) {
            String[] temp = utils.getCookies().split(" ");
            MODIFIED_COOKIES = temp[2] + " " + temp[0] + " " + temp[1] + " " + temp[3] ;
            MODIFIED_COOKIES = MODIFIED_COOKIES.substring(0,MODIFIED_COOKIES.length()-1);
//            System.out.println("--------------------------------------------------\n" + MODIFIED_COOKIES + "\n--------------------------------------------------------------------------------");
            COOKIES = utils.getCookies();
            if(utils.isNetworkAvailable())
                GetDataFromServer.getInstance().getStories(storyObserver, COOKIES);
        } else {
//            COOKIES = utils.getTempCookies();
            MODIFIED_COOKIES = utils.getTempCookies();
            binding.storiesCardview.setVisibility(View.INVISIBLE);
            binding.SavedItemsCardview.setVisibility(View.INVISIBLE);
        }
        /*  GradientDrawable gradientDrawable = new GradientDrawable(
                    GradientDrawable.Orientation.BL_TR,
                    new int[]{ Color.parseColor("#F58529"),
                            Color.parseColor("#FEDA77"),
                            Color.parseColor("#DD2A7B"),
                            Color.parseColor("#8134AF"),
                            Color.parseColor("#515BD4")
                    });
            binding.bookmarkIcon3.setBackground(gradientDrawable);*/

        /*   File[] externalStorageVolumes =
                ContextCompat.getExternalFilesDirs(activity.getApplicationContext(), null);
        for (File str : externalStorageVolumes) {
            Log.d("TAG", "onResume: " + str.getAbsolutePath());
            Log.d("TAG", "onResume: " + new StatFs(str.getAbsolutePath()).getAvailableBytes() / 1000000000);
        }

        File[] externalStorageVolumes1 =
                activity.getExternalMediaDirs();
        for (File str : externalStorageVolumes1) {
            Log.d("TAG", "onResume: " + str.getAbsolutePath());
            Log.d("TAG", "onResume: " + new StatFs(str.getAbsolutePath()).getAvailableBytes() / 1000000000);
        }

        Log.d("TAG", "onResume: " + Environment.getExternalStorageDirectory().getAbsolutePath());
        Log.d("TAG", "onResume: " + new StatFs(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getAbsolutePath()).getAvailableBytes());

        Log.d("TAG", "onResume: " + activity.getBaseContext().getFilesDir() + "    " + Environment.getDataDirectory().getAbsolutePath());*/
    }

    private void updateBookMarkedUsers(List<Saved_Profile> roomProfile) {
        if (roomProfile.size() == 0) {
            binding.bookmarkIcon1.setVisibility(View.GONE);
            binding.bookmarkUserName1.setVisibility(View.GONE);
        } else {
            Glide.with(activity).load(roomProfile.get(roomProfile.size() - 1).getProfile_pic_url()).into(binding.bookmarkIcon1);
            binding.bookmarkUserName1.setText(roomProfile.get(roomProfile.size() - 1).getName());
            binding.bookmarkIcon1.setVisibility(View.VISIBLE);
            binding.bookmarkUserName1.setVisibility(View.VISIBLE);
        }
        if (roomProfile.size() > 1) {
            Glide.with(activity).load(roomProfile.get(roomProfile.size() - 2).getProfile_pic_url()).into(binding.bookmarkIcon2);
            binding.bookmarkUserName2.setText(roomProfile.get(roomProfile.size() - 2).getName());
            binding.bookmarkIcon2.setVisibility(View.VISIBLE);
            binding.bookmarkUserName2.setVisibility(View.VISIBLE);
        } else {
            binding.bookmarkIcon2.setVisibility(View.GONE);
            binding.bookmarkUserName2.setVisibility(View.GONE);
        }
        if (roomProfile.size() > 2) {
            Glide.with(activity).load(roomProfile.get(roomProfile.size() - 3).getProfile_pic_url()).into(binding.bookmarkIcon3);
            binding.bookmarkUserName3.setText(roomProfile.get(roomProfile.size() - 3).getName());
            binding.bookmarkIcon3.setVisibility(View.VISIBLE);
            binding.bookmarkUserName3.setVisibility(View.VISIBLE);
        } else {
            binding.bookmarkIcon3.setVisibility(View.GONE);
            binding.bookmarkUserName3.setVisibility(View.GONE);
        }
        Glide.with(activity).load(R.drawable.ic_baseline_more_vert_24).into(binding.bookmarkIcon4);
        Glide.with(activity).load(R.drawable.ic_baseline_add_24).into(binding.addButton);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.downloadButton) {
            clipBoardUrl = binding.editText.getText().toString();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (!utils.checkPermission())
                    requestPermission();
                else if (utils.isExternalStorageWritable())
                    callDownload(clipBoardUrl);
                else
                    Toast.makeText(activity, "Don't have write access to Storage.", Toast.LENGTH_LONG).show();
            } else
                callDownload(clipBoardUrl);
            utils.hideSoftKeyboard(v);
        }
        else if (v == binding.storyIcon1 || v == binding.storyIcon2) {
            Intent intent = new Intent(activity, SingleProfileActivity.class);
            int index;
            if (v == binding.storyIcon1)  index = storiesCardViewModelIndex[0];
            else index = storiesCardViewModelIndex[1];
            intent.putExtra("UserId", String.valueOf(trayModelArrayList.get(index).getUser().getPk()));
            intent.putExtra("UserName", trayModelArrayList.get(index).getUser().getUsername());
            startActivity(intent);
        } else if (v == binding.storyIcon3) {
            Intent intent = new Intent(activity, AllUsersActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("REEL_USERS", trayModelArrayList);
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (v == binding.addButton) {
            binding.userProfilesConstraintLayout.setVisibility(View.GONE);
            binding.bookmarkAdduserConstraintLayout.setVisibility(View.VISIBLE);
        } else if (v == binding.bookMarkUserCancelButton) {
            binding.bookmarkAdduserConstraintLayout.setVisibility(View.GONE);
            binding.userProfilesConstraintLayout.setVisibility(View.VISIBLE);
            binding.bookmarkEditText.setText("");
            utils.hideSoftKeyboard(binding.bookMarkUserCancelButton);
        } else if (v == binding.bookMarkUserAddButton) {
            String url = binding.bookmarkEditText.getText().toString();
            if (!TextUtils.isEmpty(url)) {
                if (utils.getCookies() == null) {
                    Toast.makeText(activity, "You Need to LogIn to your Instagram Account.", Toast.LENGTH_SHORT).show();
                    binding.bookmarkEditText.setText("");
                    onClick(binding.bookMarkUserCancelButton);
                } else bookMarkEditTextMethod(url);
            } else if (clipboardManager.getPrimaryClip() != null && clipboardManager.getPrimaryClip().getItemCount() > 0) {
                String str = clipboardManager.getPrimaryClip().getItemAt(0).getText().toString();
                binding.bookmarkEditText.setText(str);
                bookMarkEditTextMethod(str);
            }
            utils.hideSoftKeyboard(binding.bookMarkUserAddButton);
        } else if (v == binding.bookmarkIcon1 || v == binding.bookmarkIcon2 || v == binding.bookmarkIcon3) {
            if (utils.getCookies() != null) {
                Intent intent = new Intent(activity, SingleProfileActivity.class);
                if (v == binding.bookmarkIcon1) {
                    intent.putExtra("UserId", savedProfiles.get(savedProfiles.size() - 1).getUserId());
                    intent.putExtra("UserName", savedProfiles.get(savedProfiles.size() - 1).getName());
                    intent.putExtra("Saved_Profile_Position", 0);
                    intent.putExtra("Saved_Profile_Profile_URL", savedProfiles.get(savedProfiles.size() - 1).getProfile_pic_url());
                } else if (v == binding.bookmarkIcon2) {
                    intent.putExtra("UserId", savedProfiles.get(savedProfiles.size() - 2).getUserId());
                    intent.putExtra("UserName", savedProfiles.get(savedProfiles.size() - 2).getName());
                    intent.putExtra("Saved_Profile_Position", 1);
                    intent.putExtra("Saved_Profile_Profile_URL", savedProfiles.get(savedProfiles.size() - 2).getProfile_pic_url());
                } else {
                    intent.putExtra("UserId", savedProfiles.get(savedProfiles.size() - 3).getUserId());
                    intent.putExtra("UserName", savedProfiles.get(savedProfiles.size() - 3).getName());
                    intent.putExtra("Saved_Profile_Position", 2);
                    intent.putExtra("Saved_Profile_Profile_URL", savedProfiles.get(savedProfiles.size() - 3).getProfile_pic_url());
                }
                startActivity(intent);
            } else callLogINFragment();
        } else if (v == binding.bookmarkIcon4) {
            Intent intent = new Intent(activity, AllUsersActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("BOOK_MARKED_USERS", "book_marked");
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (v == binding.ProgressBarConstraintLayout)
            Log.d("TAG", "onClick: do nothing");
        else if (v == binding.pasteButton) {
            if (clipboardManager.getPrimaryClip() != null && clipboardManager.getPrimaryClip().getItemCount() > 0) {
                String str = clipboardManager.getPrimaryClip().getItemAt(0).getText().toString();
                binding.editText.setText(str);
                if (!str.equals(utils.getClipBoardClip())) {
                    clipBoardUrl = str;
                    onClick(binding.downloadButton);
                }
            }
        } else if (v == binding.savedItemsConstraintLayout) {
            Intent intent = new Intent(activity, Saved_Collection_Instagram_Activity.class);
            intent.putExtra("USER_ID", utils.getUserId());
            startActivity(intent);
        } else if (v == binding.LogOUTConstraintLayout || v == binding.loggedOptionNoButton || v == binding.loggedOptionYesButton) {
            binding.LogOUTConstraintLayout.setVisibility(View.GONE);
            if (v == binding.loggedOptionYesButton) {
                utils.setCookies(null);
                CookieManager.getInstance().removeAllCookies(value -> onResume());
            }
        }
    }

    private void bookMarkEditTextMethod(String url) {
        if (!TextUtils.isEmpty(url)) {
            try {
                URI uri = new URI(url);
                String temp = checkCorrectUserUrl(uri);
                Log.d("TAG", "bookMarkEditTextMethod: " + temp);
                if (temp == null)
                    Snackbar.make(activity, binding.edittextCardview, "User Already Added To The List", Snackbar.LENGTH_LONG).show();
                else if (temp.equals("invalid"))
                    Toast.makeText(activity, "Enter a valid URL", Toast.LENGTH_SHORT).show();
                else {
                    clipBoardUrl = url;
                    addUserToSaveProfile(temp);
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
                Toast.makeText(activity, "Enter a valid URL", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(activity, "Enter a valid URL", Toast.LENGTH_SHORT).show();
            binding.bookmarkEditText.setText("");
        }
    }

    private void addUserToSaveProfile(String url) {
        try {
            if (utils.isNetworkAvailable()) {
                if (GetDataFromServer.getInstance() != null) {
//                    GetDataFromServer.getInstance().addUserSaveProfile(addToSaveProfile, url, COOKIES);
                    GetDataFromServer.getInstance().addUserSaveProfile(addToSaveProfile, url, MODIFIED_COOKIES);
                    binding.ProgressBarConstraintLayout.setVisibility(View.VISIBLE);
                } else {
                    callLogINFragment();
                    onClick(binding.bookMarkUserCancelButton);
                }
            } else {
                Toast.makeText(this, "NO Internet Connection.", Toast.LENGTH_SHORT).show();
                clipBoardUrl = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            clipBoardUrl = null;
        }
    }

    private void callLogINFragment() {
        binding.mainScrollView.setVisibility(View.GONE);
        Objects.requireNonNull(getSupportActionBar()).hide();
        getSupportFragmentManager().beginTransaction().add(R.id.mainScrollView_constraint_Layout, logInFragment).addToBackStack(null).commit();
        if (clipBoardUrl != null)
            utils.setClipBoardClip(clipBoardUrl);
    }

    private String checkCorrectUserUrl(URI uri) throws URISyntaxException {
        if ("https".equals(uri.getScheme()) && "instagram.com".equals(uri.getAuthority()) && !uri.getPath().startsWith("/p/") && !uri.getPath().startsWith("/reel/") && !uri.getPath().startsWith("/tv/") && !uri.getPath().startsWith("/stories/")) {
            assert savedProfiles != null;
            for (Saved_Profile saveProfile : savedProfiles) {
                if (uri.getPath().equals("/" + saveProfile.getName()) || uri.getPath().equals("/" + saveProfile.getName() + "/"))
                    return null;
            }
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, // Ignore the query part of the input url
                    uri.getFragment()).toString() + "?__a=1&__d=dis";
        }
        return "invalid";
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                || actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_GO
                || actionId == EditorInfo.IME_ACTION_NEXT
                || actionId == EditorInfo.IME_ACTION_SEND
                || actionId == EditorInfo.IME_ACTION_SEARCH) {
            if (v == binding.bookmarkEditText) {
                //do what you want on the press of 'done'
                String url = binding.bookmarkEditText.getText().toString();
                bookMarkEditTextMethod(url);
                utils.hideSoftKeyboard(v);
            }
            if (v == binding.editText) {
                if (!TextUtils.isEmpty(binding.editText.getText().toString()))
                    callDownload(binding.editText.getText().toString());
                else Toast.makeText(activity, "Enter Valid Url", Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    private String getUrlWithoutParameters(String url) {
        try {
            URI uri = new URI(url);
            String authority = uri.getAuthority();
            if ("instagram.com".equals(authority) || "www.instagram.com".equals(authority))
                return new URI(uri.getScheme(), authority, uri.getPath(), null, // Ignore the query part of the input url
                        uri.getFragment()).toString();
            else return "";
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return "";
        }
    }

//    logic before we start downloading checking url is in correct format or not
    private void callDownload(String Url) {
        String UrlWithoutQP = getUrlWithoutParameters(Url);
        if (UrlWithoutQP.length() == 0)
            Toast.makeText(activity, "Enter Valid Url", Toast.LENGTH_SHORT).show();
        else {
            try {
                if (utils.isNetworkAvailable()) {
                    if (GetDataFromServer.getInstance() != null && UrlWithoutQP.length() > 24) {
                        String authority = new URI(Url).getRawAuthority();
                        if ("instagram.com".equals(authority)) {
                            if (UrlWithoutQP.length() >= 30 && "/stories/".equals(UrlWithoutQP.substring(21, 30))) {
                                int index = UrlWithoutQP.lastIndexOf('/');
                                storyIdToDownload = UrlWithoutQP.substring(index + 1);
//                                GetDataFromServer.getInstance().getStoryUserIdForDownload(userInfoForSingleStoryDownload, UrlWithoutQP + "?__a=1", COOKIES);
                                GetDataFromServer.getInstance().getStoryUserIdForDownload(userInfoForSingleStoryDownload, UrlWithoutQP + "?__a=1&__d=dis", MODIFIED_COOKIES);
                            } else {
//                                GetDataFromServer.getInstance().callResult(instaObserver, UrlWithoutQP + "?__a=1", COOKIES);
                                Log.d("COOKIES",MODIFIED_COOKIES);
                                GetDataFromServer.getInstance().callResult(instaObserver, UrlWithoutQP + "?__a=1&__d=dis", MODIFIED_COOKIES);
                            }
                        } else if ("www.instagram.com".equals(authority)) {
                            String temp = UrlWithoutQP.substring(25, 28);
                            if ("/p/".equals(temp) || "/tv/".equals(temp + UrlWithoutQP.charAt(28)) || "/reel/".equals(temp + UrlWithoutQP.substring(28, 31))) {
//                                GetDataFromServer.getInstance().callResult(instaObserver, UrlWithoutQP + "?__a=1", COOKIES);
                                GetDataFromServer.getInstance().callResult(instaObserver, UrlWithoutQP + "?__a=1&__d=dis", MODIFIED_COOKIES);
                            }
                            else if ("/s/".equals(temp)) {
                                String query = new URI(Url).getQuery();
                                query = query.substring(15, query.indexOf('&'));
                                int indexOfUnderScore = query.indexOf('_');
                                String storyHighlightToDownloadUserId = query.substring(indexOfUnderScore + 1);
                                storyHighlightToDownloadPKId = query.substring(0, indexOfUnderScore);
                                GetDataFromServer.getInstance().getPhotoFullDetailFeed(storyHighlightObserver, storyHighlightToDownloadUserId, COOKIES, null, "d4d88dc1500312af6f937f7b804c68c3");
                            }
                            else  GetDataFromServer.getInstance().callResult(instaObserver, UrlWithoutQP + "?__a=1&__d=dis", COOKIES);
                        }
                        binding.ProgressBarConstraintLayout.setVisibility(View.VISIBLE);
                    }
                } else
                    Toast.makeText(this, "NO Internet Connection.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


//    private GetDataFromServer getDataFromServer = GetDataFromServer.getInstance();

    private void callStoryHighlightTODownload(int count) {
        if (utils.isNetworkAvailable()) {
            if (GetDataFromServer.getInstance() != null)
                GetDataFromServer.getInstance().getStoryToDownload(allHighlights, "highlight:" + storyHighlightEdgeList.get(count).getNode().getId(), COOKIES);
        } else {
            Toast.makeText(this, "NO Internet Connection.", Toast.LENGTH_SHORT).show();
            binding.ProgressBarConstraintLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String @NotNull [] permissions, int @NotNull [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 30) {
            if (grantResults.length > 0) {
                boolean reader = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean writer = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (reader && writer) {
                    if (utils.isExternalStorageWritable()) {
                        isPermissionGrantedToAccessStorage = true;
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(activity, "Not writable to Storage.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    isPermissionGrantedToAccessStorage = false;
                }
            } else {
                Toast.makeText(getApplicationContext(), "You Denied Permission", Toast.LENGTH_SHORT).show();
                isPermissionGrantedToAccessStorage = false;
            }
        }
    }

    void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          /*  try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                activityResultLauncher.launch(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activityResultLauncher.launch(intent);
            }*/
        } else {
            ActivityCompat.requestPermissions(activity, permission, 30);
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.LogOUTConstraintLayout.getVisibility() == View.VISIBLE)
            binding.LogOUTConstraintLayout.setVisibility(View.GONE);
        else {
//            FragmentManager fragmentManager = getSupportFragmentManager().getPrimaryNavigationFragment().getChildFragmentManager()         .getBackStackEntryCount();
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q && isTaskRoot() && getSupportFragmentManager().getBackStackEntryCount() == 0)
                finishAfterTransition();
            else
                super.onBackPressed();
            if (getSupportFragmentManager().getBackStackEntryCount() == 0 && binding.mainScrollView.getVisibility() == View.GONE) {
                if(utils.getCookies() != null) {
                    String[] temp = utils.getCookies().split(" ");
                    MODIFIED_COOKIES = temp[2] + " " + temp[0] + " " + temp[1] + " " + temp[3] ;
                    MODIFIED_COOKIES = MODIFIED_COOKIES.substring(0,MODIFIED_COOKIES.length()-1);
                    COOKIES = utils.getCookies();
                    if(utils.isNetworkAvailable())
                        GetDataFromServer.getInstance().getStories(storyObserver, COOKIES);
                }
                Objects.requireNonNull(getSupportActionBar()).show();
                binding.mainScrollView.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        albumDataViewModel.getAllMedias().removeObservers(activity);
        savedProfileViewModel.getAllContacts().removeObservers(activity);
        activity = null;
        utils = null;
        logInFragment = null;
//        was causing memoryLeak if not unregistered
        unregisterReceiver(onDownloadComplete);
        storyObserver.dispose();
        photoDetailObserver.dispose();
        Log.d("TAG", "onDestroy: " + storyObserver.isDisposed() + "   " + photoDetailObserver.isDisposed());
    }
}