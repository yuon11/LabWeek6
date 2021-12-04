package com.example.labweek6;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MyRecyclerAdapter
        extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder>
{
    SimpleDateFormat localDateFormat= new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference allPostsRef = database.getReference("Posts");
    ChildEventListener usersRefListener;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private List<PostModel> postsList;

    private Marker currentMarker =null;
    private RecyclerView r;
    private  ItemClickListener itemClickListener;
    private List<String> keyList;
    private HashMap<String,PostModel> key_to_Post;


    public MyRecyclerAdapter(RecyclerView recyclerView, HashMap<String,PostModel> kp, List<String> kl, ItemClickListener _itemClickListener){
        postsList =new ArrayList<>();
        r=recyclerView;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        keyList=kl;
        key_to_Post= kp;
        itemClickListener =_itemClickListener;

        allPostsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {

                PostModel userModel=new PostModel(dataSnapshot.child("uid").getValue().toString(),
                        dataSnapshot.child("description").getValue().toString(),
                        dataSnapshot.child("url").getValue().toString(),
                        localDateFormat.format(new Date(Long.parseLong(dataSnapshot.child("timestamp").getValue().toString()))) ,
                        dataSnapshot.getKey());


                postsList.add(userModel);
                MyRecyclerAdapter.this.notifyItemInserted(postsList.size()-1);
                r.scrollToPosition(postsList.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for (int i = 0; i < postsList.size(); i++) {
                    if(postsList.get(i).postKey.equals(snapshot.getKey()))
                    {
                        PostModel userModel=new PostModel(snapshot.child("uid").getValue().toString(),
                                snapshot.child("description").getValue().toString(),
                                snapshot.child("url").getValue().toString(),
                                localDateFormat.format(new Date(Long.parseLong(snapshot.child("timestamp").getValue().toString()))),
                                snapshot.getKey());

                        postsList.set(i,userModel);
                        notifyItemChanged(i);
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                for (int i = 0; i < postsList.size(); i++) {
                    if(postsList.get(i).postKey.equals(snapshot.getKey()))
                    {
                        postsList.remove(i);
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


//        allPostsRef.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
//
//                Marker temp =mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(dataSnapshot.child("lat").getValue().toString()), Double.parseDouble(dataSnapshot.child("lng").getValue().toString())))
//                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_grey)));
//
//                PostModel postModel = new PostModel(dataSnapshot.child("uid").getValue().toString(),
//                        dataSnapshot.child("description").getValue().toString(),
//                        dataSnapshot.child("url").getValue().toString(),
//                        localDateFormat.format(new Date(Long.parseLong(dataSnapshot.child("timestamp").getValue().toString())))
//                        , dataSnapshot.getKey(), temp);
//
//                key_to_Post.put(postModel.postKey,postModel);
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                for (int i = 0; i < keyList.size(); i++) {
//                    if(keyList.get(i).equals(snapshot.getKey()))
//                    {
//                        Marker temp =mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(snapshot.child("lat").getValue().toString()), Double.parseDouble(snapshot.child("lng").getValue().toString())))
//                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_grey)));
//
//                        PostModel postModel = new PostModel(snapshot.child("uid").getValue().toString(),
//                                snapshot.child("description").getValue().toString(),
//                                snapshot.child("url").getValue().toString(),
//                                localDateFormat.format(new Date(Long.parseLong(snapshot.child("timestamp").getValue().toString())))
//                                , snapshot.getKey(), temp);
//
//                        key_to_Post.put(postModel.postKey,postModel);
//                        break;
//                    }
//                }
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//                for (int i = 0; i < keyList.size(); i++) {
//                    if(keyList.get(i).equals(snapshot.getKey()))
//                    {
//                        keyList.remove(i);
//                        break;
//                    }
//                }
//            }
//
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent,false);
        final ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        // final PostModel u =key_to_Post.get(keyList.get(position));
        final PostModel u =postsList.get(position);
        String uid=u.uid;
        if(holder.uref!=null && holder.urefListener!=null)
        {
            holder.uref.removeEventListener(holder.urefListener);
        }
        if(holder.likesRef!=null && holder.likesRefListener!=null)
        {
            holder.likesRef.removeEventListener(holder.likesRefListener);
        }
        if(holder.likeCountRef!=null && holder.likeCountRefListener!=null)
        {
            holder.likeCountRef.removeEventListener(holder.likeCountRefListener);
        }

        if(currentUser.getUid().equals(uid))
        {
            holder.extras_v.setVisibility(View.VISIBLE);
            holder.extras_v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(v.getContext(), v);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.extras_menu, popup.getMenu());
                    popup.show();

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.delete:
                                    Toast.makeText(v.getContext(), "delete",
                                            Toast.LENGTH_SHORT).show();
                                    allPostsRef.child(u.postKey).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                         @Override
                                         public void onSuccess(Void unused) {
                                             Toast.makeText(v.getContext(), "delete", Toast.LENGTH_SHORT).show();
                                         }
                                    });
                                    return true;

                                case R.id.update:
                                    Toast.makeText(v.getContext(), "update",
                                            Toast.LENGTH_SHORT).show();

                                    Intent intent= new Intent(v.getContext(), PhotoPreview.class);
                                    intent.putExtra("key",u.postKey);
                                    v.getContext().startActivity(intent);

                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });

                }

            });
        }
        else{
            holder.extras_v.setVisibility(View.GONE);
        }

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        holder.uref = database.getReference("Users").child(uid);
        holder.uref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //
                //  Load User Profile Pic On Card View Created
                //
                if(dataSnapshot.child("profilePicture").exists())
                {
                    Picasso.get().load(dataSnapshot.child("profilePicture").getValue().toString()).transform(new CircleTransform()).into(holder.profileImageView);
                }
                holder.fname_v.setText("First Name: " +dataSnapshot.child("displayname").getValue().toString());
                holder.email_v.setText("Email:  " + dataSnapshot.child("email").getValue().toString());
                holder.phone_v.setText("Phone Num:  " + dataSnapshot.child("phone").getValue().toString());
                holder.date_v.setText("Date Created: "+u.date);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(v.getContext(), ChatActivity.class);

                // Get the UID of the profile pic
                intent.putExtra("userID",uid);
                v.getContext().startActivity(intent);
            }
        });

        holder.likeCountRef=
                database.getReference("Posts/"+u.postKey+"/likeCount");
        Log.d("LIKEC ", u.postKey);
        holder.likeCountRefListener=holder.likeCountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Log.d("CRASH", dataSnapshot.toString());
                if(dataSnapshot.getValue()!=null)
                    holder.likeCount.setText(dataSnapshot.getValue().toString()+" Likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        holder.likesRef=database.getReference("Posts/"+u.postKey+"/likes/"+currentUser.getUid());
        holder.likesRefListener=holder.likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getValue().toString().equals("true"))
                {
                    holder.likeBtn.setImageDrawable(ContextCompat.getDrawable(r.getContext(), R.drawable.like_active));
                }
                else{
                    holder.likeBtn.setImageDrawable(ContextCompat.getDrawable(r.getContext(), R.drawable.like_disabled));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.getReference("Posts/"+u.postKey).runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        PhotoPreview.Post p = mutableData.getValue(PhotoPreview.Post.class);
                        if (p == null) {
                            return Transaction.success(mutableData);
                        }

                        if (p.likes.containsKey(currentUser.getUid())) {
                            // Unstar the post and remove self from stars
                            p.likeCount = p.likeCount - 1;
                            p.likes.remove(currentUser.getUid());
                        } else {
                            // Star the post and add self to stars
                            p.likeCount = p.likeCount + 1;
                            p.likes.put(currentUser.getUid(), true);
                        }

                        // Set value and report transaction success
                        mutableData.setValue(p);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                    }
                });
            }
        });
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (currentMarker!=null)
                        currentMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_grey));

                    u.m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_red));
                    currentMarker=u.m;

                    if (itemClickListener!=null)
                        itemClickListener.onItemClick(currentMarker.getPosition());
                } catch (Exception e) {
                    Log.d("OnClickImgMarker", "onClick: ERROR CAUGHT");
                }
            }
        });

        holder.description_v.setText(u.description);
        StorageReference pathReference = FirebaseStorage.getInstance().getReference("images/"+u.url);
        pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(holder.imageView);
                //Picasso.get().load(u.url).into(holder.imageView);
            }
        });

    }
    public void removeListener(){
        if(allPostsRef !=null && usersRefListener!=null)
            allPostsRef.removeEventListener(usersRefListener);
    }
    @Override
    public int getItemCount() {
        return postsList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView fname_v;
        public TextView email_v;
        public TextView phone_v;
        public TextView date_v;
        public TextView description_v;
        public ImageView imageView;
        public ImageView profileImageView;
        public  ImageView likeBtn;
        public TextView likeCount;
        public ImageView extras_v;
        DatabaseReference uref;
        ValueEventListener urefListener;

        DatabaseReference likeCountRef;
        ValueEventListener likeCountRefListener;

        DatabaseReference likesRef;
        ValueEventListener likesRefListener;
        public ViewHolder(View v){
            super(v);
            fname_v = (TextView) v.findViewById(R.id.fname_view);
            email_v = (TextView) v.findViewById(R.id.email_view);
            phone_v = (TextView) v.findViewById(R.id.phone_view);
            date_v = (TextView) v.findViewById(R.id.date_view);
            description_v=v.findViewById(R.id.description);
            imageView=v.findViewById(R.id.postImg);
            profileImageView=v.findViewById(R.id.profileImage);
            likeBtn=v.findViewById(R.id.likeBtn);
            likeCount=v.findViewById(R.id.likeCount);
            extras_v = v.findViewById(R.id.extras);

        }
    }

}
