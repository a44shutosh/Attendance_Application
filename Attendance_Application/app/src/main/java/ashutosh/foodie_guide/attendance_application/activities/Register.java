package ashutosh.foodie_guide.attendance_application.activities;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import ashutosh.foodie_guide.attendance_application.R;
import ashutosh.foodie_guide.attendance_application.helpers.AESHelper;
import ashutosh.foodie_guide.attendance_application.utils.Utils;

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            Log.e("NFC", "these are supported technologies by the tag" + tag.toString());
        } else {
            Toast.makeText(this, "Tag not found.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Register.this, NavDrawer.class);
            finish();
            startActivity(intent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(Register.this, NavDrawer.class);
                finish();
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onRegister(View view) {
        String seedValue = "UNBREAK";
        EditText et = (EditText) findViewById(R.id.editTextRegister);
        final String MESSAGE = et.getText().toString();
        Log.e("message", "This is the MESSAGE: " + MESSAGE);

        String normalText = MESSAGE;
        String normalTextEnc = null;
        try {
            normalTextEnc = AESHelper.encrypt(seedValue, normalText);
            Log.e("AES", "this is the encrpted text: "+normalTextEnc);

            String normalTextDec = AESHelper.decrypt(seedValue, normalTextEnc);
            Log.e("AES", "this is the decrypted tex: "+normalTextDec);

        } catch (Exception e) {
            e.printStackTrace();
        }


        final String finalNormalTextEnc = normalTextEnc;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Utils.REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        switch(Integer.parseInt(response)){
                            case 200:
                                response="User Successfully registered";
                                break;
                            case 300:
                                response="User already registered";
                                break;
                            case 301:
                                response="No User exists in DB";
                                break;
                            default:
                                response="Server Error!";
                        }
                        Toast.makeText(Register.this,response,Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(Register.this,error.toString(),Toast.LENGTH_LONG).show();
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null) {
                            Log.e("Volley", "Error. HTTP Status Code:"+networkResponse.statusCode);
                        }

                        if (error instanceof TimeoutError) {
                            Log.e("Volley", "TimeoutError");
                        }else if(error instanceof NoConnectionError){
                            Log.e("Volley", "NoConnectionError");
                        } else if (error instanceof AuthFailureError) {
                            Log.e("Volley", "AuthFailureError");
                        } else if (error instanceof ServerError) {
                            Log.e("Volley", "ServerError");
                        } else if (error instanceof NetworkError) {
                            Log.e("Volley", "NetworkError");
                        } else if (error instanceof ParseError) {
                            Log.e("Volley", "ParseError");
                        }
                    }
                }){
            @Override
            protected Map<String,String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(Utils.KEY_ROLL_NO, MESSAGE);
                params.put(Utils.KEY_TAG_KEY, finalNormalTextEnc);
                Log.e("Encrypted Value", finalNormalTextEnc);
                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}