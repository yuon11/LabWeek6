package com.example.labweek6;

import com.google.firebase.database.ServerValue;

public class Message {
    public String uid; // uid
    public String senderID; // sender
    public String receiverID; // sender
    public String messageContent;
    public Object timestamp;

    public Message(String uid, String UserID, String receiverID, String messageContent) {
        this.uid=uid;
        this.senderID = UserID;
        this.receiverID = receiverID;
        this.timestamp= ServerValue.TIMESTAMP;
        this.messageContent = messageContent;
    }
    public Object getTimestamp(){
        return timestamp;
    }
}
