package instant.saver.for_instagram;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.insta_saver.R;

import instant.saver.for_instagram.adapter.SavedItemAdapter;
import instant.saver.for_instagram.api.GetDataFromServer;
import instant.saver.for_instagram.interfaces.PhotoInterface;
import instant.saver.for_instagram.model.Edge;
import instant.saver.for_instagram.model.EdgeSidecarToChildren;
import instant.saver.for_instagram.model.Node;
import instant.saver.for_instagram.model.PhotoOwner;
import instant.saver.for_instagram.model.PhotosFeedModel;
import instant.saver.for_instagram.model.ResponseModel;
import instant.saver.for_instagram.model.ShortcodeMedia;
import instant.saver.for_instagram.model.story.ReelFeedModel;
import instant.saver.for_instagram.util.Utils;

import org.jetbrains.annotations.NotNull;

import io.reactivex.observers.DisposableObserver;

public class SavedItemFragment extends Fragment implements PhotoInterface, View.OnClickListener {

    private static boolean isAccessingData;
    private PhotosFeedModel photosModel;
    private String userId, queryHash;
    private Utils utils;
    private SavedItemAdapter savedItemAdapter;
    private final DisposableObserver<PhotosFeedModel> photoDetailObserver = new DisposableObserver<PhotosFeedModel>() {
        @Override
        public void onNext(@NotNull PhotosFeedModel response) {
            try {
                savedItemAdapter.addPhotosAndEndCursor(response.getData().getUser().getEdgeSavedMedia());
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
    private View progressBarLayout;
    private final DisposableObserver<ResponseModel> instaObserver = new DisposableObserver<ResponseModel>() {
        @Override
        public void onNext(@NotNull ResponseModel versionList) {
            try {
//                Type listType = new TypeToken<ResponseModel>() { }.getType();
//                ResponseModel responseModel = new Gson().fromJson(versionList.toString(), listType);

                Saved_Collection_Instagram_Activity.setIsMediaFragmentOpened(true);
                progressBarLayout.setVisibility(View.GONE);
                if (versionList.getGraphql().getShortcode_media() != null) {
                    ShortcodeMedia shortcodeMedia = versionList.getGraphql().getShortcode_media();
                    String profilePicUrl = shortcodeMedia.getOwner().getProfile_pic_url();
                    PhotoOwner photoOwner = new PhotoOwner();
                    photoOwner.setUsername(shortcodeMedia.getOwner().getUsername());
                    String productType;
                    Log.d("TAG", "onNext: "+shortcodeMedia.getProduct_type());
                    if ("igtv".equals(shortcodeMedia.getProduct_type()))
                        productType = "igtv";
                        //                    for reel even though product type is "clips" but the url to access both posts and reel has /p/ in it
                    else productType = "p";

                    Node node = new Node();
                    node.setOwner(photoOwner);
                    node.setEdgeMediaToCaption(shortcodeMedia.getEdgeMediaToCaption());
                    node.setProduct_type(productType);
                    node.setShortcode(shortcodeMedia.getShortcode());

                    EdgeSidecarToChildren edgeSidecarToChildren = shortcodeMedia.getEdge_sidecar_to_children();
                    if (edgeSidecarToChildren != null) {
                        node.setEdge_sidecar_to_children(edgeSidecarToChildren);
                        node.set__typename("GraphSidecar");
                    } else {
                        node.setDisplay_resources(shortcodeMedia.getDisplay_resources());
                        node.setIs_video(shortcodeMedia.isIs_video());
                        if (shortcodeMedia.isIs_video()) {
                            node.setVideo_url(shortcodeMedia.getVideo_url());
                            Log.d("TAG", "onNext: "+shortcodeMedia.getVideo_url());
                            node.set__typename("GraphVideo");
                        } else
                            node.set__typename("GraphImage");
                    }
                    Edge edge = new Edge();
                    edge.setNode(node);
                    MediaFragment demoFragment = MediaFragment.newInstance(edge, profilePicUrl);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .add(R.id.saved_collection_Constraint_layout, demoFragment)
                            .addToBackStack(null)
                            .commit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(Throwable throwable) {
//            progressBarLayout.setVisibility(View.GONE);
            Log.e("TAG", "onerror: " + throwable.getCause() + " \n       " + throwable.getMessage() + " \n     " + throwable.getLocalizedMessage());
            if(errorUrl != null) {
                GetDataFromServer.getInstance().callResult(instaObserver, errorUrl, utils.getTempCookies());
                errorUrl = null;
            }
            else{
                progressBarLayout.setVisibility(View.GONE);
                Toast.makeText(requireActivity(), "Not Able To Access The Files.\nKindly Check Connection and Try Again", Toast.LENGTH_LONG).show();
                throwable.printStackTrace();
            }
        }

        @Override
        public void onComplete() {
        }
    };
    private int count_getMorePhotosMethod = 1;
    private boolean isScrollDown = false;
    private String COOKIES = null, MODIFIED_COOKIES = null;
    private String errorUrl;

    private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull @NotNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING || newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (isScrollDown && isAccessingData && savedItemAdapter.isHasNextPage() && count_getMorePhotosMethod == 1) {
                    Log.d("TAG", "onScrollStateChanged: " + newState);
                    getMorePhotos(savedItemAdapter.getCurrent_endCursor());
                    count_getMorePhotosMethod++;
                }
            }
        }

        @Override
        public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            Log.d("TAG", "onScrolled: " + isAccessingData + "   " + savedItemAdapter.getCurrentPosition() + "    " + dy);
            if (dy >= 0 && (savedItemAdapter.getCurrentPosition() == savedItemAdapter.getPhotoData().size())) {
                isScrollDown = true;
                if (!isAccessingData) {
                    isAccessingData = true;
                    count_getMorePhotosMethod = 1;
                    Log.d("TAG", "onScrolled: ");
                }
            } else
                isScrollDown = false;
        }
    };

    private ReelFeedModel reelFeedModel;

    public SavedItemFragment() {
        // Required empty public constructor
    }

/*    public static SavedItemFragment newInstance(@NotNull PhotosFeedModel response, String userId, String queryHash) {*/
    public static SavedItemFragment newInstance(String userId, String queryHash) {
        SavedItemFragment fragment = new SavedItemFragment();
        Bundle args = new Bundle();
//        args.putSerializable("PHOTOS", response);
        args.putString("USER_ID", userId);
        args.putString("QUERY_HASH", queryHash);
        fragment.setArguments(args);
        return fragment;
    }

   /* public static SavedItemFragment newInstance(@NotNull ReelFeedModel  reelFeedModel) {*/
   public static SavedItemFragment newInstance() {
        SavedItemFragment fragment = new SavedItemFragment();
        Bundle args = new Bundle();
//        args.putSerializable("AllHighLightStories", reelFeedModel);
        fragment.setArguments(args);
        return fragment;
    }

    public static void setIsAccessingData(boolean value) {
        isAccessingData = value;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (photosModel == null && getArguments().getString("USER_ID") != null) {
//                photosModel = (PhotosFeedModel) getArguments().getSerializable("PHOTOS");
                photosModel = Saved_Collection_Instagram_Activity.getPhotosFeedModel();
                userId = getArguments().getString("USER_ID");
                queryHash = getArguments().getString("QUERY_HASH");
            } else if (reelFeedModel == null) {
//                reelFeedModel = (ReelFeedModel) getArguments().getSerializable("AllHighLightStories");
                reelFeedModel = Saved_Collection_Instagram_Activity.getReelFeedModel();
            }
        }
        isAccessingData = false;
        utils = new Utils(requireActivity());
        String[] temp = utils.getCookies().split(" ");
        MODIFIED_COOKIES = temp[2] + " " + temp[0] + " " + temp[1] + " " + temp[3] ;
        MODIFIED_COOKIES = MODIFIED_COOKIES.substring(0,MODIFIED_COOKIES.length()-1);
        COOKIES = utils.getCookies();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_saved_item, container, false);
    }


