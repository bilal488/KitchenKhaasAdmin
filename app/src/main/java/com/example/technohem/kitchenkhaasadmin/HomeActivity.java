package com.example.technohem.kitchenkhaasadmin;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.technohem.kitchenkhaasadmin.Model.Menus;
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

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Recycler View code
    private DatabaseReference menusRef;
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    //delete image from storage
    private StorageReference menuImageStorage;

    //toolbar
    Toolbar toolbar;
    //navigation drawer
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer);

        menusRef = FirebaseDatabase.getInstance().getReference().child("Menus");
        // to retrieve text in app offline
        menusRef.keepSynced(true);

        //toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);//

        //navigation drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        //navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        // Recycler View code
        recyclerView = findViewById(R.id.recycler_menu);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


    }//on create end

    // Recycler View code
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Menus> options =
                new FirebaseRecyclerOptions.Builder<Menus>()
                        .setQuery(menusRef, Menus.class)
                        .build();

        FirebaseRecyclerAdapter<Menus, MenuViewHolder> adapter =
                new FirebaseRecyclerAdapter<Menus, MenuViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final MenuViewHolder holder, final int position, @NonNull final Menus model) {

                        Picasso.get().load(model.getImage()).into(holder.imageView);
                        // to retrieve image in app offline
                        Picasso.get().load(model.getImage()).networkPolicy(NetworkPolicy.OFFLINE).into(holder.imageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {

                                Picasso.get().load(model.getImage()).into(holder.imageView);
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

                                    AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                                    builder.setTitle("Do you want to delete product ?");

                                    builder.setItems(option, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int i) {

                                            if( i == 0 )
                                            {
                                                String menuID = getRef(position).getKey();

                                                menusRef.child(menuID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful())
                                                        {
                                                            //delete image from storage
                                                            menuImageStorage = FirebaseStorage.getInstance().getReferenceFromUrl(model.getImage());
                                                            menuImageStorage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    Toast.makeText(HomeActivity.this, "The menu is deleted successfully.", Toast.LENGTH_SHORT).show();

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
                                    Toast.makeText(HomeActivity.this, "No Internet! Please Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_layout, parent, false);
                        MenuViewHolder holder = new MenuViewHolder(view);
                        return holder;
                    }
                };
        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }// Recycler View code

    //popUp menu (Ctrl+O)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }//

    //popUp menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.addMenu_id:
                startActivity(new Intent(HomeActivity.this, AdminAddMenuActivity.class));
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }//

    //navigation drawer
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        int id = menuItem.getItemId();

        switch (id) {
            case R.id.nav_order:
                //Toast.makeText(this, "Order", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomeActivity.this,AdminNewOrdersActivity.class));
                finish();
                break;
            case R.id.nav_gallery:
                //Toast.makeText(this, "Starred Clicked", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomeActivity.this,GalleryActivity.class));
                finish();
                break;
            case R.id.nav_logout:
                //Toast.makeText(this, "Trash Clicked", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(HomeActivity.this,LoginActivity.class));
                finish();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }//

    //navigation drawer (to first close the navigation drawer then Application)
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
}
