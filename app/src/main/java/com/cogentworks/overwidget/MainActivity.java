package com.cogentworks.overwidget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "MainActivity";

    public SQLHelper dbHelper;
    ArrayAdapter<String> adapter;
    ListView listView;

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

        dbHelper = new SQLHelper(this);
        listView = findViewById(R.id.list_view);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int mPosition = position;
                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Profile")
                        .setMessage("Are you sure you want to delete?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dbHelper.deleteItem(listView.getItemAtPosition(mPosition).toString());
                                showItemList();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                dialog.show();

                return false;
            }
        });

        showItemList();
    }

    public void showItemList() {
        ArrayList<String> itemList = dbHelper.getList();
        if (adapter == null) {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);
            listView.setAdapter(adapter);
        } else {
            adapter.clear();
            adapter.addAll(itemList);
            adapter.notifyDataSetChanged();
        }
    }

    public void onFabClick(View view) {
        final Context context = this;
        final View dialogView = this.getLayoutInflater().inflate(R.layout.configure_dialog, null);
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

                        if (!dbHelper.getList().contains(battleTag)) {
                            Toast.makeText(getBaseContext(),"Loading player...",Toast.LENGTH_SHORT).show();
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
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public static final int REQUEST_EXIT = 2;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.settings) {
            startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_EXIT);
        }
        return super.onOptionsItemSelected(item);
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
