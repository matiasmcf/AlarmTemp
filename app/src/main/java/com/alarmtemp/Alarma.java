package com.alarmtemp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alarmtemp.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Text;

public class Alarma extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensor;
    private Handler mHandler = new Handler();
    public final static int VALOR_SENSOR_PROX = 100;
    public final static String FLAG_ALARMA = "flagAlarma";
    private int valorAlarma = 0;
    private Vibrator vibrator;
    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarma);

        long[] patron={0,500,200};
        vibrator =(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(patron,0);

        mp = MediaPlayer.create(this,R.raw.alarma);
        mp.setLooping(true);
        mp.start();

        mSensor = (SensorManager) getSystemService(SENSOR_SERVICE);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    fetchData();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    private void fetchData() {
        // Get the data from the service

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // This will run on the ui thread
                new HttpRequestGet().execute();
            }
        });

    }

    //iniciando sensores
    public void initSensores() {
        mSensor.registerListener(this, mSensor.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
    }

    //parando los sensores
    public void paraSensores() {
        mSensor.unregisterListener(this, mSensor.getDefaultSensor(Sensor.TYPE_PROXIMITY));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float valor;
        valor = event.values[0];
        controlarAlarma(valor);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initSensores();
    }


    private void controlarAlarma(float valor) {
        int proximidad = (int) valor;

        if (proximidad != VALOR_SENSOR_PROX) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Apagado de Alarma");
            dialog.setMessage("¿Desea apagar la alarma?");
            dialog.setCancelable(false);
            dialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    volver();
                }
            });
            dialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            dialog.show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    //vuelve a la acitivity de la cual se llamo
    private void volver() {
        paraSensores();
        new HttpRequestTask().execute();
        volverMedidorTemp();
        vibrator.cancel();
        mp.stop();
        finish();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    //post para apagar la alarma
    private class HttpRequestTask extends AsyncTask<Void, Void, Greeting> {
        @Override
        protected Greeting doInBackground(Void... params) {
            try {
                final String url = "http://192.168.10.119:8080/&0";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                return restTemplate.getForObject(url, Greeting.class);
            } catch (Exception e) {
                Log.e("Alarma", e.getMessage(), e);
            }
            return null;
        }
    }

    //get para saber si la alarma se apago desde el arduino o sigue prendida
    private class HttpRequestGet extends AsyncTask<Void, Void, Greeting> {
        @Override
        protected Greeting doInBackground(Void... params) {
            try {
                final String url = "http://192.168.10.119:8080/";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                return restTemplate.getForObject(url, Greeting.class);
            } catch (Exception e) {
                Log.e("Alarma", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Greeting greeting) {
            if (greeting != null) {
                valorAlarma = Integer.parseInt(greeting.getAlarm());
                if (valorAlarma == 0) {
                    volverMedidorTemp();
                    vibrator.cancel();
                    mp.stop();
                    finish();
                }
            }
        }
    }

    private void volverMedidorTemp() {
        Intent hola = new Intent(this, MedidorTemp.class);
        hola.putExtra(FLAG_ALARMA, 1);
        setResult(RESULT_OK, hola);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                vibrator.cancel();
                mp.stop();
                finish();
                return true;

            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
