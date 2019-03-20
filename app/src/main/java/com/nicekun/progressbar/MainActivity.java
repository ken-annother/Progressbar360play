package com.nicekun.progressbar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.nicekun.progreebarlib.WaterProgressBar;

public class MainActivity extends AppCompatActivity {

    private WaterProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = findViewById(R.id.waterProgressBar);
        initData();
    }

    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mProgressBar.prepare();
                    Thread.sleep(1000);
                    mProgressBar.start();

                    int i = 0;
                    do {
                        mProgressBar.setProgress(i);
                        Thread.sleep(300);
                        i++;
                        if (i == 100) {
                            i = 0;
                        }
                    } while (true);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
