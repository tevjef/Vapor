package deadpixel.app.vapor.ui.fragments;

import android.os.Bundle;
import android.widget.AbsListView;

import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;
import deadpixel.app.vapor.ui.intefaces.FilesFragment;

public class TextFragment extends RecentFragment implements FilesFragment, AbsListView.OnScrollListener {

    public TextFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.mAutoLoad = false;
        super.setType(CloudAppItem.Type.TEXT);
        super.onCreate(savedInstanceState);
    }
}


