package net.binarysea.flagger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    FileOutputStream f;
    PrintWriter pw;
    LayoutInflater inflater;
    View mView;
    AlertDialog.Builder builder;
    EditText idInput;
    TextView idValue;
    long timeOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get ntp time and calculate offset from system time (ms)
        SNTPClient.getDate(Calendar.getInstance().getTimeZone(), new SNTPClient.Listener() {
            @Override
            public void onTimeReceived(long rawDate) {
                timeOffset = System.currentTimeMillis() - rawDate;
                Log.d(TAG, "System: " + System.currentTimeMillis());
                Log.d(TAG, "NTP: " + rawDate);
                Log.d(TAG,"NTP offset: " +  timeOffset);
            }

            @Override
            public void onError(Exception ex) {
                Log.d(SNTPClient.TAG, ex.getMessage());
            }
        });

        inflater = MainActivity.this.getLayoutInflater();
        builder = new AlertDialog.Builder(this);

        // Button listeners
        Button buttonNew = findViewById(R.id.button_new);
        buttonNew.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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
                //Creating dialog box
                AlertDialog alert = builder.create();
                //Setting the title manually
                alert.setTitle("Enter new ID:");
                alert.show();
            }
        });


        Button buttonDelete = findViewById(R.id.button_delete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                idValue.setText("");
            }
        });

        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        Button button3 = findViewById(R.id.button3);
        Button button4 = findViewById(R.id.button4);
        Button button5 = findViewById(R.id.button5);
        Button button6 = findViewById(R.id.button6);
    }

    private void createNewFile(String id){
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
