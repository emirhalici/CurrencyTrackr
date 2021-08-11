package com.emirhalici.currencytrackr;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class ApiHelper {
    private static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = connection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            connection.disconnect();
        }
    }

    public static JSONArray getAllPrices() throws IOException {
        // https://api.binance.com/api/v3/ticker/price
        JSONArray json = null;
        URL url = new URL("https://api.binance.com/api/v3/ticker/price");
        String response = getResponseFromHttpUrl(url);
        try {
            json = new JSONArray(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONObject getPriceBySymbolInJSON(String symbol) throws IOException {
        // https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT
        JSONObject json = null;
        URL url = new URL("https://api.binance.com/api/v3/ticker/price?symbol="+symbol);
        String response = getResponseFromHttpUrl(url);
        try {
            json = new JSONObject(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static double getPriceBySymbolInDouble(String symbol) throws IOException, JSONException {
        JSONObject json = getPriceBySymbolInJSON(symbol);
        return json.getDouble("price");
    }
}
