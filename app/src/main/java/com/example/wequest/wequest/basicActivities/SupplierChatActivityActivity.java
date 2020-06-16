package com.example.wequest.wequest.basicActivities;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.databinding.ChatLayoutDesignBinding;
import com.example.wequest.wequest.interfaces.OnUserListener;
import com.example.wequest.wequest.interfaces.WeQuestConstants;
import com.example.wequest.wequest.models.Chat;
import com.example.wequest.wequest.models.Request;
import com.example.wequest.wequest.models.User;
import com.example.wequest.wequest.utils.ChatControllerUtil;
import com.example.wequest.wequest.utils.FireBaseReferenceUtils;
import com.example.wequest.wequest.utils.WeQuestOperation;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.Query;

public class SupplierChatActivityActivity extends AppCompatActivity {


    ImageView chatIcon, arrowTop, arrowBottom;
    private ChatControllerUtil chatController;
    private Request request;
    private ChatLayoutDesignBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.chat_layout_design);

        chatIcon=findViewById(R.id.chat_icon);
        arrowTop=findViewById(R.id.arrow1);
        arrowBottom=findViewById(R.id.arrow2);
        request = getIntent().getParcelableExtra(WeQuestConstants.REQUEST_OBJECT);

        WeQuestOperation userOP = new WeQuestOperation(request.getUid());

        userOP.setOnUserFetchedListener(new OnUserListener() {
            @Override
            public void onUserFetched(User user) {
                String requestPath = request.getUid() +"/"+request.getNeedKey();
                Query chatRef = FireBaseReferenceUtils.getChatQueryForMe(requestPath,FirebaseAuth.getInstance().getUid());

                FirebaseRecyclerOptions<Chat> recyclerOptions = ChatControllerUtil.getFirebaseRecyclerOption(chatRef, user);


                chatController = new ChatControllerUtil(recyclerOptions, SupplierChatActivityActivity.this, binding.noHintMessage, new ChatControllerUtil.OnMessageReceive() {
                    @Override
                    public void messageReceive() {

                    }
                });

                binding.chatRecycler.setAdapter(chatController);
                chatController.startListening();
            }
        });


    }

    @Override
    protected void onStop() {
        super.onStop();
        chatController.stopListening();

    }
}