    private RecyclerView recyclerView;
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("TAG", "onViewCreated: " + "called");
        TextView textView = view.findViewById(R.id.saved_collection_textView);
        recyclerView = view.findViewById(R.id.saved_collection_RecyclerView);
        if ((photosModel != null && photosModel.getData().getUser().getEdgeSavedMedia().getCount() > 0) || (reelFeedModel != null && reelFeedModel.getItems().size() > 0)) {
            progressBarLayout = view.findViewById(R.id.saved_item_progressBar_layout);
            progressBarLayout.setOnClickListener(this);
            recyclerView.setHasFixedSize(true);
            recyclerView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
            if (savedItemAdapter == null) {
                if (photosModel != null) {
                    savedItemAdapter = new SavedItemAdapter(requireActivity(), photosModel.getData().getUser().getEdgeSavedMedia(), SavedItemFragment.this);
                    recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false));
                    recyclerView.addOnScrollListener(onScrollListener);
                } else {
                    savedItemAdapter = new SavedItemAdapter(requireActivity(), reelFeedModel.getItems(), SavedItemFragment.this);
                    RecyclerView.LayoutManager layoutManager = new GridLayoutManager(requireContext(), 4, RecyclerView.HORIZONTAL, false);
                    recyclerView.setLayoutManager(layoutManager);
                }
            }/*else
                isAccessingData = false;*/
            recyclerView.setAdapter(savedItemAdapter);
            savedItemAdapter.notifyDataSetChanged();
        } else {
            recyclerView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            textView.setText("Sorry... No Posts Available Right Now To Show.");
        }
    }

    @Override
    public void getMorePhotos(String end_cursor) {
        GetDataFromServer.getInstance().getPhotoFullDetailFeed(photoDetailObserver, userId, utils.getCookies(), end_cursor, queryHash);
    }

    @Override
    public void photosFullViewClick(Edge itemModel) {
        progressBarLayout.setVisibility(View.VISIBLE);
        Log.d("TAG", "photosFullViewClick: "+itemModel.getNode().getProduct_type());
        if ("GraphVideo".equals(itemModel.getNode().get__typename()) && "igtv".equals(itemModel.getNode().getProduct_type())) {
//            GetDataFromServer.getInstance().callResult(instaObserver, "https://www.instagram.com/tv/" + itemModel.getNode().getShortcode() + "?__a=1", COOKIES);
            errorUrl = "https://www.instagram.com/tv/" + itemModel.getNode().getShortcode() + "?__a=1" ;
            GetDataFromServer.getInstance().callResult(instaObserver, "https://www.instagram.com/tv/" + itemModel.getNode().getShortcode() + "?__a=1", MODIFIED_COOKIES);

        }else {
//            GetDataFromServer.getInstance().callResult(instaObserver, "https://www.instagram.com/p/" + itemModel.getNode().getShortcode() + "?__a=1", COOKIES);
            errorUrl = "https://www.instagram.com/p/" + itemModel.getNode().getShortcode() + "?__a=1" ;
            GetDataFromServer.getInstance().callResult(instaObserver, "https://www.instagram.com/p/" + itemModel.getNode().getShortcode() + "?__a=1", MODIFIED_COOKIES);
        }
    }

    @Override
    public void photosFullViewClick(int adapterPosition) {
        Saved_Collection_Instagram_Activity.setIsMediaFragmentOpened(true);
        MediaFragment demoFragment = MediaFragment.newInstance(reelFeedModel.getItems(), adapterPosition,reelFeedModel.getUser().getUsername(), reelFeedModel.getUser().getProfile_pic_url());
        requireActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.saved_collection_Constraint_layout, demoFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onClick(View v) {
        Log.d("TAG", "onClick: progressBarLayout touched");
    }

    @Override
    public void onDestroyView() {
        if (recyclerView != null)
            recyclerView.removeOnScrollListener(onScrollListener);
        if(photosModel != null)
            photosModel = null;
        else reelFeedModel = null;
        super.onDestroyView();
    }

}
