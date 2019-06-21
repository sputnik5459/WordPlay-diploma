package com.sputnik5459.diploma;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WorkWithGames {

    DatabaseHelper dbHelper;
    SQLiteDatabase database;
    List<String> words = new ArrayList<>(), translations = new ArrayList<>(), learned = new ArrayList<>();

    public void initHelper(Context context){

        dbHelper = new DatabaseHelper(context);

        try {
            dbHelper.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        try {
            database= dbHelper.getWritableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }
    }

    public void loadDeck(String deck_name){
        List<String> words_id = new ArrayList<>();
        String deck_id;

        Cursor cursor1 = database.rawQuery("SELECT deck_id FROM "+ DatabaseHelper.TABLE_DECKS +" WHERE deck_name = ?",
                new String[]{deck_name});

        cursor1.moveToFirst();
        deck_id = cursor1.getString(0);
        cursor1.close();

        Cursor cursor2 = database.rawQuery("SELECT word_id FROM " + DatabaseHelper.TABLE_DECKS_WORDS + " WHERE deck_id = ?",
                new String[]{deck_id});
        cursor2.moveToFirst();
        while(!cursor2.isAfterLast())
        {
            words_id.add(cursor2.getString(0));
            cursor2.moveToNext();
        }

        cursor2.close();

        for(int q = 0; q < words_id.size(); q++)
        {
            Cursor cursor3;

            cursor3 = database.rawQuery("SELECT word, translation, learned FROM " + DatabaseHelper.TABLE_WORDS + " WHERE word_id = ?",
                    new String[]{words_id.get(q)});
            cursor3.moveToFirst();
            while(!cursor3.isAfterLast())
            {
                this.words.add(cursor3.getString(0));
                this.translations.add(cursor3.getString(1));
                this.learned.add(cursor3.getString(2));
                cursor3.moveToNext();
            }
            cursor3.close();
        }
    }

    public ArrayList<String> getWords(){
        return (ArrayList<String>) this.words;
    }

    public ArrayList<String> getTranslations(){
        return (ArrayList<String>) this.translations;
    }

    public ArrayList<String[]> getContent(){
        List<String[]> res = new ArrayList<String[]>();
        for(int i = 0; i < this.words.size(); i++){
            res.add(new String[]{this.words.get(i), this.translations.get(i), this.learned.get(i)});
        }
        return (ArrayList<String[]>) res;
    }
}