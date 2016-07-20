package net.mononz.lazytag;

import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import net.mononz.lazytag.database.Database;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @Bind(R.id.collapsing_toolbar) protected CollapsingToolbarLayout collapsingToolbarLayout;
    @Bind(R.id.coordinatorLayout) protected CoordinatorLayout coordinatorLayout;
    @Bind(R.id.toolbar) protected Toolbar toolbar;
    @Bind(R.id.layout) protected LinearLayout layout;
    @Bind(R.id.label) protected EditText label;
    @Bind(R.id.message) protected EditText message;
    @Bind(R.id.recycler) protected RecyclerView recycler;

    private static final int CURSOR_LOADER_ID = 1;
    private AdapterMessages mAdapterMessages;

    private static SharedPreferences pref;
    private static final String PREF_NAME = "Preferences";
    private static final String KEY_COPY = "key_copy";

    public static Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityTransitions();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        database = new Database(this);
        pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        supportPostponeEnterTransition();
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        mAdapterMessages = new AdapterMessages(null, new AdapterMessages.Callback() {
            @Override
            public void onSnack(String message) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(MainActivity.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.app_name), MainActivity.readCopy());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "Copied message to clipboard", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onDelete(long _id, String message) {
                snackbarDelete(_id, message);
            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(llm);
        recycler.setAdapter(mAdapterMessages);
    }

    private void resetViews() {
        snackbar(message.getText().toString(), "Saved!");
        // reset views
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(coordinatorLayout.getWindowToken(), 0);
        coordinatorLayout.requestFocus();
        label.setText(null);
        message.setText(null);
    }

    @OnClick(R.id.fab)
    public void fab() {
        if (message.getText() != null && message.getText().toString().length() > 0) {
            ContentValues values = new ContentValues();
            values.put(Database.Messages.label, label.getText().toString());
            values.put(Database.Messages.message, message.getText().toString());
            values.put(Database.Messages.created_at, System.currentTimeMillis() / 1000);
            Uri uri = getContentResolver().insert(Database.Messages.CONTENT_URI, values);
            resetViews();
            LazyTag.sendAction(getString(R.string.analytics_add));
        } else {
            Toast.makeText(this, "Enter Valid Text", Toast.LENGTH_SHORT).show();
        }
    }

    public void snackbar(String message, String action) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
                .setAction(action, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) { }
                })
                .show();
    }

    public void snackbarDelete(final long _id, String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG)
                .setAction("Remove", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ContentValues values = new ContentValues();
                        values.put(Database.Messages.deleted_at, System.currentTimeMillis() / 1000);
                        getContentResolver().update(Database.Messages.CONTENT_URI, values, Database.Messages._id + "=?", new String[]{Long.toString(_id)});
                        LazyTag.sendAction(getString(R.string.analytics_remove));
                    }
                })
                .show();
    }

    public static String readCopy() {
        return pref.getString(KEY_COPY, "HashTagger: Add some text");
    }

    public static void saveCopy(String value) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_COPY, value);
        editor.apply();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        try {
            return super.dispatchTouchEvent(motionEvent);
        } catch (NullPointerException e) {
            return false;
        }
    }

    private void initActivityTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide transition = new Slide();
            transition.excludeTarget(android.R.id.statusBarBackground, true);
            getWindow().setEnterTransition(transition);
            getWindow().setReturnTransition(transition);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getSupportLoaderManager().initLoader(CURSOR_LOADER_ID, null, MainActivity.this);
        LazyTag.sendScreen(getString(R.string.analytics_main));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d("Loader", "Init");
        return new CursorLoader(this, Database.Messages.CONTENT_URI, null, Database.Messages.deleted_at + " IS NULL", null, Database.Messages.label + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d("Loader", "Finished");
        mAdapterMessages.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d("Loader", "Reset");
        mAdapterMessages.swapCursor(null);
    }

}