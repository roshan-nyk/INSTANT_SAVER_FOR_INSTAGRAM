package instant.saver.for_instagram;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.insta_saver.R;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import instant.saver.for_instagram.adapter.MediaContentAdapter;
import instant.saver.for_instagram.api.GetDataFromServer;
import instant.saver.for_instagram.interfaces.MediaInterface;
import instant.saver.for_instagram.model.Edge;
import instant.saver.for_instagram.model.Node;
import instant.saver.for_instagram.model.album_gallery.AlbumDataViewModel;
import instant.saver.for_instagram.model.album_gallery.Album_Data;
import instant.saver.for_instagram.model.story.ItemModel;
import instant.saver.for_instagram.util.Utils;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class MediaFragment extends Fragment implements MediaInterface, View.OnClickListener {

    private static Album_Data currentVisibleAlbumDataOnViewPager2 = null;

//    private final String photos = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES).getAbsolutePath();
//    private final String videos = Environment.getExternalStoragePublicDirectory(DIRECTORY_MOVIES).getAbsolutePath();

    private final String photos = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getAbsolutePath();
    private final String videos = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getAbsolutePath();

    private final ArrayList<Boolean> isVideoList = new ArrayList<>();
    private final ArrayList<String> stringsUrlsToDownload = new ArrayList<>();
    private final ArrayList<Integer> viewPagerPositionsToDownload = new ArrayList<>();
    String[] permission = {READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE};
    private CardView downloadCardView, deleteCardView, shareCardView;
    private ArrayList<ItemModel> storyItemModelList = null;
    private int adapterPosition, currentViewPagerPosition;
    private long galleryAlbumUserId;
    private Edge photoModel = null;
    private TextView textView, captionUserName, captionUserCaptionText;
    private MediaContentAdapter mediaContentAdapter;
    private AlbumDataViewModel albumDataViewModel;
    private int totalRequiredDownloads = 1;
    private ImageView repostButton, shareButton, downloadButton, deleteButton, captionUserImage, cardCaptionImageView, cardCaptionImageViewReplica, copyCaptionText, instaLogo, backButton;
    private View downloadOption, userCaptionLayout;
    private Button wholeAlbumButton, singlePostButton, deleteYesButton, deleteNoButton;
    private ArrayList<String> mediaStrings = null;
    private List<Album_Data> albumData = null, demoData = null;
    private String currentVisibleMediaUrl, userName, savedProfilePicUrl, mediaCaption, shortCode, productType;
    private Utils utils;
    private boolean isVideo, isFirstTimePhotoDownloadButtonTouched = false, isDownloadButtonVisibility = false;

    private final ViewPager2.OnPageChangeCallback onPageSelected = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            setCurrentPosition(position);
            if (mediaStrings == null)
                checkDownloadButtonVisibility(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
        }
    };
    private ViewPager2 viewPager2;
    private View downloadConstraintLayout, deleteConstraintLayout;

    public MediaFragment() {
        // Required empty public constructor
    }

    public static Album_Data getCurrentVisibleAlbumDataOnViewPager2() {
        return currentVisibleAlbumDataOnViewPager2;
    }

    public static MediaFragment newInstance(ArrayList<ItemModel> storyItemModelList, int adapterPosition, String userName, String savedProfilePicUrl) {
        MediaFragment fragment = new MediaFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("StoryItem", storyItemModelList);
        bundle.putInt("ADAPTER_POSITION", adapterPosition);
        bundle.putString("USER_NAME", userName);
        bundle.putString("Saved_Profile_Pic", savedProfilePicUrl);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static MediaFragment newInstance(Edge itemModel, String savedProfilePicUrl) {
        MediaFragment fragment = new MediaFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("PHOTOS", itemModel);
        bundle.putString("Saved_Profile_Pic", savedProfilePicUrl);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static MediaFragment newInstance(ArrayList<String> mediaStrings, long userId, String userName, String mediaCaption, String savedProfilePicUrl, String productType, String shortCode) {
        MediaFragment fragment = new MediaFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("GALLERY_STRINGS", mediaStrings);
        bundle.putLong("USER_ID", userId);
        bundle.putString("USER_NAME", userName);
        bundle.putString("Saved_Profile_Pic", savedProfilePicUrl);
        bundle.putString("MEDIA_CAPTION", mediaCaption);
        bundle.putString("PRODUCT_TYPE", productType);
        bundle.putString("SHORT_CODE", shortCode);
        fragment.setArguments(bundle);
        return fragment;
    }

    private void checkDownloadButtonVisibility(int position) {
        if (storyItemModelList != null) {
            isVideo = storyItemModelList.get(position).getMedia_type() == 2;
            if (isVideo)
                currentVisibleMediaUrl = storyItemModelList.get(position).getVideo_versions().get(0).getUrl();
            else
                currentVisibleMediaUrl = storyItemModelList.get(position).getImage_versions2().getCandidates().get(0).getUrl();
        } else {
            boolean isGraphSideCar = photoModel.getNode().get__typename().equals("GraphSidecar");
            if (!isGraphSideCar) {
                isVideo = photoModel.getNode().isIs_video();
                if (isVideo)
                    currentVisibleMediaUrl = photoModel.getNode().getVideo_url();
                else
                    currentVisibleMediaUrl = photoModel.getNode().getDisplay_resources().get(2).getSrc();
            } else {
                Node sideCarNode = photoModel.getNode().getEdge_sidecar_to_children().getEdges().get(position).getNode();
                isVideo = sideCarNode.isIs_video();
                if (isVideo)
                    currentVisibleMediaUrl = sideCarNode.getVideo_url();
                else
                    currentVisibleMediaUrl = sideCarNode.getDisplay_resources().get(2).getSrc();
            }
        }
//      logic to make download button visible or not
        if (albumData != null) {
            GetDataFromServer.getInstance().getDataBaseWriteExecutor().execute(() -> {
                        boolean mediaAvailable = false;
                        if (viewPagerPositionsToDownload.size() > 0 && viewPagerPositionsToDownload.contains(currentViewPagerPosition)) {
                            mediaAvailable = true;
                            requireActivity().runOnUiThread(() -> downloadCardView.setVisibility(View.GONE));
                        }
                        if (!mediaAvailable) {
                            String fileName = utils.getFilenameFromURL(currentVisibleMediaUrl);
                            String mediaType;
                            if (fileName.endsWith(".mp4"))
                                mediaType = "InstantVideos /";
                            else mediaType = "InstantPicture/";
                            fileName = utils.getDESTINATIONPATH() + mediaType + fileName.substring(0, fileName.length() - 4);
                            for (Album_Data singleAlbum : albumData) {
                                String finalFileName = fileName;
                                if (singleAlbum.getMedia().stream().anyMatch(s -> s.contains(finalFileName))) {
                                    mediaAvailable = true;
                                    galleryAlbumUserId = singleAlbum.getUserId();
                                    requireActivity().runOnUiThread(() -> downloadCardView.setVisibility(View.GONE));
                                    break;
                                }
                            }
                            if (!mediaAvailable)
                                requireActivity().runOnUiThread(() -> downloadCardView.setVisibility(View.VISIBLE));
                        }
                    }
            );
        }
    }

//    after a post is deleted updating no. of remaining posts
    @SuppressLint("DefaultLocale")
    public void itemRemove(int position) {
        if (mediaStrings.size() == 0)
            requireActivity().onBackPressed();
        if (position != mediaStrings.size())
            textView.setText(String.format("%d/%d", position + 1, mediaStrings.size()));
        else
            textView.setText(String.format("%d/%d", position, mediaStrings.size()));
        mediaContentAdapter.notifyItemRemoved(position);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().getSerializable("StoryItem") != null) {
                storyItemModelList = (ArrayList<ItemModel>) getArguments().getSerializable("StoryItem");
                adapterPosition = getArguments().getInt("ADAPTER_POSITION");
                userName = getArguments().getString("USER_NAME");
                savedProfilePicUrl = getArguments().getString("Saved_Profile_Pic");
            } else if (getArguments().getSerializable("PHOTOS") != null) {
                photoModel = (Edge) getArguments().getSerializable("PHOTOS");
                shortCode = photoModel.getNode().getShortcode();
                savedProfilePicUrl = getArguments().getString("Saved_Profile_Pic");
                userName = photoModel.getNode().getOwner().getUsername();
            } else {
                mediaStrings = getArguments().getStringArrayList("GALLERY_STRINGS");
                galleryAlbumUserId = getArguments().getLong("USER_ID");
                userName = getArguments().getString("USER_NAME");
                savedProfilePicUrl = getArguments().getString("Saved_Profile_Pic");
                mediaCaption = getArguments().getString("MEDIA_CAPTION");
                productType = getArguments().getString("PRODUCT_TYPE");
                shortCode = getArguments().getString("SHORT_CODE");
            }

            albumDataViewModel = new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()).create(AlbumDataViewModel.class);

            utils = new Utils(requireActivity());

            AtomicInteger initialMediaStringLength = new AtomicInteger();

            albumDataViewModel.getAllMedias().observe(requireActivity(), album_data -> {
                albumData = album_data;
                if (currentVisibleAlbumDataOnViewPager2 != null)
                    Log.d("TAG", "onCreate MediaFragment: mediaString   " + mediaStrings.size() + "           viewpager+   " + currentVisibleAlbumDataOnViewPager2.getMedia().size());
//            enter if scope only once
                if (demoData == null) {
                    demoData = albumData;
                    onPageSelected.onPageSelected(currentViewPagerPosition);
//                  logic for gallery posts entered only once
                    if (mediaStrings != null) {
                        initialMediaStringLength.set(mediaStrings.size());
                        for (Album_Data singleAlbumData : albumData) {
                            if (singleAlbumData.getUserId() == galleryAlbumUserId) {
                                currentVisibleAlbumDataOnViewPager2 = new Album_Data(singleAlbumData.getMedia());
                                currentVisibleAlbumDataOnViewPager2.setUserId(singleAlbumData.getUserId());
                                currentVisibleAlbumDataOnViewPager2.setUserName(userName);
                                currentVisibleAlbumDataOnViewPager2.setMediaCaption(mediaCaption);
                                currentVisibleAlbumDataOnViewPager2.setProfilePicUrl(savedProfilePicUrl);
                                currentVisibleAlbumDataOnViewPager2.setShortcode(shortCode);
                                currentVisibleAlbumDataOnViewPager2.setProduct_type(productType);
                                mediaContentAdapter = new MediaContentAdapter(requireActivity(), mediaStrings, MediaFragment.this);
                                viewPager2.setAdapter(mediaContentAdapter);
                                break;
                            }
                        }
                    }
                } else if (mediaStrings != null && initialMediaStringLength.get() == mediaStrings.size()) {
                    mediaStrings = currentVisibleAlbumDataOnViewPager2.getMedia();
                    mediaContentAdapter.setMediaStrings(mediaStrings);
                    viewPager2.setCurrentItem(viewPager2.getCurrentItem());
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_media, container, false);
    }


    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            requireActivity().getWindow().getInsetsController().setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
//            requireActivity().getWindow().getInsetsController().setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS, WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
        }

        requireActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        requireActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            requireActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//            requireActivity().getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
//            requireActivity().getWindow().setFlags(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS, WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS);
            requireActivity().getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        viewPager2 = view.findViewById(R.id.media_viewPager_fragment);
        textView = view.findViewById(R.id.media_fragment_textView);
        downloadCardView = view.findViewById(R.id.download_cardView);
        downloadConstraintLayout = view.findViewById(R.id.download_MediaFragment_constraint_layout);
        downloadButton = view.findViewById(R.id.downlaod_card_image);
        downloadOption = view.findViewById(R.id.downloadOption_layout);
        wholeAlbumButton = view.findViewById(R.id.downloadOption_Whole_Album);
        singlePostButton = view.findViewById(R.id.downloadOption_Single_Post);

        cardCaptionImageView = view.findViewById(R.id.caption_card_imageView);
        cardCaptionImageViewReplica = view.findViewById(R.id.caption_card_imageView_replica);
        copyCaptionText = view.findViewById(R.id.card_copy_caption);

        userCaptionLayout = view.findViewById(R.id.userCaption_layout);
        captionUserImage = view.findViewById(R.id.mediaFragment_userImage);
        captionUserName = view.findViewById(R.id.mediaFragment_userName);
        captionUserCaptionText = view.findViewById(R.id.mediaFragment_userCaption);

        instaLogo = view.findViewById(R.id.media_fragment_insta_logo);
        instaLogo.setOnClickListener(this);
        backButton = view.findViewById(R.id.media_fragment_backButton);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(this);

        downloadButton.setOnClickListener(this);
        downloadOption.setOnClickListener(this);
        wholeAlbumButton.setOnClickListener(this);
        singlePostButton.setOnClickListener(this);

        userCaptionLayout.setOnClickListener(this);
        cardCaptionImageView.setOnClickListener(this);
        cardCaptionImageViewReplica.setOnClickListener(this);
        copyCaptionText.setOnClickListener(this);

//        takes time to load so loading it here
        Glide.with(requireActivity()).load(savedProfilePicUrl).into(captionUserImage);

        if (storyItemModelList != null)
            mediaContentAdapter = new MediaContentAdapter(requireActivity(), storyItemModelList, MediaFragment.this);
        else if (photoModel != null)
            mediaContentAdapter = new MediaContentAdapter(requireActivity(), photoModel.getNode(), MediaFragment.this);
        else {
            deleteCardView = view.findViewById(R.id.delete_cardView);
            deleteConstraintLayout = view.findViewById(R.id.delete_MediaFragment_constraint_layout);
            deleteButton = view.findViewById(R.id.delete_card_image);
            deleteYesButton = view.findViewById(R.id.deleteOption_Yes_Button);
            deleteNoButton = view.findViewById(R.id.deleteOption_No_Button);
            shareCardView = view.findViewById(R.id.share_cardView);
            shareButton = view.findViewById(R.id.share_card_image);
            repostButton = view.findViewById(R.id.repost_imageView);
            downloadCardView.setVisibility(View.GONE);
            deleteCardView.setVisibility(View.VISIBLE);
            shareCardView.setVisibility(View.VISIBLE);
            repostButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(this);
            deleteYesButton.setOnClickListener(this);
            deleteNoButton.setOnClickListener(this);
            shareButton.setOnClickListener(this);
            repostButton.setOnClickListener(this);
        }

        if (mediaContentAdapter != null)
            viewPager2.setAdapter(mediaContentAdapter);
        viewPager2.registerOnPageChangeCallback(onPageSelected);

        if (storyItemModelList != null)
            viewPager2.setCurrentItem(adapterPosition, false);
        else
            setCurrentPosition(0);
    }

    @Override
    public void openVideoActivity(String videoUrl) {
        Log.d("TAG", "openVideoActivity: " + videoUrl);
        Intent intent = new Intent(requireActivity(), VideoActivity.class);
        if (!videoUrl.endsWith(".mp4"))
            intent.putExtra("Video_Url", videoUrl);
        else
            intent.putExtra("PATH_NAME", videoUrl);
        startActivity(intent);
    }

