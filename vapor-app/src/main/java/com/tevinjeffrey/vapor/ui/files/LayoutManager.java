package com.tevinjeffrey.vapor.ui.files;

public class LayoutManager {

    NavContext navContext = NavContext.ALL;
    String title;

    public String getTitle() {
        switch (navContext) {
            case ALL:
                return "All Recent Files";
            case POPULAR:
                return "Popular";
            case FAVORITE:
                return "Favorites";
            case TRASH:
                return "Trash";
        }
        return title;
    }

    public NavContext getNavContext() {
        return navContext;
    }

    public void setNavContext(NavContext navContext) {
        this.navContext = navContext;
    }

    public enum NavContext  {
        ALL, POPULAR, FAVORITE, TRASH
    }
}
