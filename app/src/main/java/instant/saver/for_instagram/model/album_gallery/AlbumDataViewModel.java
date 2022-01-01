package instant.saver.for_instagram.model.album_gallery;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import instant.saver.for_instagram.room_data.AlbumDataRepository;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AlbumDataViewModel extends AndroidViewModel {

    public  static AlbumDataRepository repository;
    private final LiveData<List<Album_Data>> allMedias;

    public AlbumDataViewModel(@NonNull @NotNull Application application) {
        super(application);
        repository = new AlbumDataRepository(application.getApplicationContext());
        allMedias = repository.getAllMedias();
    }

    public LiveData<List<Album_Data>> getAllMedias() { return  allMedias; }

    public void insert(Album_Data album_data){ repository.insert(album_data);}

    public void deleteAllMedias(){ repository.deleteAllMedias();}

    public void deleteSingleMedia(long id){
        repository.deleteSingleMedia(id);
    }

    public void updateSingleAlbumData(Album_Data singleAlbumData) {
        repository.updateSingleAlbumData(singleAlbumData);
    }
}
