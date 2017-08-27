package com.example.hi.blogger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class AddPost extends AppCompatActivity
{

	public static final int GALLERY_REQUEST = 1;
	ImageButton coverpic;
	EditText title,desc;
	Button submit;
	Uri imageUri,abcd;
	ProgressDialog dialog;

	StorageReference mstorageReference;
	DatabaseReference mdatabaseReference;
	FirebaseAuth firebaseAuth;
	DatabaseReference userdatabaseReference;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_post);

		mstorageReference = FirebaseStorage.getInstance().getReference();
		mdatabaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");
		firebaseAuth = FirebaseAuth.getInstance();
		userdatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getCurrentUser().getUid());



		dialog = new ProgressDialog(AddPost.this);

		coverpic = (ImageButton) findViewById(R.id.coverpic);
		title = (EditText) findViewById(R.id.posttitle);
		desc = (EditText) findViewById(R.id.description);
		submit = (Button) findViewById(R.id.addpost);

		// Get the data from an ImageView as bytes
		coverpic.setDrawingCacheEnabled(true);
		coverpic.buildDrawingCache();

		submit.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{

				startPosting();
			}
		});

		coverpic.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if ((ContextCompat.checkSelfPermission(AddPost.this,
						android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
						!= PackageManager.PERMISSION_GRANTED)){

					ActivityCompat.requestPermissions(AddPost.this,
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
	}

	private void startPosting()
	{
		final String posttitle = title.getText().toString();
		final String description = desc.getText().toString();

		if(!TextUtils.isEmpty(posttitle) && !TextUtils.isEmpty(description) && imageUri != null)
		{
			dialog.setMessage("uploading post..");
			dialog.setCancelable(false);
			dialog.show();

				StorageReference reference = mstorageReference.child("Blog_pics").child(imageUri.getLastPathSegment());

				Bitmap bitmap = coverpic.getDrawingCache();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);

				byte[] data = baos.toByteArray();

			//UploadTask uploadTask = reference.putFile(imageUri);
			UploadTask uploadTask = reference.putBytes(data);
			uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
			{
				@Override
				public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
				{
					final Uri downloaduri = taskSnapshot.getDownloadUrl();


					dialog.cancel();

					userdatabaseReference.addValueEventListener(new ValueEventListener()
					{
						@Override
						public void onDataChange(DataSnapshot dataSnapshot)
						{
							Blog blog = new Blog();
							long millis = System.currentTimeMillis();
							int timestamp = ((int) (millis/1000))* -1;
							blog.setTimestamp(timestamp);
							blog.setViews(0);
							blog.setDescription(description);
							blog.setTitle(posttitle);
							blog.setUrl(downloaduri.toString());
							blog.setUid(firebaseAuth.getCurrentUser().getUid());
							blog.setUsername(dataSnapshot.child("name").getValue().toString());
							blog.setImage(dataSnapshot.child("image").getValue().toString());
							mdatabaseReference.push().setValue(blog).addOnSuccessListener(new OnSuccessListener<Void>()
							{
								@Override
								public void onSuccess(Void aVoid)
								{
									MainActivity.loaded = true;
									startActivity(new Intent(AddPost.this, MainActivity.class));
									finish();
								}
							}).addOnFailureListener(new OnFailureListener()
							{
								@Override
								public void onFailure(@NonNull Exception e)
								{
									Toast.makeText(AddPost.this,"Failed to post the blog.. Try again later",Toast.LENGTH_LONG).show();
								}
							});

						}

						@Override
						public void onCancelled(DatabaseError databaseError)
						{

						}
					});


				}
			});
			uploadTask.addOnFailureListener(new OnFailureListener()
			{
				@Override
				public void onFailure(@NonNull Exception e)
				{
					dialog.cancel();
					Toast.makeText(AddPost.this,"Failed to post the blog.. Try again later",Toast.LENGTH_LONG).show();
				}
			});
		}
		else
		{
			Toast.makeText(AddPost.this,"Please upload display pic and enter name both..",Toast.LENGTH_LONG).show();

		}
	}




	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK)
		{
			Uri uri = data.getData();
			abcd=uri;
			// selected image uri is sent to croppong fragment
			CropImage.activity(uri)
					.setGuidelines(CropImageView.Guidelines.ON)
					.setAspectRatio(16,9)
					.start(this);
		}
		// result from cropping fragment i.e is cropped image is returned.
		if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
		{
			CropImage.ActivityResult result = CropImage.getActivityResult(data);
			if (resultCode == RESULT_OK)
			{
				imageUri = result.getUri();
				coverpic.setImageURI(imageUri);
			}
			else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
			{
				Exception error = result.getError();
			}
		}
	}
}