//    updating page number after scrolling anySide while viewing a post
    @SuppressLint("DefaultLocale")
    private void setCurrentPosition(int position) {
        currentViewPagerPosition = position;
        if (storyItemModelList != null)
            textView.setText(String.format("%d/%d", position + 1, storyItemModelList.size()));
        else if (photoModel != null) {
            if (photoModel.getNode().get__typename().equals("GraphSidecar"))
                textView.setText(String.format("%d/%d", position + 1, photoModel.getNode().getEdge_sidecar_to_children().getEdges().size()));
            else
                textView.setText("1/1");
        } else
            textView.setText(String.format("%d/%d", position + 1, mediaStrings.size()));
    }

    void requestPermission() {
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                activityResultLauncher.launch(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activityResultLauncher.launch(intent);
            }
        } else {*/
        ActivityCompat.requestPermissions(requireActivity(), permission, 30);
//        }
    }

    @Override
    public void onClick(View v) {
        if (v == downloadButton) {
            if (utils.isNetworkAvailable()) {
                if (utils.isExternalStorageWritable()) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        if (utils.checkPermission()) startDownload();
                        else requestPermission();
                    } else
                        startDownload();
                } else
                    Toast.makeText(requireActivity(), "Don't have Write access to External Storage", Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(requireActivity(), "No Network Available.", Toast.LENGTH_LONG).show();
        } else if (v == deleteButton) {
            if (utils.isExternalStorageWritable()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    onClick(deleteYesButton);
                else {
                    instaLogo.setVisibility(View.GONE);
                    downloadOption.setVisibility(View.VISIBLE);
                    downloadConstraintLayout.setVisibility(View.GONE);
                    deleteConstraintLayout.setVisibility(View.VISIBLE);
                }
            } else
                Toast.makeText(requireActivity(), "Don't have Write access to External Storage", Toast.LENGTH_LONG).show();
        } else if (v == deleteYesButton) {
            try {
                deleteMediaFileFromDirectory(mediaStrings.get(currentViewPagerPosition));
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    downloadOption.setVisibility(View.GONE);
                    deleteConstraintLayout.setVisibility(View.GONE);
                }
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
            instaLogo.setVisibility(View.VISIBLE);
        } else if (v == deleteNoButton || v == singlePostButton || v == wholeAlbumButton || v == downloadOption) {
            downloadOption.setVisibility(View.GONE);
            instaLogo.setVisibility(View.VISIBLE);
            if(v == wholeAlbumButton) downloadAllPosts();
            else if(v == singlePostButton) downloadSingleFile();
            else if(v == downloadOption) isFirstTimePhotoDownloadButtonTouched = false;
        } else if (v == cardCaptionImageView) {
            Glide.with(requireActivity()).load(savedProfilePicUrl).into(captionUserImage);
            captionUserName.setText(userName);
            if (photoModel != null) {
                List<Edge> edgeList = photoModel.getNode().getEdgeMediaToCaption().getEdges();
                if (edgeList.size() > 0)
                    captionUserCaptionText.setText(edgeList.get(0).getNode().getText());
            } else if (mediaStrings != null)
                captionUserCaptionText.setText(mediaCaption);
            if (mediaStrings == null){
                if(downloadCardView.getVisibility() == View.VISIBLE) {
                    isDownloadButtonVisibility = true;
                    downloadCardView.setVisibility(View.GONE);
                }
            } else
                deleteCardView.setVisibility(View.GONE);
            instaLogo.setVisibility(View.GONE);
            backButton.setVisibility(View.GONE);
            userCaptionLayout.setVisibility(View.VISIBLE);
        } else if (v == cardCaptionImageViewReplica || v == copyCaptionText) {
            instaLogo.setVisibility(View.VISIBLE);
            backButton.setVisibility(View.VISIBLE);
            userCaptionLayout.setVisibility(View.GONE);
            if (mediaStrings == null){
                if(isDownloadButtonVisibility)
                    downloadCardView.setVisibility(View.VISIBLE);
            }
            else    deleteCardView.setVisibility(View.VISIBLE);
            if (v == copyCaptionText) {
                ClipboardManager clipboardManager = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(ClipData.newPlainText(null, captionUserCaptionText.getText()));
                Toast.makeText(requireContext(), "Caption Copied", Toast.LENGTH_SHORT).show();
            }
        } else if (v == instaLogo) {
            try {
                Intent launchIntent = new Intent(Intent.ACTION_VIEW).setPackage("com.instagram.android");
                if (launchIntent != null) {
                    if (photoModel != null) {
//                    if condition for future use when igtv videos are shown to user
                        if ("igtv".equals(photoModel.getNode().getProduct_type()))
                            startActivity(launchIntent.setData(Uri.parse("https://www.instagram.com/tv/" + shortCode)));
                        else
                            startActivity(launchIntent.setData(Uri.parse("https://www.instagram.com/p/" + shortCode)));
                    } else if (storyItemModelList != null)
                        startActivity(launchIntent.setData(Uri.parse("https://www.instagram.com/" + userName)));
                    else {
                        if ("igtv".equals(productType))
                            startActivity(launchIntent.setData(Uri.parse("https://www.instagram.com/tv/" + shortCode)));
                        else if ("p".equals(productType))
                            startActivity(launchIntent.setData(Uri.parse("https://www.instagram.com/p/" + shortCode)));
                        else
                            startActivity(launchIntent.setData(Uri.parse("https://www.instagram.com/" + userName)));
                    }
                } else
                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=" + "com.instagram.android")));
                //   or i can write startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.instagram.com/)));  to open in an browser
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=" + "com.instagram.android")));
            }
        } else if (v == backButton)
            requireActivity().onBackPressed();
        else if (v == shareButton || v == repostButton) {
            File file;
            String media = mediaStrings.get(viewPager2.getCurrentItem());
            if (media.endsWith(".jpg"))
                file = new File(photos, media);
            else
                file = new File(videos, media);

// wrap File object into a content provider. NOTE: authority here should match authority in manifest declaration
            Uri pictureUri = FileProvider.getUriForFile(requireActivity(), "instant.saver.for_instagram.fileprovider", file);
            if (v == shareButton) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, pictureUri);
                if (media.endsWith(".jpg"))
                    shareIntent.setType("image/jpg");
                else shareIntent.setType("video/mp4");
                Intent chooser = Intent.createChooser(shareIntent, "Share Media Using");
                List<ResolveInfo> resInfoList = requireActivity().getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    requireActivity().grantUriPermission(packageName, pictureUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                startActivity(chooser);
            } else {
                Intent repostIntent = new Intent(Intent.ACTION_SEND).setPackage("com.instagram.android");
                if (media.endsWith(".jpg"))
                    repostIntent.setType("image/jpg");
                else repostIntent.setType("video/mp4");
                repostIntent.putExtra(Intent.EXTRA_STREAM, pictureUri);
                requireActivity().grantUriPermission("com.instagram.android", pictureUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(repostIntent, "Repost Using"));
            }
        }
    }

