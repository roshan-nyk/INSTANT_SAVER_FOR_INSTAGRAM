package instant.saver.for_instagram.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import instant.saver.for_instagram.PhotosFragment;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.insta_saver.R;

import instant.saver.for_instagram.SingleProfileActivity;
import instant.saver.for_instagram.interfaces.PhotoInterface;
import instant.saver.for_instagram.model.DisplayResource;
import instant.saver.for_instagram.model.Edge;
import instant.saver.for_instagram.model.Node;
import instant.saver.for_instagram.model.PhotoTimeLineMedia;
import instant.saver.for_instagram.model.album_gallery.AlbumDataViewModel;
import instant.saver.for_instagram.model.album_gallery.Album_Data;
import instant.saver.for_instagram.util.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PhotoDetailsAdapter extends RecyclerView.Adapter< PhotoDetailsAdapter.PhotoViewHolder> {

    private final Context context;
    private final PhotoTimeLineMedia photoTimeLineMedia;
    private final PhotoInterface photoInterface;
    private final ArrayList<Edge> photoData;
    private int currentPosition;
    private String current_endCursor;
    private boolean hasNextPage;
    private final int PHOTO_ITEM_VIEW = 1, PROGRESS_ITEM_VIEW = 2;
    private final List<Album_Data> albumData;
    private final Utils utils;

    public PhotoDetailsAdapter(Activity activity, PhotoTimeLineMedia photoTimeLineMedia, PhotoInterface photoInterface) {
        this.context = activity;
        this.photoTimeLineMedia = photoTimeLineMedia;
        this.photoInterface = photoInterface;
        hasNextPage = photoTimeLineMedia.getPageInfo().isHas_next_page();
        photoData = (ArrayList<Edge>) photoTimeLineMedia.getEdgeList();
        current_endCursor = this.photoTimeLineMedia.getPageInfo().getEnd_cursor();
        utils = new Utils(activity);
        albumData = SingleProfileActivity.getAlbumData();
    }

    @NonNull
    @NotNull
    @Override
    public  PhotoDetailsAdapter.PhotoViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view ;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        PhotoViewHolder photoViewHolder;
        if(viewType == PHOTO_ITEM_VIEW) {
            view = layoutInflater.inflate(R.layout.photo_list_details, parent, false);
            photoViewHolder = new PhotoViewHolder(view,1);
        }
        else {
            view = layoutInflater.inflate(R.layout.content_loading_layout, parent, false);
            photoViewHolder = new PhotoViewHolder(view,2);
        }
        return photoViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PhotoDetailsAdapter.PhotoViewHolder holder, int position) {
        Log.d("TAG", "onBindViewHolder: posts"+position);
        if (position < photoData.size()) {
            Edge itemModel = photoData.get(position);
            holder.imageType.setVisibility(View.GONE);
            holder.addPhotoIntoFrame(itemModel.getNode());
        }
        else
            holder.progressBar.setVisibility(View.VISIBLE);
        currentPosition = position;

//        write the condition when there is no next page
    }

    @Override
    public int getItemCount() {
        return hasNextPage ? photoData.size() + 1  : photoData.size() ;
    }

    @Override
    public int getItemViewType(int position) {
               if(position == photoData.size())
                   return PROGRESS_ITEM_VIEW;
               else
                   return PHOTO_ITEM_VIEW;
    }

    public void addPhotosAndEndCursor(PhotoTimeLineMedia photoTimeLineMedia) {
        hasNextPage = photoTimeLineMedia.getPageInfo().isHas_next_page();
        if(current_endCursor.equals(photoTimeLineMedia.getPageInfo().getEnd_cursor())) {
            photoInterface.getMorePhotos(photoTimeLineMedia.getPageInfo().getEnd_cursor());
            Log.d("TAG", "addPhotosAndEndCursor: "+"same");
        }
       else{
            current_endCursor = photoTimeLineMedia.getPageInfo().getEnd_cursor();
            photoData.addAll(photoTimeLineMedia.getEdgeList());
            notifyDataSetChanged();
            PhotosFragment.setIsAccessingData(false);
        }
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public String getCurrent_endCursor() {
        return current_endCursor;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public ArrayList<Edge> getPhotoData() {
        return photoData;
    }

    protected class PhotoViewHolder extends RecyclerView.ViewHolder {
        private  View layout;
        private  ImageView imageView, imageType;
        private CardView alreadyDownloaded;
        private ProgressBar progressBar;

        private PhotoViewHolder(@NonNull @NotNull View itemView,int viewType) {
            super(itemView);
            if(viewType == 1) {
                layout = itemView.findViewById(R.id.PhotoDetailsLayout);
                imageView = itemView.findViewById(R.id.photo_details_image);
                imageType = itemView.findViewById(R.id.photo_details_image_type);
                alreadyDownloaded = itemView.findViewById(R.id.photo_details_already_downloaded);
                layout.setOnClickListener(v -> photoInterface.photosFullViewClick(photoData.get(getAdapterPosition())));
            }
            else
                progressBar = itemView.findViewById(R.id.photo_progress);
        }

       private void addPhotoIntoFrame(Node itemModel){
            alreadyDownloaded.setVisibility(View.GONE);
            imageType.setVisibility(View.GONE);
            Glide.with(context)
                    .load(itemModel.getDisplay_url())
//                    .load(itemModel.getNode().getThumbnail_resources().get(4).getSrc())
                    .centerCrop()
                    .into(imageView);

           if(itemModel.get__typename().equals("GraphSidecar")) {
               imageType.setVisibility(View.VISIBLE);
               Glide.with(context).load(R.drawable.ic_multiple_media).into(imageType);
               List<Edge> edgeArrayList = itemModel.getEdge_sidecar_to_children().getEdges();
               ArrayList<String> stringsUrlsToDownload = new ArrayList<>();
               for (int i = 0; i < edgeArrayList.size(); i++) {
                   Node node = edgeArrayList.get(i).getNode();
                   if (node.isIs_video())
                       stringsUrlsToDownload.add(node.getVideo_url());
                   else {
                       List<DisplayResource> displayResources = node.getDisplay_resources();
                       stringsUrlsToDownload.add(displayResources.get(displayResources.size() - 1).getSrc());
                   }
               }
               if (utils.checkForAlreadyExistedFile(stringsUrlsToDownload, "", albumData))
                   alreadyDownloaded.setVisibility(View.VISIBLE);
           }
           else {
               if (itemModel.isIs_video()) {
                   imageType.setVisibility(View.VISIBLE);
                   Glide.with(context).load(R.drawable.instagram_video_logo).into(imageType);
                   if (utils.checkForAlreadyExistedFile(new ArrayList<>(), itemModel.getVideo_url(), albumData))
                       alreadyDownloaded.setVisibility(View.VISIBLE);
               }else {
                   List<DisplayResource> displayResources = itemModel.getDisplay_resources();
                   if (utils.checkForAlreadyExistedFile(new ArrayList<>(), displayResources.get(displayResources.size()-1).getSrc(), albumData))
                       alreadyDownloaded.setVisibility(View.VISIBLE);
               }
           }
        }
    }
}
