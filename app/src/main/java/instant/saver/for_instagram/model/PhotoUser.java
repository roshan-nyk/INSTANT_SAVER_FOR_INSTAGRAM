package instant.saver.for_instagram.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PhotoUser implements Serializable {

    @SerializedName("edge_owner_to_timeline_media")
    private PhotoTimeLineMedia photoTimeLineMedia;
    @SerializedName("edge_saved_media")
    private PhotoEdgeSavedMedia edgeSavedMedia;
    @SerializedName("edge_highlight_reels")
    private StoryEdgeHighlightReels edgeHighlightReels;

    public StoryEdgeHighlightReels getEdgeHighlightReels() {
        return edgeHighlightReels;
    }

    public void setEdgeHighlightReels(StoryEdgeHighlightReels edgeHighlightReels) {
        this.edgeHighlightReels = edgeHighlightReels;
    }

    public PhotoEdgeSavedMedia getEdgeSavedMedia() {
        return edgeSavedMedia;
    }

    public void setEdgeSavedMedia(PhotoEdgeSavedMedia edgeSavedMedia) {
        this.edgeSavedMedia = edgeSavedMedia;
    }

    public PhotoTimeLineMedia getPhotoTimeLineMedia() {
        return photoTimeLineMedia;
    }

    public void setPhotoTimeLineMedia(PhotoTimeLineMedia photoTimeLineMedia) {
        this.photoTimeLineMedia = photoTimeLineMedia;
    }
}
