package com.example.lqy.stockalarm.data;

/**
 * Created by lqy on 16-6-5.
 */
public class Stock {
    int _id;
    String gid;
    String name;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAmount() {

        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    String code;
    String open, yesterday, nowPri, high, low, increase, increPer, number, amount;

    public String getIncrease() {
        return increase;
    }

    public void setIncrease(String increase) {
        this.increase = increase;
    }

    public String getIncrePer() {
        return increPer;
    }

    public void setIncrePer(String increPer) {
        this.increPer = increPer;
    }

    public Stock() {}

    public Stock(String gid, String name, String code) {
        this.gid = gid;
        this.name = name;
        this.code = code;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getYesterday() {
        return yesterday;
    }

    public void setYesterday(String yesterday) {
        this.yesterday = yesterday;
    }

    public String getNowPri() {
        return nowPri;
    }

    public void setNowPri(String nowPri) {
        this.nowPri = nowPri;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
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
}
