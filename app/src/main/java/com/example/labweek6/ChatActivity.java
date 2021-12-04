package com.example.labweek6;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    SimpleDateFormat localDateFormat= new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    DatabaseReference uref;
    String receiverID;
    private ChatRecyclerAdapter myRecyclerAdapter;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference allChatsRef = database.getReference("Chats");
    DatabaseReference allUsersRef = database.getReference("Users");
    DatabaseReference currentChatRef;
    DatabaseReference receiverChatRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Intent Should hold receiver UID

        Intent intent = getIntent();
        receiverID = intent.getStringExtra("userID");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        startChat();

        RecyclerView recyclerView=findViewById(R.id.recylcer_view);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);
        myRecyclerAdapter=new ChatRecyclerAdapter(recyclerView, currentChatRef, uref.getKey());
        recyclerView.setAdapter(myRecyclerAdapter);


        allUsersRef.child(receiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TextView receiverDisplayName = (TextView) findViewById(R.id.profileName);
                ImageView receiverImageName = (ImageView) findViewById(R.id.profileImage);

                String senderDisplayName = dataSnapshot.child("displayname").getValue().toString();
                receiverDisplayName.setText(senderDisplayName);
                if(dataSnapshot.child("profilePicture").exists())
                {
                    Picasso.get().load(dataSnapshot.child("profilePicture").getValue().toString()).transform(new CircleTransform()).into(receiverImageName);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FloatingActionButton sendButton = findViewById(R.id.fab);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadMessage(v);
            }
        });

    }

    private void startChat(){
        uref = database.getReference("Users").child(receiverID);
        currentChatRef = allChatsRef.child(currentUser.getUid());
        receiverChatRef = allChatsRef.child(uref.getKey());
    }

    private void uploadMessage(View view){

        TextView messageContent = (TextView) findViewById(R.id.messageText);

        DatabaseReference messageListRef = currentChatRef.child("chatmessages").push();
        DatabaseReference receiverMsgListRef = receiverChatRef.child("chatmessages").push();

        Message newMsg = new Message(messageListRef.getKey(),
                currentUser.getUid(), uref.getKey(), messageContent.getText().toString());

        Message newMsg2 = new Message(receiverMsgListRef.getKey(),
                currentUser.getUid(), uref.getKey(), messageContent.getText().toString());

        messageListRef.setValue(newMsg).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ChatActivity.this,
                                "Message Sent", Toast.LENGTH_SHORT).show();
                        // Clear space after sending text
                        messageContent.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatActivity.this, e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

        receiverMsgListRef.setValue(newMsg2)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ChatActivity.this,"New Message", Toast.LENGTH_SHORT).show();
                        // Clear space after sending text
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatActivity.this, e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}