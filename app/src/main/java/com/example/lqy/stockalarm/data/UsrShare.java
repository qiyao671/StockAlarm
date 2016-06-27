package com.example.lqy.stockalarm.data;

/**
 * Created by lqy on 16-6-5.
 */
public class UsrShare {
    int _id;
    String gid;
    String name;
    String code;
    float max;
    float min;

    public UsrShare(String gid, String name, String code) {
        this.gid = gid;
        this.name = name;
        this.code = code;

    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
    }

    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        this.min = min;
    }
}