//    only for posts with more than 1 pic/video in it to download
    private void downloadAllPosts() {
        String temp = currentVisibleMediaUrl;
        if (photoModel.getNode().get__typename().equals("GraphSidecar")) {
            List<Edge> tempFiles = photoModel.getNode().getEdge_sidecar_to_children().getEdges();
            totalRequiredDownloads = tempFiles.size();
            for (int i = 0; i < totalRequiredDownloads; i++) {
                isVideo = tempFiles.get(i).getNode().isIs_video();
                if (isVideo)
                    currentVisibleMediaUrl = tempFiles.get(i).getNode().getVideo_url();
                else
                    currentVisibleMediaUrl = tempFiles.get(i).getNode().getDisplay_resources().get(2).getSrc();
                viewPagerPositionsToDownload.add(i);
                downloadSingleFile();
            }
        } else {
            isVideo = photoModel.getNode().isIs_video();
            if (isVideo)
                currentVisibleMediaUrl = photoModel.getNode().getVideo_url();
            else
                currentVisibleMediaUrl = photoModel.getNode().getDisplay_resources().get(2).getSrc();
            downloadSingleFile();
        }
        ArrayList<String> userDetails = new ArrayList<>();
   
        try {
            userDetails.add(photoModel.getNode().getOwner().getUsername());
            userDetails.add(savedProfilePicUrl);
            userDetails.add(photoModel.getNode().getShortcode());
            userDetails.add(photoModel.getNode().getEdgeMediaToCaption().getEdges().get(0).getNode().getText());
            userDetails.add("p");
        }
        catch (Exception e){
            userDetails.add(photoModel.getNode().getOwner().getUsername());
            userDetails.add(savedProfilePicUrl);
            userDetails.add(photoModel.getNode().getShortcode());
//            userDetails.add(photoModel.getNode().getEdgeMediaToCaption().getEdges().get(0).getNode().getText());
            userDetails.add("p");
        }
//        starting downloading here instead of waiting for onDestroyView because all posts are getting downloading at this point
        utils.checkAvailableExternalStorage(stringsUrlsToDownload, userDetails, albumData, isVideoList);
        totalRequiredDownloads = -5;
        currentVisibleMediaUrl = temp;
    }

    private void downloadPhotos() {
//        asking whether to download the whole album or the single post in case of watching posts of a user not stories
        if (!isFirstTimePhotoDownloadButtonTouched) {
            isFirstTimePhotoDownloadButtonTouched = true;
            instaLogo.setVisibility(View.GONE);
            downloadOption.setVisibility(View.VISIBLE);
            downloadConstraintLayout.setVisibility(View.VISIBLE);
        } else
            downloadSingleFile();
    }

    private void downloadSingleFile() {
        //       Log.d("TAG", "downloadSingleFile: " + currentVisibleMediaUrl);
        downloadCardView.setVisibility(View.GONE);
        stringsUrlsToDownload.add(currentVisibleMediaUrl);
        isVideoList.add(isVideo);
        if (totalRequiredDownloads == 1)
            viewPagerPositionsToDownload.add(currentViewPagerPosition);
        /*if (Build.VERSION.SDK_INT >= 26) {
            if (!utils.checkAvailableInternalStorage())
                Toast.makeText(requireActivity(), "Not sufficient Memory Available", Toast.LENGTH_LONG).show();
        }*/
    }

    @Override
    public void onDestroyView() {
        if (viewPagerPositionsToDownload.size() > 0 && totalRequiredDownloads != -5) {
            ArrayList<String> userDetails = new ArrayList<>();
            if (storyItemModelList != null) {
                userDetails.add(userName);
                userDetails.add(savedProfilePicUrl);
                userDetails.add(null);
                userDetails.add(null);
                userDetails.add(null);
            } else if (photoModel != null) {
              try{
                    userDetails.add(photoModel.getNode().getOwner().getUsername());
                    userDetails.add(savedProfilePicUrl);
                    userDetails.add(photoModel.getNode().getShortcode());
                    userDetails.add(photoModel.getNode().getEdgeMediaToCaption().getEdges().get(0).getNode().getText());
                    userDetails.add("p");
                }
                catch (Exception e){
                    userDetails.add(photoModel.getNode().getOwner().getUsername());
                    userDetails.add(savedProfilePicUrl);
                    userDetails.add(photoModel.getNode().getShortcode());
//                    userDetails.add(photoModel.getNode().getEdgeMediaToCaption().getEdges().get(0).getNode().getText());
                    userDetails.add("p");
                }
            }
//            download process starting before destroying fragment
            utils.checkAvailableExternalStorage(stringsUrlsToDownload, userDetails, albumData, isVideoList);
        }
        if (currentVisibleAlbumDataOnViewPager2 != null)
            currentVisibleAlbumDataOnViewPager2 = null;
        if (albumDataViewModel.getAllMedias() != null)
            albumDataViewModel.getAllMedias().removeObservers(requireActivity());
        super.onDestroyView();
    }


    private void deleteMediaFileFromDirectory(String mediaString) throws IntentSender.SendIntentException {
//        First trying to delete from actual storage of phone
        //   Has to be a list of uri
        List<Uri> uriListTODelete = new ArrayList<>();
        if (!mediaString.endsWith(".mp4")) {
            Uri uriPhoto;
            String tempFile = new File(photos + mediaString).getAbsolutePath();
            String[] projection = {MediaStore.Images.Media._ID};
            String selectionPhoto = MediaStore.Images.Media.DATA + "=?";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                uriPhoto = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
            else uriPhoto = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Cursor mediaCursor = requireActivity().getContentResolver().query(uriPhoto, projection, selectionPhoto, new String[]{tempFile}, null);
                if (mediaCursor != null && mediaCursor.moveToNext()) {
                    int idIndex = mediaCursor.getColumnIndex(MediaStore.Images.Media._ID);
                    long mediaID = Long.parseLong(mediaCursor.getString(idIndex));
                    Log.d("TAG", "inside cursor " + mediaID);
//                        The new way to delete a media file by getting the ID of the media file and creating new URI from it
                    Uri Uri_one = ContentUris.withAppendedId(MediaStore.Images.Media.getContentUri("external"), mediaID);
                    uriListTODelete.add(Uri_one);
                    mediaCursor.close();
                }
            } else {
                int numImagesRemoved = requireActivity().getContentResolver().delete(uriPhoto, selectionPhoto, new String[]{tempFile});
                Log.d("TAG", "deleteMediaFileFromDirectory: " + numImagesRemoved);
            }
        } else {
            Uri uriVideo;
            String tempFile = new File(videos + mediaString).getAbsolutePath();
            String[] projection = {MediaStore.Video.Media._ID};
            String selectionVideo = MediaStore.Video.Media.DATA + "=?";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                uriVideo = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
            else uriVideo = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Cursor mediaCursor = requireActivity().getContentResolver().query(uriVideo, projection, selectionVideo, new String[]{tempFile}, null);
                if (mediaCursor != null && mediaCursor.moveToNext()) {
                    int idIndex = mediaCursor.getColumnIndex(MediaStore.Video.Media._ID);
                    long mediaID = Long.parseLong(mediaCursor.getString(idIndex));
                    Log.d("TAG", "inside cursor " + mediaID);
                    Uri Uri_one = ContentUris.withAppendedId(MediaStore.Video.Media.getContentUri("external"), mediaID);
                    uriListTODelete.add(Uri_one);
                    mediaCursor.close();
                }
            } else {
                int numImagesRemoved = requireActivity().getContentResolver().delete(uriVideo, selectionVideo, new String[]{tempFile});
                Log.d("TAG", "deleteMediaFileFromDirectory: " + numImagesRemoved);
            }
        }

