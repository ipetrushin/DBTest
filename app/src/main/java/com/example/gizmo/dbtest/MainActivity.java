package com.example.gizmo.dbtest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    //Button tap;
    SQLiteDatabase mDB;
    MyDBHelper DB;
    EditText note;
    TextView recordscount;
    ListView lv;
    SimpleCursorAdapter scAdapter;
    Cursor c;
    // arrays for binding DB and ListView
    String[] headers = {"_id", "timestamp", "note"};
    int[] fields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // binding controls
        note = (EditText) findViewById(R.id.noteText);
        recordscount = (TextView) findViewById(R.id.recordscount);
        lv = (ListView) findViewById(R.id.listView);
        fields = new int[]  {R.id.listid, R.id.listtimestamp, R.id.listnote,};

        DB = new MyDBHelper(this);
        mDB = DB.getWritableDatabase();

        // creating Cursor, we will reuse it later
        c = mDB.rawQuery("SELECT * FROM notes ORDER by _id DESC", null);

        // binding adaper with ListView
        scAdapter = new SimpleCursorAdapter(this, R.layout.item, c, headers, fields);
        lv.setAdapter(scAdapter);

    }

    public void tapClick(View v)
    {
        ContentValues cv = new ContentValues(1);
        cv.put("note",note.getText().toString());
        mDB.insert("notes", null, cv);

        c.requery();
        recordscount.setText("Records in table: " + Integer.toString(c.getCount()));
    }

    public void clearClick(View v)
    {
        String[] where = {};
        mDB.delete("notes", "", where);
        c.requery();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class MyDBHelper extends SQLiteOpenHelper {
        static final String DB_TABLE = "notes";
        static final String COLUMN_NOTE = "note";
        static final String COLUMN_TIMESTAMP = "timestamp";
        private static final String DB_NAME = "notes.db";
        private static final int DB_VERSION = 1;
        private static final String DB_CREATE_TABLE = "CREATE TABLE " + DB_TABLE +
                " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + COLUMN_NOTE + " TEXT NOT NULL);";
        private SQLiteDatabase mydb;

        public MyDBHelper(Context ctx) {
            super(ctx, DB_NAME, null, DB_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE_TABLE);
            mydb = db;
            ContentValues cv = new ContentValues(1);
        }
        public void reCreate(SQLiteDatabase db)
        {
            db.execSQL("DROP DATABASE "+DB_TABLE);
            db.execSQL(DB_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}



