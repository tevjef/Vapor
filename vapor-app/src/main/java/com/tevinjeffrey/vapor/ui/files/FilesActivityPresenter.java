package com.tevinjeffrey.vapor.ui.files;

import com.tevinjeffrey.vapor.ui.base.Presenter;

public interface FilesActivityPresenter extends Presenter<FilesActivityView> {
    void loadEmail();
    void setNavContext(FilesActivityPresenterImpl.NavContext navContext);
    FilesActivityPresenterImpl.NavContext getNavContext();

    enum NavContext {
        ALL("All Recent Files"), POPULAR("Popular"), FAVORITE("Favorites"), TRASH("Trash");

        private final String title;

        NavContext(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
