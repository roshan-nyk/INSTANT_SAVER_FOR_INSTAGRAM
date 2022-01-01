package instant.saver.for_instagram.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class PhotoEdgeSavedMedia implements Serializable {

    @SerializedName("page_info")
    private PhotoPageInfo pageInfo;

    @SerializedName("count")
    private long  count;

    public long getCount() {
        return count;
    }

    public PhotoPageInfo getPageInfo() {
        return pageInfo;
    }

    @SerializedName("edges")
    private List<Edge> edgeList;

    public List<Edge> getEdgeList() {
        return edgeList;
    }

}
