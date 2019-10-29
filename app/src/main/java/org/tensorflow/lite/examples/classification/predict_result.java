package org.tensorflow.lite.examples.classification;

import org.litepal.crud.LitePalSupport;

import java.util.Date;

public class predict_result extends LitePalSupport {
    private int id; //主键
    private Date date;
    private String result1;
    private float pro1;
    private String result2;
    private float pro2;
    private String result3;
    private float pro3;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getResult1() {
        return result1;
    }

    public void setResult1(String result1) {
        this.result1 = result1;
    }

    public String getResult2() {
        return result2;
    }

    public void setResult2(String result2) {
        this.result2 = result2;
    }

    public String getResult3() {
        return result3;
    }

    public void setResult3(String result3) {
        this.result3 = result3;
    }

    public float getPro1() {
        return pro1;
    }

    public void setPro1(float pro1) {
        this.pro1 = pro1;
    }

    public float getPro2() {
        return pro2;
    }

    public void setPro2(float pro2) {
        this.pro2 = pro2;
    }

    public float getPro3() {
        return pro3;
    }

    public void setPro3(float pro3) {
        this.pro3 = pro3;
    }
}
