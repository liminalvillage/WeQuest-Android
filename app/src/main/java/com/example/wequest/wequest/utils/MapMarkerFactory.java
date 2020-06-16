package com.example.wequest.wequest.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

 import com.example.wequest.wequest.interfaces.OnUserListener;
import com.example.wequest.wequest.models.User;
import com.example.wequest.wequest.R;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

public class MapMarkerFactory {

    private Context context;
    private ImageView markerPicture;
    private LatLng userLoc;
    private Bitmap bitmap;
    private User user;
    private boolean isBioNeeded;
    private OnMarkerBuiltListener markerBuiltListener;
    public MapMarkerFactory(Context context,OnMarkerBuiltListener markerBuiltListener, LatLng userLoc) {
        this.userLoc = userLoc;
        this.markerBuiltListener = markerBuiltListener;
        this.context = context;
    }
    public MapMarkerFactory(Context context,OnMarkerBuiltListener markerBuiltListener, LatLng userLoc,boolean isBioNeeded) {
        this(context,markerBuiltListener,userLoc);
        this.isBioNeeded = isBioNeeded;
    }



    public void createBitMapFromView() {


        this.bitmap = Bitmap.createBitmap(markerPicture.getLayoutParams().width, markerPicture.getLayoutParams().height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        markerPicture.layout(0, 0, markerPicture
                .getLayoutParams().width, markerPicture
                .getLayoutParams().height);
        markerPicture.draw(c);
        build(markerBuiltListener);

    }


    public void initializeMarkerForUser(String uid) {

        View view = LayoutInflater.from(context).inflate(R.layout.view_custom_marker, null);

        markerPicture = view.findViewById(R.id.marker_profile_image);
        WeQuestOperation operation = new WeQuestOperation(uid);

        operation.setOnUserFetchedListener(new OnUserListener() {


            @Override
            public void onUserFetched(User user) {
                MapMarkerFactory.this.user = user;
                Picasso.get().load(user.getPhotoUrl()).into(markerPicture);
                createBitMapFromView();
            }
        });
    }

    public interface OnMarkerBuiltListener {

        void onMarkerBuilt(MarkerOptions markerOptions);
    }


    private Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2.0f;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2.0f;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right,
                (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top,
                (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);

        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return output;
    }

    public void build(final OnMarkerBuiltListener marerBuiltListener) {


        bitmap = toRoundBitmap(bitmap);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(userLoc);
        markerOptions.title(user.getUsername());
        if(isBioNeeded) {
            markerOptions.snippet(user.getBio());
        }

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(toRoundBitmap(bitmap)));
        marerBuiltListener.onMarkerBuilt(markerOptions);


    }


}
