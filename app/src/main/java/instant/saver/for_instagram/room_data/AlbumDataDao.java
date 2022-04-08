package instant.saver.for_instagram.room_data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import instant.saver.for_instagram.model.album_gallery.Album_Data;

@Dao
public interface AlbumDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Album_Data album_data);

    @Query("DELETE FROM album_gallery_table")
    void deleteAllMedias();

    @Query("SELECT * FROM album_gallery_table")
    LiveData<List<Album_Data>> getAllMedias();

    @Query("DELETE FROM album_gallery_table WHERE userId LIKE :ID")
    void deleteSingleMedia(long ID);

    @Update(entity = Album_Data.class)
    void updateSingleAlbumData(Album_Data singleAlbumData);
}