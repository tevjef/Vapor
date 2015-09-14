package com.tevinjeffrey.vapor.ui.files;

public class LayoutManager {

    NavContext navContext;

    public NavContext getNavContext() {
        return navContext;
    }

    public void setNavContext(NavContext navContext) {
        this.navContext = navContext;
    }

    public enum NavContext  {
        ALL, POPULAR, FAVORITE, TRASH
    }

    public interface Updateable {
        public void update();
    }
}
