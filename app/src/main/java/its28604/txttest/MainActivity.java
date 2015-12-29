package its28604.txttest;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button button_start;
    Button button_stop;
    Button button_shift_to_map;
    TextView textView_x;
    TextView textView_y;
    EditText editText;
    CheckBox if_save;
    final int TIME_OUT = 10000;

    SensorManager sm;

    ArrayList<String> data_string_array = new ArrayList<>();
    ArrayList<Float> data_float_array = new ArrayList<>();
    Boolean start = false;
    Boolean save = false;
    float x = 0;
    float y = 0;

    Handler delay = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button_stop = (Button) findViewById(R.id.button_read);
        button_start = (Button) findViewById(R.id.button_write);
        button_shift_to_map = (Button) findViewById(R.id.button_shift_to_map);
        textView_x = (TextView) findViewById(R.id.x);
        textView_y = (TextView) findViewById(R.id.y);
        editText = (EditText) findViewById(R.id.editText);
        if_save = (CheckBox) findViewById(R.id.if_save);

        sm = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(listener, sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_GAME);

        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Start", Toast.LENGTH_SHORT).show();
                start = true;
                delay.postDelayed(r1, TIME_OUT);
            }
        });

        button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delay.post(r2);
            }
        });

        button_shift_to_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                startActivity(new Intent(MainActivity.this, MapActivity.class));
                finish();
            }
        });

        if_save.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                save = if_save.isChecked();
            }
        });
    }

    SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            textView_x.setText(String.valueOf(event.values[0]));
            textView_y.setText(String.valueOf(event.values[1]));
            if (start) {
                x = x + event.values[0];
                y = y + event.values[1];
                data_string_array.add(String.valueOf(event.values[0]) + "\t" + String.valueOf(event.values[1]));
                data_float_array.add(event.values[1]);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(MainActivity.this, "onDestory", Toast.LENGTH_SHORT).show();
    }

    Runnable r1 = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(MainActivity.this, "TIME OUT!", Toast.LENGTH_SHORT).show();
            if (save) {
                writeData(data_string_array.toString());
            }
            analysisData(data_float_array);
            data_string_array.clear();
            data_float_array.clear();
            start = false;
        }
    };

    Runnable r2 = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(MainActivity.this, "Restart!", Toast.LENGTH_SHORT).show();
            data_string_array.clear();
            data_float_array.clear();
            start = false;
            delay.removeCallbacks(r1);
        }
    };

    private void writeData(String data) {
        try {
            File mSDFile;

            if (Environment.getExternalStorageState().equals(Environment.MEDIA_REMOVED)) {
                Toast.makeText(MainActivity.this, "沒有SD卡!!", Toast.LENGTH_SHORT).show();
                return;
            } else {
                mSDFile = Environment.getExternalStorageDirectory();
            }

            File mFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/MyAndroid/");

            if (!mFile.exists()) {
                mFile.mkdirs();
            }
            String fileName = getFileName(mSDFile);
            FileWriter mFileWriter = new FileWriter(mSDFile.getParent() + "/" + mSDFile.getName() + "/MyAndroid/" + fileName);
            mFileWriter.write(data);
            mFileWriter.close();
            Toast.makeText(MainActivity.this, "儲存字串自" + mSDFile.getParent() + "/" + mSDFile.getName() + "/MyAndroid/" + fileName + "成功!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //分析資料
    private void analysisData(ArrayList<Float> data_array) {
        float total_data = 0;
        int count_step = 0;
        float average_data = total_data / data_array.size();
        ArrayList<Float> data_analysis_array = new ArrayList<>();

        for (int i = 0; i < data_array.size(); i++) {
            if (data_array.get(i) < (average_data - 1.5)) {
                data_analysis_array.add(data_array.get(i));
                continue;
            }
            if (data_analysis_array.size() > 2) {
                count_step += 1;
                data_analysis_array.clear();
            }
        }
        Toast.makeText(MainActivity.this, "共走了" + String.valueOf(count_step) + "步", Toast.LENGTH_SHORT).show();
    }

    private String getFileName(File mSDFile) {
        String name = editText.getText().toString();
        File mFile;
        int number = 1;
        while (true) {
            try {
                mFile = new File(mSDFile.getParent() + "/" + mSDFile.getName() + "/MyAndroid/" + name + ".txt");
                if (!mFile.exists()) {
                    number = 1;
                    return name + ".txt";
                } else {
                    Toast.makeText(MainActivity.this, "檔案已存在!", Toast.LENGTH_SHORT).show();
                    name = name + "_" + number;
                    number++;
                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
