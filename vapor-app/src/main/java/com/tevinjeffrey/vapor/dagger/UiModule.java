package com.tevinjeffrey.vapor.dagger;


import android.content.Context;

import com.squareup.otto.Bus;
import com.tevinjeffrey.vapor.VaporApp;
import com.tevinjeffrey.vapor.okcloudapp.DataManager;
import com.tevinjeffrey.vapor.ui.files.FilesActivityPresenter;
import com.tevinjeffrey.vapor.ui.files.FilesActivityPresenterImpl;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.ArchivePresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.AudioPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.BookmarkPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.ImagePresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.RecentPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.TextPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.UnknownPresenter;
import com.tevinjeffrey.vapor.ui.files.fragments.presenters.VideoPresenter;
import com.tevinjeffrey.vapor.ui.login.LoginPresenter;
import com.tevinjeffrey.vapor.ui.login.LoginPresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
class UiModule {

    @Provides
    @SessionScope
    public LoginPresenter providesLoginPresenter(Context context) {
        LoginPresenterImpl loginPresenter = new LoginPresenterImpl();
        VaporApp.uiComponent(context).inject(loginPresenter);
        return loginPresenter;
    }

    @Provides
    @SessionScope
    public RecentPresenter providesRecentPresenter() {
        return new RecentPresenter();
    }

    @Provides
    @SessionScope
    public ImagePresenter providesImagePresenter() {
        return new ImagePresenter();
    }

    @Provides
    @SessionScope
    public VideoPresenter providesVideoPresenter() {
        return new VideoPresenter();
    }

    @Provides
    @SessionScope
    public ArchivePresenter providesArchivePresenter() {
        return new ArchivePresenter();
    }

    @Provides
    @SessionScope
    public TextPresenter providesTextPresenter() {
        return new TextPresenter();
    }

    @Provides
    @SessionScope
    public AudioPresenter providesAudioPresenter() {
        return new AudioPresenter();
    }

    @Provides
    @SessionScope
    public BookmarkPresenter providesBookmarkPresenter() {
        return new BookmarkPresenter();
    }

    @Provides
    @SessionScope
    public UnknownPresenter providesUnknownPresenter() {
        return new UnknownPresenter();
    }

    @Provides
    @SessionScope
    public FilesActivityPresenter providesFilesActivityPresenter(DataManager dataManager, Bus bus) {
        return new FilesActivityPresenterImpl(dataManager, bus);
    }

}
