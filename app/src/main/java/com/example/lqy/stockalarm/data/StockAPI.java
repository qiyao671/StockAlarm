package com.example.lqy.stockalarm.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by lqy on 16-6-5.
 */
public class StockAPI {
    public static final String DEF_CHATSET = "UTF-8";
    public static final int DEF_CONN_TIMEOUT = 30000;
    public static final int DEF_READ_TIMEOUT = 30000;
    public static final String SINAURL = "http://hq.sinajs.cn";
    public static final String URL_SHLIST ="http://web.juhe.cn:8080/finance/stock/shall";//请求接口地址
    public static String userAgent =  "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";

    //配置您申请的KEY
    public static final String APPKEY ="2922ce9890eda95ccb4fde316d709a88";

    //1.沪深股市
    public static Stock getHSStock(String gid){
//        String result;
//        String url ="http://web.juhe.cn:8080/finance/stock/hs";//请求接口地址
//        Map params = new HashMap();//请求参数
//        params.put("gid", gid);//股票编号，上海股市以sh开头，深圳股市以sz开头如：sh601009
//        params.put("key",APPKEY);//APP Key
//        JSONObject data = null;
//
//        try {
//            result =net(url, params, "GET");
//            JSONObject object = new JSONObject(result);
//            if(object.getString("resultcode").equals("200")){
//                data = object.getJSONArray("result").getJSONObject(0).getJSONObject("data");
//            }else{
//                System.out.println(object.get("error_code")+":"+object.get("reason"));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return data;

        String result = StockAPI.querySinaStocks(gid);
        String stockInfo = StockAPI.sinaResultToStock(result);
        String[] values = stockInfo.split(",");
        if(values.length > 1) {
            double yesterday = Double.parseDouble(values[2]);
            double nowPrice = Double.parseDouble(values[3]);
            double increase = nowPrice - yesterday;
            double increPer = increase / yesterday * 100;

            Stock stock = new Stock(gid, values[0], gid.substring(0, 2));
            stock.setOpen(values[1]);
            stock.setYesterday(values[2]);
            stock.setNowPri(values[3]);
            stock.setHigh(values[4]);
            stock.setLow(values[5]);
            stock.setIncrease(String.format("%.2f", increase));
            stock.setIncrePer(String.format("%.2f", increPer));
            stock.setNumber(values[8]);
            stock.setAmount(values[9]);

            return stock;
        }
        else {
            return null;
        }

    }

