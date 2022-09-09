package instant.saver.for_instagram.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import instant.saver.for_instagram.R;
import com.jsibbold.zoomage.ZoomageView;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import instant.saver.for_instagram.GalleryActivity;
import instant.saver.for_instagram.fragments.MediaFragment;
import instant.saver.for_instagram.interfaces.MediaInterface;
import instant.saver.for_instagram.model.Node;
import instant.saver.for_instagram.model.album_gallery.AlbumDataViewModel;
import instant.saver.for_instagram.model.album_gallery.Album_Data;
import instant.saver.for_instagram.model.story.ItemModel;
import instant.saver.for_instagram.util.Utils;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class MediaContentAdapter extends RecyclerView.Adapter<MediaContentAdapter.ViewHolder> implements View.OnTouchListener {

    private static final int DISPLAY_OP_1 = 1;
    private static final int APPEARANCE_OPAQUE_NAVIGATION_BARS = 1 << 1;
    private static final int APPEARANCE_LOW_PROFILE_BARS = 1 << 2;
    private static final int APPEARANCE_LIGHT_STATUS_BARS = 1 << 3;
    private static final int APPEARANCE_LIGHT_NAVIGATION_BARS = 1 << 4;


    private final Context context;
    private final MediaInterface mediaInterface;
    private final Activity activity;

//    private final String photos = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES).getAbsolutePath();
//    private final String videos = Environment.getExternalStoragePublicDirectory(DIRECTORY_MOVIES).getAbsolutePath();


    private final String photos = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getAbsolutePath();
    private final String videos = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getAbsolutePath();
    private final String[] projectionPhoto = {MediaStore.Images.Media.DISPLAY_NAME};
    private final String selectionPhoto = MediaStore.Images.Media.MIME_TYPE + "=? ";
    private final String[] projectionVideo = {MediaStore.Video.Media.DISPLAY_NAME};
    private final String selectionVideo = MediaStore.Video.Media.MIME_TYPE + "=? ";
    private WindowInsetsController insetsController;
    private ArrayList<ItemModel> storyItemModelList;
    private MediaContentAdapter.ViewHolder viewHolder;
    private Album_Data currentAlbumData;
    private Node node;
    private List<String> mediaStrings;
    private AlbumDataViewModel albumDataViewModel;
    private Uri uriPhoto, uriVideo;
    private Utils utils;

    public MediaContentAdapter(Activity activity, ArrayList<ItemModel> storyItemModelList, MediaInterface mediaInterface) {
        context = activity;
        this.activity = activity;
        this.storyItemModelList = storyItemModelList;
        this.mediaInterface = mediaInterface;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            insetsController = activity.getWindow().getInsetsController();
    }

    public MediaContentAdapter(Activity activity, Node node, MediaInterface mediaInterface) {
        context = activity;
        this.activity = activity;
        this.node = node;
        this.mediaInterface = mediaInterface;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            insetsController = activity.getWindow().getInsetsController();
    }

    public MediaContentAdapter(Activity activity, List<String> mediaStrings, MediaInterface mediaInterface) {
        context = activity;
        this.activity = activity;
        this.mediaInterface = mediaInterface;
        this.mediaStrings = mediaStrings;
        this.currentAlbumData = MediaFragment.getCurrentVisibleAlbumDataOnViewPager2();
        albumDataViewModel = new ViewModelProvider.AndroidViewModelFactory(activity.getApplication()).create(AlbumDataViewModel.class);
        utils = new Utils(activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            insetsController = activity.getWindow().getInsetsController();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uriPhoto = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
            uriVideo = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            uriPhoto = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            uriVideo = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }
    }

    public void setIntDefFlag(@DisplayOptions int displayOp1, @DisplayOptions int displayOp2, @DisplayOptions int displayOp3, @DisplayOptions int displayOp4, @DisplayOptions int displayOp5) {
        Log.d("TAG", "setIntDefFlag: ");
        insetsController.setSystemBarsAppearance(displayOp1 & displayOp2 & displayOp4 & displayOp5, displayOp1 & displayOp2 & displayOp4 & displayOp5);
//                insetsController.setSystemBarsAppearance(displayOp2,displayOp2);
//        insetsController.hide(WindowInsets.Type.systemBars());
//                insetsController.setSystemBarsAppearance(displayOp3,displayOp3);
//                insetsController.setSystemBarsAppearance(displayOp4,displayOp4);
//        insetsController.setSystemBarsAppearance(displayOp5,displayOp5);
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

//        setIntDefFlag(DISPLAY_OP_1, APPEARANCE_OPAQUE_NAVIGATION_BARS, APPEARANCE_LOW_PROFILE_BARS, APPEARANCE_LIGHT_STATUS_BARS, APPEARANCE_LIGHT_NAVIGATION_BARS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);


        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.medial_items, parent, false);
        viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull @NotNull MediaContentAdapter.ViewHolder holder, int position) {
        if (storyItemModelList != null) {
            ItemModel itemModel = storyItemModelList.get(position);
            showStories(itemModel, holder, position);
        } else if (node != null)
            showPhotos(node, holder, position);
        else
            showGalleryAlbum(mediaStrings, holder, position);
        holder.imageView.setOnTouchListener(this);
    }

    @Override
    public int getItemCount() {
        if (storyItemModelList != null) {
            return storyItemModelList.size();
        } else if (node != null) {
            if ("GraphSidecar".equals(node.get__typename()))
                return node.getEdge_sidecar_to_children().getEdges().size();
            return 1;
        } else {
            return mediaStrings.size();
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        /*setSystemUiFlags(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN, true);
        setFullscreenFlags(true);*/

        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

        } else {
//                        check how to get out of full screen
//            activity.getWindow().setUiOptions(SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//            activity.getWindow().setUiOptions(SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        return false;
    }

    public void setMediaStrings(List<String> mediaStrings) {
        this.mediaStrings = mediaStrings;
        notifyDataSetChanged();
    }

    private void showGalleryAlbum(List<String> mediaStrings, ViewHolder holder, int position) {
        try {
            holder.cardViewPlay.setVisibility(View.GONE);
            String str = mediaStrings.get(position);
            if (str.endsWith(".mp4")) {
                holder.loadImage(videos + str, true);
                holder.cardViewPlay.setVisibility(View.VISIBLE);
                holder.playButton.setOnClickListener(v -> {
                    mediaInterface.openVideoActivity(videos + str);
                });
            } else
                holder.loadImage(photos + str, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showPhotos(Node node, ViewHolder holder, int position) {
        try {
            holder.cardViewPlay.setVisibility(View.GONE);
            String str;
            boolean isVideo, isGraphSideCar;
            isGraphSideCar = node.get__typename().equals("GraphSidecar");
            if (!isGraphSideCar) {
                str = node.getDisplay_resources().get(2).getSrc();
                isVideo = node.isIs_video();
            } else {
                Node sideCarNode = node.getEdge_sidecar_to_children().getEdges().get(position).getNode();
                str = sideCarNode.getDisplay_resources().get(2).getSrc();
                isVideo = sideCarNode.isIs_video();
            }
            if (isVideo) {
                holder.cardViewPlay.setVisibility(View.VISIBLE);
                holder.playButton.setOnClickListener(v -> {
                    if (!isGraphSideCar)
                        mediaInterface.openVideoActivity(node.getVideo_url());
                    else
                        mediaInterface.openVideoActivity(node.getEdge_sidecar_to_children().getEdges().get(position).getNode().getVideo_url());
                });
            }
            holder.loadImage(str, isVideo);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showStories(ItemModel itemModel, @NotNull ViewHolder holder, int position) {
        try {
            holder.cardViewPlay.setVisibility(View.GONE);
            if (itemModel.getMedia_type() == 2) {
                holder.cardViewPlay.setVisibility(View.VISIBLE);
                holder.playButton.setOnClickListener(v -> {
                    mediaInterface.openVideoActivity(itemModel.getVideo_versions().get(0).getUrl());
                });
            }
            holder.loadImage(itemModel.getImage_versions2().getCandidates().get(0).getUrl(), itemModel.getMedia_type() == 2);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

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
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView playButton;
        private final ZoomageView imageView;
        private final CardView cardViewPlay;

        private ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            cardViewPlay = itemView.findViewById(R.id.playBotton_cardView);
            imageView = itemView.findViewById(R.id.media_image);
            playButton = itemView.findViewById(R.id.media_PlayButton);
//            Glide.with(context).load(R.drawable.ic_baseline_play_circle_filled_24).into(playButton);
        }

        private void loadImage(String actualPath, boolean isVideo) {
            Log.d("TAG", "loadImage: " + isVideo + "      " + actualPath);
            Glide.with(context)
                    .load(actualPath)
                    .error(R.drawable.no_image_available1)
                    .placeholder(R.drawable.ic_baseline_photo_24)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                            String fileNameWithoutExtension = actualPath.substring(49, actualPath.length() - 4);
                            String fileNameWithoutExtension = actualPath.substring(57, actualPath.length() - 4);
                            if (!isVideo) {
                                Cursor mediaCursorImage = activity.getContentResolver().query(uriPhoto, projectionPhoto, selectionPhoto, new String[]{"image/jpeg"}, MediaStore.Images.Media.DATE_MODIFIED + " desc");
                                if (mediaCursorImage != null) {
                                    int idIndex = mediaCursorImage.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                                    while (mediaCursorImage.moveToNext()) {
                                        String actualFilenameInsideGallery = mediaCursorImage.getString(idIndex);
                                        Log.d("TAG", "onLoadFailed: " + actualFilenameInsideGallery);
                                        if (actualFilenameInsideGallery.contains(fileNameWithoutExtension)) {
                                            boolean isContain = false;
                                            for(Album_Data singleAlbum : GalleryActivity.getAlbumDataList()) {
                                                if (singleAlbum.getMedia().stream().anyMatch(s -> s.contains(actualFilenameInsideGallery))) {
                                                    isContain = true;
                                                    break;
                                                }
                                            }
                                            if(!isContain){
                                                currentAlbumData.getMedia().set(getAdapterPosition(), utils.getDESTINATIONPATH() + "InstantPicture/" + actualFilenameInsideGallery);
                                                albumDataViewModel.updateSingleAlbumData(currentAlbumData);
                                            }
                                        }
                                    }
                                    mediaCursorImage.close();
                                }
                            } else {
                                Cursor mediaCursorVideo = activity.getContentResolver().query(uriVideo, projectionVideo, selectionVideo, new String[]{"video/mp4"}, MediaStore.Video.Media.DATE_MODIFIED + " desc");
                                if (mediaCursorVideo != null) {
                                    int idIndex = mediaCursorVideo.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
                                    while (mediaCursorVideo.moveToNext()) {
                                        String actualFilenameInsideGallery = mediaCursorVideo.getString(idIndex);
                                        Log.d("TAG", "onLoadFailedVideos: " + actualFilenameInsideGallery);
                                        if (actualFilenameInsideGallery.contains(fileNameWithoutExtension)) {
                                            boolean isContain = false;
                                            for(Album_Data singleAlbum : GalleryActivity.getAlbumDataList()) {
                                                if (singleAlbum.getMedia().stream().anyMatch(s -> s.contains(actualFilenameInsideGallery))) {
                                                    isContain = true;
                                                    break;
                                                }
                                            }
                                            if(!isContain){
                                                currentAlbumData.getMedia().set(getAdapterPosition(), utils.getDESTINATIONPATH() + "InstantVideos/" + actualFilenameInsideGallery);
                                                albumDataViewModel.updateSingleAlbumData(currentAlbumData);
                                            }
                                        }
                                    }
                                    mediaCursorVideo.close();
                                }
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                   //     @Override
                   //     public boolean onException(Exception e, Object model, Target<GlideDrawable> target, boolean isFirstResource) {
          
                    //     return false;
                   //     }
                    })
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerInside()
                    .into(imageView);
        }
    }
}
