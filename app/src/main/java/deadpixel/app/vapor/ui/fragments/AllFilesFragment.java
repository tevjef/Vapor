package deadpixel.app.vapor.ui.fragments;

import android.os.Bundle;
import android.widget.AbsListView;

import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;
import deadpixel.app.vapor.ui.intefaces.FilesFragment;

public class AllFilesFragment extends RecentFragment implements FilesFragment, AbsListView.OnScrollListener {

    public AllFilesFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.autoLoadFiles = true;
        super.setType(CloudAppItem.Type.ALL);
        super.onCreate(savedInstanceState);
    }
}