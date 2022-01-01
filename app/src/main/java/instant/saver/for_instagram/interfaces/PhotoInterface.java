package instant.saver.for_instagram.interfaces;

import instant.saver.for_instagram.model.Edge;
import instant.saver.for_instagram.model.story.ItemModel;

import instant.saver.for_instagram.model.Edge;

public interface PhotoInterface {
    void getMorePhotos(String end_cursor);
    void photosFullViewClick(Edge itemModel);
    void photosFullViewClick(int position);
}
