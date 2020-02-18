package net.binarysea.flagger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    PackageInfo info;
    String[] permissions;
    Boolean hasAllPerms = true;
    FileOutputStream f;
    PrintWriter pw;
    LayoutInflater inflater;
    View mView;
    AlertDialog.Builder builder;
    EditText idInput;
    TextView idValue;
    Long timeOffset;
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_new: {
                Log.d(TAG, "onClick: new");
                mView = inflater.inflate(R.layout.dialog_new, null);
                idValue = findViewById(R.id.id_value);
                idInput = mView.findViewById(R.id.id_input);
                idInput.requestFocus();

                builder.setView(mView)
                        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                idValue.setText(idInput.getText().toString());
                                createNewFile(Integer.toString(id));
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                // Creating dialog box
                AlertDialog alert = builder.create();
                // Setting the title manually
                alert.setTitle("Enter new ID:");
                alert.show();
                break;
            }
            case R.id.button_save: {
                Log.d(TAG, "onClick: save");
                break;
            }
            case R.id.button_delete: {
                Log.d(TAG, "onClick: delete");
                idValue.setText("");
                break;
            }
            case R.id.button_start: {
                Log.d(TAG, "onClick: start");
                break;
            }
            case R.id.button_stop: {
                Log.d(TAG, "onClick: stop");
                break;
            }
            case R.id.button_1: {
                Log.d(TAG, "onClick: button1");
                break;
            }
            case R.id.button_2: {
                Log.d(TAG, "onClick: button2");
                break;
            }
            case R.id.button_3: {
                Log.d(TAG, "onClick: button3");
                break;
            }
            case R.id.button_4: {
                Log.d(TAG, "onClick: button4");
                break;
            }
            case R.id.button_5: {
                Log.d(TAG, "onClick: button5");
                break;
            }
            case R.id.button_6: {
                Log.d(TAG, "onClick: button6");
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

            activateButtons(false);
        } else {
            // If all permissions have been granted
            activateButtons(true);
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

    private void activateButtons(Boolean show) {
        buttonNew.setEnabled(show);
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

    private void createNewFile(String id) {
        String pathToExternalStorage = Environment.getExternalStorageDirectory().toString();
        File exportDir = new File(pathToExternalStorage, "/Flagger");

        if (!exportDir.exists()) {
            boolean created = exportDir.mkdirs();
        }

        final DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        final long unixTime = System.currentTimeMillis() - timeOffset;
        final String formattedDtm = Instant.ofEpochSecond(unixTime)
                .atZone(ZoneId.of("GMT-8"))
                .format(formatter);

        File file = new File(exportDir, String.format("%s - %s.csv", id, formattedDtm));

        try {
            f = new FileOutputStream(file);
            pw = new PrintWriter(f);
            pw.println("id, time, code");
            pw.println("id, time, code");
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
