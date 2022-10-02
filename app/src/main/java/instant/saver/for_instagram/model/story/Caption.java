package instant.saver.for_instagram.model.story;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Caption implements Serializable {


    @SerializedName("pk")
    private String pk;

    @SerializedName("user_id")
    private long user_id;

    @SerializedName("text")
    private String text;

    public String getPk() {
        return pk;
    }

    public long getUser_id() {
        return user_id;
    }

    public String getText() {
        return text;
    }
}
