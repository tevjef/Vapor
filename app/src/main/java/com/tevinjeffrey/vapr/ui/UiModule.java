package com.tevinjeffrey.vapr.ui;


import android.content.Context;

import com.tevinjeffrey.vapr.VaprApp;
import com.tevinjeffrey.vapr.okcloudapp.DataManager;
import com.tevinjeffrey.vapr.ui.files.fragments.FilesFragment;
import com.tevinjeffrey.vapr.ui.files.fragments.presenters.ArchivePresenter;
import com.tevinjeffrey.vapr.ui.files.fragments.presenters.AudioPresenter;
import com.tevinjeffrey.vapr.ui.files.fragments.presenters.BookmarkPresenter;
import com.tevinjeffrey.vapr.ui.files.fragments.presenters.ImagePresenter;
import com.tevinjeffrey.vapr.ui.files.fragments.presenters.RecentPresenter;
import com.tevinjeffrey.vapr.ui.files.fragments.presenters.TextPresenter;
import com.tevinjeffrey.vapr.ui.files.fragments.presenters.UnknownPresenter;
import com.tevinjeffrey.vapr.ui.files.fragments.presenters.VideoPresenter;
import com.tevinjeffrey.vapr.ui.login.LoginPresenter;
import com.tevinjeffrey.vapr.ui.login.LoginPresenterImpl;
import com.tevinjeffrey.vapr.ui.login.LoginView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = {
                LoginPresenterImpl.class,
                FilesFragment.class,
                RecentPresenter.class,
                ImagePresenter.class,
                VideoPresenter.class,
                ArchivePresenter.class,
                TextPresenter.class,
                AudioPresenter.class,
                BookmarkPresenter.class,
                UnknownPresenter.class
        },
        complete = false,
        library = true)

public class UiModule {

    @Provides
    @Singleton
    public LoginPresenter<LoginView> providesLoginPresenter(Context context) {
        LoginPresenterImpl<LoginView> loginPresenter = new LoginPresenterImpl<>();
        VaprApp.objectGraph(context).inject(loginPresenter);
        return loginPresenter;
    }

    @Provides
    @Singleton
    public RecentPresenter providesRecentPresenter(DataManager dataManager) {
        return new RecentPresenter();
    }

    @Provides
    @Singleton
    public ImagePresenter providesImagePresenter(DataManager dataManager) {
        return new ImagePresenter();
    }

    @Provides
    @Singleton
    public VideoPresenter providesVideoPresenter(DataManager dataManager) {
        return new VideoPresenter();
    }

    @Provides
    @Singleton
    public ArchivePresenter providesArchivePresenter(DataManager dataManager) {
        return new ArchivePresenter();
    }

    @Provides
    @Singleton
    public TextPresenter providesTextPresenter(DataManager dataManager) {
        return new TextPresenter();
    }

    @Provides
    @Singleton
    public AudioPresenter providesAudioPresenter(DataManager dataManager) {
        return new AudioPresenter();
    }

    @Provides
    @Singleton
    public BookmarkPresenter providesBookmarkPresenter(DataManager dataManager) {
        return new BookmarkPresenter(dataManager);
    }

    @Provides
    @Singleton
    public UnknownPresenter providesUnknownPresenter(DataManager dataManager) {
        return new UnknownPresenter();
    }
}
