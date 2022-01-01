package instant.saver.for_instagram.room_data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

import instant.saver.for_instagram.api.GetDataFromServer;
import instant.saver.for_instagram.model.album_gallery.Album_Data;

public class AlbumDataRepository {

    private final AlbumDataDao albumDataDao;
    private final LiveData<List<Album_Data>> allMedia ;

    public AlbumDataRepository(final  Context context) {
        InstaRoomDataBase db = InstaRoomDataBase.getDatabase(context);
        albumDataDao = db.albumDataDao();
        allMedia = albumDataDao.getAllMedias();
    }

    public LiveData<List<Album_Data>> getAllMedias() {
        return  allMedia;
    }

    public void  insert(Album_Data album_data){
        GetDataFromServer.getInstance().getDataBaseWriteExecutor().execute(() -> {
            albumDataDao.insert(album_data);
        });
    }

    public void deleteAllMedias(){
        GetDataFromServer.getInstance().getDataBaseWriteExecutor().execute(() -> albumDataDao.deleteAllMedias());
    }

    public void deleteSingleMedia(long id) {
        GetDataFromServer.getInstance().getDataBaseWriteExecutor().execute(() -> albumDataDao.deleteSingleMedia(id));
    }

    public void updateSingleAlbumData(Album_Data singleAlbumData) {
        GetDataFromServer.getInstance().getDataBaseWriteExecutor().execute(() -> albumDataDao.updateSingleAlbumData(singleAlbumData));
    }
}