//        condition to delete from albumData roomDB
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && uriListTODelete.size() > 0) {
            GalleryActivity.setCurrentViewPagerPosition(currentViewPagerPosition);
            PendingIntent editPendingIntent = MediaStore.createDeleteRequest(requireActivity().getApplicationContext().getContentResolver(), uriListTODelete);
            requireActivity().startIntentSenderForResult(editPendingIntent.getIntentSender(), 006, null, 0, 0, 0);
        } else {
            currentVisibleAlbumDataOnViewPager2.getMedia().remove(currentViewPagerPosition);
            int len = currentVisibleAlbumDataOnViewPager2.getMedia().size();
            if (len == 0) {
                albumDataViewModel.deleteSingleMedia(currentVisibleAlbumDataOnViewPager2.getUserId());
                currentVisibleAlbumDataOnViewPager2 = null;
                requireActivity().onBackPressed();
            } else {
                albumDataViewModel.updateSingleAlbumData(currentVisibleAlbumDataOnViewPager2);
//  don't know why notifyItemRemoved doesn't automatically update List<String> mediaStrings for logic of else condition but working for pendingIntent. So manually removing
//  may be in that case only not modified files have been deleted first then the database file names
                mediaStrings.remove(currentViewPagerPosition);
                itemRemove(currentViewPagerPosition);
            }
        }
    }

    public void startDownload() {
        if (storyItemModelList != null)
            downloadSingleFile();
        else if (photoModel != null)
            downloadPhotos();
    }

   /*
     private static final int DISPLAY_OP_1 = 1;
    private static final int APPEARANCE_OPAQUE_NAVIGATION_BARS = 1 << 1;
    private static final int APPEARANCE_LOW_PROFILE_BARS = 1 << 2;
    private static final int APPEARANCE_LIGHT_STATUS_BARS = 1 << 3;
    private static final int APPEARANCE_LIGHT_NAVIGATION_BARS = 1 << 4;

   @IntDef(
            flag = true,
            value = {
                    DISPLAY_OP_1,
                    APPEARANCE_OPAQUE_NAVIGATION_BARS,
                    APPEARANCE_LOW_PROFILE_BARS,
                    APPEARANCE_LIGHT_STATUS_BARS,
                    APPEARANCE_LIGHT_NAVIGATION_BARS
            }
    )

    @Retention(RetentionPolicy.SOURCE)
    @interface DisplayOptions {
    }*/
}
