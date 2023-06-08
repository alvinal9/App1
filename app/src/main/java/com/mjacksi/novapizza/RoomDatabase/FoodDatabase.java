package com.mjacksi.novapizza.RoomDatabase;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.mjacksi.novapizza.R;

/**
 * 3- Database
 * Connect the model {@link FoodRoom} with {@link FoodDao} in this class
 * to initialize the database
 */
@Database(entities = {FoodRoom.class}, version = 1, exportSchema = false)
public abstract class FoodDatabase extends RoomDatabase {

    private static FoodDatabase instance;

    public abstract FoodDao foodDao();

    /**
     * To make sure we just have on instance of our database every time
     * to save memory and for more efficient
     * this function has "synchronized" to set only one thread at a time can access this method
     * for avoiding making more than one object at the same time
     *
     * @param context
     * @return
     */
    public static synchronized FoodDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    FoodDatabase.class, "food_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    /**
     * Put to initial data to our database
     */
    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private FoodDao foodDao;

        private PopulateDbAsyncTask(FoodDatabase db) {
            foodDao = db.foodDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            foodDao.insert(new FoodRoom("Arabic Rolls",
                    "4 Pcs Arabic Rolls Stuffed with Yummiest Mix Served with Sauce",
                    500.00, R.drawable.arabicrolls));

            foodDao.insert(new FoodRoom("Behari Rolls",
                    "4 Pcs Behari Rolls Stuffed with Yummiest Mix Served with Sauce\"",
                    500.00, R.drawable.beharirolls));

            foodDao.insert(new FoodRoom("Cheese Sticks",
                    "Freshly Baked Bread Filled with the Yummiest Cheese Blend to Satisfy your Cravings.",
                    600.00, R.drawable.cheesesticks));

            foodDao.insert(new FoodRoom("Flaming Wings",
                    "Fresh Oven Baked Wings Tossed In Hot Peri Peri Sauce and Served With Dip Saucev",
                    900.00, R.drawable.flamingwings));

            foodDao.insert(new FoodRoom("Mexican Sandwich",
                    "Mozzarella Dipped Chicken Topped with Garlic Sauce, Tomato’s Served in Baked Bread.",
                    800.00, R.drawable.mexicansandwich));

            foodDao.insert(new FoodRoom("Roasted Platter",
                    "4 Pcs Behari Rolls, 6pcs Wings Served with Fries & Sauce",
                    1000.00, R.drawable.roastedplatter));

            return null;
        }
    }
}
