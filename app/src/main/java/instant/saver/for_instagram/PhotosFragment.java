package instant.saver.for_instagram;

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

import com.example.insta_saver.R;

import org.jetbrains.annotations.NotNull;

import instant.saver.for_instagram.adapter.PhotoDetailsAdapter;
import instant.saver.for_instagram.api.GetDataFromServer;
import instant.saver.for_instagram.interfaces.PhotoInterface;
import instant.saver.for_instagram.model.Edge;
import instant.saver.for_instagram.model.PhotosFeedModel;
import instant.saver.for_instagram.util.Utils;
import io.reactivex.observers.DisposableObserver;

public class PhotosFragment extends Fragment implements PhotoInterface {

    private static boolean isAccessingData;
    private PhotosFeedModel photosModel;
    private PhotoDetailsAdapter photoDetailsAdapter;
    private String queryHash;
    private RecyclerView recyclerView;
    private GetDataFromServer getDataFromServer;
    private Utils utils;
    private String userId, savedProfilePicUrl;
    private int count_getMorePhotosMethod = 1;
    private boolean isScrollDown = false;
    private View photoFragmentLayout;
    private MediaFragment mediaFragment;
    private final DisposableObserver<PhotosFeedModel> photoDetailObserver = new DisposableObserver<PhotosFeedModel>() {
        @Override
        public void onNext(@NotNull PhotosFeedModel response) {
            try {
                photoDetailsAdapter.addPhotosAndEndCursor(response.getData().getUser().getPhotoTimeLineMedia());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(@NotNull Throwable throwable) {
            Log.e("TAG", "onerror: " + throwable.getCause() + "        " + throwable.getMessage() + "      " + throwable.getLocalizedMessage());
        }

        @Override
        public void onComplete() {
        }
    };
    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull @NotNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (isScrollDown && isAccessingData && photoDetailsAdapter.isHasNextPage() && count_getMorePhotosMethod == 1) {
                    Log.d("TAG", "onScrollStateChanged: " + newState);
                    getMorePhotos(photoDetailsAdapter.getCurrent_endCursor());
                    count_getMorePhotosMethod++;
                }
            }
        }

        @Override
        public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            Log.d("TAG", "onScrolled: " + isAccessingData + "   " + photoDetailsAdapter.getCurrentPosition() + "    " + dy);
            if (dy >= 0 && (photoDetailsAdapter.getCurrentPosition() == photoDetailsAdapter.getPhotoData().size() - 1 || photoDetailsAdapter.getCurrentPosition() == photoDetailsAdapter.getPhotoData().size()) && !isAccessingData) {
                isScrollDown = true;
                isAccessingData = true;
                count_getMorePhotosMethod = 1;
                Log.d("TAG", "onScrolled: ");
            } /*else if (dy == 0 && photoDetailsAdapter.getCurrentPosition() == photoDetailsAdapter.getPhotoData().size() - 1 && photoDetailsAdapter.getCurrentPosition() == photoDetailsAdapter.getPhotoData().size())
                isScrollDown = true;*/ else if (dy < 0)
                isScrollDown = false;
        }
    };

    public PhotosFragment() {
        // Required empty public constructor
    }

    public static void setIsAccessingData(boolean isAccessingData) {
        PhotosFragment.isAccessingData = isAccessingData;
    }

    /* public static PhotosFragment newInstance(@NotNull PhotosFeedModel response, String userId, String savedProfilePicURL, String queryHash) {*/
    public static PhotosFragment newInstance(String userId, String savedProfilePicURL, String queryHash) {
        PhotosFragment fragment = new PhotosFragment();
        Bundle args = new Bundle();
//        args.putSerializable("PHOTOS", response);
        args.putString("USER_ID", userId);
        args.putString("Saved_Profile_Pic", savedProfilePicURL);
        args.putString("QUERY_HASH", queryHash);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TAG", "onCreated: " + "called");
        if (getArguments() != null && photosModel == null) {
            photosModel = SingleProfileActivity.getPhotosFeedModel();
            userId = getArguments().getString("USER_ID");
            savedProfilePicUrl = getArguments().getString("Saved_Profile_Pic");
            queryHash = getArguments().getString("QUERY_HASH");
        }
        isAccessingData = false;
        getDataFromServer = GetDataFromServer.getInstance();
        utils = new Utils(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("TAG", "onViewCreated: " + "called");
        photoFragmentLayout = view.findViewById(R.id.photoFragment_constraint_layout);
        if (photosModel.getData().getUser().getPhotoTimeLineMedia().getCount() > 0) {
            recyclerView = view.findViewById(R.id.Photos_RecyclerView);
            recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false));
            recyclerView.setHasFixedSize(true);
            recyclerView.addOnScrollListener(onScrollListener);
            recyclerView.setVisibility(View.VISIBLE);
            if (photoDetailsAdapter == null)
                photoDetailsAdapter = new PhotoDetailsAdapter(requireActivity(), photosModel.getData().getUser().getPhotoTimeLineMedia(), PhotosFragment.this);
            else
                isAccessingData = false;
            recyclerView.setAdapter(photoDetailsAdapter);
            photoDetailsAdapter.notifyDataSetChanged();
        } else {
            TextView textView = view.findViewById(R.id.photo_fragment_textView);
            recyclerView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            textView.setText("Sorry... No Posts Available Right Now To Show.");
        }
    }

    @Override
    public void getMorePhotos(String end_cursor) {
        getDataFromServer.getPhotoFullDetailFeed(photoDetailObserver, userId, utils.getCookies(), end_cursor, queryHash);
    }

    @Override
    public void photosFullViewClick(Edge itemModel) {
//        singleProfileActivity.hideStatusBar();
        SingleProfileActivity.setAnyOneButtonTouched(true);
        MediaFragment demoFragment = MediaFragment.newInstance(itemModel, savedProfilePicUrl);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.single_prfile_constraint_layout, demoFragment)
                .addToBackStack(null)
                .commit();
        mediaFragment = demoFragment;
    }

    //    not required here  it is for storyHighlights in savedFragment
    @Override
    public void photosFullViewClick(int position) {
    }

    @Override
    public void onDestroyView() {
        Log.d("TAG", "onDestroyView: photoFragment:-" + "called");
        if (recyclerView != null)
            recyclerView.removeOnScrollListener(onScrollListener);
        if (mediaFragment != null)
            mediaFragment = null;
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("TAG", "onSaveInstanceState photo fragment: " + outState.toString());
    }


    @Override
    public void onViewStateRestored(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d("TAG", "onViewStateRestored: " + "called");
    }

    public void startDownload() {
        if (mediaFragment != null) mediaFragment.startDownload();
        mediaFragment = null;
    }

    public void setBackground() {
        if (photoFragmentLayout != null) {
            if (photoFragmentLayout.getVisibility() == View.VISIBLE)
                photoFragmentLayout.setVisibility(View.INVISIBLE);
            else photoFragmentLayout.setVisibility(View.VISIBLE);
        }
    }
}