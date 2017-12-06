package com.byteshaft.paytmversion2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.byteshaft.requests.HttpRequest;
import com.paytm.pgsdk.PaytmMerchant;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private PaytmPGService service = null;
    private Button pay;
    String checksum = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        service = PaytmPGService.getStagingService();
        pay = findViewById(R.id.pay);
        getCheckSum();
        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doPayment();
            }
        });
    }

    private void doPayment() {
        //below parameter map is required to construct PaytmOrder object, Merchant should replace below map values with his own values
        Map<String, String> paramMap = new HashMap<>();
        //these are mandatory parameters
        paramMap.put("ORDER_ID", "ORDER0102");
        paramMap.put("MID", "merchatidfrompaytm");
        paramMap.put("CUST_ID", "CUST0102");
        paramMap.put("CHANNEL_ID", "WAP");
        paramMap.put("INDUSTRY_TYPE_ID", "Retail");
        paramMap.put("WEBSITE", "APP_STAGING");
        paramMap.put("TXN_AMOUNT", "10.00");
        paramMap.put("CHECKSUMHASH", checksum);
        paramMap.put("CALLBACK_URL", "https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp");
        PaytmOrder order = new PaytmOrder(paramMap);
        service.initialize(order, null);
        service.enableLog(getApplicationContext());
        service.startPaymentTransaction(this, true, true,
                new PaytmPaymentTransactionCallback() {
                    @Override
                    public void someUIErrorOccurred(String inErrorMessage) {
                        // Some UI Error Occurred in Payment Gateway Activity.
                        // // This may be due to initialization of views in
                        // Payment Gateway Activity or may be due to //
                        // initialization of webview. // Error Message details
                        // the error occurred.
                        Toast.makeText(getApplicationContext(), "Ui/Webview error occured.", Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void onTransactionResponse(Bundle bundle) {
                        Log.i("TAG", "------------------" + bundle.toString());

                    }

                    @Override
                    public void networkNotAvailable() { // If network is not
                        // available, then this
                        // method gets called.
                        Toast.makeText(getBaseContext(), "No Internet connection.", Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void clientAuthenticationFailed(String inErrorMessage) {
                        // This method gets called if client authentication
                        // failed. // Failure may be due to following reasons //
                        // 1. Server error or downtime. // 2. Server unable to
                        // generate checksum or checksum response is not in
                        // proper format. // 3. Server failed to authenticate
                        // that client. That is value of payt_STATUS is 2. //
                        // Error Message describes the reason for failure.
                        Log.e("TAG", "clientAuthenticationFailed");
                        Toast.makeText(getBaseContext(), "Client Authentication Failed.", Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void onErrorLoadingWebPage(int iniErrorCode,
                                                      String inErrorMessage, String inFailingUrl) {
                        Log.e("TAG", "onErrorLoadingWebPage");

                    }

                    // had to be added: NOTE
                    @Override
                    public void onBackPressedCancelTransaction() {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onTransactionCancel(String s, Bundle bundle) {

                    }

                });
    }

    private void getCheckSum() {
        HttpRequest request = new HttpRequest(getApplicationContext());
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                Log.i("TAG", request.getResponseURL());
                                Log.i("TAG", request.getResponseText());
                                try {
                                    JSONObject jsonObject = new JSONObject(request.getResponseText());
                                    checksum = jsonObject.getString("CHECKSUMHASH");
                                    System.out.println(checksum);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                        }
                }
            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, int readyState, short error, Exception exception) {

            }
        });
        String param="ORDER_ID=" +"ORDER0102"+
                "&MID=JBRFoo44539086147111"+
                "&CUST_ID="+"CUST0102"+
                "&CHANNEL_ID=WAP&INDUSTRY_TYPE_ID=Retail&WEBSITE=APP_STAGING&TXN_AMOUNT=10.00&CALLBACK_URL=https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp";
        System.out.println(param);
        request.open("GET", "serverurltogeneratechecksum?"+param);
        request.send();
    }
}
