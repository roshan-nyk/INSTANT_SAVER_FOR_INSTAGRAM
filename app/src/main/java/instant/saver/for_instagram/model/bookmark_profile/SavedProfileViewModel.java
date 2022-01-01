package instant.saver.for_instagram.model.bookmark_profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import instant.saver.for_instagram.room_data.SavedProfileRepository;
import java.util.List;

public class SavedProfileViewModel extends AndroidViewModel {
    private static SavedProfileRepository repository;
    private final LiveData<List<Saved_Profile>> allProfiles;

    public SavedProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new SavedProfileRepository(application.getApplicationContext());
        allProfiles = repository.getAllContacts();
    }

    public LiveData<List<Saved_Profile>> getAllContacts() { return  allProfiles; }

    public void insert(Saved_Profile savedProfile){ repository.insert(savedProfile);}

    public void deleteAll(){ repository.deleteAll();}

    public void updateSingleSavedProfile(Saved_Profile savedProfile) {
        repository.updateSingleSavedProfile(savedProfile);
    }

    public void deleteSingleSavedProfile(String userId){
        repository.deleteSingleSavedProfile(userId);
    }
}
