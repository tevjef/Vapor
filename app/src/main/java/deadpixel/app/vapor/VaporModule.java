package deadpixel.app.vapor;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import deadpixel.app.vapor.okcloudapp.OkCloudAppModule;
import deadpixel.app.vapor.ui.MainActivity;

@Module(
        injects = {
                MainActivity.class,

        },
        includes = {OkCloudAppModule.class,
                })
public class VaporModule {
    private final Context applicationContext;

    public VaporModule(Context applicationContext) {
        this.applicationContext = applicationContext;
    }


    @Provides
    @Singleton
    public Context provideContext() {
        return applicationContext;
    }

}
