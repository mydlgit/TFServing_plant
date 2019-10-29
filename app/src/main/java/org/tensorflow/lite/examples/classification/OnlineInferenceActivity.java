package org.tensorflow.lite.examples.classification;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.orhanobut.logger.Logger;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;
import org.tensorflow.framework.DataType;
import org.tensorflow.framework.TensorProto;
import org.tensorflow.framework.TensorShapeProto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import tensorflow.serving.Model;
import tensorflow.serving.Predict;
import tensorflow.serving.PredictionServiceGrpc;

public class OnlineInferenceActivity extends BaseActivityOfOnline implements View.OnClickListener {
    private String TAG ="OnlineInferenceActivity_LOG";
    //需要的权限数组 读/写/相机;其中WRITE_EXTERNAL_STORAGE同时授予程序读和写的能力
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA };

    private ImageView choose_picture;
    private ImageView take_photo;
    private FloatingActionButton button_send;
    private ImageView imageView;
    private TextView result_text;   //滑动菜单中的预测结果记录
    private EditText ip_editText; //ip 输入
    private String ip;

    //返回的概率最大可能性的三个物体
    private TextView online_item;
    private TextView online_pro;
    private TextView online_item1;
    private TextView online_pro1;
    private TextView online_item2;
    private TextView online_pro2;
    private TextView online_time_id;
    private TextView online_time;

    //图片尺寸及维度
    private static final int imgX = 224;
    private static final int imgY = 224;
    private static final int imgDim = 3;

    private File tempFile;  //图片临时文件
    private List<Float> imgFloatList = new ArrayList<>(); //存储图片转换后的float数据，作为request向service发送的数据
    private List<String> labels;
    private String labelPath = "plant_label.txt";

    private DrawerLayout mdrawerLayout;

    static { //设置矢量图片资源向后兼容
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_inference);
        //设置toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //设置菜单
        mdrawerLayout = findViewById(R.id.drawer_layout);
        ActionBar actionBar = getSupportActionBar(); //获取到的其实是上面的toolbar
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
            Log.d(TAG, "onCreate: menu 成功");
        }else{
            Log.d(TAG, "onCreate: 没有action bar");
        }

        LitePal.getDatabase(); //如果存在直接打开，否则新建一个数据库

        //获取ui控件
        init();

        //读取label文件
        try{
            labels = loadLabelList(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //获取button控件
    private void init(){
        choose_picture=findViewById(R.id.choose_picture);
        take_photo=findViewById(R.id.take_photo);
        choose_picture.setOnClickListener(this);
        take_photo.setOnClickListener(this);

        button_send = findViewById(R.id.send);
        button_send.setOnClickListener(this);

        imageView = findViewById(R.id.ImageView);

        result_text = findViewById(R.id.result_text);

        ip_editText = findViewById(R.id.edit_ip);
        ip=getIP();
        ip_editText.setText(ip);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    //菜单item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: menu id"+item.getItemId());
        switch (item.getItemId()){
            case android.R.id.home:     //HomeAsUp按钮(toolbar最左侧的按钮)的id永远都是android.R.id.home
                mdrawerLayout.openDrawer(GravityCompat.START);
                getResultsFromLitepal();
                Log.d(TAG, "onOptionsItemSelected: homeAsUp");
                break;
            case R.id.delete:
                AlertDialog.Builder dialog = new AlertDialog.Builder(OnlineInferenceActivity.this);
                dialog.setTitle(Html.fromHtml("<font color=\"red\">警告！</font> "));
                dialog.setMessage("删除记录将无法恢复，确定删除？");
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteResultsFromLitepal();
                    }
                });
                dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dialog.show();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CheckBox checkBox = findViewById(R.id.remember_ip);
        if(checkBox.isChecked()){
            saveIP();
        }
    }

    //保存ip数据
    private void saveIP(){
        String ip = ip_editText.getText().toString();
        FileOutputStream out=null;
        BufferedWriter writer = null;
        try{
            out = openFileOutput("ip_data", Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(ip);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if(writer!=null)
                    writer.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    //获取保存的ip
    private String getIP(){
        StringBuilder ip=new StringBuilder();
        FileInputStream in =null;
        BufferedReader reader = null;
        try{
            in = openFileInput("ip_data");
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line=reader.readLine())!=null){
                ip.append(line);
            }

        }catch (IOException e){
            e.printStackTrace();
            return ""; //没有"ip_data"文件，返回空
        }finally {
            try {
                if(reader!=null){
                    reader.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return ip.toString();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.choose_picture:
                if(verifyPermissions(OnlineInferenceActivity.this,PERMISSIONS_STORAGE[0])==0){
                    Logger.d("需要授权访问相册");
                    ActivityCompat.requestPermissions(OnlineInferenceActivity.this,new String[]{PERMISSIONS_STORAGE[0]},1);
                }else {
                    toPicture();
                }
                break;
            case R.id.take_photo:
                if(verifyPermissions(OnlineInferenceActivity.this,PERMISSIONS_STORAGE[0]) == 0
                        || verifyPermissions(OnlineInferenceActivity.this,PERMISSIONS_STORAGE[1])==0){
                    Logger.d("需要授权相机");
                    ActivityCompat.requestPermissions(OnlineInferenceActivity.this,PERMISSIONS_STORAGE,2);
                }else {//已经有权限
                    toCamera();
                }
                break;
            case R.id.send:
                if(imgFloatList.size()==0){
                    Toast.makeText(OnlineInferenceActivity.this,"缺少图片",Toast.LENGTH_SHORT).show();
                }else if(ip.length()==0){
                    Toast.makeText(OnlineInferenceActivity.this,"请输入服务器IP",Toast.LENGTH_SHORT).show();
                }
                else {
                    OnlineInference_future();
                }
                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1: //授权读写SD卡
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    toPicture();
                }else {
                    Toast.makeText(OnlineInferenceActivity.this,"取消授权将无法使用服务",Toast.LENGTH_SHORT).show();
                }
                break;
            case 2://授权读写SD卡和相机
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){
                    toCamera();
                }else {
                    Toast.makeText(OnlineInferenceActivity.this,"取消授权将无法使用服务",Toast.LENGTH_SHORT).show();
                }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //判断返回码不等于0
        if (requestCode != RESULT_CANCELED){    //RESULT_CANCELED = 0(也可以直接写“if (requestCode != 0 )”)
            //读取返回码
            switch (requestCode){
                case 100:   //相册返回的数据（相册的返回码）
                    Logger.d("相册");
                    try {
                        Uri uri01 = data.getData();
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri01));
                        tempFile = new File(getImagePath(uri01, null));

                        bitmapToFloatArray(bitmap,imgX,imgY,imgDim);
                        imageView.setImageBitmap(bitmap);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }

                    break;
                case 101:  //相机返回的数据（相机的返回码）
                    try {
                        Logger.d("on Activity result:相机");
                        Uri uri02 = Uri.fromFile(tempFile);   //图片文件
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri02));
                        bitmapToFloatArray(bitmap,imgX,imgY,imgDim);

                        Logger.d("设置显示相机图片");
                        imageView.setImageBitmap(bitmap);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch(NullPointerException e){
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
   
    private String getImagePath(Uri uri, String selection){
            String path =null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    //跳转相册
    private void toPicture() {
        Intent intent = new Intent(Intent.ACTION_PICK);  //跳转到 ACTION_IMAGE_CAPTURE
        intent.setType("image/*");
        startActivityForResult(intent,100);
        Logger.d("跳转相册成功");
    }
    //跳转相机
    private void toCamera() {
        tempFile = new File(Environment.getExternalStorageDirectory(),"fileImg.jpg"); //新建一个 File 文件（存储通过相加获取到的图片数据）
        try{
            if(tempFile.exists())
                tempFile.delete();

            tempFile.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  //跳转到 ACTION_IMAGE_CAPTURE
        //判断内存卡是否可用，可用的话就进行存储
        //putExtra：取值，Uri.fromFile：传一个拍照所得到的文件，fileImg.jpg：文件名
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
        startActivityForResult(intent,101); // 101: 相机的返回码参数（随便一个值就行，只要不冲突就好）
        Logger.d("跳转相机成功");
    }
    //将位图转换为数组
    public  void bitmapToFloatArray(Bitmap bitmap,int rx,int ry,int dim){
        if(imgFloatList.size()!=0){
            imgFloatList.clear();
        }
        float[] result = new float[rx*ry*dim];

        int height=bitmap.getHeight();
        int width = bitmap.getWidth();

        float scalewidth = ((float)rx) / width;
        float scaleheight = ((float)ry) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scalewidth,scaleheight);
        bitmap = Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true);

        int[] intvalues = new int[rx*ry];
        bitmap.getPixels(intvalues,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
        //bitmap 默认采用ARGB格式存储（Alpha代表透明度），每个占据一个字节，按顺序依次存储
        //因此通过移位 和 & 0xff方式获取RGB通道的的值，再缩放到[0,1]之间
        for(int i=0;i<intvalues.length;i++){
            final int val = intvalues[i];
            result[i*3+0] =(((val >> 16) & 0xFF)) /255.0f;
            result[i*3+1] = (((val >> 8) & 0xFF)) /255.0f;
            result[i*3+2] = ((val & 0xFF)) /255.0f;
        }
        for(float r:result){
            imgFloatList.add(r);
        }
    }

    //记住在Manifest申请INTERNET权限
    private ManagedChannel channel = null;
    //阻塞请求
    private void OnlineInfrence(){

        if(channel!=null && !channel.isShutdown())
            channel.shutdownNow();
        //usePlaintext 为true表示非ssl连接
        channel = ManagedChannelBuilder.forAddress("192.168.4.86", 8500).usePlaintext(true).build();

        PredictionServiceGrpc.PredictionServiceBlockingStub stub = PredictionServiceGrpc.newBlockingStub(channel);
        stub.withDeadlineAfter(100000, TimeUnit.MILLISECONDS);
//        PredictionServiceGrpc.PredictionServiceFutureStub stub = PredictionServiceGrpc.newFutureStub(channel);

        //创建请求
        Predict.PredictRequest.Builder request = Predict.PredictRequest.newBuilder();
        //模型名称和方法名预设
        Model.ModelSpec.Builder modelSpec = Model.ModelSpec.newBuilder();
        modelSpec.setName("plant");
        modelSpec.setSignatureName("classification");
        request.setModelSpec(modelSpec);
        //设置tensorProto参数
        TensorProto.Builder tensorProtoBuilder = TensorProto.newBuilder();
        tensorProtoBuilder.setDtype(DataType.DT_FLOAT);

        //设置维度
        TensorShapeProto.Builder shapeProtoBuilder = TensorShapeProto.newBuilder();
        shapeProtoBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(1));
        shapeProtoBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(224));
        shapeProtoBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(224));
        shapeProtoBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(3));

        tensorProtoBuilder.setTensorShape(shapeProtoBuilder.build());

        tensorProtoBuilder.addAllFloatVal(imgFloatList);
        request.putInputs("inputs",tensorProtoBuilder.build());

        //访问并获取结果
        long t1 = System.currentTimeMillis();
        Predict.PredictResponse response = stub.predict(request.build());
        long time = System.currentTimeMillis() - t1;

        List<Float> scores = response.getOutputsOrThrow("scores").getFloatValList(); //1*3
        List<Integer> indexs = response.getOutputsOrThrow("index").getIntValList(); //1*3


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showResults(scores,indexs,time);
            }
        });

    }
    //异步请求
    private void OnlineInference_future(){
        if(channel!=null && !channel.isShutdown())
            channel.shutdownNow();
        //usePlaintext 为true表示非ssl连接
        //采用ip_editText.getText方法获取ip是为了在editText中修改ip后能立即获取到
        channel = ManagedChannelBuilder.forAddress(ip_editText.getText().toString(), 8500).usePlaintext(true).build();

        PredictionServiceGrpc.PredictionServiceFutureStub futureStub = PredictionServiceGrpc.newFutureStub(channel);
        //创建请求
        Predict.PredictRequest.Builder request = Predict.PredictRequest.newBuilder();
        //模型名称和方法名预设
        Model.ModelSpec.Builder modelSpec = Model.ModelSpec.newBuilder();
        modelSpec.setName("plant");
        modelSpec.setSignatureName("classification");
        request.setModelSpec(modelSpec);
        //设置tensorProto参数
        TensorProto.Builder tensorProtoBuilder = TensorProto.newBuilder();
        tensorProtoBuilder.setDtype(DataType.DT_FLOAT);

        //设置维度
        TensorShapeProto.Builder shapeProtoBuilder = TensorShapeProto.newBuilder();
        shapeProtoBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(1));
        shapeProtoBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(224));
        shapeProtoBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(224));
        shapeProtoBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(3));

        tensorProtoBuilder.setTensorShape(shapeProtoBuilder.build());

        tensorProtoBuilder.addAllFloatVal(imgFloatList);
        //将数据放入请求
        request.putInputs("inputs",tensorProtoBuilder.build());

        ListenableFuture<Predict.PredictResponse> predict = futureStub.predict(request.build());
        try{
            long t1 = System.currentTimeMillis();
            Predict.PredictResponse response = predict.get(10000,TimeUnit.MILLISECONDS);
            long time = System.currentTimeMillis() - t1;
            List<Float> scores = response.getOutputsOrThrow("scores").getFloatValList(); //1*3
            List<Integer> indexs = response.getOutputsOrThrow("index").getIntValList(); //1*3

            //传送图片到服务器
            socket(ip);
            //保存预测结果
            saveResults(scores,indexs,new Date());
            //显示预测结果
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showResults(scores,indexs,time);
                }
            });
        }catch (io.grpc.StatusRuntimeException e){
            e.printStackTrace();

        }
        catch (ExecutionException e){
            e.printStackTrace();
            Toast.makeText(OnlineInferenceActivity.this,"连接出现问题，请稍后再试",Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e){
            e.printStackTrace();
            Toast.makeText(OnlineInferenceActivity.this,"连接中断,请稍后再试",Toast.LENGTH_SHORT).show();
        } catch (TimeoutException e){
            e.printStackTrace();
            Toast.makeText(OnlineInferenceActivity.this, "连接超时,请检查IP或稍后再试",Toast.LENGTH_SHORT).show();
        }
    }

    //显示预测结果
    private void showResults(List<Float> scores, List<Integer> indexs,long time){
        online_item = findViewById(R.id.online_detected_item);
        online_pro = findViewById(R.id.online_detected_item_value);
        online_item.setText(labels.get(indexs.get(0)));  //具体标签
        String pro = String.format("%.2f", (100 * scores.get(0))) + "%";
        online_pro.setText(pro);

        online_item1 = findViewById(R.id.online_detected_item1);
        online_pro1 = findViewById(R.id.online_detected_item1_value);
        online_item1.setText(labels.get(indexs.get(1)));
        String pro1 = String.format("%.2f",(100*scores.get(1))) + "%" ;
        online_pro1.setText(pro1);

        online_item2 = findViewById(R.id.online_detected_item2);
        online_pro2 = findViewById(R.id.online_detected_item2_value);
        online_item2.setText(labels.get(indexs.get(2)));
        String pro2 = String.format("%.2f",(100*scores.get(2))) + "%" ;
        online_pro2.setText(pro2);

        online_time_id = findViewById(R.id.timeId);
        online_time_id.setText("Time:");

        online_time = findViewById(R.id.online_time);
        online_time.setText(String.valueOf(time)+"ms");
    }

    //保存预测结果到本机上的lite pal数据库
    private void saveResults(List<Float> scores, List<Integer>indexs, Date date){
        predict_result pr = new predict_result();
        pr.setDate(date);
        pr.setResult1(labels.get(indexs.get(0)));
        pr.setPro1(scores.get(0));

        pr.setResult2(labels.get(indexs.get(1)));
        pr.setPro2(scores.get(1));

        pr.setResult3(labels.get(indexs.get(2)));
        pr.setPro3(scores.get(2));
        pr.save();
    }
    //从liet pal数据库获取预测结果记录
    private void getResultsFromLitepal(){
        List<predict_result> results = LitePal.findAll(predict_result.class);
        StringBuilder results_builder = new StringBuilder();
        for(predict_result result:results){
            Date date = result.getDate();
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String dateStr = ft.format(date);
            results_builder.append(dateStr+"  ");

            String pro = String.format("%.2f",(100*result.getPro1())) + "%";
            results_builder.append(result.getResult1()+"  "+pro);
            results_builder.append("\n");
        }
        result_text.setText(results_builder.toString());
    }

    //删除数据库中预测结果记录
    private void deleteResultsFromLitepal(){
        LitePal.deleteAll(predict_result.class);
    }

    //读取label文件数据
    private List<String> loadLabelList(Activity activity) throws IOException {
        List<String> labels = new ArrayList<String>();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(activity.getAssets().open(getLabelPath())));
        String line;
        while ((line = reader.readLine()) != null) {
            labels.add(line);
        }
        reader.close();
        return labels;
    }

