package com.emirhalici.currencytrackr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.channels.GatheringByteChannel;

import static java.lang.Math.round;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        // hotfix for android.os.NetworkOnMainThreadException
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        TextView tv_usd = findViewById(R.id.tv_usdprice);
        TextView tv_eur = findViewById(R.id.tv_europrice);
        TextView tv_gbp = findViewById(R.id.tv_poundprice);
        TextView tv_btc = findViewById(R.id.tv_btcprice);
        TextView tv_eth = findViewById(R.id.tv_ethprice);
        TextView tv_doge = findViewById(R.id.tv_dogeprice);

        tv_usd.setText(getString(R.string.priceText, sharedPref.getFloat("usd", -1)));
        tv_eur.setText(getString(R.string.priceText, sharedPref.getFloat("eur", -1)));
        tv_gbp.setText(getString(R.string.priceText, sharedPref.getFloat("gbp", -1)));
        tv_btc.setText(getString(R.string.cryptoPriceText, sharedPref.getFloat("btcUSD", -1), sharedPref.getFloat("btcTRY", -1)));
        tv_eth.setText(getString(R.string.cryptoPriceText, sharedPref.getFloat("ethUSD", -1), sharedPref.getFloat("ethTRY", -1)));
        tv_doge.setText(getString(R.string.cryptoPriceText, sharedPref.getFloat("dogeUSD", -1), sharedPref.getFloat("dogeTRY", -1)));

        if (sharedPref.getFloat("gbp", -1)==-1) {
            try {
                refreshAllValues(tv_usd, tv_eur, tv_gbp, tv_btc, tv_eth, tv_doge);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }



    }

    public static float round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }


    void refreshAllValues(TextView usd, TextView eur, TextView gbp, TextView btc, TextView eth, TextView doge) throws IOException, JSONException {

        int decimalNr = 2;

        double busdtry = ApiHelper.getPriceBySymbolInDouble("BUSDTRY");
        double eurusdt = ApiHelper.getPriceBySymbolInDouble("EURUSDT");
        double gbpusdt = ApiHelper.getPriceBySymbolInDouble("GBPUSDT");
        double btcusdt = ApiHelper.getPriceBySymbolInDouble("BTCUSDT");
        double ethusdt = ApiHelper.getPriceBySymbolInDouble("ETHUSDT");
        double dogeusdt = ApiHelper.getPriceBySymbolInDouble("DOGEUSDT");

        float usdPrice = round(busdtry, decimalNr);
        float eurPrice = round( (double) busdtry * eurusdt , decimalNr);
        float gbpPrice = round( (double) busdtry * gbpusdt , decimalNr);
        float btcPriceUSD = round(btcusdt, decimalNr);
        float ethPriceUSD = round(ethusdt, decimalNr);
        float dogePriceUSD = round(dogeusdt, decimalNr);
        float btcPriceTRY = round((double) btcusdt * busdtry, decimalNr);
        float ethPriceTRY = round((double) ethusdt * busdtry, decimalNr);
        float dogePriceTRY = round((double) dogeusdt * busdtry, decimalNr);

        usd.setText(getString(R.string.priceText, usdPrice));
        eur.setText(getString(R.string.priceText, eurPrice));
        gbp.setText(getString(R.string.priceText, gbpPrice));
        btc.setText(getString(R.string.cryptoPriceText, btcPriceUSD, btcPriceTRY));
        eth.setText(getString(R.string.cryptoPriceText, ethPriceUSD, ethPriceTRY));
        doge.setText(getString(R.string.cryptoPriceText, dogePriceUSD, dogePriceTRY));
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
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

        editor.apply();

    }
}