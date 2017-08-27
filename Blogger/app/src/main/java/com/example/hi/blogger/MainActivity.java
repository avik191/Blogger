package com.example.hi.blogger;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
{

	RecyclerView recyclerView;
	MyCustomAdapter adapter;
	ArrayList<Blog> list;
	SearchView searchView;
	static ProgressDialog dialog;
	FloatingActionButton floatingActionButton;

	private DatabaseReference mdatabaseReference;
	private ChildEventListener mchildEventListener;
	private DatabaseReference userdatabaseReference;
	private DatabaseReference likesdatabaseReference;

	private Query mquery;



	private FirebaseAuth firebaseAuth;
	private FirebaseAuth.AuthStateListener authStateListener;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		dialog = new ProgressDialog(MainActivity.this);

		floatingActionButton = (FloatingActionButton)findViewById(R.id.addpost);
		floatingActionButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent i = new Intent(MainActivity.this,AddPost.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
			}
		});
//		//FirebaseDatabase.getInstance().setPersistenceEnabled(true);
		mdatabaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");
		userdatabaseReference= FirebaseDatabase.getInstance().getReference().child("Users");
		likesdatabaseReference= FirebaseDatabase.getInstance().getReference().child("Likes");

		mquery = mdatabaseReference.orderByChild("timestamp");
//		mdatabaseReference.keepSynced(true);
//		userdatabaseReference.keepSynced(true);
//		likesdatabaseReference.keepSynced(true);

		firebaseAuth = FirebaseAuth.getInstance();

		authStateListener = new FirebaseAuth.AuthStateListener()
		{
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
			{
				if(firebaseAuth.getCurrentUser() == null)
				{
					Intent i = new Intent(MainActivity.this, Login.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					//startActivity(i);
					startActivityForResult(i,10);
				}
				else
				{
					if(dialog != null)
					{
						dialog.setMessage("Fetching blogs...");
						dialog.setCancelable(false);
						dialog.show();
					}
				}
			}
		};

		list = new ArrayList<>();

		adapter = new MyCustomAdapter(this,list,likesdatabaseReference,firebaseAuth,mdatabaseReference);
		recyclerView = (RecyclerView) findViewById(R.id.recycle);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(adapter);

		mchildEventListener = new ChildEventListener() {
			@Override
			public void onChildAdded(DataSnapshot dataSnapshot, String s) {
				if(dialog != null && dialog.isShowing())
				{
					dialog.dismiss();
					dialog = null;
				}
				String j = dataSnapshot.getKey();
				Blog friendlyMessage = dataSnapshot.getValue(Blog.class);
				friendlyMessage.setPostkey(j);
				list.add(friendlyMessage);
				adapter.notifyDataSetChanged();

			}

			public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
			public void onChildRemoved(DataSnapshot dataSnapshot) {}
			public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
			public void onCancelled(DatabaseError databaseError) {}
		};
		//mdatabaseReference.addChildEventListener(mchildEventListener);
		mquery.addChildEventListener(mchildEventListener);



	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main_menu,menu);
		searchView = (SearchView) menu.findItem(R.id.searchbtn).getActionView();
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String query)
			{
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText)
			{
				if(adapter != null)
				{
					newText = newText.toLowerCase();
					ArrayList<Blog> newList = new ArrayList<>();
					for (Blog blog : list)
					{
						if (blog.getTitle().toLowerCase().contains(newText) || blog.getUsername().toLowerCase().contains(newText))
							newList.add(blog);
					}
					adapter.filterList(newList);
				}
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		if(item.getItemId() == R.id.signout)
		{
			firebaseAuth.signOut();
		}
		if(item.getItemId() == R.id.about)
		{
			final Runnable r = new Runnable()
			{
				@Override
				public void run()
				{
					final Dialog dialog = new Dialog(MainActivity.this);
					dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
					dialog.setContentView(R.layout.about_dialog_box);
					dialog.getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
					dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
					dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
					dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
					dialog.setCancelable(true);
					dialog.show();
					Button okBtn = (Button) dialog.findViewById(R.id.okBtn);
					okBtn.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							dialog.dismiss();

						}
					});
				}
			};
			new Handler(getMainLooper()).post(r);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart()
	{
		super.onStart();


		ConnectivityManager check = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

		NetworkInfo info = check.getActiveNetworkInfo();			//for checking whether app is connected to a network or not..
		if(info!=null && info.isConnected())
		{
			//Toast.makeText(MainActivity.this, "network available", Toast.LENGTH_SHORT).show();
			firebaseAuth.addAuthStateListener(authStateListener);
			checkUserExists();
		}
		else {
			//Toast.makeText(MainActivity.this, "No network available", Toast.LENGTH_SHORT).show();

			final Dialog dialog = new Dialog(MainActivity.this);
			dialog.setContentView(R.layout.error_dialog);
			dialog.setTitle("Error..");


			Button dialogButton = (Button) dialog.findViewById(R.id.okbtn);
			// if button is clicked, close the custom dialog
			dialogButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					dialog.dismiss();
				}
			});
			dialog.show();

		}


	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 10 && resultCode == RESULT_CANCELED)
			finish();
	}


	private void checkUserExists()
	{
		if(firebaseAuth.getCurrentUser() != null)
		{
			final String uid = firebaseAuth.getCurrentUser().getUid();
			userdatabaseReference.addValueEventListener(new ValueEventListener()
			{
				@Override
				public void onDataChange(DataSnapshot dataSnapshot)
				{
					if (!dataSnapshot.hasChild(uid))
					{
						Intent i = new Intent(MainActivity.this, Setup.class);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivityForResult(i,10);
					}
				}

				@Override
				public void onCancelled(DatabaseError databaseError)
				{

				}
			});
		}
	}
}
