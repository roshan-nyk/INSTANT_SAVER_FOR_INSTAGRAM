package instant.saver.for_instagram.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.insta_saver.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import instant.saver.for_instagram.interfaces.StoriesDetailsInterface;
import instant.saver.for_instagram.model.Edge;
import instant.saver.for_instagram.model.Node;

public class StoryHighlightDPAdapter extends RecyclerView.Adapter<StoryHighlightDPAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Edge> storyHighlightsList;
    private final StoriesDetailsInterface storiesDetailsInterface;

    public StoryHighlightDPAdapter(Context context, List<Edge> storyHighlightsList, StoriesDetailsInterface storiesDetailsInterface) {
        this.context = context;
        this.storyHighlightsList = (ArrayList<Edge>) storyHighlightsList;
        this.storiesDetailsInterface = storiesDetailsInterface;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_user_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull StoryHighlightDPAdapter.ViewHolder holder, int position) {
        Node node = storyHighlightsList.get(position).getNode();
        Glide.with(context)
                .load(node.getCover_media().getThumbnail_src())
                .placeholder(R.drawable.ic_baseline_photo_24)
                .error(R.drawable.no_image_available1)
                .into(holder.imageView);
        holder.realName.setText(node.getTitle());
        holder.storyHighlightDpLayout.setOnClickListener(v -> storiesDetailsInterface.storiesHighlightFullViewClick(node.getId()));
    }

    @Override
    public int getItemCount() {
        return storyHighlightsList.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private final View storyHighlightDpLayout;
        private final ImageView imageView;
        private final TextView realName;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            storyHighlightDpLayout = itemView.findViewById(R.id.User_dp_Layout);
            imageView = itemView.findViewById(R.id.story_icon);
            realName = itemView.findViewById(R.id.real_name);
        }
    }
}
