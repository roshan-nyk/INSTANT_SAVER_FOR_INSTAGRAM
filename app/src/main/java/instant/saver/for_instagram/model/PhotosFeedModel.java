package instant.saver.for_instagram.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PhotosFeedModel implements Serializable {

    @SerializedName("data")
    private PhotoData data ;

    public void setData(PhotoData data) {
        this.data = data;
    }

    public PhotoData getData() {
        return data;
    }
}
