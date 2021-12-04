package com.example.labweek6;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.ViewHolder>
{
    SimpleDateFormat localDateFormat= new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference allUsersRef = database.getReference("Users");
    final DatabaseReference chatRef;
    final String receiverID;
    ChildEventListener usersRefListener;
    final private FirebaseAuth mAuth;
    final private FirebaseUser currentUser;
    final private List<MessageModel> msgList;
    final private RecyclerView r;

    private class MessageModel{
        public String uid;
        public String date;
        public String sender;
        public String receiver;
        public String messageContent;
        public MessageModel(String uid, String date, String sender, String receiver, String messageContent) {
            this.uid=uid;
            this.date=date;
            this.sender=sender;
            this.receiver=receiver;
            this.messageContent=messageContent;
        }
    }

    public ChatRecyclerAdapter(RecyclerView recyclerView, DatabaseReference currentChatRef, String clickedReceiverID){
        msgList =new ArrayList<MessageModel>();
        r=recyclerView;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        chatRef = currentChatRef;
        receiverID = clickedReceiverID;

        currentChatRef.child("chatmessages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                MessageModel userMsg=new MessageModel(dataSnapshot.child("uid").getValue().toString(),
                        localDateFormat.format(new Date(Long.parseLong(dataSnapshot.child("timestamp").getValue().toString()))),
                        dataSnapshot.child("senderID").getValue().toString(),
                        dataSnapshot.child("receiverID").getValue().toString(),
                        dataSnapshot.child("messageContent").getValue().toString());

                if(userMsg.sender.equals(clickedReceiverID) ||
                        userMsg.receiver.equals(clickedReceiverID))
                {
                    msgList.add(userMsg);
                    ChatRecyclerAdapter.this.notifyItemInserted(msgList.size()-1);
                    r.scrollToPosition(msgList.size()-1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    for (int i = 0; i < msgList.size(); i++) {
                        if(msgList.get(i).uid.equals(snapshot.child("uid").getValue().toString()))
                    {
                        MessageModel userMsg=new MessageModel(snapshot.child("uid").getValue().toString(),
                                localDateFormat.format(new Date(Long.parseLong(snapshot.child("timestamp").getValue().toString()))),
                                snapshot.child("senderID").getValue().toString(),
                                snapshot.child("receiverID").getValue().toString(),
                                snapshot.child("messageContent").getValue().toString());

                        if(userMsg.sender.equals(clickedReceiverID) ||
                                userMsg.receiver.equals(clickedReceiverID))
                        {
                            msgList.set(i,userMsg);
                            notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    for (int i = 0; i < msgList.size(); i++) {
                        if(msgList.get(i).uid.equals(snapshot.getKey()))
                    {
                        msgList.remove(i);
                        notifyItemRemoved(i);
                        break;
                    }
                }
            }


            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    @NonNull
    @Override
    public ChatRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_card_view, parent,false);
        final ChatRecyclerAdapter.ViewHolder vh = new ChatRecyclerAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatRecyclerAdapter.ViewHolder holder, int position) {
        final MessageModel u =msgList.get(position);
        String uid=u.uid;

        chatRef.child("chatmessages").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


               if (dataSnapshot.exists()){
                   holder.chatMsg_v.setText(dataSnapshot.child("messageContent").getValue().toString());
                   String senderID = dataSnapshot.child("senderID").getValue().toString();
                   allUsersRef.child(senderID).child("displayname").addValueEventListener(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                           String senderDisplayName = "From: "+dataSnapshot.getValue().toString();
                           holder.extras_v.setText(senderDisplayName);
                       }

                       @Override
                       public void onCancelled(@NonNull DatabaseError databaseError) {

                       }
                   });

                   String dateSent = localDateFormat.format(new Date(Long.parseLong(dataSnapshot.child("timestamp").getValue().toString())));
                   holder.date_v.setText("Sent: "+dateSent);
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public void removeListener(){
        if(chatRef !=null && usersRefListener!=null)
            chatRef.removeEventListener(usersRefListener);
    }
    @Override
    public int getItemCount() {
            return msgList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView chatMsg_v;
        public TextView date_v;
        public TextView extras_v;

        public ViewHolder(View v){
            super(v);
            chatMsg_v = (TextView) v.findViewById(R.id.chatMessage);
            date_v = (TextView) v.findViewById(R.id.date_view);
            extras_v = (TextView) v.findViewById(R.id.otherData);
        }
    }

}