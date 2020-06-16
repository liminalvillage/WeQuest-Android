package com.example.wequest.wequest.notifications;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.example.wequest.wequest.R;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.onesignal.OneSignal;
import com.squareup.picasso.Picasso;

public class WeQuestApplication extends MultiDexApplication {


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);




        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .setNotificationOpenedHandler(new NotificationOpenHandler(this))
                .setNotificationReceivedHandler(new MyNotificationReceivedHandler(this))
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        // Call syncHashedEmail anywhere in your app if you have the user's email.
        // This improves the effectiveness of OneSignal's "best-time" notification scheduling feature.
        // OneSignal.syncHashedEmail(userEmail);


        //initialize and create the image loader logic
        DrawerImageLoader.init(new DrawerImageLoader.IDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.get().load(uri).placeholder(placeholder).into(imageView);

            }
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder,String tag) {
                Picasso.get().load(uri).placeholder(placeholder).into(imageView);
            }


            @Override
            public void cancel(ImageView imageView) {
                Picasso.get().cancelRequest(imageView);
            }

            @Override
            public Drawable placeholder(Context ctx) {
                return ContextCompat.getDrawable(ctx, R.drawable.user_profile);
            }

            @Override
            public Drawable placeholder(Context ctx,String tag) {
                return ContextCompat.getDrawable(ctx, R.drawable.user_profile);
            }

        });

    }
}