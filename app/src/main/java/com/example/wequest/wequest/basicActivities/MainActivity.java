package com.example.wequest.wequest.basicActivities;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.interfaces.MyOnBackPressedListener;
import com.example.wequest.wequest.mainFragments.AboutFragment;
import com.example.wequest.wequest.mainFragments.ContactFragment;
import com.example.wequest.wequest.mainFragments.MissedNotificationsFragment;
import com.example.wequest.wequest.mainFragments.NeedsFragment;
import com.example.wequest.wequest.mainFragments.RequestsFragment;
import com.example.wequest.wequest.mainFragments.RequestsNearMeFragment;
import com.example.wequest.wequest.mainFragments.UserDashboardFragment;
import com.example.wequest.wequest.utils.FireBaseHelper;
import com.example.wequest.wequest.utils.FireBaseReferenceUtils;
import com.example.wequest.wequest.utils.InternetAvailabilityChecker;
import com.example.wequest.wequest.utils.WeQuestOperation;
import com.facebook.login.DeviceLoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.ArrayList;

import static com.example.wequest.wequest.interfaces.WeQuestConstants.GOTO_REQUEST_FRAGMENT;

public class MainActivity extends AppCompatActivity implements NeedsFragment.OnHumanSubneedListListener {


    private static final String SAVED_FRAGMENT_STATE = "savedFragmentStateInt";
    private static final String CURRENT_OPENED_FRAGMENT = "CURRENT_OPENED_FRAGMENT";
    private Toolbar toolbar;
    private Drawer drawerBuilder;
    private ArrayList<Fragment> fragments;
    private int currentOpenedFragment;
    private boolean isNeedsFragmentOnSecondList;
    private MyOnBackPressedListener backPressedListener;
    private static final String FRAGMENT_INSTANCE = "FRAGMENT_INSTANCE";

    @Override
    protected void onStart() {
        super.onStart();

        if (getIntent().hasExtra(GOTO_REQUEST_FRAGMENT))
            fragmentTransaction(new RequestsFragment());
        else {
            fragmentTransaction(fragments.get(currentOpenedFragment));
        }
        WeQuestOperation.saveCurrentServerTime(this);

        if (!InternetAvailabilityChecker.hasInternetConnection(this))
            MDToast.makeText(this, "Please connect to internet to use WeQuest!", MDToast.TYPE_WARNING, Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_waiting_screen);

        boolean isLoggedBefore = checkIfUserLodgedIn();
        if (isLoggedBefore) {
            setContentView(R.layout.activity_home);
            fragments = createFragmentList(savedInstanceState);

            toolbar = findViewById(R.id.toolbar_drawer);
            setupMaterialDrawer(savedInstanceState);
        }

    }

    private void setupMaterialDrawer(Bundle savedInstanceState) {
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
//            int selectedItemPosition = savedInstanceState.getInt(SAVED_FRAGMENT_STATE, 0);
            currentOpenedFragment = savedInstanceState.getInt(CURRENT_OPENED_FRAGMENT, 0);

            if (currentOpenedFragment < 0 || currentOpenedFragment >= fragments.size())
                currentOpenedFragment = 0;

            fragmentTransaction(fragments.get(currentOpenedFragment));
        }


        // todo make the user name to be retrieved from the user, since we may add the edit name


