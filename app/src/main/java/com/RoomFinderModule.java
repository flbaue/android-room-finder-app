package com;

import android.app.Application;

import com.data.CheckForDataUpdateTask;
import com.data.RetrieveDataTask;
import com.squareup.otto.Bus;
import com.ui.activity.SplashScreenActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        complete = true,
        library = true,
        injects = { RoomFinderApplication.class, SplashScreenActivity.class, RetrieveDataTask.class }
)
public class RoomFinderModule {

    private final Application app;

    public RoomFinderModule(Application application) {
        this.app = application;
    }

    @Provides
    public RetrieveDataTask provideRetrieveDataTask(Bus eventsBus) {
        return new RetrieveDataTask(eventsBus);
    }

    @Provides
    public CheckForDataUpdateTask provideCheckForUpdateTask(Bus eventsBus) {
        return new CheckForDataUpdateTask(eventsBus);
    }

    @Provides
    @Singleton
    public Application provideRoomFinderApplication() {
        return app;
    }

    @Provides
    @Singleton
    public Bus provideEventsBus() {
        return new Bus();
    }

}
