package com.example.mapguide;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Multimediaguide {

   public int id;
    public String name;
    public String description;
    public int km;
    public String category;
   // public ArrayList<pointOfInterest> poiList;
    public String imgPath;
   public List<Station> stationList;


    public Multimediaguide() {
    }

    public Multimediaguide(String name_, String description_, String imgPath_, int km_, String category_){
        this.name = name_;
        this.description = description_;
        this. imgPath = imgPath_;
        this.km = km_;
        this.category = category_;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setKm(int km) {
        this.km = km;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getDescription() {
        return description;
    }


    public String getName() {
        return name;
    }

    public int getKm() {
        return km;
    }

    public String getCategory() {
        return category;
    }

    public String getImgPath() {
        return imgPath;
    }




    /**
     * A Point of Interest contains values such as
     *
     */
    /**
    private class pointOfInterest{

        private String name;
        private String description;
        private String imgPath;

    }
**/

}
