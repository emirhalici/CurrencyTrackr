package com.emirhalici.currencytrackr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.channels.GatheringByteChannel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static java.lang.Math.round;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPref = getSharedPreferences(getPackageName()+"_preferences", MODE_PRIVATE);
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        // hotfix for android.os.NetworkOnMainThreadException
        // StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        // StrictMode.setThreadPolicy(policy);

        TextView tv_usd = findViewById(R.id.tv_usdprice);
        TextView tv_eur = findViewById(R.id.tv_europrice);
        TextView tv_gbp = findViewById(R.id.tv_poundprice);
        TextView tv_btc = findViewById(R.id.tv_btcprice);
        TextView tv_eth = findViewById(R.id.tv_ethprice);
        TextView tv_doge = findViewById(R.id.tv_dogeprice);
        TextView tv_lastUpdated = findViewById(R.id.tv_lastUpdated);
        SwipeRefreshLayout refreshLayout = findViewById(R.id.swiperefresh);

        try {
            refreshAllValuesFromPreferences(tv_usd, tv_eur, tv_gbp, tv_btc, tv_eth, tv_doge, tv_lastUpdated, refreshLayout);
        } catch (IOException | JSONException e) {
            Toast.makeText(getApplicationContext(), "An error occurred when trying to load values from local cache.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        // checking to see if values are null (default value is -1 hence null)
        if ((int) sharedPref.getFloat("gbp", -1) == -1) {
            // if so, this thread is run to update the values using
            // refreshAllPreferenceValues method
            updaterThread thread = new updaterThread();
            thread.start();
            // now that all values are hopefully updated, textbox views are updated
            try {
                refreshAllValuesFromPreferences(tv_usd, tv_eur, tv_gbp, tv_btc, tv_eth, tv_doge, tv_lastUpdated, refreshLayout);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("onCreate", "io exception: error while refreshAllValuesFromPreferences");
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("onCreate", "json exception: error while refreshAllValuesFromPreferences");
            }
        }

        // swipe to refresh motion listener
        refreshLayout.setOnRefreshListener(() -> {
            updaterThread thread = new updaterThread();
            thread.start();
            try {
                refreshAllValuesFromPreferences(tv_usd, tv_eur, tv_gbp, tv_btc, tv_eth, tv_doge, tv_lastUpdated, refreshLayout);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("refreshLayout", "io exception: error while refreshAllValuesFromPreferences");
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("refreshLayout", "json exception: error while refreshAllValuesFromPreferences");
            }
        });

    }

    // returns the double value rounded with given decimal places.
    public static float round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    // using the values stored in sharedPreferences, all textviews are updated
    void refreshAllValuesFromPreferences(TextView usd, TextView eur, TextView gbp, TextView btc, TextView eth, TextView doge, TextView tv_lastUpdated, SwipeRefreshLayout refreshLayout) throws IOException, JSONException {
        SharedPreferences sharedPref = getSharedPreferences(getPackageName()+"_preferences", MODE_PRIVATE);
        usd.setText(getString(R.string.priceText, sharedPref.getFloat("usd", -1)));
        eur.setText(getString(R.string.priceText, sharedPref.getFloat("eur", -1)));
        gbp.setText(getString(R.string.priceText, sharedPref.getFloat("gbp", -1)));
        btc.setText(getString(R.string.cryptoPriceText, sharedPref.getFloat("btcUSD", -1), sharedPref.getFloat("btcTRY", -1)));
        eth.setText(getString(R.string.cryptoPriceText, sharedPref.getFloat("ethUSD", -1), sharedPref.getFloat("ethTRY", -1)));
        doge.setText(getString(R.string.cryptoPriceText, sharedPref.getFloat("dogeUSD", -1), sharedPref.getFloat("dogeTRY", -1)));
        tv_lastUpdated.setText(getString(R.string.lastUpdated, sharedPref.getString("lastUpdate", "unknown")));
        refreshLayout.setRefreshing(false);
    }
    
    // returns a simple string containing date and time in format
    public String getDateTimeString() {
        String pattern = "dd/MM/yyyy HH:mm:ss";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(new Date());
    }

    public Date parseDateTimeString(String dateTimeStr) throws ParseException {
        String pattern = "dd/MM/yyyy HH:mm:ss";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.parse(dateTimeStr);
    }

    // network thread to update currency values from api
    class updaterThread extends Thread {
        private JSONObject findJSONObjectBySymbol(JSONArray jsonArray, String symbol) {
            JSONObject object = new JSONObject();
            for (int index = 0; index < jsonArray.length(); index++) {
                try {
                    String jsonSymbol = jsonArray.getJSONObject(index).getString("symbol");
                    if (symbol.equals(jsonSymbol)) {
                        object = jsonArray.getJSONObject(index);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return object;
        }

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            try {
                JSONArray arr = ApiHelper.getAllPrices();
                double busdtry = findJSONObjectBySymbol(arr,"BUSDTRY").getDouble("price");
                double eurusdt = findJSONObjectBySymbol(arr,"EURUSDT").getDouble("price");
                double gbpusdt = findJSONObjectBySymbol(arr,"GBPUSDT").getDouble("price");
                double btcusdt = findJSONObjectBySymbol(arr,"BTCUSDT").getDouble("price");
                double ethusdt = findJSONObjectBySymbol(arr,"ETHUSDT").getDouble("price");
                double dogeusdt = findJSONObjectBySymbol(arr,"DOGEUSDT").getDouble("price");
                int decimalNr = 2;
                float usdPrice = round(busdtry, decimalNr);
                float eurPrice = round( (double) busdtry * eurusdt , decimalNr);
                float gbpPrice = round( (double) busdtry * gbpusdt , decimalNr);
                float btcPriceUSD = round(btcusdt, decimalNr);
                float ethPriceUSD = round(ethusdt, decimalNr);
                float dogePriceUSD = round(dogeusdt, decimalNr);
                float btcPriceTRY = round((double) btcusdt * busdtry, decimalNr);
                float ethPriceTRY = round((double) ethusdt * busdtry, decimalNr);
                float dogePriceTRY = round((double) dogeusdt * busdtry, decimalNr);
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putFloat("usd", usdPrice);
                editor.putFloat("eur", eurPrice);
                editor.putFloat("gbp", gbpPrice);
                editor.putFloat("btcUSD", btcPriceUSD);
                editor.putFloat("ethUSD", ethPriceUSD);
                editor.putFloat("dogeUSD", dogePriceUSD);
                editor.putFloat("btcTRY", btcPriceTRY);
                editor.putFloat("ethTRY", ethPriceTRY);
                editor.putFloat("dogeTRY", dogePriceTRY);
                String dateTime = getDateTimeString();
                editor.putString("lastUpdate", dateTime);
                editor.apply();
                Log.e("updaterThread", dateTime + " - thread ran and shared preferences are updated successfully.");
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            long time = System.currentTimeMillis() - startTime;
            Log.e("updaterThread", "My thread " + this.getId() + " execution time: " + time + " ms");
        }
    }



}