    public static String querySinaStocks(String list) {
        String result = "";
        Map params = new HashMap();
        params.put("list", list);
        try {
            result = net(SINAURL, params, "GET");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String sinaResultToStock(String stock) {
        if (stock != null) {
            String[] stockLR = stock.split("=");
            if (stockLR.length < 2)
                return null;

            String right = stockLR[1].replaceAll("\"", "");
            if (right.isEmpty())
                return null;

            String left = stockLR[0];
            if (left.isEmpty())
                return null;

            return right;
        }
        else
            return null;
    }

    //2.香港股市
    public static void getHkStock(){
        String result;
        String url ="http://web.juhe.cn:8080/finance/stock/hk";//请求接口地址
        Map params = new HashMap();//请求参数
        params.put("num","");//股票代码，如：00001 为“长江实业”股票代码
        params.put("key",APPKEY);//APP Key

        try {
            result =net(url, params, "GET");
            JSONObject object = new JSONObject(result);
            if(object.getInt("error_code")==0){
                System.out.println(object.get("result"));
            }else{
                System.out.println(object.get("error_code")+":"+object.get("reason"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //3.美国股市
    public static void getUsStock(){
        String result;
        String url ="http://web.juhe.cn:8080/finance/stock/usa";//请求接口地址
        Map params = new HashMap();//请求参数
        params.put("gid","");//股票代码，如：aapl 为“苹果公司”的股票代码
        params.put("key",APPKEY);//APP Key

        try {
            result =net(url, params, "GET");
            JSONObject object = new JSONObject(result);
            if(object.getInt("error_code")==0){
                System.out.println(object.get("result"));
            }else{
                System.out.println(object.get("error_code")+":"+object.get("reason"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //4.香港股市列表
    public static void getHkStockList(){
        String result;
        String url ="http://web.juhe.cn:8080/finance/stock/hkall";//请求接口地址
        Map params = new HashMap();//请求参数
        params.put("key",APPKEY);//您申请的APPKEY
        params.put("page","");//第几页,每页20条数据,默认第1页

        try {
            result =net(url, params, "GET");
            JSONObject object = new JSONObject(result);
            if(object.getInt("error_code")==0){
                System.out.println(object.get("result"));
            }else{
                System.out.println(object.get("error_code")+":"+object.get("reason"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //5.美国股市列表
    public static void getUkStockLIst(){
        String result;
        String url ="http://web.juhe.cn:8080/finance/stock/usaall";//请求接口地址
        Map params = new HashMap();//请求参数
        params.put("key",APPKEY);//您申请的APPKEY
        params.put("page","");//第几页,每页20条数据,默认第1页

        try {
            result =net(url, params, "GET");
            JSONObject object = new JSONObject(result);
            if(object.getInt("error_code")==0){
                System.out.println(object.get("result"));
            }else{
                System.out.println(object.get("error_code")+":"+object.get("reason"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //6.深圳股市列表
    public static void getSzStockList(){
        String result;
        String url ="http://web.juhe.cn:8080/finance/stock/szall";//请求接口地址
        Map params = new HashMap();//请求参数
        params.put("key",APPKEY);//您申请的APPKEY
        params.put("page","");//第几页(每页20条数据),默认第1页

        try {
            result =net(url, params, "GET");
            JSONObject object = new JSONObject(result);
            if(object.getInt("error_code")==0){
                System.out.println(object.get("result"));
            }else{
                System.out.println(object.get("error_code")+":"+object.get("reason"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getPageCount(String url) {
        int pageCount = 0;
        Map params = new HashMap();//请求参数
        params.put("key",APPKEY);//您申请的APPKEY
        params.put("page","");//第几页,每页20条数据,默认第1页

        try {
            String result = net(url, params, "GET");
            JSONObject object = new JSONObject(result);
            int totalCount = Integer.parseInt(object.getJSONObject("result").getString("totalCount"));
            pageCount = totalCount / 20 + 1;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pageCount;
    }

    //7.沪股列表
    public static JSONArray getShStockList(int page){
        String result;
        JSONArray data = null;
        Map params = new HashMap();//请求参数
        params.put("key",APPKEY);//您申请的APPKEY
        params.put("page","" + page);//第几页,每页20条数据,默认第1页

        try {
            result =net(URL_SHLIST, params, "GET");
            JSONObject object = new JSONObject(result);
            if(object.getInt("error_code")==0){
                data = object.getJSONObject("result").getJSONArray("data");
            }else{
                System.out.println(object.get("error_code")+":"+object.get("reason"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    /**
     *
     * @param strUrl 请求地址
     * @param params 请求参数
     * @param method 请求方法
     * @return  网络请求字符串
     * @throws Exception
     */
    public static String net(String strUrl, Map params,String method) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        String rs = null;
        try {
            StringBuffer sb = new StringBuffer();
            if(method==null || method.equals("GET")){
                    strUrl = strUrl+"?"+urlencode(params);

            }
            URL url = new URL(strUrl);
            conn = (HttpURLConnection) url.openConnection();
            if(method==null || method.equals("GET")){
                conn.setRequestMethod("GET");
            }else{
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
            }
            conn.setRequestProperty("User-agent", userAgent);
            conn.setUseCaches(false);
            conn.setConnectTimeout(DEF_CONN_TIMEOUT);
            conn.setReadTimeout(DEF_READ_TIMEOUT);
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            if (params!= null && method.equals("POST")) {
                try {
                    DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                    out.writeBytes(urlencode(params));
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
            InputStream is = conn.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, DEF_CHATSET));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sb.append(strRead);
            }
            rs = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return rs;
    }

    //将map型转为请求参数型
    public static String urlencode(Map<String,Object>data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry i : data.entrySet()) {
            sb.append(i.getKey()).append("=").append(i.getValue()).append("&");
        }
        sb.deleteCharAt(sb.lastIndexOf("&"));
        return sb.toString();
    }
}
