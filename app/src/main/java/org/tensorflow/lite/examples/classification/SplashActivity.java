package org.tensorflow.lite.examples.classification;

import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.content.Context;
import android.widget.TextView;
import android.graphics.Typeface;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //创建版本展示页
        setContentView(R.layout.activity_splash);
        TextView c1 = findViewById(R.id.c1);
        TextView c2 = findViewById(R.id.c2);
        Typeface typeface = Typeface.createFromAsset(getAssets(),"font/zhuanshu.ttf");
        c1.setTypeface(typeface);
        c2.setTypeface(typeface);

        // 两秒后进入主页面
        new Handler().postDelayed(() -> {
            boolean isFirst = isFirstRun();
            if (!isFirst) {
                // 如果是第一次启动，则先进入功能引导页
                //TODO: QuickGuideActivity
                Intent intent = new Intent(SplashActivity.this, QuickGuideActivity.class);
                startActivity(intent);
                finish();
            } else {
                //否则直接进入主界面
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);
    }

    private boolean isFirstRun(){
        SharedPreferences sps =  getSharedPreferences("FirstRun", Context.MODE_PRIVATE);
        boolean isFirst = sps.getBoolean("isFirst", false);
        if (isFirst){
            SharedPreferences.Editor editor = sps.edit();
            editor.putBoolean("isFirst", false);
            editor.apply();
            return true;
        } else {
            return false;
        }
    }
}
