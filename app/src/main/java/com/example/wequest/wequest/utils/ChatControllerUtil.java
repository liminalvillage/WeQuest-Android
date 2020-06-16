package com.example.wequest.wequest.utils;

import android.content.Context;

import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wequest.wequest.R;
import com.example.wequest.wequest.databinding.ChatListItemBinding;
import com.example.wequest.wequest.models.Chat;
import com.example.wequest.wequest.models.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import static android.view.View.GONE;


public class ChatControllerUtil extends FirebaseRecyclerAdapter<Chat, ChatControllerUtil.MyChatViewHolder> {


    public static final String IS_USER_SUPPLIER = "isUserSupplier";
    private Context context;
    private MediaPlayer mediaPlayer;
    private LinearLayout startMessage;
    private OnMessageReceive onMessageReceive;

    public ChatControllerUtil(FirebaseRecyclerOptions<Chat> recyclerOptions, Context context, LinearLayout startMessage,OnMessageReceive onMessageReceive) {
        super(recyclerOptions);
        this.context = context;
        mediaPlayer = MediaPlayer.create(context, R.raw.sent);
        this.startMessage = startMessage;
        this.onMessageReceive = onMessageReceive;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public MyChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ChatListItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.chat_list_item, parent, false);


        return new ChatControllerUtil.MyChatViewHolder(binding);


    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    protected void onBindViewHolder(@NonNull MyChatViewHolder holder, int position, @NonNull Chat model) {
        ChatListItemBinding binding = holder.binding;



        if (model.getName().contains(" "))
            binding.userName.setText(model.getName().substring(0, model.getName().indexOf(" ")));
        else
            binding.userName.setText(model.getName());


        binding.userChatItemMessage.setText(model.getBody());
        Picasso.get().load(model.getUserPic()).into(binding.userChatItemPic);

        if (!model.getID().equals(FirebaseAuth.getInstance().getUid())) {
            Log.e("MESSAGE",holder.binding.userChatItemMessage.getText().toString());

            // its the supplier

            binding.userChatItemMessage.setBackgroundResource(R.drawable.him2);

            binding.userChatItemMessage.setTextColor(ContextCompat.getColor(
                    context, R.color.primary_text));


            // moving the user pic to right
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.userChatItemPic.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

            binding.userChatItemPic.setLayoutParams(params);

            setMargins(binding.userChatItemPic, 16, 0);


            // moving the user chat body message to right of the supplier pic
            params = (RelativeLayout.LayoutParams) binding.userChatItemMessage.getLayoutParams();
            params.addRule(RelativeLayout.RIGHT_OF, R.id.user_chat_item_pic);

            //todo check if device has latest google play services

            binding.userChatItemMessage.setLayoutParams(params);


            // moving the user name to below of user pic
            params = (RelativeLayout.LayoutParams) binding.userName.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);


            binding.userName.setLayoutParams(params);

            setMargins(binding.userName, 8, 0);

        } else {
//            // its me :D
//
//
            binding.userChatItemMessage.setBackgroundResource(R.drawable.me2);


            binding.userChatItemMessage.setTextColor(ContextCompat.getColor(
                    context, R.color.colorWhite));


//                    // moving the user pic to right
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.userChatItemPic.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);

            binding.userChatItemPic.setLayoutParams(params);
            setMargins(binding.userChatItemPic, 0, 16);
//
////                     moving the chat body to left of user pic
            params = (RelativeLayout.LayoutParams) binding.userChatItemMessage.getLayoutParams();
            params.addRule(RelativeLayout.LEFT_OF, R.id.user_chat_item_pic);
            params.addRule(RelativeLayout.RIGHT_OF, 0);


            binding.userChatItemMessage.setLayoutParams(params);
//
//                    // moving the user name to below of user pic
            params = (RelativeLayout.LayoutParams) binding.userName.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);

            setMargins(binding.userName,0,8);


        }
    }

    private void setMargins(View view, int leftMargin, int rightMargin){
        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        marginParams.leftMargin = leftMargin;
        marginParams.rightMargin = rightMargin;
    }

    @Override
    public void onDataChanged() {
        onMessageReceive.messageReceive();
        if(getItemCount() == 0)
        startMessage.setVisibility(View.VISIBLE);
        else
            startMessage.setVisibility(GONE);
    }

    public interface OnMessageReceive {
        void messageReceive();
    }


    public static FirebaseRecyclerOptions<Chat> getFirebaseRecyclerOption(Query reference, final User user) {


        SnapshotParser<Chat> chatParser = snapshot -> {

            Chat chat = snapshot.getValue(Chat.class);
            if (chat.getID().equals(FirebaseAuth.getInstance().getUid())) {

                chat.setUserPic(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString());
                chat.setName("You");
            } else {

                chat.setUserPic(user.getPhotoUrl());

                chat.setName(user.getUsername());
            }

            return chat;
        };

        return new FirebaseRecyclerOptions.Builder<Chat>()
                .setQuery(reference, chatParser).build();


    }

    public class MyChatViewHolder extends RecyclerView.ViewHolder {

        ChatListItemBinding binding;


        public MyChatViewHolder(ChatListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void sendMessage(String message, DatabaseReference chatRef) {


        Chat chat = new Chat();
        chat.setBody(message);
        chat.setID(FirebaseAuth.getInstance().getUid());
        mediaPlayer.start();
        chatRef.setValue(chat, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                mediaPlayer.stop();
            }
        });
    }
}
