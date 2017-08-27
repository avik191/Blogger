package com.example.hi.blogger;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import static android.os.Looper.getMainLooper;

/**
 * Created by HI on 18-July-17.
 */
public class MyCustomAdapter extends RecyclerView.Adapter<MyCustomAdapter.myviewholder> {

    Context context;
    ArrayList<Blog> list;
    LayoutInflater inflator;
	int lastPosition = 1;
	boolean mprogressLike = true,mprogressview = true;
	DatabaseReference likeDatabaseReference;
	FirebaseAuth firebaseAuth;
	DatabaseReference blogDatabaseReference;


	public MyCustomAdapter(Context context, ArrayList<Blog> list,DatabaseReference likeDatabaseReference,FirebaseAuth firebaseAuth,DatabaseReference blogDatabaseReference) {

        this.context=context;
        this.list=list;
		inflator=LayoutInflater.from(context);
		this.likeDatabaseReference=likeDatabaseReference;
		this.firebaseAuth=firebaseAuth;
		this.blogDatabaseReference=blogDatabaseReference;
    }

    @Override
    public myviewholder onCreateViewHolder(ViewGroup parent, int position) {
        View v=inflator.inflate(R.layout.row_element,parent,false);//this view will contain appearance of each layout i.e each row..
        myviewholder holder=new myviewholder(v);// we are passing the view of each row to the myviewholder class
        return holder;
    }

    @Override
    public void onBindViewHolder(final myviewholder holder, int position) {//here we will inflate datas in the widgets i.e image and title..
        //It is called for each row..so every row is inflated here..

	    final Blog p=list.get(position);
	    holder.desc.setText(p.getDescription());
	    holder.title.setText(p.getTitle());
	    holder.username.setText(p.getUsername());
	    Picasso.with(context).load(p.getUrl()).into(holder.imageview);
	    Picasso.with(context).load(p.getImage()).into(holder.userdp);

	    likeDatabaseReference.addValueEventListener(new ValueEventListener()
	    {
		    @Override
		    public void onDataChange(DataSnapshot dataSnapshot)
		    {


				    String post_key = p.getPostkey().toString();
				    if(dataSnapshot.child(post_key).hasChild(firebaseAuth.getCurrentUser().getUid()))
				    {
					    holder.likebtn.setImageResource(R.mipmap.liked);
				    }
				    else
				    {

					    holder.likebtn.setImageResource(R.mipmap.notliked);
				    }

		    }

		    @Override
		    public void onCancelled(DatabaseError databaseError)
		    {

		    }
	    });

	    likeDatabaseReference.addValueEventListener(new ValueEventListener()
	    {
		    String post_key = p.getPostkey().toString();

		    @Override
		    public void onDataChange(DataSnapshot dataSnapshot)
		    {
			    if(dataSnapshot.child(post_key).getChildrenCount()>1)
			         holder.likeno.setText(dataSnapshot.child(post_key).getChildrenCount()+" "+"Likes");
			    else
				    holder.likeno.setText(dataSnapshot.child(post_key).getChildrenCount()+" "+"Like");

		    }

		    @Override
		    public void onCancelled(DatabaseError databaseError)
		    {

		    }
	    });

	    blogDatabaseReference.addValueEventListener(new ValueEventListener()
	    {
		    String post_key = p.getPostkey().toString();

		    @Override
		    public void onDataChange(DataSnapshot dataSnapshot)
		    {
			    String v =  dataSnapshot.child(post_key).child("views").getValue().toString();
			    int view = Integer.parseInt(v);
			    if(view>0)
			    {
				    holder.viewbtn.setImageResource(R.mipmap.eyeviewed);
				    holder.viewno.setText(view+" "+"views");
			    }
			    else
			    {
				    holder.viewbtn.setImageResource(R.mipmap.eye);
				    holder.viewno.setText(view+" "+"view");
			    }

		    }

		    @Override
		    public void onCancelled(DatabaseError databaseError)
		    {

		    }
	    });

    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    public class myviewholder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
	    // It contains the elements in each row that we will inflate in the recyclerView..
	    TextView title,desc,username,likeno,viewno;
	    ImageView imageview,userdp;
	    ImageButton likebtn,viewbtn;

	    public myviewholder(View itemView)
	    {
		    super(itemView);
		    title = (TextView) itemView.findViewById(R.id.posttitle);//here we link with the elements of each view i.e each row and
		    desc = (TextView) itemView.findViewById(R.id.postdesc);//here we link with the elements of each view i.e each row and
		    username = (TextView) itemView.findViewById(R.id.username);
		    viewno = (TextView) itemView.findViewById(R.id.viewno);
		    likeno = (TextView) itemView.findViewById(R.id.likeno);
		    userdp = (ImageView) itemView.findViewById(R.id.userdp);
		    imageview = (ImageView) itemView.findViewById(R.id.postImage);//here we link with the elements of each view i.e each row and
			likebtn = (ImageButton)itemView.findViewById(R.id.likebtn);
		    viewbtn = (ImageButton)itemView.findViewById(R.id.viewbtn);


		    likebtn.setOnClickListener(new View.OnClickListener()
		    {
			    @Override
			    public void onClick(View view)
			    {
					mprogressLike = true;
				    likeDatabaseReference.addValueEventListener(new ValueEventListener()
				    {
					    @Override
					    public void onDataChange(DataSnapshot dataSnapshot)
					    {
						    if(mprogressLike)
						    {
							    Blog b = list.get(getAdapterPosition());
							    String post_key = b.getPostkey().toString();
							    if(dataSnapshot.child(post_key).hasChild(firebaseAuth.getCurrentUser().getUid()))
							    {
								    likeDatabaseReference.child(post_key).child(firebaseAuth.getCurrentUser().getUid()).removeValue();
								    mprogressLike = false;

							    }
							    else
							    {
								    likeDatabaseReference.child(post_key).child(firebaseAuth.getCurrentUser().getUid()).setValue("default");
								    mprogressLike = false;

							    }
						    }
					    }

					    @Override
					    public void onCancelled(DatabaseError databaseError)
					    {

					    }
				    });
			    }
		    });

	        itemView.setOnClickListener(this);
	      }


	    @Override
	    public void onClick(View view)
	    {
		    mprogressview = true;
		   int pos = getAdapterPosition();
		    Blog b = list.get(pos);
		   final DatabaseReference viewReference = blogDatabaseReference.child(b.getPostkey().toString());

		    viewReference.addValueEventListener(new ValueEventListener()
		    {
			    @Override
			    public void onDataChange(DataSnapshot dataSnapshot)
			    {
				    if(mprogressview && !(dataSnapshot.child("uid").getValue().toString().equals(firebaseAuth.getCurrentUser().getUid().toString())))
				    {

					    String v =  dataSnapshot.child("views").getValue().toString();
					    int view = Integer.parseInt(v);
					    viewReference.child("views").setValue(view+1);
					    mprogressview = false;
				    }
			    }

			    @Override
			    public void onCancelled(DatabaseError databaseError)
			    {

			    }
		    });
		    Intent intent = new Intent(context,SinglePost.class);
		    Bundle bundle = new Bundle();
		    bundle.putString("post_key",b.getPostkey().toString());
		    intent.putExtras(bundle);
		    context.startActivity(intent);
	    }


    }



	public void  filterList(ArrayList<Blog> newList)
    {
	    list=newList;
	    notifyDataSetChanged();
    }

}
