package com.alarmtemp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;


public class MainActivity extends AppCompatActivity {

    private ImageView galileo;
    private ImageView onOff;
    private ImageView  frioCalor;
    private Handler mHandler = new Handler();
    private int flagAlarma = 1;
    private int request_code = 1;
    private int flagEncendido;
    private int service=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        galileo = (ImageView) findViewById(R.id.galileo);
        galileo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                medidorTemp();
            }
        });

        onOff = (ImageView) findViewById(R.id.onOff);
        onOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apagarSistema();
            }
        });

        frioCalor = (ImageView) findViewById(R.id.union);
        frioCalor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setearTemp();
            }
        });


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

    private void apagarSistema() {
        new HttpRequestTask();
    }

    //lanzar actividad para ver la temperatura medida
    public void medidorTemp(){
        Intent medidor = new Intent(this, MedidorTemp.class);
        startActivity(medidor);
    }

    //lanzar la actividad para setear la tempertura maxima
    public void setearTemp(){
        Intent seteoTemp = new Intent(this, SetearTemp.class);
        startActivity(seteoTemp);
    }

    //post para apagar el sistema
    private class HttpRequestTask extends AsyncTask<Void, Void, Greeting> {
        @Override
        protected Greeting doInBackground(Void... params) {
            try {
                final String url = "http://192.168.10.119:8080/&0";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                return restTemplate.getForObject(url, Greeting.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }
            return null;
        }
    }

    //get que consulta si el sistema se encuentra apagado o prendido
    private class HttpRequestGet extends AsyncTask<Void, Void, Greeting> {
        @Override
        protected Greeting doInBackground(Void... params) {
            try {
                final String url = "http://192.168.10.119:8080/";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                return restTemplate.getForObject(url, Greeting.class);
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Greeting greeting) {
            if(greeting!=null){

                if(Integer.parseInt(greeting.getEncendido()) == 1 && flagEncendido == 0) {
                    onOff.setBackgroundResource(R.drawable.boton);//fondo verde
                    flagEncendido=1;
                }
                else{
                    if(Integer.parseInt(greeting.getEncendido()) == 0 && flagEncendido == 1) {
                        onOff.setBackgroundResource(R.drawable.boton); //fondo rojo
                        flagEncendido = 0;
                    }
                }

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
