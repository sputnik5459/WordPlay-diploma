package com.sputnik5459.diploma;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    GraphView graph;
    ListView history_list;
    SQLiteDatabase database;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

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

        graph = (GraphView) findViewById(R.id.graph);
        history_list = findViewById(R.id.history_list);
        Button clear_btn = new Button(HistoryActivity.this);
        clear_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                builder.setMessage("Вы уверены, что хотите очистить всю историю изучения?");
                builder.setPositiveButton("Очистить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        database.delete(DatabaseHelper.TABLE_HISTORY, null, null);
                        refreshHistoryList();
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
        });
        clear_btn.setText("Очистить");
        history_list.addFooterView(clear_btn);
        initGraph();
        refreshHistoryList();
    }

    private void initGraph(){

        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(new DataPoint[] {
                new DataPoint(1, 0),
                new DataPoint(2, 0),
                new DataPoint(3, 4),
                new DataPoint(4, 0),
                new DataPoint(5, 10),
                new DataPoint(6, 5),
                new DataPoint(7, 0)
        });

        graph.setTitle("Прогресс по словам (c пн. по вскр.)");
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(1);
        graph.getViewport().setMaxX(7);
        series.setSpacing(15);
        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        graph.addSeries(series);

        series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
            @Override
            public int get(DataPoint data) {
                return Color.rgb((int) data.getX()*255/4, (int) Math.abs(data.getY()*255/6), 100);
            }
        });
        series.setDrawValuesOnTop(true);
        series.setValuesOnTopColor(Color.BLACK);
    }

    private void refreshHistoryList(){
        List<String> res = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM "+ DatabaseHelper.TABLE_HISTORY, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()){
            res.add("Дата: "+cursor.getString(1)+", Количество слов: "+cursor.getString(2));
            cursor.moveToNext();
        }

        cursor.close();
        ArrayAdapter<String> history = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, res);
        history_list.setAdapter(history);
    }
}
