package instant.saver.for_instagram.model;

import instant.saver.for_instagram.model.story.User;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class UserData implements Serializable {
    @SerializedName("user")
    private User user;

    public User getUser() {
        return user;
    }
}