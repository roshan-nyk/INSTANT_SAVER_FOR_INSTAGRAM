package instant.saver.for_instagram.interfaces;

import instant.saver.for_instagram.model.bookmark_profile.Saved_Profile;
import instant.saver.for_instagram.model.story.TrayModel;

public interface SelectedUserInterface {
    void selectedUserClick(int position, TrayModel trayModel);
    void selectedUserClick(int position, Saved_Profile savedProfile);
    void getCurrentPosition(int position);
}
