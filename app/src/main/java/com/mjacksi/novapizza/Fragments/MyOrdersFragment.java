package com.mjacksi.novapizza.Fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mjacksi.novapizza.Adapters.OrdersAdapter;
import com.mjacksi.novapizza.Models.FirebaseOrder;
import com.mjacksi.novapizza.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fragment where all user orders and its statues shown
 * getting all data from firebase database
 */
public class MyOrdersFragment extends Fragment {

    public final String TAG = MyOrdersFragment.class.getSimpleName();
    OrdersAdapter adapter;

    public MyOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_orders, container, false);

        adapter = new OrdersAdapter(getActivity());
        RecyclerView recyclerView = v.findViewById(R.id.my_orders_recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(getString(R.string.firebase_users_slash) + currentUser.getUid() + getString(R.string.firebase_slash_orders));

        myRef.orderByChild(getString(R.string.firebase_time)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                List<FirebaseOrder> orders = new ArrayList<>();
                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    FirebaseOrder order = orderSnapshot.getValue(FirebaseOrder.class);
                    orders.add(order);
                }
                Collections.reverse(orders);
                adapter.setNewList(orders);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return v;
    }

}
