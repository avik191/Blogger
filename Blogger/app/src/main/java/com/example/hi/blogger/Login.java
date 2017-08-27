package com.example.hi.blogger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity
{

	EditText email,password;
	Button login,register;

	SignInButton googlesignin;

	ProgressDialog dialog;
	FirebaseAuth firebaseAuth;
	DatabaseReference databaseReference;

	private static int RC_SIGN_IN = 2;
	private static String TAG = "login";

	private GoogleApiClient mGoogleApiClient;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		dialog=new ProgressDialog(Login.this);
		dialog.setMessage("signinging in...");
		dialog.setCancelable(false);

		firebaseAuth=FirebaseAuth.getInstance();
		databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");

		email= (EditText)findViewById(R.id.useremail);
		password=(EditText)findViewById(R.id.userpass);
		googlesignin = (SignInButton)findViewById(R.id.googlesignin);

		googlesignin.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				setupGoogleAccount();
			}
		});

		register=(Button)findViewById(R.id.registeruser);
		register.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent i = new Intent(Login.this, Register.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
			}
		});
		login=(Button)findViewById(R.id.login);
		login.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				startlogin();
			}
		});

		// Configure Google Sign In
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(getString(R.string.default_web_client_id))
				.requestEmail()
				.build();

		mGoogleApiClient = new GoogleApiClient.Builder(Login.this)
				.enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener()
				{
					@Override
					public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
					{

					}
				}).addApi(Auth.GOOGLE_SIGN_IN_API,gso)
				.build();

	}

	private void setupGoogleAccount()
	{
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
		startActivityForResult(signInIntent, RC_SIGN_IN);
	}

	private void startlogin()
	{
		String e = email.getText().toString();
		String p = password.getText().toString();

		if(!TextUtils.isEmpty(e) && !TextUtils.isEmpty(p))
		{
			dialog.show();
			firebaseAuth.signInWithEmailAndPassword(e,p).addOnCompleteListener(new OnCompleteListener<AuthResult>()
			{
				@Override
				public void onComplete(@NonNull Task<AuthResult> task)
				{
					if(task.isSuccessful())
					{
						dialog.dismiss();
						checkUserExists();
					}
					else
					{
						dialog.dismiss();
						Toast.makeText(Login.this,"Enter correct values",Toast.LENGTH_LONG).show();
					}
				}
			});
		}
		else
			Toast.makeText(Login.this,"Enter all values",Toast.LENGTH_LONG).show();
	}

	private void checkUserExists()
	{
		if(firebaseAuth.getCurrentUser() != null)
		{
			final String uid = firebaseAuth.getCurrentUser().getUid();
			databaseReference.addValueEventListener(new ValueEventListener()
			{
				@Override
				public void onDataChange(DataSnapshot dataSnapshot)
				{
					if (dataSnapshot.hasChild(uid))
					{
						Intent i = new Intent(Login.this, MainActivity.class);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
					}
					else
					{
						Toast.makeText(Login.this, "Setup your account", Toast.LENGTH_LONG).show();
						Intent i = new Intent(Login.this, Setup.class);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
					}
				}

				@Override
				public void onCancelled(DatabaseError databaseError)
				{

				}
			});
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
		if (requestCode == RC_SIGN_IN) {

			dialog.setMessage("signing in..");
			dialog.show();
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
			if (result.isSuccess()) {
				// Google Sign In was successful, authenticate with Firebase
				GoogleSignInAccount account = result.getSignInAccount();
				firebaseAuthWithGoogle(account);
			} else {
				// Google Sign In failed, update UI appropriately
				// ...
				dialog.dismiss();
			}
		}
	}

	private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
		Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

		AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
		firebaseAuth.signInWithCredential(credential)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							// Sign in success, update UI with the signed-in user's information
							Log.d(TAG, "signInWithCredential:success");
							FirebaseUser user = firebaseAuth.getCurrentUser();
							checkUserExists();

						} else {
							// If sign in fails, display a message to the user.
							Log.w(TAG, "signInWithCredential:failure", task.getException());
							Toast.makeText(Login.this, "Authentication failed.",
									Toast.LENGTH_SHORT).show();
						}

						// ...
						dialog.dismiss();
					}
				});
	}
}
