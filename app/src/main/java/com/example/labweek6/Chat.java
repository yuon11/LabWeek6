package com.example.labweek6;

import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Chat {
    public String uid; // uid
    public List<String> memberIDs; // sender
    public List<Message> chatMessages;
    public Object timestamp;


    public Chat(String uid) {

        this.uid = uid;
        this.chatMessages = new ArrayList<Message>();
        this.memberIDs = new ArrayList<String>();
        this.timestamp = ServerValue.TIMESTAMP;

    }
    public Object getTimestamp(){
        return timestamp;
    }

    public void addUser(String uid){
        memberIDs.add(uid);
    }

    public void removeUser(String uid){
        memberIDs.remove(uid);
    }

    public void addMessage(Message message){
        chatMessages.add(message);
    }

    public void removeMessage(Message message){
        chatMessages.remove(message);
    }
}