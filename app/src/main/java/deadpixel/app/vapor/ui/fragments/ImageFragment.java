package deadpixel.app.vapor.ui.fragments;

import android.os.Bundle;
import android.widget.AbsListView;

import deadpixel.app.vapor.cloudapp.api.model.CloudAppItem;
import deadpixel.app.vapor.ui.MainActivity;
import deadpixel.app.vapor.ui.intefaces.FilesFragment;

public class ImageFragment extends RecentFragment implements FilesFragment, AbsListView.OnScrollListener {

    public ImageFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.mAutoLoad = false;
        super.setType(CloudAppItem.Type.IMAGE);
        super.onCreate(savedInstanceState);

    }
}
