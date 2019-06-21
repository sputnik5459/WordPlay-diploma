package com.sputnik5459.diploma;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GameOne extends AppCompatActivity {

    WorkWithGames workWithGames = new WorkWithGames();
    TextView cur_word;
    Button show, yes, no;
    ProgressBar progressBar;
    Animation fadein;
    int words_length, true_words = 0;
    int cur_word_num = 0;
    SQLiteDatabase database;
    DatabaseHelper dbHelper;
    ArrayList<String[]> words;
    String results = "Вы отлично поработали!\nВы увеличили изучение слов:\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_one);

        fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
        fadein.setDuration((long)1000);

        cur_word = findViewById(R.id.word);
        show = findViewById(R.id.show);
        yes = findViewById(R.id.yes);
        no = findViewById(R.id.no);
        progressBar = findViewById(R.id.progressBar);

        dbHelper = new DatabaseHelper(this);

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

        String deck_name = getIntent().getExtras().get("deck_name").toString();

        workWithGames.initHelper(this);
        workWithGames.loadDeck(deck_name);

        words = workWithGames.getContent();

        words_length = words.size();
        if(words_length <= 0)
        {
            finish();
            return;
        }

        cur_word.setText(words.get(cur_word_num)[0]);

        progressBar.setMax(words_length);

        View.OnClickListener show_trans = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowTrans();
            }
        };
        View.OnClickListener yes_action = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Answer(0.5);
            }
        };

        View.OnClickListener no_action = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Answer(0);
            }
        };

        show.setOnClickListener(show_trans);
        yes.setOnClickListener(yes_action);
        no.setOnClickListener(no_action);
    }

    private void ShowTrans(){
        yes.setVisibility(View.VISIBLE);
        no.setVisibility(View.VISIBLE);
        show.setVisibility(View.GONE);
        cur_word.setText(words.get(cur_word_num)[1]);
    }

    private void Answer(double know){

        if(know != 0){
            true_words++;
            double res = know + Double.valueOf(words.get(cur_word_num)[2]);
            results += words.get(cur_word_num)[0]+" до "+String.valueOf(res)+"\n";
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.KEY_LEARNED, String.valueOf(res));
            database.update(DatabaseHelper.TABLE_WORDS, contentValues, "word = ?", new String[]{words.get(cur_word_num)[0]});
            contentValues.clear();
        }

        cur_word_num+=1;
        if (cur_word_num <= words_length-1)
        {
            progressBar.incrementProgressBy(1);
            cur_word.setText(words.get(cur_word_num)[0]);
            cur_word.startAnimation(fadein);
            yes.setVisibility(View.GONE);
            no.setVisibility(View.GONE);
            show.setVisibility(View.VISIBLE);
        }
        else
        {
            AlertDialog alertDialog = new AlertDialog.Builder(GameOne.this).create();
            alertDialog.setTitle("Результаты");
            alertDialog.setMessage(results);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            Date date = new Date();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            String cur_date = simpleDateFormat.format(date);

                            Cursor cursor = database.rawQuery("SELECT  words_count FROM "+ DatabaseHelper.TABLE_HISTORY+
                                            " WHERE hist_date = ?",
                                    new String[]{cur_date});

                            cursor.moveToFirst();

                            if(cursor.getCount()<=0){
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(DatabaseHelper.KEY_HIST_DATE, simpleDateFormat.format(date));
                                contentValues.put(DatabaseHelper.KEY_COUNT, true_words);
                                database.insert(DatabaseHelper.TABLE_HISTORY, null, contentValues);
                            }
                            else{
                                Integer true_words2 = Integer.valueOf(cursor.getString(0));
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(DatabaseHelper.KEY_COUNT, (true_words2+true_words));
                                database.update(DatabaseHelper.TABLE_HISTORY, contentValues,
                                        "hist_date = ?", new String[]{cur_date});
                            }
                            cursor.close();
                            finish();
                        }
                    });
            alertDialog.setCancelable(false);
            alertDialog.show();
        }
    }
}