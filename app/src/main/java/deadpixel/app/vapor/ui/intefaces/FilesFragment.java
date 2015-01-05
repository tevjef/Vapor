package deadpixel.app.vapor.ui.intefaces;

import java.util.ArrayList;
import deadpixel.app.vapor.database.model.DatabaseItem;

public interface FilesFragment {

    static final String ALL_FRAGMENT = "all_fragment";
    static final String IMAGE_FRAGMENT = "image_fragment";
    static final String VIDEO_FRAGMENT = "video_fragment";
    static final String AUDIO_FRAGMENT = "audio_fragment";
    static final String TEXT_FRAGMENT = "text_fragment";
    static final String ARCHIVE_FRAGMENT = "archive_fragment";
    static final String BOOKMARK_FRAGMENT = "bookmark_fragment";
    static final String OTHER_FRAGMENT = "other_fragment";
    static final String TRASH_FRAGMENT = "trash_fragment";

    static final String AUTOLOAD = "auto_run";
    static final String FRAGMENT_TYPE = "fragment_type";

    final public static String EXTRA_NAME = "item_name";

    public void datebaseUpdateEvent(ArrayList<DatabaseItem> items);
    public void errorEvent();
    public void refresh();


}