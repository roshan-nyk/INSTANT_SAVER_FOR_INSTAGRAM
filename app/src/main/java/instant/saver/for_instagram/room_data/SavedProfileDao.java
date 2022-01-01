package instant.saver.for_instagram.room_data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import instant.saver.for_instagram.model.bookmark_profile.Saved_Profile;

@Dao
public interface SavedProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Saved_Profile savedProfile);

    @Query("DELETE FROM saved_profile_table")
    void deleteAll();

    @Query("SELECT * FROM saved_profile_table")
    LiveData<List<Saved_Profile>> getAllContacts() ;

    @Update(entity = Saved_Profile.class)
    void updateSingleProfile(Saved_Profile savedProfile);

    @Query("DELETE FROM saved_profile_table WHERE userId LIKE :userId")
    void deleteSingleSavedProfile(String userId);
}