//    获取label文件地址
    private String getLabelPath(){
        return labelPath;
    }

    //通过socket上传图片到服务器
    private void socket(String ip){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare(); //调用 Looper.prepare()来给线程创建消息循环，然后再通过，Looper.loop()来使消息循环起作用。
                //Handler为了能在子线程中接收消息（或修改UI），就必须获得当前线程的Looper;
                //而Toast又属于UI控件，其在调用makeText方法时，会创建Handler对象获取当前线程中的looper
                //在主线程中，系统会自动创建looper
                try {//如果server端不开启端口，则会提醒java.net.ConnectException
                    Socket socket = new Socket(ip, 8888);
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    FileInputStream fis = new FileInputStream(tempFile);
                    //发送图片大小
                    int size = fis.available();
                    String s = String.valueOf(size);
                    while(s.length()<10){
                        s = s + " ";
                    }
                    
                    Log.d(TAG, "run: "+size);
                    
                    byte[] bytes = s.getBytes();
                    out.write(bytes);
                    out.flush();
                    //发送图片
                    //读取图片到ByteArrayOutputStream
                    byte[] sendBytes = new byte[1024];
                    int length = 0;
                    while ((length = fis.read(sendBytes, 0, sendBytes.length)) > 0) {
                        out.write(sendBytes, 0, length);
                        out.flush();
                    }
                    fis.close();
                    Log.d(TAG, "run: 发送成功");

                    Log.d(TAG, "run: 开始接收response");
                    String receive = br.readLine();
                    Toast.makeText(OnlineInferenceActivity.this,receive,Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "run: "+receive);
                    socket.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
                Looper.loop();
            }
        }).start();
    }
}
