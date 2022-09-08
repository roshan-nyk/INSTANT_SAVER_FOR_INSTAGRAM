package instant.saver.for_instagram.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import instant.saver.for_instagram.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import instant.saver.for_instagram.fragments.SavedItemFragment;
import instant.saver.for_instagram.interfaces.PhotoInterface;
import instant.saver.for_instagram.model.Edge;
import instant.saver.for_instagram.model.PhotoEdgeSavedMedia;
import instant.saver.for_instagram.model.story.ItemModel;

public class SavedItemAdapter extends RecyclerView.Adapter<SavedItemAdapter.SavedItemViewHolder> {

    private final Context context;
    private PhotoEdgeSavedMedia photoEdgeSavedMedia;
    private final PhotoInterface photoInterface;
    private ArrayList<Edge> photoData;
    private ArrayList<ItemModel> itemModels;
    private final int PHOTO_ITEM_VIEW = 1, PROGRESS_ITEM_VIEW = 2;
    private int currentPosition;
    private String current_endCursor;
    private boolean hasNextPage;


    public SavedItemAdapter(Context context, PhotoEdgeSavedMedia edgeSavedMedia, PhotoInterface photoInterface) {
        this.context = context;
        this.photoEdgeSavedMedia = edgeSavedMedia;
        this.photoInterface = photoInterface;
        hasNextPage = photoEdgeSavedMedia.getPageInfo().isHas_next_page();
        photoData = (ArrayList<Edge>) photoEdgeSavedMedia.getEdgeList();
        Log.d("TAG", "SavedItemAdapter: "+photoData.size());
        current_endCursor = this.photoEdgeSavedMedia.getPageInfo().getEnd_cursor();
    }

    public SavedItemAdapter(Context context, ArrayList<ItemModel> items, PhotoInterface photoInterface) {
        this.context = context;
        this.photoInterface = photoInterface;
        itemModels = items;
    }


    private SavedItemViewHolder savedItemViewHolder;

    @NonNull
    @NotNull
    @Override
    public SavedItemViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        if(photoData != null) {
            if (viewType == PHOTO_ITEM_VIEW) {
                view = layoutInflater.inflate(R.layout.photo_list_details, parent, false);
                savedItemViewHolder = new SavedItemViewHolder(view, 1);
            } else {
                view = layoutInflater.inflate(R.layout.content_loading_layout, parent, false);
                savedItemViewHolder = new SavedItemViewHolder(view, 2);
            }
        }
        else{
            view = layoutInflater.inflate(R.layout.stories_details_list, parent, false);
            savedItemViewHolder = new SavedItemViewHolder(view, 1);
        }
        return savedItemViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SavedItemAdapter.SavedItemViewHolder holder, int position) {
        Log.d("TAG", "onBindViewHolder: posts" + position);
        if(photoData != null) {
            if (position < photoData.size()) {
                Edge itemModel = photoData.get(position);
                holder.addPhotoIntoFrame(itemModel);
            } else
                holder.progressBar.setVisibility(View.VISIBLE);
        }
        else
            holder.addPhotoIntoFrame(itemModels.get(position));
        currentPosition = position;
    }


    @Override
    public int getItemCount() {
     if(photoData != null)   return hasNextPage ? photoData.size() + 1 : photoData.size();
     else return itemModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(photoData != null) {
            if (position == photoData.size())
                return PROGRESS_ITEM_VIEW;
            else
                return PHOTO_ITEM_VIEW;
        }
        else
            return PHOTO_ITEM_VIEW;
    }

    public void addPhotosAndEndCursor(PhotoEdgeSavedMedia photoEdgeSavedMedia) {
        hasNextPage = photoEdgeSavedMedia.getPageInfo().isHas_next_page();
        if (current_endCursor.equals(photoEdgeSavedMedia.getPageInfo().getEnd_cursor())) {
            photoInterface.getMorePhotos(photoEdgeSavedMedia.getPageInfo().getEnd_cursor());
            Log.d("TAG", "addPhotosAndEndCursor: " + "same");
        } else {
            current_endCursor = photoEdgeSavedMedia.getPageInfo().getEnd_cursor();
            photoData.addAll(photoEdgeSavedMedia.getEdgeList());
            Log.d("TAG", "addPhotosAndEndCursor: "+photoEdgeSavedMedia.getEdgeList().size()+"     "+photoData.size());
            notifyDataSetChanged();
            SavedItemFragment.setIsAccessingData(false);
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

    protected class SavedItemViewHolder extends RecyclerView.ViewHolder {
        private View layout;
        private ImageView imageView;
        private ProgressBar progressBar;

       private SavedItemViewHolder(@NonNull @NotNull View itemView, int viewType) {
            super(itemView);
            if (viewType == 1) {
               if(photoData != null) {
                   layout = itemView.findViewById(R.id.PhotoDetailsLayout);
                   imageView = itemView.findViewById(R.id.photo_details_image);
                   layout.setOnClickListener(v -> photoInterface.photosFullViewClick(photoData.get(getAdapterPosition())));
               }
               else {
                   layout = itemView.findViewById(R.id.storyDetailsLayout);
                   imageView = itemView.findViewById(R.id.story_details_image);
                   layout.setOnClickListener(v ->  photoInterface.photosFullViewClick(getAdapterPosition()));
               }
            } else
                progressBar = itemView.findViewById(R.id.photo_progress);
        }

        private void addPhotoIntoFrame(Edge itemModel) {
            Glide.with(context)
                    .load(itemModel.getNode().getDisplay_url())
//                    .load(itemModel.getNode().getThumbnail_resources().get(4).getSrc())
                    .placeholder(R.drawable.ic_baseline_photo_24)
                    .error(R.drawable.no_image_available1)
                    .centerCrop()
                    .into(imageView);
        }

        private void addPhotoIntoFrame(ItemModel itemModel) {
            Glide.with(context)
                    .load(itemModel.getImage_versions2().getCandidates().get(0).getUrl())
//                    .load(itemModel.getNode().getThumbnail_resources().get(4).getSrc())
                    .placeholder(R.drawable.ic_baseline_photo_24)
                    .error(R.drawable.no_image_available1)
                    .centerCrop()
                    .into(imageView);
        }
    }
}

