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

import java.util.ArrayList;
import java.util.List;

import instant.saver.for_instagram.interfaces.SelectedUserInterface;
import instant.saver.for_instagram.model.bookmark_profile.Saved_Profile;
import instant.saver.for_instagram.model.story.TrayModel;
import instant.saver.for_instagram.model.story.UserModel;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {
    private final Context context;
    private final SelectedUserInterface selectedUserInterface;
    private ArrayList<TrayModel> trayModelArrayList;
    private List<Saved_Profile> saved_profiles;
    private boolean isUserLayoutLongPressed = false;
    private final ArrayList<Integer> storeUserPositionsToDelete = new ArrayList<>();
    private int userListSize;

    public UserListAdapter(Context context, ArrayList<TrayModel> list, SelectedUserInterface listInterface) {
        this.context = context;
        this.trayModelArrayList = list;
        this.selectedUserInterface = listInterface;
    }

    public UserListAdapter(Context context, List<Saved_Profile> saved_profiles, SelectedUserInterface listInterface) {
        this.context = context;
        this.saved_profiles = saved_profiles;
        this.selectedUserInterface = listInterface;
        userListSize = this.saved_profiles.size() -1 ;
    }

    public ArrayList<Integer> getStoreUserPostionsToDelete() {
        return storeUserPositionsToDelete;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        View view = layoutInflater.inflate(R.layout.item_user_list, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        if (trayModelArrayList != null) {
            UserModel userModel = trayModelArrayList.get(position).getUser();
            if(userModel != null) {
                viewHolder.realName.setText(userModel.getFull_name());
                Glide.with(context).load(userModel.getProfile_pic_url())
                        .thumbnail(0.2f).into(viewHolder.storyIcon);
                viewHolder.User_dp_layout.setOnClickListener(v -> {
                    selectedUserInterface.selectedUserClick(position, trayModelArrayList.get(position));
                });
            }
        } else {
            viewHolder.realName.setText(saved_profiles.get(userListSize - position).getName());
            Glide.with(context).load(saved_profiles.get(userListSize - position).getProfile_pic_url()).thumbnail(0.2f).into(viewHolder.storyIcon);
            if (!isUserLayoutLongPressed) {
                viewHolder.deleteLayout.setVisibility(View.GONE);
                viewHolder.deleteDone.setVisibility(View.GONE);
                viewHolder.User_dp_layout.setOnClickListener(v -> {
                    if (!isUserLayoutLongPressed)
                        selectedUserInterface.selectedUserClick(position, saved_profiles.get(userListSize - position));
                });
                viewHolder.User_dp_layout.setOnLongClickListener(v -> {
                    isUserLayoutLongPressed = true;
                    storeUserPositionsToDelete.add(userListSize - position);
                    notifyItemRangeChanged(0, saved_profiles.size());
                    selectedUserInterface.getCurrentPosition(position);
                    return false;
                });
            } else {
                viewHolder.deleteLayout.setVisibility(View.VISIBLE);
                viewHolder.deleteDone.setVisibility(View.GONE);
                if(storeUserPositionsToDelete.contains(userListSize - position))
                    viewHolder.deleteDone.setVisibility(View.VISIBLE);
                viewHolder.deleteLayout.setOnClickListener(v -> {
                    if (viewHolder.deleteDone.getVisibility() == View.GONE) {
                        viewHolder.deleteDone.setVisibility(View.VISIBLE);
                        storeUserPositionsToDelete.add(userListSize -position);
                    } else {
                        viewHolder.deleteDone.setVisibility(View.GONE);
                        storeUserPositionsToDelete.remove(Integer.valueOf(userListSize -position));
                        if (storeUserPositionsToDelete.size() == 0)
                            uncheckAllSavedProfiles("Random");
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return trayModelArrayList == null ? saved_profiles.size() : trayModelArrayList.size();
    }

    public void setSaved_profiles(List<Saved_Profile> saved_profiles) {
        this.saved_profiles = saved_profiles;
        userListSize = this.saved_profiles.size() -1 ;
    }

    public void uncheckAllSavedProfiles(String Button) {
        isUserLayoutLongPressed = false;
        selectedUserInterface.getCurrentPosition(-5);
        if (Button.equals("Yes_Button")) notifyDataSetChanged();
        else notifyItemRangeChanged(0, saved_profiles.size());
        storeUserPositionsToDelete.clear();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView realName;
        private final ImageView storyIcon, deleteDone;
        private final View User_dp_layout, deleteLayout;

        public ViewHolder(View view) {
            super(view);
            realName = view.findViewById(R.id.real_name);
            storyIcon = view.findViewById(R.id.story_icon);
            User_dp_layout = view.findViewById(R.id.User_dp_Layout);
            deleteLayout = view.findViewById(R.id.saved_profile_delete_layout);
            deleteDone = view.findViewById(R.id.saved_profile_delete_done_imageView);
        }
    }
}