package its28604.txttest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by its28 on 2015/12/21.
 */
public class MapActivity extends Activity {

    Maps maps;
    Button button_shift_to_main;
    Button button_clear;
    Button button_pause;
    TextView textView_step;
    String STEP_TEXT = "走了 %s 步";

    SensorManager sm;
    float[] accelerometer_Values = new float[3];
    float[] magnetic_Field_Values = new float[3];

    ArrayList<Float> data_y_float_array = new ArrayList<>();
    ArrayList<Float> data_x_float_array = new ArrayList<>();
    boolean start;

    Handler record_data_handler = new Handler();
    final int TEN_SECOND = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);
        maps = (Maps) findViewById(R.id.mMaps);
        button_shift_to_main = (Button) findViewById(R.id.button_shift_to_main);
        button_clear = (Button) findViewById(R.id.button_clear);
        button_pause = (Button) findViewById(R.id.button_pause);
        textView_step = (TextView) findViewById(R.id.textView_step);

        //註冊SensorManager
        sm = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(listener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(listener, sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener(listener, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);

        //切回主程式
        button_shift_to_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapActivity.this, MainActivity.class));
                finish();
            }
        });

        //清空Maps
        button_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maps.Invalidate();
                data_x_float_array.clear();
                data_y_float_array.clear();
                record_data_handler.removeCallbacks(start_record);
                record_data_handler.removeCallbacks(pause_record);
                record_data_handler.post(start_record);
                record_data_handler.postDelayed(pause_record, TEN_SECOND);
            }
        });

        //暫停紀錄
        button_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record_data_handler.removeCallbacks(start_record);
                record_data_handler.removeCallbacks(pause_record);
            }
        });

        record_data_handler.post(start_record);
        record_data_handler.postDelayed(pause_record, TEN_SECOND);
    }

    //監聽並記錄Sensor變化
    SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    Log.d("acc", event.values[0] + "");
                    accelerometer_Values = event.values;
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    Log.d("mag", event.values[0] + "");
                    magnetic_Field_Values = event.values;
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    Log.d("liner", event.values[0] + "");
                    if (start) {
                        data_x_float_array.add(event.values[0]);
                        data_y_float_array.add(event.values[1]);
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    //開始紀錄資料
    Runnable start_record = new Runnable() {
        @Override
        public void run() {
            start = true;
        }
    };
    //每十秒分析一次資料, 並繪圖
    Runnable pause_record = new Runnable() {
        @Override
        public void run() {
            record_data_handler.removeCallbacks(start_record);
            start = false;
            float y_analysisData = analysisData(data_y_float_array);
            float[] position = calculateOrientation(analysisData(data_y_float_array));
            maps.addDataPoint(position[0] * 10, position[1] * 10);
            Log.d("position_x", position[0] + "");
            Log.d("position_x", position[1] + "");
            Log.d("y_analysisData", String.valueOf(y_analysisData));
            data_x_float_array.clear();
            data_y_float_array.clear();
            record_data_handler.post(start_record);
            record_data_handler.postDelayed(pause_record, TEN_SECOND);
        }
    };

    private float[] calculateOrientation(float y) {
        float[] values = new float[3];
        float[] R = new float[9];
        int degrees;
        float[] position = new float[2];
        SensorManager.getRotationMatrix(R, null, accelerometer_Values, magnetic_Field_Values);
        SensorManager.getOrientation(R, values);

        Log.d("value0", values[0] + "");
        degrees = (int) Math.toDegrees(values[0]);
        Log.d("degress", degrees + "");
        position[0] = (float) (y * Math.cos(values[0]));
        position[1] = (float) (y * Math.sin(values[0]));
        Log.d("cake", position[0] + "");
//        if (degrees >= 0 && degrees < 90) {
//            //第一象限
//            position[0] = (float) (y * Math.cos(degrees));
//            position[1] = (float) (y * Math.sin(degrees));
//        } else if (degrees >= 90 && degrees <= 180) {
//            //第二象限
//            position[0] = (float) (-y * Math.cos(180-degrees));
//            position[1] = (float) (y * Math.sin(180-degrees));
//        } else if (degrees < 0 && degrees > -90) {
//            //第三象限
//            position[0] = (float) (y * Math.sin(degrees));
//            position[1] = (float) (y * Math.cos(degrees));
//        } else {
//            //第四象限
//            position[0] = (float) (y * Math.sin(degrees));
//            position[1] = (float) (y * Math.cos(degrees));
//        }
        return position;
    }

    //分析資料
    private float analysisData(ArrayList<Float> data_array) {
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
        textView_step.setText(String.format(STEP_TEXT, count_step));
        return count_step;
    }

    @Override
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(listener);
    }

}
