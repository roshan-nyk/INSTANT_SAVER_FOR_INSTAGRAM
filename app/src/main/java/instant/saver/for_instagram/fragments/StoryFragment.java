package instant.saver.for_instagram.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import instant.saver.for_instagram.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import instant.saver.for_instagram.Saved_Collection_Instagram_Activity;
import instant.saver.for_instagram.SingleProfileActivity;
import instant.saver.for_instagram.adapter.StoriesDetailsAdapter;
import instant.saver.for_instagram.adapter.StoryHighlightDPAdapter;
import instant.saver.for_instagram.interfaces.StoriesDetailsInterface;
import instant.saver.for_instagram.model.Edge;
import instant.saver.for_instagram.model.PhotosFeedModel;
import instant.saver.for_instagram.model.story.FullDetailModel;
import instant.saver.for_instagram.model.story.ItemModel;

public class StoryFragment extends Fragment implements StoriesDetailsInterface {

    private FullDetailModel response;
    private StoriesDetailsAdapter storiesListAdapter;
    private String userName, savedProfilePicUrl;
    private PhotosFeedModel storyHighlightModel;
    private StoryHighlightDPAdapter storyHighlightDPAdapter;
    private MediaFragment mediaFragment;
    private View storyFragmentLayout;

    public StoryFragment() {
        // Required empty public constructor
    }

    public static StoryFragment newInstance(@NotNull FullDetailModel response, String userName, String savedProfilePicURL, PhotosFeedModel storyHighlightModel) {
        StoryFragment fragment = new StoryFragment();
        Bundle args = new Bundle();
        args.putSerializable("Single_User_Stories", response);
        args.putSerializable("Story_Highlight_Model", storyHighlightModel);
        args.putString("USER_NAME", userName);
        args.putString("Saved_Profile_Pic", savedProfilePicURL);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && response == null) {
            response = (FullDetailModel) getArguments().getSerializable("Single_User_Stories");
            userName = getArguments().getString("USER_NAME");
            savedProfilePicUrl = getArguments().getString("Saved_Profile_Pic");
            storyHighlightModel = (PhotosFeedModel) getArguments().getSerializable("Story_Highlight_Model");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_story, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        storyFragmentLayout = view.findViewById(R.id.storyFragment_constraint_layout);
        List<Edge> highlightEdgeList = storyHighlightModel.getData().getUser().getEdgeHighlightReels().getEdgeList();
        if (highlightEdgeList.size() > 0) {
            RecyclerView recyclerView = view.findViewById(R.id.Stories_highlights_RecyclerView);
            recyclerView.setVisibility(View.VISIBLE);
            if (storyHighlightDPAdapter == null)
                storyHighlightDPAdapter = new StoryHighlightDPAdapter(requireActivity(), highlightEdgeList, StoryFragment.this);
            recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 1, RecyclerView.HORIZONTAL, false));
            recyclerView.setAdapter(storyHighlightDPAdapter);
            storyHighlightDPAdapter.notifyDataSetChanged();
        } else {
            RecyclerView recyclerView = view.findViewById(R.id.Stories_highlights_RecyclerView);
            recyclerView.setVisibility(View.GONE);
        }
        if (response.getReels_media().size() > 0) {
            RecyclerView recyclerView;
            if (highlightEdgeList.size() > 0)
                recyclerView = view.findViewById(R.id.Stories_RecyclerView1);
            else recyclerView = view.findViewById(R.id.Stories_RecyclerView2);
            recyclerView.setVisibility(View.VISIBLE);
            if (storiesListAdapter == null)
                storiesListAdapter = new StoriesDetailsAdapter(requireContext(), response.getReels_media().get(0).getItems(), StoryFragment.this);
            recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3, RecyclerView.HORIZONTAL, false));
            recyclerView.setAdapter(storiesListAdapter);
            storiesListAdapter.notifyDataSetChanged();
        } else {
            TextView textView = view.findViewById(R.id.story_fragment_textView);
            textView.setVisibility(View.VISIBLE);
            textView.setText("Sorry... No Stories Available Right Now To Show.");
        }
    }

    @Override
    public void storiesFullViewClick(ArrayList<ItemModel> storyItemModelList, int adapterPosition) {
        SingleProfileActivity.setAnyOneButtonTouched(true);
        MediaFragment demoFragment = MediaFragment.newInstance(storyItemModelList, adapterPosition, userName, savedProfilePicUrl);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.single_prfile_constraint_layout, demoFragment)
                .addToBackStack(null)
                .commit();
        mediaFragment = demoFragment;
    }


    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("TAG", "onSaveInstanceState story fragment: " + outState.toString());
    }

    @Override
    public void storiesHighlightFullViewClick(String id) {
        Intent intent = new Intent(requireActivity(), Saved_Collection_Instagram_Activity.class);
        intent.putExtra("USER_ID", id);
        intent.putExtra("STORY_HIGHLIGHT", "story_highlight");
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        Log.d("TAG", "onDestroyView: StoryFragment:-" + "called");
        if (mediaFragment != null)
            mediaFragment = null;
        super.onDestroyView();
    }

    public void startDownload() {
        if (mediaFragment != null) mediaFragment.startDownload();
        mediaFragment = null;
    }

    public void setBackground() {
        if (storyFragmentLayout != null) {
            if (storyFragmentLayout.getVisibility() == View.INVISIBLE)
                storyFragmentLayout.setVisibility(View.VISIBLE);
            else storyFragmentLayout.setVisibility(View.INVISIBLE);
        }
    }
}