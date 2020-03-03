package com.journaldev.androidlivedata;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.journaldev.androidlivedata.db.DbSettings;
import com.journaldev.androidlivedata.db.FavouritesDBHelper;

import java.util.ArrayList;
import java.util.List;

public class FavouritesViewModel extends AndroidViewModel {

    private FavouritesDBHelper mFavHelper;
    private MutableLiveData<List<Favourites>> mMutableLiveData;

    public FavouritesViewModel(Application application) {
        super(application);
        mFavHelper = new FavouritesDBHelper(application);
    }

    public MutableLiveData<List<Favourites>> getFavs() {
        if (mMutableLiveData == null) {
            mMutableLiveData = new MutableLiveData<>();
            loadFavs();
        }

        return mMutableLiveData;
    }

    private void loadFavs() {
        List<Favourites> newFavs = new ArrayList<>();
        SQLiteDatabase db = mFavHelper.getReadableDatabase();
        Cursor cursor = db.query(DbSettings.DBEntry.TABLE,
                new String[]{
                        DbSettings.DBEntry._ID,
                        DbSettings.DBEntry.COL_FAV_URL,
                        DbSettings.DBEntry.COL_FAV_DATE
                },
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            int idxId = cursor.getColumnIndex(DbSettings.DBEntry._ID);
            int idxUrl = cursor.getColumnIndex(DbSettings.DBEntry.COL_FAV_URL);
            int idxDate = cursor.getColumnIndex(DbSettings.DBEntry.COL_FAV_DATE);
            newFavs.add(new Favourites(cursor.getLong(idxId), cursor.getString(idxUrl), cursor.getLong(idxDate)));
        }

        cursor.close();
        db.close();
        mMutableLiveData.setValue(newFavs);
    }


    public void addFav(String url, long date) {

        long id = insertIntoTable(url, date);

        List<Favourites> favsList = mMutableLiveData.getValue();
        ArrayList<Favourites> clonedFavs = new ArrayList<>(favsList.size());

        // copying data to clonedFavs
        for (int i = 0; i < favsList.size(); i++) {
            clonedFavs.add(favsList.get(i));
        }

        // add  new data
        Favourites mFavourites = new Favourites(id, url, date);
        clonedFavs.add( mFavourites );

        mMutableLiveData.setValue(clonedFavs);
    }

    private long insertIntoTable(String url, long date) {
        SQLiteDatabase db = mFavHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DbSettings.DBEntry.COL_FAV_URL, url);
        values.put(DbSettings.DBEntry.COL_FAV_DATE, date);
        long id = db.insertWithOnConflict(DbSettings.DBEntry.TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE);
        db.close();

        return  id;
    }

    public void removeFav(long id) {

        removeFromTable(id);

        List<Favourites> favsList = mMutableLiveData.getValue();
        ArrayList<Favourites> clonedFavs = new ArrayList<>(favsList.size());

        // copying data to clonedFavs
        for (int i = 0; i < favsList.size(); i++) {
            clonedFavs.add(favsList.get(i));
        }

        for (int j = 0; j < clonedFavs.size(); j++) {
            if (clonedFavs.get(j).mId == id)
            {
                clonedFavs.remove(j);
            }
        }
        mMutableLiveData.setValue(clonedFavs);
    }

    private void removeFromTable(long id) {

        SQLiteDatabase db = mFavHelper.getWritableDatabase();
        db.delete(
                DbSettings.DBEntry.TABLE,
                DbSettings.DBEntry._ID + " = ?",
                new String[]{Long.toString(id)}
        );
        db.close();
    }

}
