package instant.saver.for_instagram.model;

import instant.saver.for_instagram.model.story.User;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Graphql implements Serializable {

    @SerializedName("shortcode_media")
    private ShortcodeMedia shortcode_media;

    @SerializedName("user")
    private User  user;

    public User getUser() {
        return user;
    }

    public ShortcodeMedia getShortcode_media() {
        return shortcode_media;
    }

    public void setShortcode_media(ShortcodeMedia shortcode_media) {
        this.shortcode_media = shortcode_media;
    }
}
