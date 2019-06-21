package com.sputnik5459.diploma;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import java.io.IOException;
import java.io.InputStream;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class Reader extends AppCompatActivity {

    DatabaseHelper dbHelper;
    SQLiteDatabase database;
    TextView txtViewer;
    EditText search_edit;
    Button search_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        txtViewer = findViewById(R.id.txt_viewer);
        search_edit = findViewById(R.id.search_edit);
        search_btn = findViewById(R.id.search_btn);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_word = search_edit.getText().toString();
                if(!new_word.isEmpty()){
                    findWord(new_word);
                }
                else{
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Пожалуйста, введите слово", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        String path = getIntent().getExtras().get("path").toString();
        if(path.contains(".pdf"))
        {
            loadPDF(path.replace("file:///android_asset/", ""));
        }
        else if(path.contains(".txt"))
        {
            loadTXT(path.replace("file:///android_asset/", ""));
        }
    }

    private void loadPDF(String path){
        PDFView pdfView = findViewById(R.id.pdfView);
        pdfView.fromAsset(path)
                .enableSwipe(true) // allows to block changing pages using swipe
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                .password(null)
                .scrollHandle(null)
                .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                // spacing between pages in dp. To define spacing color, set view background
                .spacing(0)
                .pageFitPolicy(FitPolicy.WIDTH)
                .load();
    }

    private void loadTXT(String path){

        String text = "";
        try {
            InputStream is = getAssets().open(path);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            text = new String(buffer);

        } catch (IOException e) {
            e.printStackTrace();
        }

        txtViewer.setVisibility(View.VISIBLE);
        txtViewer.setTextIsSelectable(true);
        txtViewer.setText(text);
    }

    private void findWord(final String new_word) {

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

        Cursor cursor = database.rawQuery("SELECT translation FROM " + DatabaseHelper.TABLE_WORDS +" WHERE word = ?",
                new String[]{new_word});
        if(cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            String translation = cursor.getString(0);
            cursor.close();

            AlertDialog.Builder builder = new AlertDialog.Builder(Reader.this);
            builder.setTitle("Мы его нашли!");
            builder.setMessage("\""+new_word+"\" переводится как \"" + translation +
                    "\"\nСохранить это слово в словарь?");
            builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DatabaseHelper.KEY_SAVED, 1);
                    database.update(DatabaseHelper.TABLE_WORDS, contentValues, "word = ?", new String[]{new_word});
                    contentValues.clear();
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
            final EditText taskEditText = new EditText(Reader.this);
            AlertDialog dialog = new AlertDialog.Builder(Reader.this)
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
                                contentValues.put(DatabaseHelper.KEY_WORD, new_word.toLowerCase());
                                contentValues.put(DatabaseHelper.KEY_TRANSLATION, new_trans);
                                contentValues.put(DatabaseHelper.KEY_SAVED, 1);
                                database.insert(DatabaseHelper.TABLE_WORDS, null, contentValues);
                                contentValues.clear();
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
}
