package instant.saver.for_instagram.room_data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

import instant.saver.for_instagram.api.GetDataFromServer;
import instant.saver.for_instagram.model.bookmark_profile.Saved_Profile;

public class SavedProfileRepository {
    private final SavedProfileDao savedProfileDao;
    private final LiveData<List<Saved_Profile>> allContacts ;

    public SavedProfileRepository(final Context context){
        InstaRoomDataBase db = InstaRoomDataBase.getDatabase(context);
        savedProfileDao = db.savedProfileDao();
        allContacts = savedProfileDao.getAllContacts();
    }

    public LiveData<List<Saved_Profile>> getAllContacts(){  return allContacts; }

    public void  insert(Saved_Profile savedProfile){
        GetDataFromServer.getInstance().getDataBaseWriteExecutor().execute(() -> {
                     savedProfileDao.insert(savedProfile);
        });
    }

    public void  deleteAll(){
        GetDataFromServer.getInstance().getDataBaseWriteExecutor().execute(() -> savedProfileDao.deleteAll());
    }

    public void updateSingleSavedProfile(Saved_Profile savedProfile) {
        GetDataFromServer.getInstance().getDataBaseWriteExecutor().execute(() -> savedProfileDao.updateSingleProfile(savedProfile));
    }

    public void deleteSingleSavedProfile(String userId) {
        GetDataFromServer.getInstance().getDataBaseWriteExecutor().execute(() -> savedProfileDao.deleteSingleSavedProfile(userId));
    }
}
