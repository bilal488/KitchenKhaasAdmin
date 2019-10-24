package com.example.technohem.kitchenkhaasadmin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddGalleryImageActivity extends AppCompatActivity {

    private Button AddNewImageButton;
    private ImageView InputGalleryImage;
    private Uri ImageUri;
    private static final int GalleryPick = 1;

    private String saveCurrentDate,saveCurrentTime;

    private String imageRandomKey,downloadImageUrl;

    private StorageReference ImageRef;
    private DatabaseReference ImagedbRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gallery_image);

        ImageRef = FirebaseStorage.getInstance().getReference().child("Gallery Images");
        ImagedbRef = FirebaseDatabase.getInstance().getReference().child("GalleryImages");
        // to retrieve text in app offline
        ImagedbRef.keepSynced(true);

        AddNewImageButton = (Button)findViewById(R.id.add_new_gallery_image);
        InputGalleryImage = (ImageView)findViewById(R.id.select_gallery_image);
        loadingBar = new ProgressDialog(this);

        InputGalleryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SelectImageFromGallery();
            }
        });

        AddNewImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // check internet status
                Boolean internetStatus = checkInternet();

                if (internetStatus == true)
                {
                    ValidateGalleryImageData();
                }
                else
                {
                    Toast.makeText(AddGalleryImageActivity.this, "No Internet! Please Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }// on create end

    private void SelectImageFromGallery() {

        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GalleryPick && resultCode==RESULT_OK && data !=null)
        {

            ImageUri = data.getData();
            InputGalleryImage.setImageURI(ImageUri);
        }
    }

    private void ValidateGalleryImageData() {

        if(ImageUri == null)
        {
            Toast.makeText(this, "Image is Mandatory", Toast.LENGTH_SHORT).show();
        }else{

            StoreMenuInformation();
        }
    }

    private void StoreMenuInformation() {

        loadingBar.setTitle("Add New Image");
        loadingBar.setMessage("Dear Admin, please wait while we are adding the new image.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy ");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat(" HH:mm:ss a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        imageRandomKey = saveCurrentDate + saveCurrentTime;

        final StorageReference filePath = ImageRef.child(ImageUri.getLastPathSegment()+ imageRandomKey + ".jpg");

        final UploadTask uploadTask = filePath.putFile(ImageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                String message = e.toString();
                Toast.makeText(AddGalleryImageActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Toast.makeText(AddGalleryImageActivity.this, "Gallery Image Uploaded Successfully...", Toast.LENGTH_SHORT).show();
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        downloadImageUrl = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if(task.isSuccessful())
                        {

                            downloadImageUrl = task.getResult().toString();
                            Toast.makeText(AddGalleryImageActivity.this, "Got the Gallery Image URL Successfully...", Toast.LENGTH_SHORT).show();

                            SaveGalleryImageInfoToDatabase();

                        }
                    }
                });
            }
        });
    }

    private void SaveGalleryImageInfoToDatabase() {

        HashMap<String, Object> imageMap = new HashMap<>();
        imageMap.put("image_id", imageRandomKey);
        imageMap.put("date", saveCurrentDate);
        imageMap.put("time", saveCurrentTime);
        imageMap.put("g_image", downloadImageUrl);

        ImagedbRef.child(imageRandomKey).updateChildren(imageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {
                    Intent intent = new Intent(AddGalleryImageActivity.this, GalleryActivity.class);
                    startActivity(intent);
                    finish();
                    loadingBar.dismiss();
                    Toast.makeText(AddGalleryImageActivity.this, "Image is added Successfully..", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.dismiss();
                    String message = task.getException().toString();
                    Toast.makeText(AddGalleryImageActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    // check internet status
    public boolean checkInternet() {

        ConnectivityManager cm = (ConnectivityManager)getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected())
        {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddGalleryImageActivity.this, GalleryActivity.class);
        startActivity(intent);
        finish();
    }

}
