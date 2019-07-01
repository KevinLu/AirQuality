
package com.kevinlu.airquality;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Pollution {

    @SerializedName("ts")
    @Expose
    private String ts;
    @SerializedName("aqius")
    @Expose
    private Integer aqius;
    @SerializedName("aqicn")
    @Expose
    private Integer aqicn;

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public Integer getAqius() {
        return aqius;
    }

    public void setAqius(Integer aqius) {
        this.aqius = aqius;
    }

    public Integer getAqicn() {
        return aqicn;
    }

    public void setAqicn(Integer aqicn) {
        this.aqicn = aqicn;
    }

}
