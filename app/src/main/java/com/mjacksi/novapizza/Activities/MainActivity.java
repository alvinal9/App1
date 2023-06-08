package com.mjacksi.novapizza.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.mjacksi.novapizza.BuildConfig;
import com.mjacksi.novapizza.Fragments.HomeFragment;
import com.mjacksi.novapizza.Fragments.MyOrdersFragment;
import com.mjacksi.novapizza.Fragments.TermsFragment;
import com.mjacksi.novapizza.R;
import com.mjacksi.novapizza.RoomDatabase.FoodViewModel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final int RC_SIGN_IN = 444;
    private FoodViewModel foodViewModel;
    private FloatingActionButton fab;

    FirebaseAuth firebaseAuth;

    boolean thereIsOrder = false;

    /**
     * The first activity in the application
     * every time the app opened, it checks if the user is opening the app for the first time to show the intro.
     * <code>FirebaseDatabase.getInstance().setPersistenceEnabled(true);</code> to make data available for the user even offline
     * <p>
     * Setting the fab action for adding items to cart.
     * Set the drawer information
     * <p>
     * FoodViewModel to listen to data changing to change the appearance of the cart-button (FAB)
     * and the total view if there are some items selected
     * FirebaseAuth.getInstance() if the user is signed in, then show logout to the user else hide it and show the login
     * <p>
     * FoodViewModel to manage the database, listen to data changing, select and un-select items to change the appearance of the recycler-view
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showIntro();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, OrderActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }


        foodViewModel = ViewModelProviders.of(this).get(FoodViewModel.class);
        foodViewModel.getCount().observe(MainActivity.this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer != null && integer != 0) {
                    fab.show();
                    thereIsOrder = true;
                } else {
                    fab.hide();
                    thereIsOrder = false;
                }
                invalidateOptionsMenu();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                updateLogoutLogout(firebaseAuth.getCurrentUser());
            }
        });
    }

    /**
     * Hide drawer if it's open when click back button
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Set menu from menu/main.xml
     *
     * @param menu the menu of activity
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.action_reset_all_orders);
        item.setVisible(thereIsOrder);
        return true;
    }

    /**
     * Set on click of the menu button (delete all)
     *
     * @param item pressed item on menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_reset_all_orders) {
            foodViewModel.resetAllOrders();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Set actions of all options in drawer menu
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        } else if (id == R.id.nav_orders) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new MyOrdersFragment()).commit();
        } else if (id == R.id.nav_terms) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new TermsFragment()).commit();
        } else if (id == R.id.nav_share) {
            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "FestMunch");
                String shareMessage = "\nOrder delicious meals from our app\n";
                shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n\n";
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "Choose one"));
            } catch (Exception e) {
                //e.toString();
            }
        } else if (id == R.id.login) {
            signIn();
        } else if (id == R.id.logout) {
            firebaseAuth.signOut();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Update the UI to be suitable for the user's login state
     *
     * @param currentUser the user information who logged
     */
    private void updateLogoutLogout(FirebaseUser currentUser) {
        NavigationView navigationView = findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);
        TextView nameNavHeader = headerView.findViewById(R.id.nav_name_text);
        Menu nav_Menu = navigationView.getMenu();
        if (currentUser == null) {
            nav_Menu.findItem(R.id.logout).setVisible(false);
            nav_Menu.findItem(R.id.login).setVisible(true);
            nav_Menu.findItem(R.id.nav_orders).setVisible(false);
        } else {
            nav_Menu.findItem(R.id.logout).setVisible(true);
            nav_Menu.findItem(R.id.login).setVisible(false);
            nav_Menu.findItem(R.id.nav_orders).setVisible(true);
        }
    }

    /**
     * Start the pre-built Firebase sign-in UI
     */
    void signIn() {
        List<AuthUI.IdpConfig> providers = Collections.singletonList(
                new AuthUI.IdpConfig.PhoneBuilder().build());
        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.AppTheme_NoActionBar)
                        .build(),
                RC_SIGN_IN);
    }


    /**
     * The result comes from Firebase sign-in UI
     *
     * @param requestCode request code
     * @param resultCode  result code
     * @param data        the data came from the closed activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                updateLogoutLogout(user);
                // ...
            } else {
                Snackbar.make(getWindow().getDecorView().getRootView()
                                , "Failed login, retry again!", Snackbar.LENGTH_LONG)
                        .setAction("Failed", null).show();
            }
        }
    }

    /**
     * Show intro activity if the user is first time opening the app
     */
    void showIntro() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                // Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                // If the activity has never started before...
                if (isFirstStart) {

                    // Launch app intro
                    final Intent i = new Intent(MainActivity.this, IntroActivity.class);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(i);
                        }
                    });

                    // Make a new preferences editor
                    SharedPreferences.Editor e = getPrefs.edit();

                    // Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);

                    // Apply changes
                    e.apply();
                }
            }
        });

        // Start the thread
        t.start();
    }

}
