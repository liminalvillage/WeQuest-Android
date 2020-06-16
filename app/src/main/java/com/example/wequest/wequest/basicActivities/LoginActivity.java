package com.example.wequest.wequest.basicActivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.interfaces.CustomLocationListener;
import com.example.wequest.wequest.interfaces.WeQuestConstants;
import com.example.wequest.wequest.models.User;
import com.example.wequest.wequest.utils.InternetAvailabilityChecker;
import com.example.wequest.wequest.utils.LocationHandler;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;
import com.valdesekamdem.library.mdtoast.MDToast;

import net.steamcrafted.loadtoast.LoadToast;

import java.util.ArrayList;

import static com.example.wequest.wequest.utils.LocationHandler.REQUEST_CHECK_SETTINGS;

public class LoginActivity extends AppCompatActivity implements CustomLocationListener {
    private static final int GOOGLE_SING_REQ_CODE = 1090;
    private static final int FACEBOOK_SIGN_REQ_CODE = 2090;

    private static final String TAG = "LoginActivity";
    SignInButton googleLogin;
    CallbackManager callbackManager;
    FirebaseAuth mFirebaseAuth;

    private LocationHandler handler;

    // Configure sign-in to request the fetchUser's ID, email address, and basic
    // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("957366749040-vb5batiuckr67qv14jmm5osus0tdhjvm.apps.googleusercontent.com")
            .requestEmail()
            .build();

    private GoogleSignInClient mGoogleSignInClient;
    private LoadToast toast;
    private Location location;
    private FirebaseAuth mAuth;
    private boolean isDeviceFirstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        checkingAppFirstTimeRunning();

        handler = new LocationHandler(this, this);

