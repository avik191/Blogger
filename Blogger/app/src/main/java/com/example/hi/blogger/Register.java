package com.example.hi.blogger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity
{
	EditText name,email,password;
	Button register;

	ProgressDialog dialog;

	DatabaseReference databaseReference;
	FirebaseAuth firebaseAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		dialog = new ProgressDialog(Register.this);
		dialog.setMessage("Registering...");

		firebaseAuth = FirebaseAuth.getInstance();
		databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

	//	name= (EditText)findViewById(R.id.name);
		email= (EditText)findViewById(R.id.email);
		password=(EditText)findViewById(R.id.pass);

		register=(Button)findViewById(R.id.register);
		register.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				startRegister();
			}
		});
	}

	private void startRegister()
	{
		//final String n=name.getText().toString();
		String e=email.getText().toString();
		String p=password.getText().toString();

		if(!TextUtils.isEmpty(e) && !TextUtils.isEmpty(p))
		{
			dialog.setCancelable(false);
			dialog.show();
			firebaseAuth.createUserWithEmailAndPassword(e,p).addOnCompleteListener(new OnCompleteListener<AuthResult>()
			{
				@Override
				public void onComplete(@NonNull Task<AuthResult> task)
				{

					if(task.isSuccessful())
					{
						dialog.dismiss();

						Intent i = new Intent(Register.this,Setup.class);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);
					}
				}
			}).addOnFailureListener(new OnFailureListener()
			{
				@Override
				public void onFailure(@NonNull Exception e)
				{
					Toast.makeText(Register.this,e.toString(),Toast.LENGTH_LONG).show();
					dialog.dismiss();
				}
			});


		}
		else
		{
			Toast.makeText(Register.this,"Please enter all fields",Toast.LENGTH_LONG).show();
		}
	}
}
