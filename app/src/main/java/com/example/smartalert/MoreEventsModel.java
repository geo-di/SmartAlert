package com.example.smartalert;

import android.graphics.Bitmap;

public class MoreEventsModel {

    private  String user;
    private  Double kmDiffernce;
    private Bitmap image;

    public MoreEventsModel() {}


    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Double getKmDiffernce() {
        return kmDiffernce;
    }

    public void setKmDiffernce(Double kmDiffernce) {
        this.kmDiffernce = kmDiffernce;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}