        drawerBuilder = new DrawerBuilder()
                .withShowDrawerOnFirstLaunch(true)
                .withActionBarDrawerToggle(true)
                .withDisplayBelowStatusBar(true)
                .withToolbar(toolbar)
                .addDrawerItems(getDrawerItems())
                .withOnDrawerItemClickListener(new MyOnDrawerItemClickListener())
                .withSavedInstance(savedInstanceState)
                .withAccountHeader(getAccountHeader(savedInstanceState))
                .withActivity(this)
                .build();
    }


    private IDrawerItem[] getDrawerItems() {


        PrimaryDrawerItem requestItem = new PrimaryDrawerItem().withIcon(R.drawable.ic_request).withIdentifier(0)
                .withName(R.string.needs_fragment_title);

        PrimaryDrawerItem requestNearItem = new PrimaryDrawerItem().withIcon(R.drawable.ic_location_on_black_24dp
        ).withIdentifier(1).withName(R.string.near_request_fragment_title);

        PrimaryDrawerItem statusItem = new PrimaryDrawerItem().withIdentifier(2)
                .withIcon(R.drawable.ic_requests_black_24dp).withName(R.string.request_status_fragment_title);


        PrimaryDrawerItem meItem = new PrimaryDrawerItem().withIdentifier(3)
                .withIcon(R.drawable.ic_me_ind_black_24dp).withName(R.string.me_fragment_title);

        PrimaryDrawerItem missedNotificationItem = new PrimaryDrawerItem().withIdentifier(4)
                .withIcon(R.drawable.ic_notifications_active_black_24dp).withName(R.string.missed_notification_fragment_title);

        PrimaryDrawerItem aboutItem = new PrimaryDrawerItem().withIdentifier(5)
                .withIcon(R.drawable.ic_about_black_24dp).withName(R.string.about_fragment_title);


        PrimaryDrawerItem requestaNewNeed = new PrimaryDrawerItem().withName(R.string.request_a_futue_fragment_title).
                withIcon(R.drawable.ic_about_black_24dp).withIdentifier(6);
        PrimaryDrawerItem contactUsItem = new PrimaryDrawerItem().withName("Contact Us").withIcon(R.drawable.ic_email_black_24dp).withIdentifier(8);


        PrimaryDrawerItem qrCodeScannerItem = new PrimaryDrawerItem().withIdentifier(7).
                withIcon(R.drawable.ic_photo_camera_black_24dp).withName(R.string.scan_fragment_title)
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {

                    IntentIntegrator intentIntegrator = new IntentIntegrator(MainActivity.this);
                    intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                    intentIntegrator.setPrompt("Scan");
                    intentIntegrator.setCameraId(0);
                    intentIntegrator.setBeepEnabled(true);
                    intentIntegrator.setBarcodeImageEnabled(false);
                    intentIntegrator.initiateScan();

                    return false;
                });

        PrimaryDrawerItem exitItem = new PrimaryDrawerItem().withIdentifier(9)
                .withIcon(R.drawable.ic_exit_to_app_black_24dp).withName(R.string.signout_fragment_title);


        IDrawerItem[] drawerItems = {new SectionDrawerItem().withName(R.string.primary_section_title).withDivider(false),
                requestItem, requestNearItem, statusItem, meItem, missedNotificationItem, qrCodeScannerItem
                , new DividerDrawerItem()
                , new SecondaryDrawerItem().withName(R.string.comm_section_title)
                , requestaNewNeed, aboutItem, contactUsItem,
                new DividerDrawerItem(),
                exitItem};


        return drawerItems;

    }

    private AccountHeader getAccountHeader(Bundle savedInstanceState) {
        return new AccountHeaderBuilder()
                .withActivity(this)
                .withSavedInstance(savedInstanceState)
                .withTranslucentStatusBar(true)
                .withSelectionListEnabledForSingleProfile(false)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem().withName(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()).
                                withEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail()).
                                withIcon(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                ).build();
    }


    private boolean checkIfUserLodgedIn() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


        if (currentUser != null) {
            //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

            WeQuestOperation user = new WeQuestOperation(FireBaseHelper.getUid());
            user.setOnUserFetchedListener(userProfile -> {
                if (userProfile == null) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    MainActivity.this.finish();
                    return;
                }
            });
            return true;
        } else {

            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            MainActivity.this.finish();
            return false;
        }
    }


    private ArrayList<Fragment> createFragmentList(Bundle savedInstanceState) {

        ArrayList<Fragment> fragments = new ArrayList<>();

        Fragment needsFragment;
        if (savedInstanceState != null) {
            needsFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_INSTANCE);
            if (needsFragment instanceof NeedsFragment) {
                fragments.add(needsFragment);
            } else fragments.add(new NeedsFragment());

        } else
            fragments.add(new NeedsFragment());

        backPressedListener = (MyOnBackPressedListener) fragments.get(0);


        fragments.add(new RequestsNearMeFragment());
        fragments.add(new RequestsFragment());
        fragments.add(new UserDashboardFragment());
        fragments.add(new MissedNotificationsFragment());
        fragments.add(new AboutFragment());
        fragments.add(new ContactFragment());


        return fragments;
    }

    @Override
    public void onBackPressed() {
        if (drawerBuilder != null && drawerBuilder.isDrawerOpen())
            drawerBuilder.closeDrawer();
        else {

            if (isNeedsFragmentOnSecondList) {
                backPressedListener.onBackClicked();
            } else
                finish();


            /*
                if (HumanNeedLListOnSecondList)
                    envoke the previous method  on the fragment activity
                    else if (anyother conidtih like other fragments or second lis)
                    finish
             */

        }

    }


    /**
     * this method will sign out the account from firebase
     * <p>
     */
    private void signOut() {

        new AlertDialog.Builder(this).setMessage("Are you sure you want to sign out?").
                setPositiveButton("Yes", (dialogInterface, i) -> {
                    if (DeviceLoginManager.getInstance() != null)
                        DeviceLoginManager.getInstance().logOut();

                    FirebaseAuth.getInstance().signOut();

                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();

                }).setNegativeButton("No", (dialog, which) -> dialog.cancel())
                .setCancelable(false)
                .show();
    }


    public void requestNewNeed() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "roberto@wequest.it", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "WeQuest feature request");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Type your request ...");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    void fragmentTransaction(Fragment fragment) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_view, fragment);
        transaction.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        if (drawerBuilder != null) {
            outState = drawerBuilder.saveInstanceState(outState);
            //add the values which need to be saved from the accountHeader to the bundle
//        outState = headerResult.saveInstanceState(outState);
            outState.putInt(SAVED_FRAGMENT_STATE, (int) drawerBuilder.getCurrentSelection());
            outState.putInt(CURRENT_OPENED_FRAGMENT, currentOpenedFragment);

            // todo lera crush dabit
            //ragment NeedsFragment{214d8e64} is not currently in the FragmentManager
            if (getSupportFragmentManager().getFragments().contains(fragments.get(currentOpenedFragment)))
                getSupportFragmentManager().putFragment(outState, FRAGMENT_INSTANCE, fragments.get(currentOpenedFragment));

        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            String resultContents = result.getContents();
            if (resultContents != null) {
                if (!result.getContents().equals(FireBaseHelper.getUid())) {
                    MDToast.makeText(this, "Your Are Not The Supplier For This Request!", MDToast.TYPE_ERROR, Toast.LENGTH_LONG).show();
                    return;
                }

                DatabaseReference qrCodeRef = FireBaseReferenceUtils.getQrCodeTransactionRef(FireBaseHelper.getUid());
                qrCodeRef.getRef().setValue(0);
            }


        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void isOnSubNeedList(boolean isOnList) {
        isNeedsFragmentOnSecondList = isOnList;
    }

    private class MyOnDrawerItemClickListener implements Drawer.OnDrawerItemClickListener {
        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            if (drawerItem != null) {
                switch ((int) drawerItem.getIdentifier()) {
                    case 6:
                        MainActivity.this.requestNewNeed();
                        break;
                    case 9:
                        MainActivity.this.signOut();
                        break;
                    case 7:
                        drawerBuilder.closeDrawer();
                        break;

                    default: {
                        if (drawerItem instanceof PrimaryDrawerItem) {
                            String fragmentName = ((PrimaryDrawerItem) drawerItem).getName().getText(view.getContext());
                            toolbar.setTitle(fragmentName);
                            currentOpenedFragment = (int) drawerItem.getIdentifier();
                            MainActivity.this.fragmentTransaction(fragments.get(currentOpenedFragment));
                        }
                    }
                }

            }
            return false;
        }
    }
}

