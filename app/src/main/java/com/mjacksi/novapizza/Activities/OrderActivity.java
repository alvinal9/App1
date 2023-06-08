package com.mjacksi.novapizza.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mjacksi.novapizza.Adapters.FoodRecyclerViewAdapter;
import com.mjacksi.novapizza.Adapters.OrderRecyclerViewAdapter;
import com.mjacksi.novapizza.R;
import com.mjacksi.novapizza.RoomDatabase.FoodRoom;
import com.mjacksi.novapizza.RoomDatabase.FoodViewModel;

import java.text.DecimalFormat;
import java.util.List;

/**
 * The activity shown when the user selects items and clicks on the cart button (FAB)
 */
public class OrderActivity extends AppCompatActivity {
    private static final String TAG = OrderActivity.class.getSimpleName();
    FoodViewModel foodViewModel;
    private TextView amountTextView;
    OrderRecyclerViewAdapter adapter;

    /**
     * Get the items selected from the previous activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        toolbarSetup();

        adapter = new OrderRecyclerViewAdapter(this);
        RecyclerView recyclerView = findViewById(R.id.order_recyclerview);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        foodViewModel = ViewModelProviders.of(this).get(FoodViewModel.class);
        foodViewModel.getAllOrderedFood().observe(this, new Observer<List<FoodRoom>>() {
            /**
             * Calculate the total amount after changing the count of ordered items
             *
             * @param foods the Room database model
             */
            @Override
            public void onChanged(@Nullable List<FoodRoom> foods) {
                if (foods.size() == 0) finish();

                adapter.newFoodRooms(foods);

                double totalAmount = 0;
                for (int i = 0; i < foods.size(); i++) {
                    if (foods.get(i).getCount() != 0) {
                        totalAmount += (foods.get(i).getPrice() * foods.get(i).getCount());
                    }
                }
                setAmount(totalAmount);
            }
        });

        amountTextView = findViewById(R.id.text_view_order_amount);

        adapter.setOnItemClickListenerInc(new FoodRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FoodRoom food, int position) {
                food.setCount(food.getCount() + 1);
                foodViewModel.update(food);
            }
        });

        adapter.setOnItemClickListenerDec(new FoodRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FoodRoom food, int position) {
                food.setCount(food.getCount() - 1);
                foodViewModel.update(food);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab_done);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                placeOrderActivity();
            }
        });
    }

    private void toolbarSetup() {
        Toolbar toolbar = findViewById(R.id.order_toolbar);
        toolbar.setTitle("Review order");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Go to the place order activity
     */
    void placeOrderActivity() {
        Intent intent = new Intent(OrderActivity.this, PlaceOrderActivity.class);
        startActivity(intent);
    }

    /**
     * Format the price amount from double to string
     *
     * @param totalAmount will return as "Total amount: $*.**"
     */
    private void setAmount(double totalAmount) {
        DecimalFormat df = new DecimalFormat("#.##");
        amountTextView.setText("Total amount: Rs" + df.format(totalAmount));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_reset_all_orders) {
            foodViewModel.resetAllOrders();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
