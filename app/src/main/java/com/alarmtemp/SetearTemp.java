package com.alarmtemp;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class SetearTemp extends AppCompatActivity implements SensorEventListener {

    private Button bActualizarDatos;
    private SensorManager mSensor;
    private int tempMinSeteo;
    private int tempMaxSeteo;
    private int flagMin;
    private int flagMax;
    private SQLControlador dbconeccion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setear_temp);

        dbconeccion = new SQLControlador(this);
        dbconeccion.abrirBaseDeDatos();

        mSensor = (SensorManager) getSystemService(SENSOR_SERVICE);

        bActualizarDatos = (Button) findViewById(R.id.actualizar);
        bActualizarDatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizarDatos();
            }
        });

        Cursor tempPrimeraVez = dbconeccion.leerDatos();
        if(tempPrimeraVez.getCount()==0)
            dbconeccion.insertarDatos("20","30");

        TextView minima = (TextView) findViewById(R.id.editMin);
        TextView maxima = (TextView) findViewById(R.id.editMax);

        Cursor cursorTemp = dbconeccion.leerDatos();

        cursorTemp.moveToLast();
        String tempMax= cursorTemp.getString(1);
        String tempMin= cursorTemp.getString(2);

        minima.setText(tempMin);
        maxima.setText(tempMax);

        tempMinSeteo = Integer.parseInt(tempMin);
        tempMaxSeteo = Integer.parseInt(tempMax);


        minima.setInputType(InputType.TYPE_NULL);
        minima.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                seteoMinima();
            }
        });

        maxima.setInputType(InputType.TYPE_NULL);
        maxima.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                seteoMaxima();
            }
        });
    }



    private int seteoMaxima() {
        flagMax = 1;
        flagMin = 0;
        initSensores();
        return flagMax;
    }

    private void seteoMinima() {
        flagMax = 0;
        flagMin = 1;
        initSensores();
    }

    //creando el dialog de confirmacion de actualizacion de datos
    private void actualizarDatos() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Actualización de Temperaturas");
        dialog.setMessage("¿Desea actualizar las temperaturas?");
        dialog.setCancelable(false);
        dialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                volverAlMain();
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

    private void volverAlMain() {
        dbconeccion.insertarDatos(String.valueOf(tempMinSeteo),String.valueOf(tempMaxSeteo));
        paraSensores();
        new HttpRequestTask().execute();
        finish();
    }

    public void initSensores() {
        mSensor.registerListener(this, mSensor.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void paraSensores() {
        mSensor.unregisterListener(this, mSensor.getDefaultSensor(Sensor.TYPE_GRAVITY));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float valor;
        valor = event.values[0];
        if (flagMin == 1) {
            String min = "min";
            cambiarTemperatura(valor, min);
        } else {
            String max = "max";
            cambiarTemperatura(valor, max);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void cambiarTemperatura(float inclinacion, String temp) {

        int valor = (int) inclinacion;

        if (temp == "max") {
            if (valor != 0 && valor > 0 && tempMaxSeteo > tempMinSeteo) {
                TextView temperatura = (TextView) findViewById(R.id.editMax);
                tempMaxSeteo = Integer.parseInt(temperatura.getText().toString()); //convierto el texto del textView en int
                tempMaxSeteo--;
                temperatura.setText(String.valueOf(tempMaxSeteo));
            } else if (valor != 0 && valor < 0) {
                TextView temperatura = (TextView) findViewById(R.id.editMax);
                tempMaxSeteo = Integer.parseInt(temperatura.getText().toString()); //convierto el texto del textView en int
                tempMaxSeteo++;
                temperatura.setText(String.valueOf(tempMaxSeteo));
            }
        }
        if(temp == "min"){
            if (valor != 0 && valor > 0) {
                TextView temperatura = (TextView) findViewById(R.id.editMin);
                tempMinSeteo = Integer.parseInt(temperatura.getText().toString()); //convierto el texto del textView en int
                tempMinSeteo--;
                temperatura.setText(String.valueOf(tempMinSeteo));
            } else if (valor != 0 && valor < 0 && tempMinSeteo < tempMaxSeteo) {
                TextView temperatura = (TextView) findViewById(R.id.editMin);
                tempMinSeteo = Integer.parseInt(temperatura.getText().toString()); //convierto el texto del textView en int
                tempMinSeteo++;
                temperatura.setText(String.valueOf(tempMinSeteo));
            }
        }
    }

    //post para enviar la temperatura max y min seteadas
    private class HttpRequestTask extends AsyncTask<Void, Void, Greeting> {
        @Override
        protected Greeting doInBackground(Void... params) {
            try {
                final String url = "http://192.168.10.119:8080/@"+tempMaxSeteo+","+tempMinSeteo + "@";
                Log.d("url", url);
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                return restTemplate.getForObject(url, Greeting.class);
            } catch (Exception e) {
                Log.e("SetearTemp", e.getMessage(), e);
            }
            return null;
        }
    }
}

