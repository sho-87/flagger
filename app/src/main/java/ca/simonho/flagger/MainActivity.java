package ca.simonho.flagger;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    PackageInfo info;
    String[] permissions;
    Boolean hasAllPerms = true;
    DBHelper dbHelper;
    LayoutInflater inflater;
    View mView;
    AlertDialog.Builder builder;
    Long timeOffset;
    EditText idInput;
    TextView idValue;
    Short subID;
    Button buttonNew;
    Button buttonDelete;
    Button buttonSave;
    Button buttonStart;
    Button buttonStop;
    Button button1;
    Button button2;
    Button button3;
    Button button4;
    Button button5;
    Button button6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check permissions
        try {
            info = getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            permissions = info.requestedPermissions;
            for (String p : permissions) {
                if (!hasPermission(p)) {
                    hasAllPerms = false;
                    break;
                }
            }

            if (!hasAllPerms) {
                ActivityCompat.requestPermissions(this, permissions, 10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get NTP time and calculate offset from system time (ms)
        SNTPClient.getDate(Calendar.getInstance().getTimeZone(), new SNTPClient.Listener() {
            @Override
            public void onTimeReceived(long rawDate) {
                timeOffset = System.currentTimeMillis() - rawDate;
                Log.d(TAG, "System: " + System.currentTimeMillis());
                Log.d(TAG, "NTP: " + rawDate);
                Log.d(TAG, "NTP offset: " + timeOffset);
            }

            @Override
            public void onError(Exception ex) {
                Log.d(SNTPClient.TAG, ex.getMessage());
            }
        });

        // Create dbHelper
        dbHelper = DBHelper.getInstance(this);

        inflater = MainActivity.this.getLayoutInflater();
        builder = new AlertDialog.Builder(this);

        // Button listeners
        buttonNew = findViewById(R.id.button_new);
        buttonNew.setOnClickListener(this);

        buttonSave = findViewById(R.id.button_save);
        buttonSave.setOnClickListener(this);

        buttonDelete = findViewById(R.id.button_delete);
        buttonDelete.setOnClickListener(this);

        buttonStart = findViewById(R.id.button_start);
        buttonStart.setOnClickListener(this);

        buttonStop = findViewById(R.id.button_stop);
        buttonStop.setOnClickListener(this);

        button1 = findViewById(R.id.button_1);
        button1.setOnClickListener(this);

        button2 = findViewById(R.id.button_2);
        button2.setOnClickListener(this);

        button3 = findViewById(R.id.button_3);
        button3.setOnClickListener(this);

        button4 = findViewById(R.id.button_4);
        button4.setOnClickListener(this);

        button5 = findViewById(R.id.button_5);
        button5.setOnClickListener(this);

        button6 = findViewById(R.id.button_6);
        button6.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.closeDB();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_new: {
                mView = inflater.inflate(R.layout.dialog_new, null);
                idValue = findViewById(R.id.id_value);
                idInput = mView.findViewById(R.id.id_input);
                idInput.requestFocus();

                builder.setView(mView)
                        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                showKeyboard(false, MainActivity.this);
                                String curID = idInput.getText().toString();
                                if (!dbHelper.hasID(Short.parseShort(curID))) {
                                    idValue.setText(curID);
                                    subID = Short.parseShort(curID);
                                    activateButtons(true);
                                } else {
                                    final Snackbar sb = Snackbar.make(findViewById(android.R.id.content), "ID already exists", Snackbar.LENGTH_SHORT);

                                    sb.setAction("Dismiss", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            sb.dismiss();
                                        }
                                    }).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                showKeyboard(false, MainActivity.this);
                                dialog.cancel();
                            }
                        });
                // Creating dialog box
                AlertDialog alert = builder.create();
                // Setting the title manually
                alert.setTitle("Enter new ID:");
                alert.show();
                showKeyboard(true, this);
                break;
            }
            case R.id.button_save: {
                new ExportDatabaseCSVTask().execute();
                break;
            }
            case R.id.button_delete: {
                dbHelper.deleteTempData();
                idValue.setText("");
                activateButtons(false);
                break;
            }
            case R.id.button_start: {
                dbHelper.insertTempData(subID, "start", calcTime());
                break;
            }
            case R.id.button_stop: {
                dbHelper.insertTempData(subID, "stop", calcTime());
                break;
            }
            case R.id.button_1: {
                dbHelper.insertTempData(subID, "1", calcTime());
                break;
            }
            case R.id.button_2: {
                dbHelper.insertTempData(subID, "2", calcTime());
                break;
            }
            case R.id.button_3: {
                dbHelper.insertTempData(subID, "3", calcTime());
                break;
            }
            case R.id.button_4: {
                dbHelper.insertTempData(subID, "4", calcTime());
                break;
            }
            case R.id.button_5: {
                dbHelper.insertTempData(subID, "5", calcTime());
                break;
            }
            case R.id.button_6: {
                dbHelper.insertTempData(subID, "6", calcTime());
                break;
            }
        }
    }

    private boolean hasPermission(String permission) {
        return (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // If at least one permission has been denied, show snackbar
        if (grantResults.length == 0 || !arePermissionsGranted(grantResults)) {
            Snackbar.make(findViewById(android.R.id.content), "Storage and internet permissions required for this app to work", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Request", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this, permissions, 10);
                        }
                    })
                    .show();

            buttonNew.setEnabled(false);
            activateButtons(false);
        } else {
            // If all permissions have been granted
            buttonNew.setEnabled(true);
        }
    }

    private boolean arePermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void showKeyboard(Boolean show, MainActivity mainActivity) {
        InputMethodManager imm = (InputMethodManager) mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (show) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else {
            imm.hideSoftInputFromWindow(mView.getWindowToken(), 0);
        }
    }

    private void activateButtons(Boolean show) {
        buttonSave.setEnabled(show);
        buttonDelete.setEnabled(show);
        buttonStart.setEnabled(show);
        buttonStop.setEnabled(show);
        button1.setEnabled(show);
        button2.setEnabled(show);
        button3.setEnabled(show);
        button4.setEnabled(show);
        button5.setEnabled(show);
        button6.setEnabled(show);
    }

    private long calcTime() {
        return System.currentTimeMillis() - timeOffset;
    }

    //Async class for CSV export task
    public class ExportDatabaseCSVTask extends AsyncTask<String, Integer, Boolean> {
        protected Boolean doInBackground(final String... args) {
            //Create directories for the output csv files
            String pathToExternalStorage = Environment.getExternalStorageDirectory().toString();
            File exportDir = new File(pathToExternalStorage, "/Flagger");
            File dataDir = new File(exportDir, "/data");

            if (!exportDir.exists()) {
                boolean created = exportDir.mkdirs();
                Log.d(TAG, "Export Dir created: " + created);
            }

            if (!dataDir.exists()) {
                boolean created = dataDir.mkdirs();
                Log.d(TAG, "Data Dir created: " + created);
            }

            //If all directories have been created successfully
            if (exportDir.exists() && dataDir.exists()) {
                try {
                    //Copy temp data to persistent db tables
                    dbHelper.saveTempData();

                    //Backup the SQL DB file
                    File data = Environment.getDataDirectory();
                    String currentDBPath = "//data//ca.simonho.flagger//databases//" + DBHelper.DATABASE_NAME;
                    File currentDB = new File(data, currentDBPath);
                    File destDB = new File(exportDir, DBHelper.DATABASE_NAME);

                    if (exportDir.canWrite()) {
                        if (currentDB.exists()) {
                            FileChannel src = new FileInputStream(currentDB).getChannel();
                            FileChannel dst = new FileOutputStream(destDB).getChannel();
                            dst.transferFrom(src, 0, src.size());
                            src.close();
                            dst.close();
                        }
                    }

                    //Export individual subject data
                    File dataFile = new File(dataDir, subID.toString() + ".csv");

                    try {
                        dbHelper.exportSubjectData(dataFile, subID.toString());
                    } catch (SQLException | IOException e) {
                        Log.d(TAG, "exportSubjectData error", e);
                    }

                    //Scan all files for MTP
                    List<String> fileList = getListFiles(exportDir);
                    String[] allFiles = new String[fileList.size()];
                    allFiles = fileList.toArray(allFiles);

                    MediaScanner mediaScanner = new MediaScanner();

                    try {
                        mediaScanner.scanFile(getApplicationContext(), allFiles, null);
                    } catch (Exception e) {
                        Log.e(TAG, "Media scanner exception", e);
                    }

                    return true;
                } catch (SQLException | IOException e) {
                    Log.e(TAG, "Save data exception", e);
                    return false;
                }
            } else {
                //Directories don't exist
                if (!exportDir.exists()) {
                    Log.e(TAG, "Flagger directory not found");
                } else if (!dataDir.exists()) {
                    Log.e(TAG, "Data directory not found");
                }
                return false;
            }
        }

        protected void onPostExecute(final Boolean success) {
            if (success) {
                final Snackbar sb = Snackbar.make(findViewById(android.R.id.content), "Data saved", Snackbar.LENGTH_SHORT);

                sb.setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sb.dismiss();
                    }
                }).show();

                idValue.setText("");
                activateButtons(false);
            }
        }

        //Recursive file lister for MTP
        private List<String> getListFiles(File parentDir) {
            ArrayList<String> inFiles = new ArrayList<>();
            File[] files = parentDir.listFiles();

            //Loop through everything in base directory, including folders
            for (File file : files) {
                if (file.isDirectory()) {
                    //Recursively add files from subdirectories
                    inFiles.addAll(getListFiles(file));
                } else {
                    inFiles.add(file.getAbsolutePath());
                }
            }
            return inFiles;
        }
    }
}
