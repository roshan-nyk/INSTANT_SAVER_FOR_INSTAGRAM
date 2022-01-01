package instant.saver.for_instagram.interfaces;

import java.util.ArrayList;
import instant.saver.for_instagram.model.story.ItemModel;

public interface StoriesDetailsInterface {
    void storiesFullViewClick(ArrayList<ItemModel> storyItemModelList, int adapterPosition);
    void storiesHighlightFullViewClick(String id);
}
