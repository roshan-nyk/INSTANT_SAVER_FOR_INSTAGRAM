package instant.saver.for_instagram;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import instant.saver.for_instagram.databinding.ActivityAllUsersBinding;
import instant.saver.for_instagram.adapter.UserListAdapter;

import instant.saver.for_instagram.fragments.LogInFragment;
import instant.saver.for_instagram.interfaces.SelectedUserInterface;
import instant.saver.for_instagram.model.bookmark_profile.SavedProfileViewModel;
import instant.saver.for_instagram.model.bookmark_profile.Saved_Profile;
import instant.saver.for_instagram.model.story.TrayModel;
import instant.saver.for_instagram.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AllUsersActivity extends AppCompatActivity implements SelectedUserInterface, View.OnClickListener {

    private AllUsersActivity activity;
    private UserListAdapter userListAdapter;
    private ArrayList<TrayModel> trayModels;
    private ActivityAllUsersBinding binding;
    private SavedProfileViewModel savedProfileViewModel;
    private List<Saved_Profile> savedProfileList;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAllUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        activity = this;
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle.getSerializable("REEL_USERS") != null) {
            trayModels = (ArrayList<TrayModel>) bundle.getSerializable("REEL_USERS");
            Objects.requireNonNull(getSupportActionBar()).setTitle("Stories");
        }
        if ("book_marked".equals(bundle.getString("BOOK_MARKED_USERS", null))) {
            savedProfileViewModel = new ViewModelProvider.AndroidViewModelFactory(activity.getApplication())
                    .create(SavedProfileViewModel.class);
            Objects.requireNonNull(getSupportActionBar()).setTitle("Book Marked Users");
        }

        if (savedProfileViewModel != null) {
            savedProfileViewModel.getAllContacts().observe(activity, saved_profiles -> {
                savedProfileList = saved_profiles;
//                enter if condition only once before deleting any profile
                if (count == 0) {
                    userListAdapter = new UserListAdapter(activity, savedProfileList, activity);
                    if (savedProfileList.size() > 0)
                        createRecyclerView();
                    else
                        createTextView();
                    count = -5;
                } else {
                    if (savedProfileList.size() > 0) {
                        userListAdapter.setSaved_profiles(savedProfileList);
                        userListAdapter.uncheckAllSavedProfiles("Yes_Button");
                    } else createTextView();
                }
            });
        }

        utils = new Utils(activity);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return (super.onOptionsItemSelected(item));
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (trayModels != null) {
            if (trayModels.size() > 0) {
                userListAdapter = new UserListAdapter(activity, trayModels, activity);
                createRecyclerView();
            } else createTextView();
        }
    }


    private void createTextView() {
        binding.userListRecyclerView.setVisibility(View.GONE);
        binding.allUsersTextView.setVisibility(View.VISIBLE);
        binding.deleteSavedProfileConstraintLayout.setVisibility(View.GONE);
        if (trayModels != null)
            binding.allUsersTextView.setText("No User Available Right Now To Show");
        else
            binding.allUsersTextView.setText("No User Has Been Added To The BookMark");
    }

    private void createRecyclerView() {
        binding.allUsersTextView.setVisibility(View.GONE);
        binding.userListRecyclerView.setVisibility(View.VISIBLE);
        binding.userListRecyclerView.setLayoutManager(new GridLayoutManager(activity, 4, RecyclerView.HORIZONTAL, false));
        binding.userListRecyclerView.setAdapter(userListAdapter);
        userListAdapter.notifyDataSetChanged();
    }

    @Override
    public void selectedUserClick(int position, TrayModel trayModel) {
        Intent intent = new Intent(activity, SingleProfileActivity.class);
        intent.putExtra("UserId", String.valueOf(trayModel.getUser().getPk()));
        intent.putExtra("UserName", trayModel.getUser().getUsername());
        intent.putExtra("Saved_Profile_Profile_URL", trayModel.getUser().getProfile_pic_url());
        startActivity(intent);
    }

    private final LogInFragment logInFragment = LogInFragment.newInstance("All_Users_Activity");
    private Utils utils;
    private int clickedPosition;
    private Saved_Profile savedProfile;
    @Override
    public void selectedUserClick(int position, Saved_Profile savedProfile) {
        if(utils.getCookies() != null) {
            Intent intent = new Intent(activity, SingleProfileActivity.class);
            intent.putExtra("UserId", savedProfile.getUserId());
            intent.putExtra("UserName", savedProfile.getName());
            intent.putExtra("Saved_Profile_Position", position);
            intent.putExtra("Saved_Profile_Profile_URL", savedProfile.getProfile_pic_url());
            startActivity(intent);
        }
        else {
            clickedPosition = position;
            this.savedProfile = savedProfile;
            binding.userListRecyclerView.setVisibility(View.GONE);
            Objects.requireNonNull(getSupportActionBar()).hide();
            getSupportFragmentManager().beginTransaction().add(R.id.login_fragment_constraint_layout, logInFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void getCurrentPosition(int position) {
        Log.d("TAG", "getCurrentPosition: " + position);
        if (position == -5)
            binding.deleteSavedProfileConstraintLayout.setVisibility(View.GONE);
        else {
            binding.deleteSavedProfileConstraintLayout.setVisibility(View.VISIBLE);
            binding.deleteSavedProfileConstraintLayout.setOnClickListener(this);
            binding.deleteSavedProfileImageView.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == binding.deleteSavedProfileConstraintLayout || v == binding.deleteSavedProfileImageView) {
            binding.deleteSavedProfileCardViewConstraintLayout.setVisibility(View.VISIBLE);
            binding.deleteSavedProfileCardViewConstraintLayout.setOnClickListener(this);
            binding.deleteSavedProfileCardView.setOnClickListener(this);
            binding.deleteSavedProfileOptionYesButton.setOnClickListener(this);
            binding.deleteSavedProfileOptionNoButton.setOnClickListener(this);
        } else if (v == binding.deleteSavedProfileCardViewConstraintLayout || v == binding.deleteSavedProfileCardView)
            binding.deleteSavedProfileCardViewConstraintLayout.setVisibility(View.GONE);
        else if (v == binding.deleteSavedProfileOptionNoButton) {
            binding.deleteSavedProfileCardViewConstraintLayout.setVisibility(View.GONE);
            binding.deleteSavedProfileConstraintLayout.setVisibility(View.GONE);
            userListAdapter.uncheckAllSavedProfiles("No_Button");
        } else if (v == binding.deleteSavedProfileOptionYesButton) {
            ArrayList<Integer> profilePositionsToDelete = userListAdapter.getStoreUserPostionsToDelete();
            profilePositionsToDelete.sort(null);
            int temp = 0;
            for (int position : profilePositionsToDelete) {
                if (temp++ == 0)
                    userListAdapter.notifyItemRemoved(position);
                else userListAdapter.notifyItemRemoved(position - 1);
//                observer will be called only once after we get out of else if scope not every time we delete that's why we are deleting position not position-1
                savedProfileViewModel.deleteSingleSavedProfile(savedProfileList.get(position).getUserId());
            }
            binding.deleteSavedProfileCardViewConstraintLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if(binding.deleteSavedProfileCardViewConstraintLayout.getVisibility() == View.VISIBLE )
            binding.deleteSavedProfileCardViewConstraintLayout.setVisibility(View.GONE);
        else if( binding.deleteSavedProfileConstraintLayout.getVisibility() == View.VISIBLE) {
            binding.deleteSavedProfileConstraintLayout.setVisibility(View.GONE);
            userListAdapter.uncheckAllSavedProfiles("No_Button");
        }else {
            super.onBackPressed();
            if(getSupportFragmentManager().getBackStackEntryCount() == 0 )
                Objects.requireNonNull(getSupportActionBar()).show();
            if(binding.allUsersTextView.getVisibility() != View.VISIBLE)
                        binding.userListRecyclerView.setVisibility(View.VISIBLE);
            if (utils.getCookies() != null && savedProfile != null && clickedPosition != -5) {
                selectedUserClick(clickedPosition,savedProfile);
                savedProfile = null;
                clickedPosition = -5;
            }
        }
    }
}