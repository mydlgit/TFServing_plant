package org.tensorflow.lite.examples.classification;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class WelcomActivity extends AppCompatActivity {
    private final int WEICOME_DISPLAY_LENGHT = 2000; // 两秒后进入系统

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcom);
        TextView c1 = findViewById(R.id.c1);
        TextView c2 = findViewById(R.id.c2);
        Typeface typeface = Typeface.createFromAsset(getAssets(),"font/zhuanshu.ttf");
        c1.setTypeface(typeface);
        c2.setTypeface(typeface);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(WelcomActivity.this, MainActivity.class);
                WelcomActivity.this.startActivity(intent);
                WelcomActivity.this.finish();
            }
        },WEICOME_DISPLAY_LENGHT);

    }
}
