package instant.saver.for_instagram.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.insta_saver.R;
import instant.saver.for_instagram.interfaces.StoriesDetailsInterface;
import instant.saver.for_instagram.model.story.ItemModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class StoriesDetailsAdapter extends RecyclerView.Adapter<StoriesDetailsAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<ItemModel> storyItemModelList;
    private final StoriesDetailsInterface storiesDetailsInterface;

    public StoriesDetailsAdapter(Context context, ArrayList<ItemModel> storyItemModelList, StoriesDetailsInterface storiesDetailsInterface) {
        this.context = context;
        this.storyItemModelList = storyItemModelList;
        this.storiesDetailsInterface = storiesDetailsInterface;
    }

    @NonNull
    @NotNull
    @Override
    public StoriesDetailsAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.stories_details_list, parent, false);
        return new StoriesDetailsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull StoriesDetailsAdapter.ViewHolder holder, int position) {
        Log.d("TAG", "onBindViewHolder: stories"+position);
        if(position < storyItemModelList.size()){
            ItemModel itemModel = storyItemModelList.get(position);
            holder.imageType.setVisibility(View.GONE);
            if (itemModel != null) {
                Glide.with(context)
                        .load(itemModel.getImage_versions2().getCandidates().get(0).getUrl())
                        .error(R.drawable.no_image_available1)
                        .placeholder(R.drawable.ic_baseline_photo_24)
                        .centerCrop()
                        .into(holder.imageView);
                if(itemModel.getMedia_type() == 2)
                    holder.imageType.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return storyItemModelList == null ? 0 : storyItemModelList.size() ;
    }


    protected class ViewHolder extends RecyclerView.ViewHolder {

        private final View layout;
        private final ImageView imageView, imageType;

        private ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.storyDetailsLayout);
            imageView = itemView.findViewById(R.id.story_details_image);
            imageType = itemView.findViewById(R.id.story_details_image_type);
            layout.setOnClickListener(v ->
                    storiesDetailsInterface.storiesFullViewClick(storyItemModelList,getAdapterPosition())
            );
        }
    }
}