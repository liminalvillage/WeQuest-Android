package com.example.wequest.wequest.mainFragments;


 import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

 import androidx.annotation.NonNull;
 import androidx.core.content.ContextCompat;
 import androidx.databinding.DataBindingUtil;
 import androidx.fragment.app.Fragment;

 import com.example.wequest.wequest.R;
import com.example.wequest.wequest.basicActivities.MainActivity;
import com.example.wequest.wequest.databinding.FragmentUserDashboardBinding;
import com.example.wequest.wequest.utils.FireBaseHelper;
import com.example.wequest.wequest.utils.FireBaseReferenceUtils;
import com.example.wequest.wequest.utils.WeQuestOperation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserDashboardFragment extends Fragment {


    private static final int MAX_BIO_LENGTH = 150;
    private FragmentUserDashboardBinding binding;
    private String oldUserBio;
    private MenuItem doneItem, editItem;

    public UserDashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.edit_info:
                toggleItemMenu(false);
                toggleItemVisibilities(true);
                oldUserBio = binding.userProfileLayout.myBio.getText().toString();
                binding.userProfileLayout.bioEd.setText(oldUserBio);
                break;
            case R.id.edit_done:

                boolean isBioChanged;
                String newUserBio = binding.userProfileLayout.bioEd.getText().toString();
                isBioChanged = !newUserBio.equals(oldUserBio);

                if (isBioChanged) {
                    // if the bio was changed then upload the new one
                    // otherwise dont do extra process!
                    binding.userProfileLayout.myBio.setText(newUserBio);


                    ((MainActivity) getContext()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    binding.userProfileLayout.progressBar.setVisibility(VISIBLE);

                    DatabaseReference userBioRef;
                    userBioRef = FireBaseReferenceUtils.getUserBioRef(FirebaseAuth.getInstance().getUid());
                    userBioRef.setValue(newUserBio, (err1, err2) -> onComplete());
                } else
                    onComplete();


                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleItemVisibilities(boolean makeTwoGone) {
        binding.userProfileLayout.userCardInfo.setVisibility(makeTwoGone ? GONE : VISIBLE);
        binding.userProfileLayout.myBio.setVisibility(makeTwoGone ? GONE : VISIBLE);
        binding.userProfileLayout.editUserBio.setVisibility(!makeTwoGone ? GONE : VISIBLE);
    }

    private void toggleItemMenu(boolean itemVisibilty) {
        editItem.setVisible(itemVisibilty);
        doneItem.setVisible(!itemVisibilty);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

         inflater.inflate(R.menu.user_edit_menu, menu);

        doneItem = menu.findItem(R.id.edit_done);

        editItem = menu.findItem(R.id.edit_info);
        setListener();

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_dashboard, container, false);
        setHasOptionsMenu(true);

        binding.userProfileLayout.reputation.setStepSize(0.1f);


        WeQuestOperation operation = new WeQuestOperation(FireBaseHelper
                .getUid());

        operation.setOnUserFetchedListener(user -> {

            Picasso.get().load(user.getPhotoUrl()).into(binding.userProfileLayout.cirleImage);
            binding.userProfileLayout.userName.setText(user.getUsername());
            binding.userProfileLayout.timeCredit.setText(String.valueOf(user.getTimeCredit()));
            binding.userProfileLayout.karma.setText(String.valueOf(user.getKarma()));
            binding.userProfileLayout.reputation.setRating((float) user.getRating());
            binding.userProfileLayout.myBio.setText(user.getBio());



        });

        return binding.getRoot();
    }

    void setListener() {
        binding.userProfileLayout.bioEd.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (getContext() == null)
                    return;

                binding.userProfileLayout.bioCharCount.setText(getString(R.string.bio_max_count, s.length(), MAX_BIO_LENGTH));

                if (s.length() > MAX_BIO_LENGTH || s.length() == 0) {
                    binding.userProfileLayout.bioCharCount.setTextColor(
                            ContextCompat.getColor(getContext(), android.R.color.holo_red_light));
                    doneItem.setEnabled(false);
                } else {
                    binding.userProfileLayout.bioCharCount.setTextColor(
                            ContextCompat.getColor(getContext(), R.color.primary_text));
                    doneItem.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void onComplete() {
        binding.userProfileLayout.progressBar.setVisibility(GONE);
        ((MainActivity) getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        toggleItemMenu(true);
        toggleItemVisibilities(false);
    }
}
