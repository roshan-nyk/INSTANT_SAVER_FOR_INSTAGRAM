package instant.saver.for_instagram.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Node implements Serializable {

    @SerializedName("product_type")
    private String product_type;
    @SerializedName("shortcode")
    private String shortcode;
    @SerializedName("id")
    private String id;
    @SerializedName("text")
    private String text;
    @SerializedName("owner")
    private PhotoOwner owner;
    @SerializedName("__typename")
    private String __typename;
    @SerializedName("display_url")
    private String display_url;
    @SerializedName("title")
    private String title;
    @SerializedName("cover_media")
    private CoverMedia cover_media;
    @SerializedName("display_resources")
    private List<DisplayResource> display_resources;
    @SerializedName("is_video")
    private boolean is_video;
    @SerializedName("video_url")
    private String video_url;
    @SerializedName("edge_sidecar_to_children")
    private EdgeSidecarToChildren edge_sidecar_to_children;
    @SerializedName("thumbnail_resources")
    private List<PhotoThumbnail> thumbnail_resources;
    @SerializedName("edge_media_to_caption")
    private EdgeMediaToCaption edgeMediaToCaption;

    public CoverMedia getCover_media() {
        return cover_media;
    }

    public void setCover_media(CoverMedia cover_media) {
        this.cover_media = cover_media;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProduct_type() {
        return product_type;
    }

    public void setProduct_type(String product_type) {
        this.product_type = product_type;
    }

    public String getShortcode() {
        return shortcode;
    }

    public void setShortcode(String shortcode) {
        this.shortcode = shortcode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public PhotoOwner getOwner() {
        return owner;
    }

    public void setOwner(PhotoOwner owner) {
        this.owner = owner;
    }

    public String get__typename() {
        return __typename;
    }

    public void set__typename(String __typename) {
        this.__typename = __typename;
    }

    public EdgeMediaToCaption getEdgeMediaToCaption() {
        return edgeMediaToCaption;
    }

    public void setEdgeMediaToCaption(EdgeMediaToCaption edgeMediaToCaption) {
        this.edgeMediaToCaption = edgeMediaToCaption;
    }

    public List<PhotoThumbnail> getThumbnail_resources() {
        return thumbnail_resources;
    }

    public void setThumbnail_resources(List<PhotoThumbnail> thumbnail_resources) {
        this.thumbnail_resources = thumbnail_resources;
    }

    public EdgeSidecarToChildren getEdge_sidecar_to_children() {
        return edge_sidecar_to_children;
    }

    public void setEdge_sidecar_to_children(EdgeSidecarToChildren edge_sidecar_to_children) {
        this.edge_sidecar_to_children = edge_sidecar_to_children;
    }

    public String getDisplay_url() {
        return display_url;
    }

    public void setDisplay_url(String display_url) {
        this.display_url = display_url;
    }

    public List<DisplayResource> getDisplay_resources() {
        return display_resources;
    }

    public void setDisplay_resources(List<DisplayResource> display_resources) {
        this.display_resources = display_resources;
    }

    public boolean isIs_video() {
        return is_video;
    }

    public void setIs_video(boolean is_video) {
        this.is_video = is_video;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }
}
