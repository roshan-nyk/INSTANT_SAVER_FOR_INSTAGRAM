package instant.saver.for_instagram.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PhotoOwner implements Serializable {
    @SerializedName("id")
    private String id;

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @SerializedName("username")
    private String username;


}
