package com.example.gizmo.dbtest;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final int CM_DELETE_ID = 1;

    // there are two modes for tapbutton
    private static final int MODE_ADD = 0;
    private static final int MODE_EDIT = 1;
    int mode = MODE_ADD;

    // "global" used vars
    Button tap;
    SQLiteDatabase mDB;
    MyDBHelper DB;
    EditText note;
    TextView recordscount, displayid;
    ListView lv;
    SimpleCursorAdapter scAdapter;
    Cursor c;

    // constants for broadcast messages
    public static final String NOTE_ADDED = "com.example.gizmo.dbtest.NOTE_ADDED";
    //public static final String ALARM_MESSAGE = ;

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
        displayid= (TextView) findViewById(R.id.displayid);
        lv = (ListView) findViewById(R.id.listView);
        fields = new int[]  {R.id.listid, R.id.listtimestamp, R.id.listnote,};
        tap = (Button) findViewById(R.id.tapButton);

        DB = new MyDBHelper(this);
        mDB = DB.getWritableDatabase();

        // creating Cursor, we will reuse it later
        c = mDB.rawQuery("SELECT * FROM notes ORDER by _id DESC", null);

        // binding adaper with ListView
        scAdapter = new SimpleCursorAdapter(this, R.layout.item, c, headers, fields);
        lv.setAdapter(scAdapter);
        registerForContextMenu(lv);

        // binding click listener for items of listview
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            String notetext;
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                c.moveToPosition( position);
                notetext = c.getString(2);
                Log.d("DeBUG", "itemClick: position = " + position + ", id = "
                        + id + " value = " + notetext);
                displayid.setText(Long.toString(id));
                note.setText(notetext);
                mode = MODE_EDIT;
                tap.setText("EDIT NOTE");
            }
        });

    }

    public void tapClick(View v)
    {

        if (mode == MODE_ADD) {
            ContentValues cv = new ContentValues(1);
            cv.put("note", note.getText().toString());
            mDB.insert("notes", null, cv);
        }
        if (mode == MODE_EDIT) {
            int id = Integer.parseInt(displayid.getText().toString());
            mDB.execSQL("UPDATE notes SET note = '" + note.getText().toString() + "' WHERE _id = " + id);

            mode = MODE_ADD;
            displayid.setText("");
            note.setText("");
            tap.setText("ADD NOTE");
        }
        c.requery(); // refresh listview
        recordscount.setText("Records in table: " + Integer.toString(c.getCount()));

        // sending broadcast message
        Intent intent = new Intent();
        intent.setAction(NOTE_ADDED);
        intent.putExtra("com.example.gizmo.dbtest.Message", note.getText().toString());
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(intent);
    }

    public void clearClick(View v)
    {
        String[] where = {};
        mDB.delete("notes", "", where);
        c.requery(); // refresh listview

        mode = MODE_ADD;
        displayid.setText("");
        note.setText("");
        tap.setText("ADD NOTE");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_DELETE_ID, 0, "Delete");
    }

    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == CM_DELETE_ID) {

            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            mDB.delete("notes", "_id = " + acmi.id, null);
            c.requery(); // refresh listview

            return true;
        }
        return super.onContextItemSelected(item);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


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


        public MyDBHelper(Context ctx) {
            super(ctx, DB_NAME, null, DB_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE_TABLE);

            //ContentValues cv = new ContentValues(1);
        }
        public void reCreate(SQLiteDatabase db)
        {
            // doesn't work. leave it here for future
            db.execSQL("DROP DATABASE "+DB_TABLE);
            db.execSQL(DB_CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}



