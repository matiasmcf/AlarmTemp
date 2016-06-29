package com.alarmtemp;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class MedidorTemp extends AppCompatActivity {

    public AnimationDrawable anim;
    private Handler mHandler = new Handler();
    private int request_code = 1;
    public int flagAlarma = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medidor_temp);

        ImageView termometro = (ImageView) findViewById(R.id.termo_img);
        termometro.setBackgroundResource(R.drawable.termo_25);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    fetchData();
                    try {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    //cambiar imagen del termometro
    public void cambiarAnimacion(float t) {
        int temp = (int) t;
        ImageView termo = (ImageView) findViewById(R.id.termo_img);

        if(temp < 15)
            temp = 15;
        else {
            if (temp > 45)
                temp = 45;
        }

        int d = getResources().getIdentifier("termo_" + temp, "drawable", getPackageName());
        termo.setBackgroundResource(d);
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

    //get para obtener temperatura actual y estado de la alarma
    private class HttpRequestGet extends AsyncTask<Void, Void, Greeting> {
        @Override
        protected Greeting doInBackground(Void... params) {
            try {
                final String url = "http://192.168.10.119:8080/";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                return restTemplate.getForObject(url, Greeting.class);
            } catch (Exception e) {
                Log.e("MedidorTemp", e.getMessage(), e);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Greeting greeting) {
            if(greeting!=null){
                TextView greetingTemp = (TextView) findViewById(R.id.content_value);
                greetingTemp.setText(greeting.getTemp());

                cambiarAnimacion(Float.parseFloat(greetingTemp.getText().toString()));
                verificarAlarma(Integer.parseInt(greeting.getAlarm()));
            }
        }
    }

    private void verificarAlarma(int iniciada) {
        if(flagAlarma == 1 && iniciada == 1) {
            flagAlarma = 0;
            Intent alarma = new Intent(this, Alarma.class);
            startActivityForResult(alarma, request_code);
        }
    }

    //obtiene el valor del flag de la alarma que se setea en Alarma.java
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == request_code) {
            if (resultCode == RESULT_OK) {
                flagAlarma = data.getIntExtra(Alarma.FLAG_ALARMA, 1);
            }
        }
    }

}
