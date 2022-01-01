package instant.saver.for_instagram.model;

import instant.saver.for_instagram.model.story.User;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import instant.saver.for_instagram.model.story.User;

public class UserInfoForSingleStoryDownload implements Serializable {
    @SerializedName("user")
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
