package instant.saver.for_instagram.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PhotoPageInfo implements Serializable {

    @SerializedName("has_next_page")
    private boolean has_next_page;

    @SerializedName("end_cursor")
    private String end_cursor;

    public boolean isHas_next_page() {
        return has_next_page;
    }

    public String getEnd_cursor() {
        return end_cursor;
    }
}
