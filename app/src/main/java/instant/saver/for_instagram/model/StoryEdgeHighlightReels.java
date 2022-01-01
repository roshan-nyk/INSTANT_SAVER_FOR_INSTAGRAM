package instant.saver.for_instagram.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class StoryEdgeHighlightReels  implements Serializable {
    @SerializedName("edges")
    private List<Edge> edgeList;

    public List<Edge> getEdgeList() {
        return edgeList;
    }
}
