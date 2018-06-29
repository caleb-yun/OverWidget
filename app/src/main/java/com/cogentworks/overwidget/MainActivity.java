package com.cogentworks.overwidget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "MainActivity";

    public SQLHelper dbHelper;
    ProfileAdapter adapter;
    ListView listView;

    boolean isBusy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean useDarkTheme = PreferenceManager.getDefaultSharedPreferences(getBaseContext())
                .getBoolean(SettingsActivity.PREF_DARK_THEME, false);
        if (useDarkTheme)
            setTheme(R.style.Blackwatch);

        setContentView(R.layout.activity_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);


        // Set swipe to refresh behavior
        final MainActivity activityContext = this;
        SwipeRefreshLayout mSwipeRefreshLayout = findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i(TAG, "onRefresh called from SwipeRefreshLayout");

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        UpdateListTask updateListTask = new UpdateListTask(activityContext, dbHelper.getList());
                        updateListTask.execute();
                    }
                }
        );

        dbHelper = new SQLHelper(this);
        listView = findViewById(R.id.list);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (!isBusy) {
                    final int mPosition = position;
                    AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Delete Profile")
                            .setMessage("Are you sure you want to delete?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    isBusy = true;
                                    Gson gson = new Gson();
                                    dbHelper.deleteItem(gson.toJson(listView.getItemAtPosition(mPosition)));
                                    showItemList();
                                    isBusy = false;
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .create();
                    dialog.show();
                } else {
                    Toast.makeText(MainActivity.this,"List is busy", Toast.LENGTH_LONG).show();
                }

                return false;
            }
        });

        showItemList();
    }

    public void showItemList() {
        isBusy = true;

        if (adapter == null) {
            ((SwipeRefreshLayout)findViewById(R.id.swiperefresh)).setRefreshing(true);
            adapter = new ProfileAdapter(this, dbHelper.getList());
            listView.setAdapter(adapter);

            UpdateListTask updateListTask = new UpdateListTask(this, dbHelper.getList());
            updateListTask.execute();

        } else {
            adapter.clear();
            adapter.addAll(dbHelper.getList());
            adapter.notifyDataSetChanged();
        }

        isBusy = false;
    }

    public void onFabClick(View view) {
        final Context context = this;
        final View dialogView = this.getLayoutInflater().inflate(R.layout.configure_dialog, null);

        if (!isBusy) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Add a new player")
                    .setView(dialogView)
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText editText = dialogView.findViewById(R.id.config_battletag);
                            String battleTag = String.valueOf(editText.getText());
                            Spinner platformSpinner = dialogView.findViewById(R.id.platform_spinner);
                            String platform = platformSpinner.getSelectedItem().toString();
                            Spinner regionSpinner = dialogView.findViewById(R.id.region_spinner);
                            String region = regionSpinner.getSelectedItem().toString();

                            if (!dbHelper.getList("BattleTag").contains(battleTag)) {
                                Toast.makeText(getBaseContext(), "Adding player...", Toast.LENGTH_SHORT).show();
                                AddProfileTask addTask = new AddProfileTask(context, battleTag, platform, region);
                                addTask.execute();
                            } else {
                                Snackbar.make(findViewById(R.id.layout_main), "Player already added to list", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
            dialog.show();

            Spinner regionSpinner = dialogView.findViewById(R.id.region_spinner);
            ArrayAdapter<CharSequence> regionAdapter = ArrayAdapter.createFromResource(this, R.array.region_array, android.R.layout.simple_spinner_item);
            regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            regionSpinner.setAdapter(regionAdapter);

            Spinner platformSpinner = dialogView.findViewById(R.id.platform_spinner);
            ArrayAdapter<CharSequence> platformAdapter = ArrayAdapter.createFromResource(this, R.array.platform_array, android.R.layout.simple_spinner_item);
            platformAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            platformSpinner.setAdapter(platformAdapter);
        } else {
            Toast.makeText(this,"List is busy", Toast.LENGTH_LONG).show();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public static final int REQUEST_EXIT = 2;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem checkable = menu.findItem(R.id.dark_check);
        boolean isChecked = PreferenceManager.getDefaultSharedPreferences(getBaseContext())
                .getBoolean(SettingsActivity.PREF_DARK_THEME, false);
        checkable.setChecked(isChecked);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.dark_check:
                boolean isChecked = !item.isChecked();
                item.setChecked(isChecked);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(SettingsActivity.PREF_DARK_THEME, isChecked);
                editor.commit();

                Intent intent = getIntent();
                finish();
                startActivity(intent);

                return true;
            case R.id.settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_EXIT);
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_EXIT) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, MainActivity.class);
                this.finish();
                startActivity(intent);
            }
        }
    }



}
