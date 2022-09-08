package instant.saver.for_instagram.adapter;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import instant.saver.for_instagram.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import instant.saver.for_instagram.GalleryActivity;
import instant.saver.for_instagram.fragments.MediaFragment;
import instant.saver.for_instagram.interfaces.GalleryAlbumInterface;
import instant.saver.for_instagram.model.album_gallery.AlbumDataViewModel;
import instant.saver.for_instagram.model.album_gallery.Album_Data;
import instant.saver.for_instagram.util.Utils;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class GalleryAlbumAdapter extends RecyclerView.Adapter<GalleryAlbumAdapter.ViewHolder> {

    private final Context context;
    private final GalleryAlbumInterface galleryAlbumInterface;
//    private final String photos = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES).getAbsolutePath();
//    private final String videos = Environment.getExternalStoragePublicDirectory(DIRECTORY_MOVIES).getAbsolutePath();


    private final String photos = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getAbsolutePath();
    private final String videos = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getAbsolutePath();


    private final ArrayList<Integer> storeAlbumPositionsToDelete = new ArrayList<>();
    private final Uri uriPhoto, uriVideo;
    private final String[] projectionPhoto = {MediaStore.Images.Media.DISPLAY_NAME};
    private final String selectionPhoto = MediaStore.Images.Media.MIME_TYPE + "=? ";
    private final String[] projectionVideo = {MediaStore.Video.Media.DISPLAY_NAME};
    private final String selectionVideo = MediaStore.Video.Media.MIME_TYPE + "=? ";
    private final AlbumDataViewModel albumDataViewModel;
    private List<Album_Data> albumDataList;
    private int gallerySize;
    private boolean isUserLayoutLongPressed = false;
    private Cursor mediaCursorImage, mediaCursorVideo;
    private final Utils utils;

    public GalleryAlbumAdapter(GalleryAlbumInterface galleryAlbumInterface, Activity activity, List<Album_Data> albumDataList) {
        this.galleryAlbumInterface = galleryAlbumInterface;
        this.context = activity;
        this.albumDataList = albumDataList;
        gallerySize = albumDataList.size() - 1;
        utils = new Utils(activity);
        albumDataViewModel = new ViewModelProvider.AndroidViewModelFactory(activity.getApplication()).create(AlbumDataViewModel.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uriPhoto = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
            uriVideo = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            uriPhoto = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            uriVideo = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }
    }

    public ArrayList<Integer> getStoreAlbumPositionsToDelete() {
        return storeAlbumPositionsToDelete;
    }

    @NonNull
    @NotNull
    @Override
    public GalleryAlbumAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.gallery_image, parent, false);
        return new GalleryAlbumAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull GalleryAlbumAdapter.ViewHolder holder, int position) {
        ArrayList<String> stringArrayList = albumDataList.get(gallerySize - position).getMedia();
        String str = stringArrayList.get(0);

        holder.mediaType.setVisibility(View.GONE);
        Log.d("TAG", "onBindViewHolder: " + str);
        holder.loadImage(str, stringArrayList.size(), position);

        /* Bitmap thumb = ThumbnailUtils.createVideoThumbnail(photos + str , MediaStore.Video.Thumbnails.MINI_KIND);
            holder.imageView.setImageBitmap(thumb);*/

        if (!isUserLayoutLongPressed) {
//            Log.d("TAG", "onBindViewHolder: entered"+position);
            holder.deleteLayout.setVisibility(View.GONE);
            holder.dim_layout.setVisibility(View.GONE);

            holder.galley_dp_layout.setOnClickListener(v -> {
//                if condition is required as after longClick on layout, onClick can also be called so we need to prevent it
                if (!isUserLayoutLongPressed) {
                    galleryAlbumInterface.currentSingleAlbum(albumDataList.get(gallerySize - position));
                    Album_Data album_data = albumDataList.get(gallerySize - position);
                    MediaFragment mediaFragment = MediaFragment.newInstance(album_data.getMedia(), album_data.getUserId(), album_data.getUserName(), album_data.getMediaCaption(), album_data.getProfilePicUrl(), album_data.getProduct_type(), album_data.getShortcode());
                    galleryAlbumInterface.callMediaFragment(mediaFragment, position);
                }
            });
            holder.galley_dp_layout.setOnLongClickListener(v -> {
                isUserLayoutLongPressed = true;
                storeAlbumPositionsToDelete.add(gallerySize - position);
//                notifyDataSetChanged();
                notifyItemRangeChanged(0, albumDataList.size());
                galleryAlbumInterface.getCurrentPosition(position);
                return false;
            });
        } else {
            holder.deleteLayout.setVisibility(View.VISIBLE);
            holder.dim_layout.setVisibility(View.GONE);

//            as onBindViewHolder can be called anytime while scrolling up and down we need to check whether that position is previously selected or not
            if (storeAlbumPositionsToDelete.contains(gallerySize - position))
                holder.dim_layout.setVisibility(View.VISIBLE);

            holder.deleteLayout.setOnClickListener(v -> {
                if (holder.dim_layout.getVisibility() == View.GONE) {
                    holder.dim_layout.setVisibility(View.VISIBLE);
                    storeAlbumPositionsToDelete.add(gallerySize - position);
                } else {
                    holder.dim_layout.setVisibility(View.GONE);
                    storeAlbumPositionsToDelete.remove(Integer.valueOf(gallerySize - position));
                    if (storeAlbumPositionsToDelete.size() == 0)
                        uncheckAllSelectedAlbums();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return albumDataList != null ? albumDataList.size() : 0;
    }

    public List<Album_Data> getAlbumDataList() {
        return albumDataList;
    }

    public void setAlbumDataList(List<Album_Data> albumDataList) {
        this.albumDataList = albumDataList;
        gallerySize = this.albumDataList.size() - 1;
    }

    public void uncheckAllSelectedAlbums() {
        isUserLayoutLongPressed = false;
        galleryAlbumInterface.getCurrentPosition(-5);
        storeAlbumPositionsToDelete.clear();
//        notifyDataSetChanged();
//        if ("Clear_All_Options".equals(Button))
        notifyItemRangeChanged(0, albumDataList.size());
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageView;
        private final ImageView mediaType;
        private final View deleteLayout, galley_dp_layout, dim_layout;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.Gallery_Image);
            mediaType = itemView.findViewById(R.id.Gallery_Media_Type);
            galley_dp_layout = itemView.findViewById(R.id.Gallery_dp_Layout);
            deleteLayout = itemView.findViewById(R.id.Gallery_delete_layout);
            dim_layout = itemView.findViewById(R.id.gallery_image_dim_layout);
        }

        private void loadImage(String str, int size, int position) {
            Log.d("TAG", "loadImage: " + str);
            if (!str.endsWith(".mp4")) {
                Glide.with(context)
//                BitmapFactory.decodeFile(photos + str) this will make rendering slow
                        .load(photos + str)
                        .thumbnail(0.2f)
                        .error(R.drawable.no_image_available1)
                        .placeholder(R.drawable.ic_baseline_photo_24)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                mediaCursorImage = context.getContentResolver().query(uriPhoto, projectionPhoto, selectionPhoto, new String[]{"image/jpeg"}, MediaStore.Images.Media.DATE_MODIFIED + " desc");
                                if (mediaCursorImage != null) {
                                    int idIndex = mediaCursorImage.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
//                                    String fileNameWithoutExtension = str.substring(21, str.length() - 4);
                                    String fileNameWithoutExtension = str.substring(29, str.length() - 4);
                                    Album_Data tempAlbumData = albumDataList.get(gallerySize - position);
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
                                            if(!isContain && getAdapterPosition() < tempAlbumData.getMedia().size()){
                                                tempAlbumData.getMedia().set(getAdapterPosition(), utils.getDESTINATIONPATH() + "InstantPicture/" + actualFilenameInsideGallery);
                                                albumDataViewModel.updateSingleAlbumData(tempAlbumData);
                                            }
                                        }
                                    }
                                    mediaCursorImage.close();
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .centerCrop()
//                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(imageView);
            } else {
                mediaType.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(videos + str)
                        .thumbnail(0.2f)
                        .error(R.drawable.no_image_available1)
                        .placeholder(R.drawable.ic_baseline_photo_24)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable @org.jetbrains.annotations.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                mediaCursorVideo = context.getContentResolver().query(uriVideo, projectionVideo, selectionVideo, new String[]{"video/mp4"}, MediaStore.Video.Media.DATE_MODIFIED + " desc");
                                if (mediaCursorVideo != null) {
                                    int idIndex = mediaCursorVideo.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
//                                    String fileNameWithoutExtension = str.substring(21, str.length() - 4);
                                    String fileNameWithoutExtension = str.substring(29, str.length() - 4);
                                    Album_Data tempAlbumData = albumDataList.get(gallerySize - position);
                                    while (mediaCursorVideo.moveToNext()) {
                                        String actualFilenameInsideGallery = mediaCursorVideo.getString(idIndex);
                                        if (actualFilenameInsideGallery.contains(fileNameWithoutExtension)) {
                                            boolean isContain = false;
                                            for(Album_Data singleAlbum : GalleryActivity.getAlbumDataList()) {
                                                if (singleAlbum.getMedia().stream().anyMatch(s -> s.contains(actualFilenameInsideGallery))) {
                                                    isContain = true;
                                                    break;
                                                }
                                            }
                                            if(!isContain){
                                                tempAlbumData.getMedia().set(getAdapterPosition(), utils.getDESTINATIONPATH() + "InstantVideos/" + actualFilenameInsideGallery);
                                                albumDataViewModel.updateSingleAlbumData(tempAlbumData);
                                            }
                                        }
                                    }
                                    mediaCursorVideo.close();
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .centerCrop()
//                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(imageView);
                Glide.with(context)
                        .load(ResourcesCompat.getDrawable(context.getResources(), R.drawable.instagram_video_logo, null))
                        .into(mediaType);
            }
            if (size > 1) {
                mediaType.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_multiple_media, null))
                        .into(mediaType);
            }
        }
    }
}