        toast = new LoadToast(LoginActivity.this);
        mAuth = FirebaseAuth.getInstance();
        // initialing the view
        mFirebaseAuth = FirebaseAuth.getInstance();
        googleLogin = findViewById(R.id.google_login);


        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.facebook_login);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                toast.hide();
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                toast.error();
                // ...
            }
        });

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        googleLogin.setOnClickListener(view -> {

            Intent googleIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(googleIntent, GOOGLE_SING_REQ_CODE);

        });


    }

    private void checkingAppFirstTimeRunning() {
        Thread thread = new Thread(() -> {

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            boolean isFirstTime = preferences.getBoolean("first", true);

            if (isFirstTime) {
                startActivity(new Intent(LoginActivity.this, IntroductionActivity.class));
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("first", false);
                editor.apply();
                finish();
            }

        });
        thread.start();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    private void updateUI(final FirebaseUser account) {

        new Handler().postDelayed(() -> {
            if (account != null) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                toast.success();
                finish();
            }
        }, 1000);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GOOGLE_SING_REQ_CODE:
                    if (!InternetAvailabilityChecker.hasInternetConnection(this)) {
                        toast.hide();
                        MDToast.makeText(LoginActivity.this, "Please Connect to Internet", MDToast.LENGTH_LONG, MDToast.TYPE_WARNING).show();
                        return;
                    }
                    toast.setText("Logging With Google...");
                    toast.setTranslationY(100); // y offset in pixels
                    toast.show();

                    Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
                    handleSignInResult(accountTask);
                    break;
                case REQUEST_CHECK_SETTINGS:
                    if (resultCode == RESULT_OK)
                        handler.getLocationPermission();
                    else
                        MDToast.makeText(this, "Location not enabled", MDToast.TYPE_ERROR, MDToast.LENGTH_LONG).show();
                    break;
                default:
                    if (callbackManager != null) {
                        toast = new LoadToast(LoginActivity.this);
                        toast.setText("Logging With Facebook...");
                        toast.setTranslationY(100); // y offset in pixels
                        toast.show();
                        callbackManager.onActivityResult(requestCode, resultCode, data);
                    }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        handler.handleRequestPermissionResult(requestCode, grantResults);

    }

    public void handleSignInResult(Task<GoogleSignInAccount> completedTask) {

        try {
            GoogleSignInAccount signInAccount = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(signInAccount);

        } catch (ApiException e) {
            if (!completedTask.isSuccessful())
                toast.hide();
            Log.e("STATUS_CODE", String.valueOf(e.getStatusCode()));
            MDToast.makeText(LoginActivity.this, "Google Play Service is Out of date", MDToast.LENGTH_LONG, MDToast.TYPE_WARNING).show();
            e.printStackTrace();
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        signWithCredentials(credential);
    }

    private void handleFacebookAccessToken(AccessToken token) {

        Log.e(getLocalClassName(), "Sign in With Facebook");
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        signWithCredentials(credential);

    }

    private void signWithCredentials(AuthCredential credential) {
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(getLocalClassName(), "signInWithCredential:success");

                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.e(getLocalClassName(), "Sign in With Facebook" + user.getEmail());
                        createUserInDB();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(getLocalClassName(), "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }

                });
    }


    void createUserInDB() {
        final FirebaseUser user = mFirebaseAuth.getCurrentUser();
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference("wequest").child("userProfiles/" + user.getUid());

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                OneSignal.sendTag("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());

                if (!dataSnapshot.exists()) {
                    handler.getLocationPermission();
                } else {
                    updateUI(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(getLocalClassName(), databaseError.getMessage());
            }
        });
    }

    private void uploadUserData(final FirebaseUser user) {

        // when creating the user, upliad his android ID, so next time we know about such device
        // and not give him free points


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(WeQuestConstants.UNIQUE_ID);
        String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        ref.child(androidID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    if (!dataSnapshot.exists()) {
                                                                        isDeviceFirstTime = true;
                                                                        dataSnapshot.getRef().setValue(true);
                                                                    }
                                                                    final DatabaseReference database = FirebaseDatabase.getInstance().
                                                                            getReference("wequest").
                                                                            child("userProfiles/" + user.getUid());

                                                                    database.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            if (!dataSnapshot.exists()) {

                                                                                final LoadToast toast2 = new LoadToast(LoginActivity.this);
                                                                                toast2.setText("First Time Creating Account");
                                                                                toast2.setTranslationY(100);
                                                                                toast2.show();
                                                                                User newUser = new User();
                                                                                newUser.setUsername(user.getDisplayName());
                                                                                newUser.setPhotoUrl(String.valueOf(user.getPhotoUrl()));
                                                                                newUser.setLatitude(location.getLatitude());
                                                                                newUser.setLongitude(location.getLongitude());
                                                                                newUser.setEmail(user.getEmail());
                                                                                newUser.setPhoneNo(user.getPhoneNumber());
                                                                                newUser.setRating(0);
                                                                                newUser.setKarma(isDeviceFirstTime ? 10 : 0);
                                                                                newUser.setFeedbacks(new ArrayList<>());
                                                                                newUser.setBio(getString(R.string.no_bio));
                                                                                ArrayList<Integer> ratings = new ArrayList<>();
                                                                                ratings.add(0);
                                                                                ratings.add(0);
                                                                                ratings.add(0);
                                                                                ratings.add(0);
                                                                                ratings.add(0);

                                                                                newUser.setRatings(ratings);
                                                                                newUser.setTimeCredit(isDeviceFirstTime ? 10 : 0);
                                                                                newUser.setUid(user.getUid());
                                                                                database.setValue(newUser);
                                                                                toast2.success();
                                                                                updateUI(user);
                                                                            }
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                        }
                                                                    });
                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            }
        );


    }


    @Override
    public void onLocationFetched(Location location) {
        this.location = location;
        uploadUserData(FirebaseAuth.getInstance().getCurrentUser());
    }

    @Override
    public void onLocationFailed(ConnectionResult connectionRequest) {
        MDToast.makeText(this, "Couldn't get the location\nclose the app and Try Again Latter", MDToast.TYPE_WARNING, Toast.LENGTH_LONG).show();
    }
}