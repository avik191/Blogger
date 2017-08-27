package com.example.hi.blogger;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class SinglePost extends AppCompatActivity
{

	ImageView postpic,displaypic;
	EditText title,desc;
	TextView username,title1,desc1;
	Button remove,update;
	String post_key;

	ActionBar actionBar;

	DatabaseReference databaseReference;
	FirebaseAuth firebaseAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_post);

		actionBar=getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);


		databaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");
		firebaseAuth = FirebaseAuth.getInstance();

		postpic = (ImageView) findViewById(R.id.postImage);
		displaypic = (ImageView) findViewById(R.id.userdp);

		title = (EditText) findViewById(R.id.posttitle);
		desc = (EditText) findViewById(R.id.postdesc);
		title1 = (TextView) findViewById(R.id.posttitle1);
		desc1 = (TextView) findViewById(R.id.postdesc1);
		username = (TextView) findViewById(R.id.username);

		remove = (Button) findViewById(R.id.removepost);
		update = (Button) findViewById(R.id.updatepost);

		Bundle b = getIntent().getExtras();
		post_key = b.getString("post_key");

		databaseReference.addValueEventListener(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot dataSnapshot)
			{
				try{
					title.setText(dataSnapshot.child(post_key).child("title").getValue().toString());
					desc.setText(dataSnapshot.child(post_key).child("description").getValue().toString());
					title1.setText(dataSnapshot.child(post_key).child("title").getValue().toString());
					desc1.setText(dataSnapshot.child(post_key).child("description").getValue().toString());
					username.setText(dataSnapshot.child(post_key).child("username").getValue().toString());

					String url =dataSnapshot.child(post_key).child("url").getValue().toString();
					Picasso.with(SinglePost.this).load(url).into(postpic);

					url =dataSnapshot.child(post_key).child("image").getValue().toString();
					Picasso.with(SinglePost.this).load(url).into(displaypic);

					if(firebaseAuth.getCurrentUser().getUid().toString().equals(dataSnapshot.child(post_key).child("uid").getValue().toString()))
					{
						remove.setVisibility(View.VISIBLE);
						update.setVisibility(View.VISIBLE);
						title1.setVisibility(View.GONE);
						desc1.setVisibility(View.GONE);
					}
					else
					{

						title.setVisibility(View.GONE);
						desc.setVisibility(View.GONE);
					}
				}
				catch (Exception e)
				{}

			}

			@Override
			public void onCancelled(DatabaseError databaseError)
			{

			}
		});

		remove.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				databaseReference.child(post_key).removeValue();
				MainActivity.loaded = true;
				Intent i = new Intent(SinglePost.this,MainActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
			}
		});

		update.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				String t=title.getText().toString();
				String d=desc.getText().toString();
				if(!TextUtils.isEmpty(t) && !TextUtils.isEmpty(d))
				{
					databaseReference.child(post_key).child("title").setValue(t);
					databaseReference.child(post_key).child("description").setValue(d).addOnCompleteListener(new OnCompleteListener<Void>()
					{
						@Override
						public void onComplete(@NonNull Task<Void> task)
						{
							if(task.isSuccessful())
							{
								Toast.makeText(SinglePost.this,"post updated..",Toast.LENGTH_SHORT).show();
								MainActivity.loaded = true;
								Intent i = new Intent(SinglePost.this,MainActivity.class);
								i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(i);
							}
						}
					});
				}
				else
				{
					Toast.makeText(SinglePost.this,"enter all fields",Toast.LENGTH_SHORT).show();
				}
			}
		});

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}


}
