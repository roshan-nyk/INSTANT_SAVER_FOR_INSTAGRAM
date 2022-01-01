package instant.saver.for_instagram.model.story;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class FullDetailModel implements Serializable {

 @SerializedName("reels_media")
    private ArrayList<ReelFeedModel> reels_media;

    public ArrayList<ReelFeedModel> getReels_media() {
        return reels_media;
    }

    public void setReels_media(ArrayList<ReelFeedModel> reels_media) {
        this.reels_media = reels_media;
    }
/*

   @SerializedName("user_detail")
    private UserDetailModel user_detail;

  @SerializedName("reel_feed")
    private ReelFeedModel reel_feed;

    @SerializedName("feed")
    private PhotosFeedModel photo_feed;

    public PhotosFeedModel getPhoto_Feed() {
        return photo_feed;
    }

    public void setFeed(PhotosFeedModel feed) {
        this.photo_feed= feed;

         public ReelFeedModel getReel_feed() {
        return reel_feed;
    }

    public void setReel_feed(ReelFeedModel reel_feed) {
        this.reel_feed = reel_feed;
    }

    }

    public UserDetailModel getUser_detail() {
        return user_detail;
    }

    public void setUser_detail(UserDetailModel user_detail) {
        this.user_detail = user_detail;
    }
    */

}
