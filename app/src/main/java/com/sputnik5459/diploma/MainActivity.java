package com.sputnik5459.diploma;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.Toast;

import com.folioreader.FolioReader;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private LinearLayout lay_games;
    private RelativeLayout lay_decks, lay_dict, lay_lib;
    private ListView lv_decks, tab_lv1, tab_lv2, tab_lv3, dict_lv, books_grid;
    private Button decks_add_btn, decks_delete_btn, tab1_btn, tab2_btn, tab3_btn;
    private Button dict_add_btn, dict_delete_btn, help1_btn, help2_btn, help3_btn;
    private TabHost tabHost;
    private RadioButton difM2, difH2, difM3, difH3, russian, english;
    private EditText dict_edit_add, decks_edit_add;
    private ImageView lightbulb, help_lib, plus_lib;
    private ArrayAdapter<String> decks_adapter;
    AlertDialog.Builder mBuilder;
    AlertDialog.Builder deck_info;
    ArrayList<Integer> mUserItems;

    DatabaseHelper dbHelper;
    SQLiteDatabase database;

    ArrayAdapter<String> books_list, for_games_list;
    ArrayList<String> books_names, books_path;
    AssetManager assetManager;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_lib:
                    lay_lib.setVisibility(View.VISIBLE);
                    lay_decks.setVisibility(View.GONE);
                    lay_games.setVisibility(View.GONE);
                    lay_dict.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_games:
                    refreshGamesLists();
                    lay_games.setVisibility(View.VISIBLE);
                    lay_decks.setVisibility(View.GONE);
                    lay_lib.setVisibility(View.GONE);
                    lay_dict.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_decks:
                    lay_decks.setVisibility(View.VISIBLE);
                    lay_games.setVisibility(View.GONE);
                    lay_lib.setVisibility(View.GONE);
                    lay_dict.setVisibility(View.GONE);
                    return true;
                case R.id.navigation_dict:
                    refreshDictList();
                    lay_dict.setVisibility(View.VISIBLE);
                    lay_decks.setVisibility(View.GONE);
                    lay_games.setVisibility(View.GONE);
                    lay_lib.setVisibility(View.GONE);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initMenu();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(0);

        books_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = "file:///android_asset/books/" + books_names.get(position);
                if(path.contains(".epub"))
                {
                    FolioReader folioReader = FolioReader.get();
                    folioReader.openBook(path);
                }
                else
                {
                    Intent intent = new Intent(MainActivity.this, Reader.class);
                    intent.putExtra("path", path);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                break;

            case R.id.history:
                intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadLib(){
        refreshLibList();

        help_lib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Помощь");
                alertDialog.setMessage("Чтобы добавить книгу в библиотеку, нажмите на иконку плюса справа\n" +
                        "ВНИМАНИЕ!\n" +
                        "На данный момент наше приложение поддерживает только расширения: pdf, txt, epub\n" +
                        "Интерфейс чтения книг с расширением epub отличается от интерфейса pdf и txt файлов");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });
        plus_lib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                myFileIntent.setType("*/*");
                startActivityForResult(myFileIntent, 10);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case 10:
                if(resultCode==RESULT_OK){
                    String path = data.getData().getPath();
                    String name = path.replaceAll(".+/", "");
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DatabaseHelper.KEY_NAME, name);
                    contentValues.put(DatabaseHelper.KEY_PATH, path);
                    database.insert(DatabaseHelper.TABLE_BOOKS, null, contentValues);
                    contentValues.clear();
                }
                break;
        }
        refreshLibList();
    }

    private String[] loadDict(){
        List<String> res = new ArrayList<>();
        String res1 = "";

        Cursor cursor = database.rawQuery("SELECT * FROM "+ DatabaseHelper.TABLE_WORDS +" WHERE saved = 1", null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()){
            res1 = cursor.getString(1) + " - " + cursor.getString(2);
            res.add(res1);
            cursor.moveToNext();
        }

        cursor.close();

        return res.toArray(new String[0]);
    }

    private void refreshLibList(){
        books_names = new ArrayList<>();
        books_path = new ArrayList<>();

        Cursor cursor = database.rawQuery("SELECT * FROM "+ DatabaseHelper.TABLE_BOOKS, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()){
            books_names.add(cursor.getString(1));
            books_path.add(cursor.getString(2));
            cursor.moveToNext();
        }

        cursor.close();

        String[] files = null;
        try {
            files = getAssets().list("books");
        } catch (IOException e) {
            e.printStackTrace();
        }

        books_names.addAll(Arrays.asList(files));

        books_list = new ArrayAdapter<String>(getApplicationContext(), R.layout.grid_item, R.id.tvText, books_names);
        books_grid.setAdapter(books_list);
    }

    private void initTabHost(){
        tabHost = (TabHost)findViewById(R.id.tabHost);
        tabHost.setup();

        TabHost.TabSpec tabSpec;

        tabSpec = tabHost.newTabSpec("tag1");
        tabSpec.setIndicator("Запомнить");
        tabSpec.setContent(R.id.tab1);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setIndicator("Перевод");
        tabSpec.setContent(R.id.tab2);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag3");
        tabSpec.setIndicator("Вставка букв");
        tabSpec.setContent(R.id.tab3);
        tabHost.addTab(tabSpec);
    }

    private void refreshGamesLists(){
        List<String> res = new ArrayList<>();
        Double prog;
        Cursor cursor = database.rawQuery("SELECT * FROM "+ DatabaseHelper.TABLE_DECKS, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()){
            prog = 100 * Double.valueOf(cursor.getString(2));
            res.add(cursor.getString(1));
            cursor.moveToNext();
        }

        cursor.close();
        for_games_list = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, res);
        tab_lv1.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        tab_lv2.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        tab_lv3.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        tab_lv1.setAdapter(for_games_list);
        tab_lv2.setAdapter(for_games_list);
        tab_lv3.setAdapter(for_games_list);
    }

    private void refreshDictList(){
        List<String> res = new ArrayList<>();
        String res1 = "";

        Cursor cursor = database.rawQuery("SELECT * FROM "+ DatabaseHelper.TABLE_WORDS +" WHERE saved = 1", null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()){
            //String next = "<font color='#EE0000'>red</font>";
            //t.setText(Html.fromHtml(first + next));
            String status = null;
            Double prog = Double.valueOf(cursor.getString(4));
            if(prog < 1) status = "Не выучено";
            else if(prog >=1 && prog<3) status = "В процессе";
            else if(prog >= 3) status = "Выучено";
            res1 = cursor.getString(1) + " - " + cursor.getString(2) +
                                   " (" + status + ")";
            res.add(res1);
            cursor.moveToNext();
        }

        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, res);

        dict_lv.setAdapter(adapter);
    }

    private void refreshDecksList(){
        List<String> res = new ArrayList<>();
        Double prog;
        Cursor cursor = database.rawQuery("SELECT * FROM "+ DatabaseHelper.TABLE_DECKS, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()){
            prog = 100 * Double.valueOf(cursor.getString(2));
            res.add(cursor.getString(1));
            cursor.moveToNext();
        }

        cursor.close();
        decks_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, res);
        lv_decks.setAdapter(decks_adapter);
    }

    private void initMenu(){

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

        //АКТИВИТИ
        lay_lib = (RelativeLayout) findViewById(R.id.lay_lib);
        lay_games = (LinearLayout)findViewById(R.id.lay_games);
        lay_decks = (RelativeLayout)findViewById(R.id.lay_decks);
        lay_dict = (RelativeLayout)findViewById(R.id.lay_dict);

        //БИБЛИОТЕКА
            books_grid = (ListView)findViewById(R.id.lib_grid);
            help_lib = (ImageView)findViewById(R.id.help_lib);
            plus_lib = (ImageView)findViewById(R.id.plus_lib);

        //НАБОРЫ
            decks_add_btn = (Button)findViewById(R.id.decks_add_btn);
            decks_delete_btn = (Button)findViewById(R.id.decks_delete_btn);
            lv_decks = (ListView)findViewById(R.id.decks_lv);
            decks_edit_add = findViewById(R.id.decks_edit_add);

        //ИГРЫ
            tab1_btn = (Button)findViewById(R.id.tab1_start_btn);
            tab2_btn = (Button)findViewById(R.id.tab2_start_btn);
            tab3_btn = (Button)findViewById(R.id.tab3_start_btn);
            help1_btn = (Button)findViewById(R.id.tab1_help_btn);
            help2_btn = (Button)findViewById(R.id.tab2_help_btn);
            help3_btn = (Button)findViewById(R.id.tab3_help_btn);
            tab_lv1 = (ListView)findViewById(R.id.tab1_lv);
            tab_lv2 = (ListView)findViewById(R.id.tab2_lv);
            tab_lv3 = (ListView)findViewById(R.id.tab3_lv);

        //СЛОВАРЬ
            dict_add_btn = findViewById(R.id.dict_add_btn);
            dict_delete_btn = findViewById(R.id.dict_delete_btn);
            dict_lv = (ListView)findViewById(R.id.dict_lv);
            difM2 = (RadioButton)findViewById(R.id.eazy2);
            difH2 = (RadioButton)findViewById(R.id.hard2);
            difM3 = (RadioButton)findViewById(R.id.eazy3);
            difH3 = (RadioButton)findViewById(R.id.hard3);
            russian = (RadioButton)findViewById(R.id.russian);
            english = (RadioButton)findViewById(R.id.english);
            dict_edit_add = findViewById(R.id.dict_edit_add);
            lightbulb = findViewById(R.id.light_png);

        difM2.setChecked(true);
        difM3.setChecked(true);
        russian.setChecked(true);

        assetManager = getAssets();

        initTabHost();
        initDict();
        initDecks();
        initGames();
        loadLib();
        refreshDecksList();
        refreshGamesLists();
        refreshDictList();
    }

    private void initDict(){
        dict_add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String new_word = dict_edit_add.getText().toString().toLowerCase();
                if(!new_word.isEmpty())
                {
                    Cursor cursor = database.rawQuery("SELECT translation FROM " + DatabaseHelper.TABLE_WORDS +" WHERE word = ?",
                            new String[]{new_word});

                    if(cursor.getCount() > 0)
                    {
                        cursor.moveToFirst();
                        String translation = cursor.getString(0);
                        cursor.close();

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Мы его нашли!");
                        builder.setMessage("\""+new_word+"\" переводится как \"" + translation +
                                "\"\nСохранить это слово в словарь?");
                        builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(DatabaseHelper.KEY_SAVED, 1);
                                database.update(DatabaseHelper.TABLE_WORDS, contentValues, "word = ?", new String[]{new_word});
                                contentValues.clear();
                                refreshDictList();
                            }
                        });
                        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    else
                    {
                        final EditText taskEditText = new EditText(MainActivity.this);
                        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Мы не справились :(")
                                .setMessage("К сожалению, у нас нет перевода для слова \""+
                                        new_word+"\"\nХотите ввести свой перевод ниже и сохранить?")
                                .setView(taskEditText)
                                .setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String new_trans = taskEditText.getText().toString();
                                        if(!new_trans.isEmpty())
                                        {
                                            ContentValues contentValues = new ContentValues();
                                            contentValues.put(DatabaseHelper.KEY_WORD, dict_edit_add.getText().toString().toLowerCase());
                                            contentValues.put(DatabaseHelper.KEY_TRANSLATION, new_trans);
                                            contentValues.put(DatabaseHelper.KEY_SAVED, 1);
                                            database.insert(DatabaseHelper.TABLE_WORDS, null, contentValues);
                                            contentValues.clear();
                                            refreshDictList();
                                        }
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create();
                        dialog.show();
                    }
                }
                else
                {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Пожалуйста, введите слово", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        dict_delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String new_word = dict_edit_add.getText().toString().toLowerCase();
                if(!new_word.isEmpty())
                {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DatabaseHelper.KEY_SAVED, 0);
                    database.update(DatabaseHelper.TABLE_WORDS, contentValues, "word = ?", new String[]{new_word});
                    contentValues.clear();
                    refreshDictList();
                }
                else
                {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Пожалуйста, введите слово", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        lightbulb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Помощь");
                alertDialog.setMessage("Чтобы добавить слово в словарь, введите его в" +
                        "поле для ввода и нажмите на \"Найти перевод\". " +
                        "Если перевод не будет найден в нашей базе данных," +
                        "то вам предложится ввести его самостоятельно.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });
    }

    private void initDecks(){
        decks_add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String create_name = decks_edit_add.getText().toString();
                if(!create_name.equals("") && decks_adapter.getPosition(create_name) <= 0){
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DatabaseHelper.KEY_DECKNAME, create_name);
                    database.insert(DatabaseHelper.TABLE_DECKS, null, contentValues);
                    createDialogBuilder();
                    mBuilder.show();
                }
                else if (decks_adapter.getPosition(create_name) > 0)
                {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Колода с таким названием уже существует", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else
                {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Пожалуйста, введите название колоды", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        decks_delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String delete_name = decks_edit_add.getText().toString();
                if(!delete_name.isEmpty()) {
                    Cursor cursor1;
                    cursor1 = database.rawQuery("SELECT deck_id FROM " + DatabaseHelper.TABLE_DECKS +" WHERE deck_name = ?",
                                new String[]{delete_name});

                    cursor1.moveToFirst();
                    database.delete(DatabaseHelper.TABLE_DECKS,
                                "deck_name = ?",
                                new String[]{delete_name});
                    database.delete(DatabaseHelper.TABLE_DECKS_WORDS,
                                "deck_id = ?",
                                new String[]{cursor1.getString(0)});
                    cursor1.close();
                    refreshDecksList();
                }
                else{
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Пожалуйста, введите название колоды", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        lv_decks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor1, cursor2, cursor3;
                String deck_id;
                String res;
                ArrayList<String> words = new ArrayList<String>();
                cursor1 = database.rawQuery("SELECT deck_id FROM " + DatabaseHelper.TABLE_DECKS +
                        " WHERE deck_name = ?", new String[]{parent.getItemAtPosition(position).toString()});
                cursor1.moveToFirst();
                deck_id = cursor1.getString(0);
                cursor1.close();

                cursor2 = database.rawQuery("SELECT word_id FROM " +DatabaseHelper.TABLE_DECKS_WORDS +
                        " WHERE deck_id = ?", new String[]{deck_id});

                cursor2.moveToFirst();
                while (!cursor2.isAfterLast()){
                    cursor3 = database.rawQuery("SELECT word, translation FROM " + DatabaseHelper.TABLE_WORDS +
                            " WHERE word_id = ?", new String[]{cursor2.getString(0)});
                    cursor3.moveToFirst();
                    res = cursor3.getString(0) + " - " + cursor3.getString(1);
                    words.add(res);
                    cursor3.close();
                    cursor2.moveToNext();
                }
                cursor2.close();
                deck_info = new AlertDialog.Builder(MainActivity.this);
                deck_info.setTitle("Редактор колоды");
                deck_info.setItems(words.toArray(new String[0]), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("///", "heh");
                    }
                });
                deck_info.show();
            }
        });
    }

    private void initGames(){
        View.OnClickListener tab1_OK = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String deck;
                try
                {
                    deck = for_games_list.getItem(tab_lv1.getCheckedItemPosition());
                }
                catch (Exception e)
                {
                    deck = "";
                }
                if(deck.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Выберите набор для игры",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Intent intent = new Intent(MainActivity.this, GameOne.class);
                    intent.putExtra("deck_name", deck);
                    startActivity(intent);
                }
            }
        };

        View.OnClickListener tab2_OK = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String deck;
                try
                {
                    deck = for_games_list.getItem(tab_lv2.getCheckedItemPosition());
                }
                catch (Exception e)
                {
                    deck = "";
                }
                int dif = 1;
                int lang = 0;
                if(deck.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Выберите набор для игры",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if(difH2.isChecked())
                    {
                        dif = 2;
                    }
                    if(english.isChecked())
                    {
                        lang = 1;
                    }
                    Intent intent = new Intent(MainActivity.this, GameTwo.class);
                    intent.putExtra("deck_name", deck);
                    intent.putExtra("difficult", dif);
                    intent.putExtra("language", lang);
                    startActivity(intent);
                }
            }
        };

        View.OnClickListener tab3_OK = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String deck;
                try
                {
                    deck = for_games_list.getItem(tab_lv3.getCheckedItemPosition());
                }
                catch (Exception e)
                {
                    deck = "";
                }
                int dif = 1;
                if(deck.isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Выберите набор для игры",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if(difH3.isChecked())
                    {
                        dif = 2;
                    }
                    Intent intent = new Intent(MainActivity.this, GameThree.class);
                    intent.putExtra("deck_name", deck);
                    intent.putExtra("difficult", dif);
                    startActivity(intent);
                }
            }
        };

        View.OnClickListener help1 = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Игра на запоминание");
                alertDialog.setMessage("Вам последовательно предоставляются карточки со словами на иностранном языке. " +
                        "Вы должны перевести эти слова в голове, после чего перевернуть карточку и оценить свой перевод.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        };

        View.OnClickListener help2 = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Игра на перевод");
                alertDialog.setMessage("Вам последовательно предоставляются слова, либо на русском, либо на английском языке. " +
                        "Ваша задача состоит в том, чтобы выбрать для слов правильные переводы.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        };

        View.OnClickListener help3 = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Игра на вставку букв");
                alertDialog.setMessage("Вам последовательно предоставляются слова с случайными пропущенными буквами. " +
                        "Ваша задача состоит в том, чтобы вставить их в слово в нужном порядке.");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        };

        tab1_btn.setOnClickListener(tab1_OK);
        tab2_btn.setOnClickListener(tab2_OK);
        tab3_btn.setOnClickListener(tab3_OK);
        help1_btn.setOnClickListener(help1);
        help2_btn.setOnClickListener(help2);
        help3_btn.setOnClickListener(help3);
    }

    private void createDialogBuilder(){
        final String[] stringList = loadDict();
        boolean[] checkedItems = new boolean[stringList.length];
        mUserItems = new ArrayList<>();

        mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle("Добавить в колоду");
        mBuilder.setMultiChoiceItems(stringList, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if(isChecked)
                {
                    mUserItems.add(which);
                }
                else
                {
                    mUserItems.remove((Integer.valueOf(which)));
                }
            }
        });

        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String cur_word;
                String res1 = "";
                List<String> res2 = new ArrayList<>();
                Cursor cursor1, cursor2;
                ContentValues contentValues = new ContentValues();

                cursor1 = database.rawQuery("SELECT deck_id FROM " + DatabaseHelper.TABLE_DECKS +" WHERE deck_name = ?",
                        new String[]{decks_edit_add.getText().toString()});

                cursor1.moveToFirst();
                res1 = cursor1.getString(0);
                cursor1.close();

                for(int i = 0; i<mUserItems.size(); i++)
                {
                    cur_word = stringList[mUserItems.get(i)];
                    cur_word = cur_word.split(" ")[0];

                    cursor2 = database.rawQuery("SELECT word_id FROM "+ DatabaseHelper.TABLE_WORDS +" WHERE word = ?",
                            new String[]{cur_word});

                    cursor2.moveToFirst();
                    while (!cursor2.isAfterLast()){
                        res2.add(cursor2.getString(0));
                        cursor2.moveToNext();
                    }

                    cursor2.close();
                }

                for(int q = 0; q<res2.size(); q++)
                {
                    contentValues.put(DatabaseHelper.KEY_DECKID, res1);
                    contentValues.put(DatabaseHelper.KEY_WORDID, res2.get(q));
                    database.insert(DatabaseHelper.TABLE_DECKS_WORDS, null, contentValues);
                }

                refreshDecksList();
            }
        });
        refreshDecksList();
    }
}
