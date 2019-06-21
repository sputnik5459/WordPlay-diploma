package com.sputnik5459.diploma;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class GameTwo extends AppCompatActivity {

    SQLiteDatabase database;
    DatabaseHelper dbHelper;
    ArrayList<String[]> words;
    ArrayList<String> all_translations;
    WorkWithGames workWithGames = new WorkWithGames();
    ProgressBar progressBar;
    int WORD, TRANSLATION, words_length, cur_word_num, difficult;
    LinearLayout lay;
    String results = "Вы отлично поработали!\nВы увеличили изучение слов:\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_two);

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
        difficult = (int)getIntent().getExtras().get("difficult");
        int lang = (int)getIntent().getExtras().get("language");

        lay = findViewById(R.id.lay);
        progressBar = findViewById(R.id.progressBar);

        cur_word_num = 0;

        workWithGames.initHelper(this);
        workWithGames.loadDeck(deck_name);
        words = workWithGames.getContent();

        if(lang == 1)
        {
            WORD = 0;
            TRANSLATION = 1;
            all_translations = workWithGames.getTranslations();
        }
        else
        {
            WORD = 1;
            TRANSLATION = 0;
            all_translations = workWithGames.getWords();
        }

        words_length = words.size();

        progressBar.setMax(words_length);

        Collections.shuffle(words);

        makeQuestion();
    }

    private void makeQuestion(){

        if(cur_word_num >= words.size()){
            AlertDialog alertDialog = new AlertDialog.Builder(GameTwo.this).create();
            alertDialog.setTitle("Результаты");
            alertDialog.setMessage(results);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            alertDialog.setCancelable(false);
            alertDialog.show();
            return;
        }

        Collections.shuffle(all_translations);

        lay.removeAllViews();

        //ТЕКСТ ВЬЮ СО СЛОВОМ, КОТОРОЕ НАДО ПЕРЕВЕСТИ
        TextView word = new TextView(this);
        word.setText(words.get(cur_word_num)[WORD]);
        word.setTextSize(25);
        word.setGravity(Gravity.CENTER);
        lay.addView(word);

        //СПИСОК ИЗ ТЕКСТ ВЬЮ С ВАРИАНТАМИ ОТВЕТА
        final List<Button> tasks = new ArrayList<>();

        //ЗАПОЛНЯЕМ СПИСОК С ВАРИАНТАМИ ОТВЕТА
        for(int i = 0; i < all_translations.size(); i++)
        {
            try {
                tasks.add(new Button(this));
                tasks.get(i).setText(all_translations.get(i));
                tasks.get(i).setGravity(Gravity.CENTER);
                final int finalI = i;
                tasks.get(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(tasks.get(finalI).getText() == words.get(cur_word_num)[TRANSLATION])
                        {
                            double res = 0.5 + Double.valueOf(words.get(cur_word_num)[2]);
                            results += words.get(cur_word_num)[0]+" до "+String.valueOf(res)+"\n";
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(DatabaseHelper.KEY_LEARNED, String.valueOf(res));
                            database.update(DatabaseHelper.TABLE_WORDS, contentValues, "word = ?", new String[]{words.get(cur_word_num)[0]});
                            contentValues.clear();
                        }

                        cur_word_num++;
                        progressBar.incrementProgressBy(1);
                        makeQuestion();
                    }
                });
            } catch (Exception ignored){}
        }

        for(int q = 0; q < tasks.size(); q++)
        {
            lay.addView(tasks.get(q));
        }
    }
}
