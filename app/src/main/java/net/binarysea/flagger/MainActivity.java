package net.binarysea.flagger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
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
}
