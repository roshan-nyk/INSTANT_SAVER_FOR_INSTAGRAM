package instant.saver.for_instagram.model.bookmark_profile;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "saved_profile_table")
public class Saved_Profile {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "userId")
    private String userId;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "profile_pic_url")
    private String profile_pic_url;

    public Saved_Profile(@NotNull String userId, String name, String profile_pic_url) {
        this.userId = userId;
        this.name = name;
        this.profile_pic_url = profile_pic_url;
    }

    public @NotNull String getUserId() {
        return userId;
    }

    public void setUserId(@NotNull String userId) { this.userId = userId; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfile_pic_url() {
        return profile_pic_url;
    }

    public void setProfile_pic_url(String profile_pic_url) {
        this.profile_pic_url = profile_pic_url;
    }
}
