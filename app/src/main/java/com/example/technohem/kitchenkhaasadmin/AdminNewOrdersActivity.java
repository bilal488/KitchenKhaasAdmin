package com.example.technohem.kitchenkhaasadmin;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.technohem.kitchenkhaasadmin.Model.AdminOrders;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminNewOrdersActivity extends AppCompatActivity {

    private RecyclerView ordersList;
    private DatabaseReference ordersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_new_orders);

        ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders");
        // to retrieve text in app offline
        ordersRef.keepSynced(true);

        ordersList = findViewById(R.id.orders_list);
        ordersList.setLayoutManager(new LinearLayoutManager(this));

    }// on create end

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<AdminOrders> options = new FirebaseRecyclerOptions.Builder<AdminOrders>()
                .setQuery(ordersRef, AdminOrders.class)
                .build();

        FirebaseRecyclerAdapter<AdminOrders, AdminOrdersViewHolder > adapter =
                new FirebaseRecyclerAdapter<AdminOrders, AdminOrdersViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull AdminOrdersViewHolder holder, final int position, @NonNull final AdminOrders model) {

                        holder.userName.setText("Name: "+ model.getName());
                        holder.userPhone.setText("Phone: "+ model.getPhone());
                        holder.userAddress.setText("Location Address: "+ model.getAddress());
                        holder.userDate.setText("Date: "+ model.getDate());
                        holder.userTime.setText("Time: "+ model.getTime());
                        holder.userTotalPrice.setText("Total Price = "+ model.getTotalAmount()+" Rs");
                        holder.userDescription.setText("Description: "+ model.getDescription());

                        holder.showReservationMenuBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                String uID = getRef(position).getKey();

                                Intent intent = new Intent(AdminNewOrdersActivity.this,AdminUserMenusActivity.class);
                                intent.putExtra("uid" , uID);
                                startActivity(intent);
                            }
                        });

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                CharSequence option[] = new CharSequence[]
                                        {
                                                "Yes",
                                                "No"
                                        };

                                AlertDialog.Builder builder = new AlertDialog.Builder(AdminNewOrdersActivity.this);
                                builder.setTitle("Have the Event is done ?");

                                builder.setItems(option, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {

                                        if( i == 0 )
                                        {
                                            String uID = getRef(position).getKey();

                                            RemoveOrder(uID);
                                        }
                                        else {

                                            //Intent intent = new Intent(AdminNewOrdersActivity.this,AdminUserMenusActivity.class);
                                            //startActivity(intent);
                                        }
                                    }
                                });

                                builder.show();
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public AdminOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.orders_layout, parent,false);
                        return new AdminOrdersViewHolder(view);
                    }
                };

        ordersList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class AdminOrdersViewHolder extends RecyclerView.ViewHolder {

        public TextView userName,userPhone,userAddress,userTime,userDate,userTotalPrice,userDescription;
        public Button showReservationMenuBtn;

        public AdminOrdersViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.order_User_name);
            userPhone = itemView.findViewById(R.id.order_phone_number);
            userAddress = itemView.findViewById(R.id.order_address);
            userTime = itemView.findViewById(R.id.order_time);
            userDate = itemView.findViewById(R.id.order_date);
            userTotalPrice = itemView.findViewById(R.id.order_Total_Price);
            userDescription = itemView.findViewById(R.id.order_description);

            showReservationMenuBtn = itemView.findViewById(R.id.show_reservation_menu_btn);
        }
    }

    private void RemoveOrder(String uID) {

        ordersRef.child(uID).removeValue();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AdminNewOrdersActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
