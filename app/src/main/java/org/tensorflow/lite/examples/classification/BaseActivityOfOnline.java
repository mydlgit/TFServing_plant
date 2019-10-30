package org.tensorflow.lite.examples.classification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger ;


public class BaseActivityOfOnline extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.addLogAdapter(new AndroidLogAdapter());

        //跳转相机动态权限
        //StrictMode类是Android 2.3 （API 9）引入的一个工具类，
        // 可以用来帮助开发者发现代码中的一些不规范的问题，以达到提升应用响应能力的目的。
        // 严苛模式主要检测两大问题，一个是线程策略，即ThreadPolicy，另一个是VM策略，即VmPolicy
        //只在开发中使用，发布时禁用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
    }

    /**
     * 检查是否有对应权限
     *
     * @param activity 上下文
     * @param permission 要检查的权限
     * @return  结果标识
     */
    public int verifyPermissions(Activity activity, java.lang.String permission) {
        int Permission = ActivityCompat.checkSelfPermission(activity,permission);
        if (Permission == PackageManager.PERMISSION_GRANTED) {
            Logger.d("已经同意权限");

            return 1;
        }else{
            Logger.d("没有同意权限");
            return 0;
        }
    }
}
