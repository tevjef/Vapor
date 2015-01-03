package deadpixel.app.vapor.ui.fragments;

import android.os.Bundle;
import android.widget.AbsListView;

import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;
import deadpixel.app.vapor.ui.intefaces.FilesFragment;

public class BookmarkFragment extends RecentFragment implements FilesFragment, AbsListView.OnScrollListener {

    public BookmarkFragment() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.autoLoadFiles = false;
        super.setType(CloudAppItem.Type.BOOKMARK);
        super.onCreate(savedInstanceState);
    }
}