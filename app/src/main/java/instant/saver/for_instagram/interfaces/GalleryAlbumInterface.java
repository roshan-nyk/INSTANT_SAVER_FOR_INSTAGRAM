package instant.saver.for_instagram.interfaces;

import instant.saver.for_instagram.MediaFragment;
import instant.saver.for_instagram.model.album_gallery.Album_Data;


public interface GalleryAlbumInterface {
    void callMediaFragment(MediaFragment mediaFragment, int position);
    void currentSingleAlbum(Album_Data album_data);
    void getCurrentPosition(int i);
}
