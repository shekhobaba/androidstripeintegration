package com.example.androidstripeintegration;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.*;


public class MainActivity extends AppCompatActivity {

    Button payment;
    String PublishableKey="YOUR_PUBLISHABLE_KEY_HERE"; //Your PublishableKey here
    String SecretKey="YOUR_SECRET_KEY_HERE"; //Your SecretKey here

    String CustomersURL = "https://api.stripe.com/v1/customers";

    String EphericalKeyURL = "https://api.stripe.com/v1/ephemeral_keys";

    String ClientSecretURL = "https://api.stripe.com/v1/payment_intents";

    String CustomerId;
    String EphericalKey;
    String ClientSecret;
    PaymentSheet paymentSheet;
    String Amount = "1000"; //Your Product Amount Here
    String Currency = "EUR"; //Your Default Currency Here


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        payment =findViewById(R.id.payment);

        PaymentConfiguration.init(this,PublishableKey);

        paymentSheet = new PaymentSheet(this, this::onPaymentResult);

        payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                paymentFlow();

            }
        });


        StringRequest request = new StringRequest(Request.Method.POST, CustomersURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject object = null;
                try {
                    object = new JSONObject(response);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                try {
                    CustomerId =object.getString("id");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                Toast.makeText(MainActivity.this, CustomerId, Toast.LENGTH_SHORT).show();

                getEmphericalKey();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            public Map<String, String> getHeaders() throws AuthFailureError{

                Map<String, String> header= new HashMap<>();
                header.put("Authorization","Bearer "+SecretKey);

                return header;

            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);

    }

    private void paymentFlow() {

        paymentSheet.presentWithPaymentIntent(ClientSecret,new PaymentSheet.Configuration("Stripe",new PaymentSheet.CustomerConfiguration(

            CustomerId, EphericalKey

        )));

    }

    private void onPaymentResult(PaymentSheetResult paymentSheetResult) {

        if(paymentSheetResult instanceof PaymentSheetResult.Completed){
            Toast.makeText(this,"Payment Success",Toast.LENGTH_SHORT).show();
        }
    }

    private void getEmphericalKey() {

        StringRequest request = new StringRequest(Request.Method.POST,EphericalKeyURL , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject object = null;
                try {
                    object = new JSONObject(response);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                try {
                    EphericalKey =object.getString("id");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                Toast.makeText(MainActivity.this, CustomerId, Toast.LENGTH_SHORT).show();

                getClientSecret(CustomerId,EphericalKey);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            public Map<String, String> getHeaders() throws AuthFailureError{

                Map<String, String> header= new HashMap<>();
                header.put("Authorization","Bearer "+SecretKey);
                header.put("Stripe-Version","2022-11-15");

                return header;

            }
            public Map<String, String> getParams() throws AuthFailureError{

                Map<String, String> params= new HashMap<>();
                params.put("customer",CustomerId);
                return params;

            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);

    }

    private void getClientSecret(String customerId, String ephericalKey) {

        StringRequest request = new StringRequest(Request.Method.POST, ClientSecretURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject object = null;
                try {
                    object = new JSONObject(response);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                try {
                    ClientSecret = object.getString("client_secret");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

                Toast.makeText(MainActivity.this, ClientSecret, Toast.LENGTH_SHORT).show();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            public Map<String, String> getHeaders() throws AuthFailureError{

                Map<String, String> header= new HashMap<>();
                header.put("Authorization","Bearer "+SecretKey);

                return header;

            }
            public Map<String, String> getParams() throws AuthFailureError{

                Map<String, String> params= new HashMap<>();
                params.put("customer",CustomerId);
                params.put("amount",Amount);
                params.put("currency",Currency);
                params.put("automatic_payment_methods[enabled]","true");
                return params;

            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);

    }

}