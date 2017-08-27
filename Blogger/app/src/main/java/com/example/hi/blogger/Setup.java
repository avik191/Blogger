package com.example.hi.blogger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class Setup extends AppCompatActivity
{
	EditText name;
	Button submit;
	ImageButton dp;
	Uri imageUri;

	StorageReference storageReference;
	DatabaseReference databaseReference;
	FirebaseAuth firebaseAuth;

	ProgressDialog dialog;


	public static int GALLERY_REQUEST = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup);

		name = (EditText)findViewById(R.id.displayname);
		dp = (ImageButton)findViewById(R.id.displaypic);
		submit = (Button)findViewById(R.id.submit);

		dialog = new ProgressDialog(Setup.this);
		dialog.setMessage("signing in...");
		dialog.setCancelable(false);

		databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
		firebaseAuth = FirebaseAuth.getInstance();
		storageReference = FirebaseStorage.getInstance().getReference().child("Display_pics");

		dp.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if ((ContextCompat.checkSelfPermission(Setup.this,
						android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
						!= PackageManager.PERMISSION_GRANTED)){

					ActivityCompat.requestPermissions(Setup.this,
							new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},10);

				}
				else
				{
					Intent i = new Intent(Intent.ACTION_GET_CONTENT);
					i.setType("image/*");
					startActivityForResult(i, GALLERY_REQUEST);
				}
			}
		});

		submit.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				setupAccount();
			}
		});
	}

	private void setupAccount()
	{
		final String n = name.getText().toString();
		if(!TextUtils.isEmpty(n) && imageUri != null)
		{
			dialog.show();
			final String uid = firebaseAuth.getCurrentUser().getUid();
			StorageReference reference = storageReference.child(imageUri.getLastPathSegment());
			reference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
			{
				@Override
				public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
				{
					String downloaduri = taskSnapshot.getDownloadUrl().toString();
					databaseReference.child(uid).child("name").setValue(n);
					databaseReference.child(uid).child("image").setValue(downloaduri);
					dialog.dismiss();

					Intent i = new Intent(Setup.this,MainActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
				}
			}).addOnFailureListener(new OnFailureListener()
			{
				@Override
				public void onFailure(@NonNull Exception e)
				{
					Toast.makeText(Setup.this,"Failed to sign in..Try again later",Toast.LENGTH_LONG).show();
				}
			});
		}
		else
		{
			Toast.makeText(Setup.this,"Enter all fields",Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK)
		{
			Uri uri = data.getData();

			// selected image uri is sent to croppong fragment
			CropImage.activity(uri)
					.setGuidelines(CropImageView.Guidelines.ON)
					.setAspectRatio(1,1)
					.start(this);
		}
		// result from cropping fragment i.e is cropped image is returned.
		if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
		{
			CropImage.ActivityResult result = CropImage.getActivityResult(data);
			if (resultCode == RESULT_OK)
			{
				imageUri = result.getUri();
				dp.setImageURI(imageUri);
			}
			else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
			{
				Exception error = result.getError();
			}
		}
	}
}
