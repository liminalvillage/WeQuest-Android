package com.example.wequest.wequest.basicActivities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.databinding.ActivityChooseSupplierBinding;
import com.example.wequest.wequest.interfaces.OnUserListener;
import com.example.wequest.wequest.models.Chat;
import com.example.wequest.wequest.models.Request;
import com.example.wequest.wequest.models.User;
import com.example.wequest.wequest.utils.ChatControllerUtil;
import com.example.wequest.wequest.utils.FireBaseReferenceUtils;
import com.example.wequest.wequest.utils.WeQuestOperation;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import net.steamcrafted.loadtoast.LoadToast;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.ACCEPTED;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.FIREBASE_USER_REQUESTS_PATH;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.ON_PROGRESS_STATUS;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.PENDING;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.SUPPLIER_NAME;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.USER_PROVIDER;
import static com.example.wequest.wequest.interfaces.WeQuestConstants.USER_REQUEST_PATH;
import static com.example.wequest.wequest.utils.ChatControllerUtil.IS_USER_SUPPLIER;

public class ChooseSupplierActivity extends AppCompatActivity {


    private String requestPath;
    private ChatControllerUtil adapter;
    private boolean isFirstMessage;
    private String providerID;
    private ActivityChooseSupplierBinding binding;
    private Request myRequest;
    private String supplierName;
    private boolean isUserSupplier;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_choose_supplier);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        isUserSupplier = getIntent().getBooleanExtra(IS_USER_SUPPLIER, false);
        providerID = getIntent().getStringExtra(USER_PROVIDER);
        requestPath = getIntent().getStringExtra(USER_REQUEST_PATH);

        supplierName = getIntent().getStringExtra(SUPPLIER_NAME);

        binding.sendMessage.setClickable(false);


        setListeners();

        if (isUserSupplier) {
            binding.confirmationSection.confirmationOfTheTransfer.setText("Confirmation for the transfer of");
            binding.confirmationSection.supplierIsChosen.setText("Your Status:");
            binding.confirmationSection.supplierIsChosen.setVisibility(VISIBLE);
            binding.confirmationSection.chosenStatus.setVisibility(VISIBLE);

        } else {
            isFirstTimeMessage();
        }

        updateChooseButton();
        buildingChatList();
    }

    private void buildingChatList() {

        WeQuestOperation userOP;
        if (isUserSupplier) {
            userOP = new WeQuestOperation(requestPath.substring(0, requestPath.indexOf("/")));
        } else {
            userOP = new WeQuestOperation(providerID);
        }

        userOP.setOnUserFetchedListener(new MyOnUserListener());


    }

    private void isFirstTimeMessage() {
        DatabaseReference supplierChosenForRequestPath = FireBaseReferenceUtils.
                getChatRefForMe(requestPath, providerID);


        supplierChosenForRequestPath.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isFirstMessage = !dataSnapshot.exists();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null)
            adapter.startListening();
    }

    private void setListeners() {


        binding.messageED.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty())
                    binding.sendMessage.setClickable(true);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty())
                    binding.sendMessage.setClickable(true);
            }
        });


        DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference(FIREBASE_USER_REQUESTS_PATH).
                child(requestPath);


        transactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {


                myRequest = snapshot.getValue(Request.class);

                if (myRequest != null) {
                    double hourToCommit = myRequest.getHour();
//                    double totalMin = hourToCommit * 60;
//                    double timeCredit = totalMin / 6;


                    binding.confirmationSection.confirmationToSupplier.setText(getString(R.string.credit_for_supplier, String.valueOf(hourToCommit), String.valueOf(myRequest.getKarma()), supplierName));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        binding.sendMessage.setOnClickListener(view -> {

            if (binding.messageED.getText().toString().isEmpty()) {
                Toast.makeText(ChooseSupplierActivity.this, "Write Something!", Toast.LENGTH_SHORT).show();
                return;
            }

            // check if its a first time message, then let the supplier know about this
            // by adding this request to his/her requestChosenFor list
            if (isFirstMessage) {
                DatabaseReference requestChosenFor;

                requestChosenFor = FireBaseReferenceUtils.getRequestsChosenForRef(providerID).child(requestPath);
                Request requestForSupplier = getRequestObjectForSupplier(PENDING);
                requestChosenFor.setValue(requestForSupplier);

                // also send the first message as the request's description
                sendMessage(myRequest.getRequestInformation());
                isFirstMessage = false;
            }


            //sending message
            sendMessage(binding.messageED.getText().toString());
            binding.messageED.setText("");


        });

        binding.confirmationSection.yes.setOnClickListener(view -> chooseSupplier());
        binding.confirmationSection.no.setOnClickListener(view -> cancelChosenSupplier());


    }

    private void sendMessage(String message) {
        // send the message


        DatabaseReference chatRef;
        chatRef = FireBaseReferenceUtils.getChatRefForMe(requestPath, providerID);


        adapter.sendMessage(message, chatRef.push());
    }

    private void cancelChosenSupplier() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Supplier Cancellation");
        builder.setMessage("Are Sue You Want Yo Cancel This Supplier,\n" +
                "This will cost you 1 Karma?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            WeQuestOperation.decreaseUserKarma();
            WeQuestOperation.cancelSupplier(requestPath);
            updateChooseButton();
        });

        builder.setNegativeButton("No", (dialog, which) -> {

            //if user_profile select "No", just cancel this dialog and
            dialog.dismiss();

        });
        builder.show();


    }

    private void updateChooseButton() {
        DatabaseReference chosenProviderRef = FireBaseReferenceUtils.getRequestChosenProviderRef(requestPath);

        chosenProviderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                //if there is a chosen supplier for the request
                if (snapshot.exists()) {
                    boolean isCurrentSupplierTheChosenSupplier = snapshot.getValue(String.class).equals(providerID);
                    // if the current viewing user is the supplier
                    if (isUserSupplier) {
                        // if the chosen supplier is me as a supplier
                        if (isCurrentSupplierTheChosenSupplier)
                            setRequestStatusForSupplier(R.color.md_green_A700, "Accepted");
                        else // its not me, another one is chosen as a supplier
                            setRequestStatusForSupplier(android.R.color.holo_red_light, "Another supplier is chosen");
                    } else {
                        //if the current viewing supplier the one is chosen, give the requester penalty for a possible cancellation
                        if (isCurrentSupplierTheChosenSupplier) {
                            binding.confirmationSection.penaltyCancellation.setVisibility(VISIBLE);
                            binding.confirmationSection.no.setVisibility(VISIBLE);

                        } else
//                       else its another supplier beside the one he has choose, so tell him/her another one is chosen
                            binding.confirmationSection.supplierIsChosen.setVisibility(VISIBLE);
                    }

                } else {
                    if (isUserSupplier) {
                        setRequestStatusForSupplier(R.color.primary, "Not Accepted Yet");
                    } else {
                        binding.confirmationSection.yes.setVisibility(VISIBLE);
                        binding.confirmationSection.no.setVisibility(GONE);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(getClass().getSimpleName(), databaseError.toString());
            }
        });
    }

    private void setRequestStatusForSupplier(int statusColor, String statusText) {
        int color = ContextCompat.getColor(ChooseSupplierActivity.this, statusColor);
        binding.confirmationSection.chosenStatus.setTextColor(color);
        binding.confirmationSection.chosenStatus.setText(statusText);

    }


    void chooseSupplier() {

        final LoadToast loadToast = new LoadToast(this);


        // updating the request
        WeQuestOperation.updateRequestStatus(requestPath, ON_PROGRESS_STATUS);
        // setting this one as a chosen providerider
        DatabaseReference chosenProviderRef = FireBaseReferenceUtils.getRequestChosenProviderRef(requestPath);
        chosenProviderRef.setValue(providerID);

        //setting this request to the supplier list of Requests chosen  For
        DatabaseReference requestChosenForRef = FireBaseReferenceUtils.getRequestsChosenForRef(providerID);


        Request requestForSupplier = getRequestObjectForSupplier(ACCEPTED);


        requestChosenForRef.child(requestPath).setValue(requestForSupplier, (databaseError, databaseReference) -> {
            loadToast.success();
            finish();
        });

        loadToast.setText("please wait...");
        loadToast.show();
    }

    Request getRequestObjectForSupplier(int status) {
        Request requestForSupplier = new Request();

        requestForSupplier.setUid(myRequest.getUid());
        requestForSupplier.setNeedKey(myRequest.getNeedKey());
        requestForSupplier.setNeedTitle(myRequest.getNeedTitle());
        requestForSupplier.setLatitude(myRequest.getLatitude());
        requestForSupplier.setLongitude(myRequest.getLongitude());
        requestForSupplier.setStatus(status);
        return requestForSupplier;


    }


    private class MyOnUserListener implements OnUserListener {
        @Override
        public void onUserFetched(User user) {
            Query reference = FireBaseReferenceUtils.getChatQueryForMe(requestPath, providerID);

            FirebaseRecyclerOptions<Chat> options = ChatControllerUtil.getFirebaseRecyclerOption(reference, user);
            adapter = new ChatControllerUtil(options, ChooseSupplierActivity.this, binding.chatLayout.noHintMessage, () -> {
                    if (binding.chatLayout.chatRecycler.getAdapter().getItemCount() > 0)
                        binding.chatLayout.chatRecycler.smoothScrollToPosition(binding.chatLayout.chatRecycler.getAdapter().getItemCount() - 1);

            });
            binding.chatLayout.chatRecycler.setLayoutManager(new LinearLayoutManager(ChooseSupplierActivity.this));
            binding.chatLayout.chatRecycler.setHasFixedSize(true);
            binding.chatLayout.chatRecycler.setRecycledViewPool(new RecyclerView.RecycledViewPool());
            binding.chatLayout.chatRecycler.setAdapter(adapter);
            adapter.startListening();

        }
    }

}
