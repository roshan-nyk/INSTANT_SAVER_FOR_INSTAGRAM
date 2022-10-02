package instant.saver.for_instagram.model.story;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class ItemModel implements Serializable {

    @SerializedName("taken_at")
    private long taken_at;

    @SerializedName("pk")
    private long pk;

    @SerializedName("id")
    private String id;

    @SerializedName("device_timestamp")
    private long device_timestamp;

    @SerializedName("media_type")
    private int media_type;

    @SerializedName("code")
    private String code;

    @SerializedName("client_cache_key")
    private String client_cache_key;

    @SerializedName("filter_type")
    private int filter_type;

    @SerializedName("image_versions2")
    private ImageVersionModel image_versions2;

    @SerializedName("original_width")
    private int original_width;

    @SerializedName("original_height")
    private int original_height;

    @SerializedName("video_versions")
    private ArrayList<VideoVersionModel> video_versions;

    @SerializedName("has_audio")
    private boolean has_audio;

    @SerializedName("video_duration")
    private double video_duration;

    @SerializedName("caption_is_edited")
    private boolean caption_is_edited;

    @SerializedName("caption_position")
    private int caption_position;

    @SerializedName("is_reel_media")
    private boolean is_reel_media;

    @SerializedName("photo_of_you")
    private boolean photo_of_you;

    @SerializedName("organic_tracking_token")
    private String organic_tracking_token;

    @SerializedName("expiring_at")
    private long expiring_at;

    @SerializedName("can_reshare")
    private boolean can_reshare;

    @SerializedName("can_reply")
    private boolean can_reply;

    @SerializedName("user")
    private User user;

    public User getUser() {
        return user;
    }

    @SerializedName("caption")
    private Caption caption;

    public Caption getCaption() {
        return caption;
    }

    @SerializedName("carousel_media")
    private ArrayList<ItemModel> carousel_media;

    public ArrayList<ItemModel> getCarouselMedia() {
        return carousel_media;
    }

    public long getTaken_at() {
        return taken_at;
    }

    public void setTaken_at(long taken_at) {
        this.taken_at = taken_at;
    }

    public long getPk() {
        return pk;
    }

    public void setPk(long pk) {
        this.pk = pk;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getDevice_timestamp() {
        return device_timestamp;
    }

    public void setDevice_timestamp(long device_timestamp) {
        this.device_timestamp = device_timestamp;
    }

    public int getMedia_type() {
        return media_type;
    }

    public void setMedia_type(int media_type) {
        this.media_type = media_type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getClient_cache_key() {
        return client_cache_key;
    }

    public void setClient_cache_key(String client_cache_key) {
        this.client_cache_key = client_cache_key;
    }

    public int getFilter_type() {
        return filter_type;
    }

    public void setFilter_type(int filter_type) {
        this.filter_type = filter_type;
    }

    public ImageVersionModel getImage_versions2() {
        return image_versions2;
    }

    public void setImage_versions2(ImageVersionModel image_versions2) {
        this.image_versions2 = image_versions2;
    }

    public ArrayList<VideoVersionModel> getVideo_versions() {
        return video_versions;
    }

    public void setVideo_versions(ArrayList<VideoVersionModel> video_versions) {
        this.video_versions = video_versions;
    }

    public boolean isHas_audio() {
        return has_audio;
    }

    public void setHas_audio(boolean has_audio) {
        this.has_audio = has_audio;
    }

    public double getVideo_duration() {
        return video_duration;
    }

    public void setVideo_duration(double video_duration) {
        this.video_duration = video_duration;
    }

    public boolean isCaption_is_edited() {
        return caption_is_edited;
    }

    public void setCaption_is_edited(boolean caption_is_edited) {
        this.caption_is_edited = caption_is_edited;
    }

    public int getCaption_position() {
        return caption_position;
    }

    public void setCaption_position(int caption_position) {
        this.caption_position = caption_position;
    }

    public void setCan_reshare(boolean can_reshare) {
        this.can_reshare = can_reshare;
    }


}
