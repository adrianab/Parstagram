package com.example.parstagram;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Post.class);

        // Register your parse models
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("KemA63Vo3I6aF7pemJub1LtbazZ8s8sHm5EREls9")
                .clientKey("f8ZJcuLTgm92xC9lYaUUFaSYhbAoUwm2VdB7Dj7N")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
