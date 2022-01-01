package instant.saver.for_instagram.model;

import instant.saver.for_instagram.model.story.User;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import instant.saver.for_instagram.model.story.User;

public class ShortcodeMedia implements Serializable {

    @SerializedName("display_url")
    private String display_url;

    @SerializedName("display_resources")
    private List<DisplayResource> display_resources;

    @SerializedName("is_video")
    private boolean is_video;

    @SerializedName("video_url")
    private String video_url;

    @SerializedName("edge_sidecar_to_children")
    private EdgeSidecarToChildren edge_sidecar_to_children;

    @SerializedName("accessibility_caption")
    private String accessibility_caption;

    @SerializedName("edge_media_to_caption")
    private EdgeMediaToCaption edgeMediaToCaption;

    @SerializedName("owner")
    private User owner;
    @SerializedName("product_type")
    private String product_type;
    @SerializedName("shortcode")
    private String shortcode;

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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public EdgeMediaToCaption getEdgeMediaToCaption() {
        return edgeMediaToCaption;
    }

    public void setEdgeMediaToCaption(EdgeMediaToCaption edgeMediaToCaption) {
        this.edgeMediaToCaption = edgeMediaToCaption;
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

    public EdgeSidecarToChildren getEdge_sidecar_to_children() {
        return edge_sidecar_to_children;
    }

    public void setEdge_sidecar_to_children(EdgeSidecarToChildren edge_sidecar_to_children) {
        this.edge_sidecar_to_children = edge_sidecar_to_children;
    }

    public String getAccessibility_caption() {
        return accessibility_caption;
    }

    public void setAccessibility_caption(String accessibility_caption) {
        this.accessibility_caption = accessibility_caption;
    }
}
