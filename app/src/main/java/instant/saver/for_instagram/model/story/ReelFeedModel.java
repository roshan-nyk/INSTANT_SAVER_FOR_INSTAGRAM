package instant.saver.for_instagram.model.story;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class ReelFeedModel implements Serializable {

    @SerializedName("user")
    private User user;
    @SerializedName("items")
    private ArrayList<ItemModel> items;
    @SerializedName("media_count")
    private int media_count;

    @SerializedName( "media_ids")
    private ArrayList<String>  media_ids;

    public ArrayList<String> getMedia_ids() {
        return media_ids;
    }

    public void setMedia_ids(ArrayList<String> media_ids) {
        this.media_ids = media_ids;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ArrayList<ItemModel> getItems() {
        return items;
    }

    public void setItems(ArrayList<ItemModel> items) {
        this.items = items;
    }

    public int getMedia_count() {
        return media_count;
    }

    public void setMedia_count(int media_count) {
        this.media_count = media_count;
    }


 /*   @SerializedName("id")
    private long id;

    @SerializedName("latest_reel_media")
    private long latest_reel_media;
    @SerializedName("expiring_atexpiring_at")
    private long expiring_at;
    @SerializedName("seen")
    private long seen;
    @SerializedName("reel_type")
    private String reel_type;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLatest_reel_media() {
        return latest_reel_media;
    }

    public void setLatest_reel_media(long latest_reel_media) {
        this.latest_reel_media = latest_reel_media;
    }

    public long getExpiring_at() {
        return expiring_at;
    }

    public void setExpiring_at(long expiring_at) {
        this.expiring_at = expiring_at;
    }

    public long getSeen() {
        return seen;
    }

    public void setSeen(long seen) {
        this.seen = seen;
    }

    public String getReel_type() {
        return reel_type;
    }

    public void setReel_type(String reel_type) {
        this.reel_type = reel_type;
    }
    */
}

