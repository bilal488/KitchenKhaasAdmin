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

public class AdminAddMenuActivity extends AppCompatActivity {

    private Button AddNewMenuButton;
    private ImageView InputMenuImage;
    private EditText InputMenuName, InputMenuPrice;
    private Uri ImageUri;
    private static final int GalleryPick = 1;

    private String Price,Mname,saveCurrentDate,saveCurrentTime;

    private String menuRandomKey,downloadImageUrl;

    private StorageReference MenuImageRef;
    private DatabaseReference MenusRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_menu);

        MenuImageRef = FirebaseStorage.getInstance().getReference().child("Menu Images");
        MenusRef = FirebaseDatabase.getInstance().getReference().child("Menus");
        // to retrieve text in app offline
        MenusRef.keepSynced(true);

        Toast.makeText(this, "Welcome Admin...", Toast.LENGTH_SHORT).show();

        AddNewMenuButton = (Button)findViewById(R.id.add_new_menu);
        InputMenuImage = (ImageView)findViewById(R.id.select_menu_image);
        InputMenuName = (EditText)findViewById(R.id.menu_name);
        InputMenuPrice = (EditText)findViewById(R.id.menu_price);
        loadingBar = new ProgressDialog(this);

        InputMenuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                OpenGallery();
            }
        });

        AddNewMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // check internet status
                Boolean internetStatus = checkInternet();

                if (internetStatus == true)
                {
                    ValidateProductData();
                }
                else
                {
                    Toast.makeText(AdminAddMenuActivity.this, "No Internet! Please Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                }

            }
        });
    } // on create end

    private void OpenGallery() {
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
            InputMenuImage.setImageURI(ImageUri);
        }
    }

    private void ValidateProductData()
    {

        Mname = InputMenuName.getText().toString();
        Price = InputMenuPrice.getText().toString();

        if(ImageUri == null)
        {
            Toast.makeText(this, "Product Image is Mandatory", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Mname))
        {
            Toast.makeText(this, "Please Write product name...", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(Price))
        {
            Toast.makeText(this, "Please Write product price...", Toast.LENGTH_SHORT).show();
        }else{
            StoreMenuInformation();
        }

    }

    private void StoreMenuInformation() {

        loadingBar.setTitle("Add New Menu");
        loadingBar.setMessage("Dear Admin, please wait while we are adding the new menu.");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy ");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat(" HH:mm:ss a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        menuRandomKey = saveCurrentDate + saveCurrentTime;

        final StorageReference filePath = MenuImageRef.child(ImageUri.getLastPathSegment()+ menuRandomKey + ".jpg");

        final UploadTask uploadTask = filePath.putFile(ImageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                String message = e.toString();
                Toast.makeText(AdminAddMenuActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Toast.makeText(AdminAddMenuActivity.this, "Menu Image Uploaded Successfully...", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(AdminAddMenuActivity.this, "Got the Menu Image URL Successfully...", Toast.LENGTH_SHORT).show();

                            SaveProductInfoToDatabase();

                        }
                    }
                });
            }
        });


    }

    private void SaveProductInfoToDatabase() {

        HashMap<String, Object> productMap = new HashMap<>();
        productMap.put("pid", menuRandomKey);
        productMap.put("date", saveCurrentDate);
        productMap.put("time", saveCurrentTime);
        productMap.put("image", downloadImageUrl);
        productMap.put("price", Price);
        productMap.put("pname", Mname);

        MenusRef.child(menuRandomKey).updateChildren(productMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    Intent intent = new Intent(AdminAddMenuActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                    loadingBar.dismiss();
                    Toast.makeText(AdminAddMenuActivity.this, "Menu is added Successfully..", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.dismiss();
                    String message = task.getException().toString();
                    Toast.makeText(AdminAddMenuActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(AdminAddMenuActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
