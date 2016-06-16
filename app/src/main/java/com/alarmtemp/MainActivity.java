package com.alarmtemp;

import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;


public class MainActivity extends ActionBarActivity {
    public AnimationDrawable anim;
    private Handler mHandler = new Handler();
    private int estado;
    private int test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView fondo = (ImageView) findViewById(R.id.galileoView);
        fondo.setBackgroundResource(R.drawable.anim_fuego_hielo);
        anim = (AnimationDrawable) fondo.getBackground();
        anim.start();
        estado=0;
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

    public void cambiarAnimacion(int t) {
        if ((t / 30) % 2 == 0) {
            if (estado != 0) {
                ImageView fondo = (ImageView) findViewById(R.id.galileoView);
                fondo.setBackgroundResource(R.drawable.anim_fuego_hielo);
                anim = (AnimationDrawable) fondo.getBackground();
                anim.start();
                estado=0;
            }
        }
        else
            if (estado != 1) {
                anim.stop();
                ImageView fondo = (ImageView) findViewById(R.id.galileoView);
                fondo.setBackgroundResource(R.drawable.anim_barra);
                anim = (AnimationDrawable) fondo.getBackground();
                anim.start();
                estado=1;
            }
    }

    private void fetchData() {
        // Get the data from the service

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // This will run on the ui thread
                new HttpRequestTask().execute();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        new HttpRequestTask().execute();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            new HttpRequestTask().execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private class HttpRequestTask extends AsyncTask<Void, Void, Greeting> {
        @Override
        protected Greeting doInBackground(Void... params) {
            try {
                final String url = "http://rest-service.guides.spring.io/greeting";
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
                TextView greetingIdText = (TextView) findViewById(R.id.id_value);
                TextView greetingContentText = (TextView) findViewById(R.id.content_value);
                greetingIdText.setText(greeting.getId());
                greetingContentText.setText(greeting.getContent());
                //Aca se actulizaria el fondo y las animaciones
                cambiarAnimacion(Integer.parseInt(greetingIdText.getText().toString()));
        }

    }
    }
}
