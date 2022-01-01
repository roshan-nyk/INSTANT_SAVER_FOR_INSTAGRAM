package instant.saver.for_instagram.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PhotoData implements Serializable {

    @SerializedName("user")
    private PhotoUser user ;

    public void setUser(PhotoUser user) {
        this.user = user;
    }

    public PhotoUser getUser() {
        return user;
    }
}
