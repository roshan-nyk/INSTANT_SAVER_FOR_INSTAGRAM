package instant.saver.for_instagram;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.insta_saver.R;
import com.example.insta_saver.databinding.ActivityGalleryBinding;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import instant.saver.for_instagram.adapter.GalleryAlbumAdapter;
import instant.saver.for_instagram.interfaces.GalleryAlbumInterface;
import instant.saver.for_instagram.model.album_gallery.AlbumDataViewModel;
import instant.saver.for_instagram.model.album_gallery.Album_Data;
import instant.saver.for_instagram.util.Utils;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class GalleryActivity extends AppCompatActivity implements View.OnClickListener, GalleryAlbumInterface {

    private static int currentViewPagerPosition;
//    private final String photos = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES).getAbsolutePath();
//    private final String videos = Environment.getExternalStoragePublicDirectory(DIRECTORY_MOVIES).getAbsolutePath();
    private static List<Album_Data> albumDataList;
    private final String photos = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getAbsolutePath();
    private final String videos = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getAbsolutePath();
    private final List<Uri> uriListTODelete = new ArrayList<>();
    String[] permission = {READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE};
    ActivityResultLauncher<Intent> activityResultLauncher;
    private ActivityGalleryBinding binding;
    private Activity activity;
    private AlbumDataViewModel albumDataViewModel;
    private WindowInsetsController windowInsetsController;
    private Album_Data singleAlbumData = null;
    private MediaFragment mediaFragment;
    private GalleryAlbumAdapter galleryAlbumAdapter;
    private int count = 0;
    private Utils utils;
    private int currentSelectedAlbumForMediaFragment;

    public static List<Album_Data> getAlbumDataList() {
        return albumDataList;
    }

    public static void setCurrentViewPagerPosition(int currentViewPagerPosition) {
        GalleryActivity.currentViewPagerPosition = currentViewPagerPosition;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activity = this;
        albumDataViewModel = new ViewModelProvider.AndroidViewModelFactory(activity.getApplication()).create(AlbumDataViewModel.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            windowInsetsController = activity.getWindow().getInsetsController();

//        for version >= R but not needed
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        GridLayoutManager gridLayoutManager = new GridLayoutManager(activity, 3, RecyclerView.VERTICAL, false);
        binding.GalleryRecyclerView.setLayoutManager(gridLayoutManager);

        utils = new Utils(activity);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if (utils.checkPermission()) callAlbumViewModel();
            else requestPermission();
        } else callAlbumViewModel();

        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        activity.getWindow().setStatusBarColor(Color.BLACK);

        binding.galleryActivityUserName.setText("All photos and videos");
        binding.galleryActivityBackButton.setOnClickListener(this);

        Objects.requireNonNull(getSupportActionBar()).hide();
    }

    private void callAlbumViewModel() {
        albumDataViewModel.getAllMedias().observe(GalleryActivity.this, album_data -> {
            albumDataList = album_data;
            if (albumDataList.size() == 0) {
                binding.GalleryRecyclerViewTextView.setVisibility(View.VISIBLE);
                binding.GalleryRecyclerView.setVisibility(View.GONE);
                binding.deleteGalleryConstraintLayout.setVisibility(View.GONE);
                binding.deleteGalleryAlbumCardViewConstraintLayout.setVisibility(View.GONE);
                binding.galleryActivityPicsCount.setText("0 Photos, 0 Videos");
            } else {
                binding.GalleryRecyclerViewTextView.setVisibility(View.GONE);
                binding.GalleryRecyclerView.setVisibility(View.VISIBLE);
                if (utils.isExternalStorageReadOnly() || utils.isExternalStorageWritable()) {

            /*            String[] projection = {
                                MediaStore.Images.Media._ID
                        };
                        Uri ext_uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        String where = "(" + MediaStore.Images.Media.MIME_TYPE
                                + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?)"
                                + " and " + MediaStore.Images.Media.SIZE + ">=?";
                        Cursor c = MediaStore.Images.Media.query(
                                contentResolver,
                                ext_uri,
                                projection,
                                where,
                                new String[] { "image/jpeg", "image/png", "102400" },
                                MediaStore.Images.Media.DATE_MODIFIED + " desc");*/

//                will be called only once at the beginning
                    if (count == 0) {
                        count = albumDataList.size();
                        galleryAlbumAdapter = new GalleryAlbumAdapter(GalleryActivity.this, activity, albumDataList);
                        binding.GalleryRecyclerView.setAdapter(galleryAlbumAdapter);
                        count = -5;
                    } else {
                        List<Album_Data> oldAlbumDataList = galleryAlbumAdapter.getAlbumDataList();
                        if (albumDataList.size() >= oldAlbumDataList.size()) {
                            galleryAlbumAdapter.setAlbumDataList(albumDataList);
                            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                                @Override
                                public int getOldListSize() {
                                    return oldAlbumDataList.size();
                                }

                                @Override
                                public int getNewListSize() {
                                    return albumDataList.size();
                                }

                                @Override
                                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                                    return false;
                                }

                                @Override
                                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                                    return false;
                                }
                            });
                            result.dispatchUpdatesTo(galleryAlbumAdapter);
                        } else {
                            if (currentSelectedAlbumForMediaFragment != -5) {
                                galleryAlbumAdapter.notifyItemRemoved(currentSelectedAlbumForMediaFragment);
                                galleryAlbumAdapter.setAlbumDataList(albumDataList);
//                                galleryAlbumAdapter.notifyItemRangeChanged(0,albumDataList.size());
                                galleryAlbumAdapter.uncheckAllSelectedAlbums();
                                currentSelectedAlbumForMediaFragment = -5;
                            } else {
                                ArrayList<Integer> getAlbumPositionsToDelete = galleryAlbumAdapter.getStoreAlbumPositionsToDelete();
                                int size = oldAlbumDataList.size() - 1;
                                for (int i = 0; i <= getAlbumPositionsToDelete.size() - 1; i++)
                                    galleryAlbumAdapter.notifyItemRemoved(size - getAlbumPositionsToDelete.get(i));
                                galleryAlbumAdapter.setAlbumDataList(albumDataList);
                                galleryAlbumAdapter.uncheckAllSelectedAlbums();
//                                galleryAlbumAdapter.notifyItemRangeChanged(0,albumDataList.size());
                            }
                        }
                    }
                    int photoCount = 0, videoCount = 0;
                    for (Album_Data singleAlbum : albumDataList) {
                        for (String mediaFile : singleAlbum.getMedia()) {
                            if (mediaFile.endsWith(".mp4")) videoCount++;
                            else photoCount++;
                        }
                    }
                    binding.galleryActivityPicsCount.setText(photoCount + " Photos, " + videoCount + " Videos");
                } else
                    Toast.makeText(activity, "Don't have read access to External Storage", Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        binding.GalleryConstraintLayout.setBackgroundColor(Color.parseColor("#C3C3C3"));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String @NotNull [] permissions, int @NotNull [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 30) {
            if (grantResults.length > 0) {
                boolean reader = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean writer = grantResults[1] == PackageManager.PERMISSION_GRANTED;
//                for version 10 only read permission required
                if (reader && (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q ||  writer)) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    callAlbumViewModel();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(getApplicationContext(), "You Denied Permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == binding.deleteGalleryImageView) {
            binding.deleteGalleryAlbumCardViewConstraintLayout.setOnClickListener(this);
            binding.deleteGalleryConstraintLayout.setOnClickListener(this);
            binding.deleteGalleryAlbumNoButton.setOnClickListener(this);
            binding.deleteGalleryAlbumYesButton.setOnClickListener(this);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
                binding.deleteGalleryAlbumCardViewConstraintLayout.setVisibility(View.VISIBLE);
            else onClick(binding.deleteGalleryAlbumYesButton);
        } else if (v == binding.deleteGalleryAlbumCardViewConstraintLayout || v == binding.GalleryAlbumDeleteOptionCardView)
            binding.deleteGalleryAlbumCardViewConstraintLayout.setVisibility(View.GONE);
        else if (v == binding.deleteGalleryAlbumNoButton) {
            binding.deleteGalleryAlbumCardViewConstraintLayout.setVisibility(View.GONE);
            binding.deleteGalleryConstraintLayout.setVisibility(View.GONE);
            galleryAlbumAdapter.uncheckAllSelectedAlbums();
        } else if (v == binding.deleteGalleryAlbumYesButton) {
            if (utils.isExternalStorageWritable()) {
                try {
                    deleteSelectedAlbums();
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            } else
                Toast.makeText(activity, "Don't have access to modify external storage", Toast.LENGTH_LONG).show();
        } else if (v == binding.shareGalleryImageView) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            Intent chooser = Intent.createChooser(shareIntent, "Share Media Using");
            ArrayList<Uri> imageUris = new ArrayList<>();
            File file;
            List<ResolveInfo> resInfoList = activity.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);
            ArrayList<Integer> getAlbumPositionsToDelete = galleryAlbumAdapter.getStoreAlbumPositionsToDelete();
            for (int i = 0; i < getAlbumPositionsToDelete.size(); i++) {
                Album_Data singleAlbumData = albumDataList.get(getAlbumPositionsToDelete.get(i));
                for (String media : singleAlbumData.getMedia()) {
                    if (media.endsWith(".jpg"))
                        file = new File(photos, media);
                    else file = new File(videos, media);
// wrap File object into a content provider. NOTE: authority here should match authority in manifest declaration
                    Uri pictureUri = FileProvider.getUriForFile(activity, "instant.saver.for_instagram.fileprovider", file);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        activity.grantUriPermission(packageName, pictureUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    imageUris.add(pictureUri);
                }
            }
            shareIntent.setType("*/*");
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
            startActivity(chooser);
        } else if (v == binding.galleryActivityBackButton)
            finish();
    }

    private void deleteSelectedAlbums() throws IntentSender.SendIntentException {
        ArrayList<Integer> getAlbumPositionsToDelete = galleryAlbumAdapter.getStoreAlbumPositionsToDelete();
        for (int i = 0; i < getAlbumPositionsToDelete.size(); i++) {
            Album_Data singleAlbumData = albumDataList.get(getAlbumPositionsToDelete.get(i));
            deleteMediaFileFromDirectory(singleAlbumData);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
                albumDataViewModel.deleteSingleMedia(singleAlbumData.getUserId());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (uriListTODelete.size() > 0) {
                GalleryActivity.setCurrentViewPagerPosition(currentViewPagerPosition);
                PendingIntent editPendingIntent = MediaStore.createDeleteRequest(getApplicationContext().getContentResolver(), uriListTODelete);
                startIntentSenderForResult(editPendingIntent.getIntentSender(), 007, null, 0, 0, 0);
            } else {
                for (int i = 0; i < getAlbumPositionsToDelete.size(); i++)
                    albumDataViewModel.deleteSingleMedia(albumDataList.get(getAlbumPositionsToDelete.get(i)).getUserId());
            }
        }
    }

    private void deleteMediaFileFromDirectory(Album_Data singleAlbumData) {

        Uri uriPhoto, uriVideo;
        Cursor mediaCursorImage, mediaCursorVideo;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            uriPhoto = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        else uriPhoto = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projectionPhoto = {MediaStore.Images.Media._ID};
        String selectionPhoto = MediaStore.Images.Media.DATA + "=?";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            uriVideo = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        else uriVideo = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projectionVideo = {MediaStore.Video.Media._ID};
        String selectionVideo = MediaStore.Video.Media.DATA + "=?";


        for (String str : singleAlbumData.getMedia()) {
            String tempFile;
            if (!str.endsWith(".mp4")) {
                tempFile = new File(photos + str).getAbsolutePath();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    mediaCursorImage = getContentResolver().query(uriPhoto, projectionPhoto, selectionPhoto, new String[]{tempFile}, null);
                    if (mediaCursorImage != null && mediaCursorImage.moveToNext()) {
                        int idIndex = mediaCursorImage.getColumnIndex(MediaStore.Images.Media._ID);
                        long mediaID = Long.parseLong(mediaCursorImage.getString(idIndex));
                        Log.d("TAG", "inside cursor " + mediaID);
//                        The new way to delete a media file by getting the ID of the media file and creating new URI from it
                        Uri Uri_one = ContentUris.withAppendedId(MediaStore.Images.Media.getContentUri("external"), mediaID);
                        uriListTODelete.add(Uri_one);
                        mediaCursorImage.close();
                    }
                } else {
                    int numImagesRemoved = getContentResolver().delete(uriPhoto, selectionPhoto, new String[]{tempFile});
                    Log.d("TAG", "deleteMediaFileFromDirectory: " + numImagesRemoved);
                }
            } else {
                tempFile = new File(videos + str).getAbsolutePath();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    mediaCursorVideo = getContentResolver().query(uriVideo, projectionVideo, selectionVideo, new String[]{tempFile}, null);
                    if (mediaCursorVideo != null && mediaCursorVideo.moveToNext()) {
                        int idIndex = mediaCursorVideo.getColumnIndex(MediaStore.Video.Media._ID);
                        long mediaID = Long.parseLong(mediaCursorVideo.getString(idIndex));
                        Log.d("TAG", "inside cursor " + mediaID);
//                        The new way to delete a media file by getting the ID of the media file and creating new URI from it
                        Uri Uri_one = ContentUris.withAppendedId(MediaStore.Video.Media.getContentUri("external"), mediaID);
                        uriListTODelete.add(Uri_one);
                        mediaCursorVideo.close();
                    }
                } else {
                    int numImagesRemoved = getContentResolver().delete(uriVideo, selectionVideo, new String[]{tempFile});
                    Log.d("TAG", "deleteMediaFileFromDirectory: " + numImagesRemoved);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 007) {
            uriListTODelete.clear();
            if (resultCode == Activity.RESULT_OK) {
                ArrayList<Integer> getAlbumPositionsToDelete = galleryAlbumAdapter.getStoreAlbumPositionsToDelete();
                for (int i = getAlbumPositionsToDelete.size() - 1; i >= 0; i--)
                    albumDataViewModel.deleteSingleMedia(albumDataList.get(getAlbumPositionsToDelete.get(i)).getUserId());
            } else
                galleryAlbumAdapter.uncheckAllSelectedAlbums();
        } else if (requestCode == 006) {
            if (resultCode == Activity.RESULT_OK) {
                singleAlbumData.getMedia().remove(currentViewPagerPosition);
                int len = singleAlbumData.getMedia().size();
                if (len == 0) {
                    albumDataViewModel.deleteSingleMedia(singleAlbumData.getUserId());
                    onBackPressed();
                } else {
                    albumDataViewModel.updateSingleAlbumData(singleAlbumData);
                    mediaFragment.itemRemove(currentViewPagerPosition);
                }
            }
        }
    }

/*
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
    }*/

    @Override
    public void callMediaFragment(MediaFragment mediaFragment, int currentSelectedAlbum) {
//        setSystemUiFlags(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN, true);
//        setFullscreenFlags(true);

        this.currentSelectedAlbumForMediaFragment = currentSelectedAlbum;
        this.mediaFragment = mediaFragment;
        getSupportFragmentManager().beginTransaction()
                .add(R.id.Gallery_Constraint_Layout, mediaFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void currentSingleAlbum(Album_Data album_data) {
        singleAlbumData = album_data;
    }

    @Override
    public void getCurrentPosition(int position) {
        Log.d("TAG", "getCurrentPosition: " + position);
        if (position == -5) {
            binding.deleteGalleryConstraintLayout.setVisibility(View.GONE);
            binding.deleteGalleryAlbumCardViewConstraintLayout.setVisibility(View.GONE);
        } else {
            currentSelectedAlbumForMediaFragment = -5;
            binding.deleteGalleryConstraintLayout.setVisibility(View.VISIBLE);
            binding.deleteGalleryImageView.setOnClickListener(this);
            binding.shareGalleryImageView.setOnClickListener(this);
        }
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
        ActivityCompat.requestPermissions(activity, permission, 30);
//        }
    }


    @Override
    public void onBackPressed() {

        if (binding.deleteGalleryAlbumCardViewConstraintLayout.getVisibility() == View.VISIBLE)
            binding.deleteGalleryAlbumCardViewConstraintLayout.setVisibility(View.GONE);
        else if (binding.deleteGalleryConstraintLayout.getVisibility() == View.VISIBLE) {
            binding.deleteGalleryConstraintLayout.setVisibility(View.GONE);
            galleryAlbumAdapter.uncheckAllSelectedAlbums();
        } else
            super.onBackPressed();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && windowInsetsController != null)
            windowInsetsController.show(WindowInsets.Type.systemBars());

        /*setSystemUiFlags( View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY, false);
        setFullscreenFlags(false);*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//            getWindow().clearFlags(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS);
        }

        getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() & ~(View.SYSTEM_UI_FLAG_FULLSCREEN));
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }

    @Override
    protected void onStop() {
        Log.d("TAG", "onStop: galleryactivity");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        albumDataList = null;
        albumDataViewModel.getAllMedias().removeObservers((LifecycleOwner) activity);
        utils = null;
        mediaFragment = null;
        activity = null;
        Log.d("TAG", "onDestroy: galleryActivity");
    }
}