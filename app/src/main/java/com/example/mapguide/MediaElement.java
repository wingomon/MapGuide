package com.example.mapguide;


import android.provider.MediaStore;

import java.io.Serializable;

/** This Class is for storing information for each Media-Element.
 * These elements can be added in the Station_Edit_Activity
 * and can be whether type=IMAGE or type=TEXT
 *
 * If type=IMAGE the String "store" will store the imagepath
 * for type=Text it will store the text
 */
public class MediaElement implements Serializable {

    private String store;
    private String type;

    public MediaElement(String type, String store){
        this.type = type;
        this.store = store;
    }

    public MediaElement(){}

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
