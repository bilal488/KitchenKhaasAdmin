package com.example.technohem.kitchenkhaasadmin;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.technohem.kitchenkhaasadmin.Model.GalleryImages;
import com.example.technohem.kitchenkhaasadmin.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class GalleryActivity extends AppCompatActivity {

    // Recycler View code
    private DatabaseReference gallery_imagesRef;
    private RecyclerView gallery_recyclerView;
    RecyclerView.LayoutManager gallery_layoutManager;

    //delete image from storage
    private StorageReference galleryImageStorage;

    //toolbar
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        gallery_imagesRef = FirebaseDatabase.getInstance().getReference().child("GalleryImages");
        // to retrieve text in app offline
        gallery_imagesRef.keepSynced(true);

        //toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_gallery);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Gallery");//

        // Recycler View code
        gallery_recyclerView = findViewById(R.id.gallery_recycler_images);
        gallery_recyclerView.setHasFixedSize(true);
        gallery_layoutManager = new GridLayoutManager(this, 2);
        gallery_recyclerView.setLayoutManager(gallery_layoutManager);


    } // on create end

    // Recycler View code
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<GalleryImages> options =
                new FirebaseRecyclerOptions.Builder<GalleryImages>()
                        .setQuery(gallery_imagesRef, GalleryImages.class)
                        .build();

        FirebaseRecyclerAdapter<GalleryImages, MenuViewHolder> adapter =
                new FirebaseRecyclerAdapter<GalleryImages, MenuViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final MenuViewHolder holder, final int position, @NonNull final GalleryImages model) {

                        //Picasso.get().load(model.getG_image()).into(holder.imageView1);
                        // to retrieve image in app offline
                        Picasso.get().load(model.getG_image()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.imageView1, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {

                                Picasso.get().load(model.getG_image()).into(holder.imageView1);
                            }
                        });

                        // set click Listener
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                // check internet status
                                Boolean internetStatus = checkInternet();

                                if (internetStatus == true)
                                {
                                    CharSequence option[] = new CharSequence[]
                                            {
                                                    "Yes",
                                                    "No"
                                            };

                                    AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);
                                    builder.setTitle("Do you want to delete image ?");

                                    builder.setItems(option, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int i) {

                                            if( i == 0 )
                                            {
                                                String menuID = getRef(position).getKey();

                                                gallery_imagesRef.child(menuID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful())
                                                        {
                                                            //delete image from storage
                                                            galleryImageStorage = FirebaseStorage.getInstance().getReferenceFromUrl(model.getG_image());
                                                            galleryImageStorage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    Toast.makeText(GalleryActivity.this, "The gallery image is deleted successfully.", Toast.LENGTH_SHORT).show();

                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {

                                                                }
                                                            });

                                                        }

                                                    }
                                                });
                                            }
                                            else {

                                                //do nothing when user press no
                                            }
                                        }
                                    });

                                    builder.show();
                                }
                                else
                                {
                                    Toast.makeText(GalleryActivity.this, "No Internet! Please Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_image_layout, parent, false);
                        MenuViewHolder holder = new MenuViewHolder(view);
                        return holder;
                    }
                };
        gallery_recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    //popUp menu (Ctrl+O)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        return true;
    }//

    //popUp menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.addImage_id:
                startActivity(new Intent(GalleryActivity.this, AddGalleryImageActivity.class));
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }//

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
        startActivity(new Intent(GalleryActivity.this,HomeActivity.class));
        finish();
    }
}
