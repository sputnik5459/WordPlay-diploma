package com.sputnik5459.diploma;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameThree extends AppCompatActivity {

    SQLiteDatabase database;
    DatabaseHelper dbHelper;
    int difficult, cur_word_num, words_length, cur_char = 0;
    ArrayList<String[]> words;
    WorkWithGames workWithGames = new WorkWithGames();
    LinearLayout lay;
    ProgressBar progressBar;
    String results = "Вы отлично поработали!\nВы увеличили изучение слов:\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_three);

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
        workWithGames.initHelper(this);
        workWithGames.loadDeck(deck_name);
        words = workWithGames.getContent();
        lay = findViewById(R.id.lay);
        progressBar = findViewById(R.id.progressBar);
        cur_word_num = 0;

        words_length = words.size();
        progressBar.setMax(words_length);
        Collections.shuffle(words);

        makeQuestion();
    }

    private void makeQuestion(){
        lay.removeAllViews();

        if(cur_word_num >= words.size()){
            AlertDialog alertDialog = new AlertDialog.Builder(GameThree.this).create();
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

        //ТЕКСТ ВЬЮ СО СЛОВОМ, КОТОРОЕ НАДО ПЕРЕВЕСТИ
        final TextView word = new TextView(this);
        ArrayList<char[]> emptyword_with_chars = makeWord(words.get(cur_word_num)[0]);
        word.setText(String.valueOf(emptyword_with_chars.get(0)) + " - " + words.get(cur_word_num)[1]);
        word.setTextSize(25);
        word.setGravity(Gravity.CENTER);
        lay.addView(word);

        //СПИСОК ИЗ ТЕКСТ ВЬЮ С ВАРИАНТАМИ ОТВЕТА
        final List<Button> tasks = new ArrayList<>();

        for(int i = 0; i < emptyword_with_chars.get(1).length; i++)
        {
            try {
                tasks.add(new Button(this));
                tasks.get(i).setText(String.valueOf(emptyword_with_chars.get(1)[i]));
                tasks.get(i).setGravity(Gravity.CENTER);
                final int finalI = i;
                tasks.get(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tasks.get(finalI).setEnabled(false);
                        if(cur_char >= 2){
                            String mega_word = word.getText().toString().replaceFirst("_", tasks.get(finalI).getText().toString());
                            Log.d("///", mega_word);
                            if(words.get(cur_word_num)[0].equals(mega_word.replaceAll(" -.+", "")))
                            {
                                double res = 0.5 + Double.valueOf(words.get(cur_word_num)[2]);
                                results += words.get(cur_word_num)[0]+" до "+String.valueOf(res)+"\n";
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(DatabaseHelper.KEY_LEARNED, String.valueOf(res));
                                database.update(DatabaseHelper.TABLE_WORDS, contentValues, "word = ?", new String[]{words.get(cur_word_num)[0]});
                                contentValues.clear();
                            }
                            else
                            {
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        "Правильный ответ: "+words.get(cur_word_num)[0], Toast.LENGTH_LONG);
                                toast.show();
                            }
                            cur_char = 0;
                            cur_word_num++;
                            makeQuestion();
                            progressBar.incrementProgressBy(1);
                        }
                        else{
                            String mega_word = word.getText().toString().replaceFirst("_", tasks.get(finalI).getText().toString());
                            word.setText(mega_word);
                            cur_char++;
                        }
                    }
                });
            } catch (Exception ignored){}
        }

        for(int q = 0; q < tasks.size(); q++)
        {
            lay.addView(tasks.get(q));
        }
    }

    private ArrayList<char[]> makeWord(String emptyword){
        final Random random = new Random();
        char[] word = emptyword.toCharArray();
        char[] chars = new char[3];
        char[] positions = new char[3];
        for(int i = 0; i < 3; i++)
        {
            int rand = random.nextInt(word.length - 1);
            if(word[rand] != '_')
            {
                chars[i] = word[rand];
                word[rand] = '_';
                positions[i] = (char) rand;
            }
            else
            {
                i--;
            }
        }
        ArrayList<char[]> res = new ArrayList<>();
        res.add(word);
        res.add(chars);
        res.add(positions);
        return res;
    }
}
