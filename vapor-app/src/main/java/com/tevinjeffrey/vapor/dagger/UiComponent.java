package com.tevinjeffrey.vapor.dagger;

import com.tevinjeffrey.vapor.okcloudapp.DigestAuthenticator;
import com.tevinjeffrey.vapor.services.IntentBridge;
import com.tevinjeffrey.vapor.services.UploadService;
import com.tevinjeffrey.vapor.ui.files.FilesActivity;
import com.tevinjeffrey.vapor.ui.files.FilesActivityPresenter;
import com.tevinjeffrey.vapor.ui.files.FilesFragmentAdapter;
import com.tevinjeffrey.vapor.ui.files.fragments.FilesFragment;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.ArchivePresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.AudioPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.BaseFilesPresenterImpl;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.BookmarkPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.BottomSheetPresenterImpl;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.ImagePresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.RecentPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.TextPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.UnknownPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.VideoPresenter;
import com.tevinjeffrey.vapor.ui.login.LoginActivity;
import com.tevinjeffrey.vapor.ui.login.LoginPresenterImpl;

import dagger.Component;

@SessionScope
@Component (modules = {
        UiModule.class,
}, dependencies = VaporAppComponent.class)
public interface UiComponent {
    void inject(LoginActivity loginActivity);
    void inject(FilesActivity filesActivity);
    void inject(IntentBridge intentBridge);
    void inject(UploadService uploadService);
    void inject(FilesFragmentAdapter.FilesVH filesVH);
    void inject(BottomSheetPresenterImpl bottomSheetPresenter);

    void inject(DigestAuthenticator digestAuthenticator);
    void inject(RecentPresenter presenter);
    void inject(ImagePresenter presenter);
    void inject(VideoPresenter presenter);
    void inject(ArchivePresenter presenter);
    void inject(TextPresenter presenter);
    void inject(AudioPresenter presenter);
    void inject(BookmarkPresenter presenter);
    void inject(UnknownPresenter presenter);
    void inject(BaseFilesPresenterImpl presenter);
    void inject(FilesActivityPresenter presenter);


    void inject(LoginPresenterImpl loginPresenter);

    void inject(FilesFragment filesFragment);
    RecentPresenter getRecentPresenter();
    ImagePresenter getImagePresenter();
    VideoPresenter getVideoPresenter();
    ArchivePresenter getArchivePresenter();
    TextPresenter getTextPresenter();
    AudioPresenter getAudioPresenter();
    BookmarkPresenter getBookmarkPresenter();
    UnknownPresenter getUnknownPresenter();
    FilesActivityPresenter getFilesActivityPresenter();

}
