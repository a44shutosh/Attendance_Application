package ashutosh.foodie_guide.attendance_application.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import java.io.IOException;
import java.nio.charset.Charset;
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


        // Construct the data to write to the tag
        // Should be of the form [relay/group]-[rid/gid]-[cmd]
        /*String nfcMessage = relay_type + "-" + id + "-" + cmd;*/
        String nfcMessage = finalNormalTextEnc;

        // When an NFC tag comes into range, call the main activity which handles writing the data to the tag
        Context context=getApplicationContext();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);

        Intent nfcIntent = new Intent(context, Register.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        nfcIntent.putExtra("nfcMessage", nfcMessage);
        PendingIntent pi = PendingIntent.getActivity(context, 0, nfcIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

        nfcAdapter.enableForegroundDispatch(Register.this, pi, new IntentFilter[] {tagDetected}, null);
    }

    public void onNewIntent(Intent intent) {
        // When an NFC tag is being written, call the write tag function when an intent is
        // received that says the tag is within range of the device and ready to be written to
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String nfcMessage = intent.getStringExtra("nfcMessage");

        if(nfcMessage != null) {
            if(writeTag(this, tag, nfcMessage)){
                Log.e("NFC", "data successfully written");
            }else{
                Log.e("NFC", "data could not be written");
            }
        }
    }

    public static boolean writeTag(Context context, Tag tag, String data) {
        // Record to launch Play Store if app is not installed
        NdefRecord appRecord = NdefRecord.createApplicationRecord(context.getPackageName());

        // Record with actual data we care about
        /*NdefRecord relayRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, new String("application/" + context.getPackageName()).getBytes(Charset.forName("US-ASCII")),
                null, data.getBytes());*/

        NdefRecord relayRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, null, data.getBytes());
        // Complete NDEF message with both records
        /*NdefMessage message = new NdefMessage(new NdefRecord[] {relayRecord, appRecord});*/


        NdefMessage message = new NdefMessage(new NdefRecord[] {appRecord, relayRecord});

        try {
            // If the tag is already formatted, just write the message to it
            Ndef ndef = Ndef.get(tag);
            if(ndef != null) {
                ndef.connect();

                // Make sure the tag is writable
                if(!ndef.isWritable()) {
                    //DialogUtils.displayErrorDialog(context, R.string.nfcReadOnlyErrorTitle, R.string.nfcReadOnlyError);
                    Log.e("NFC-error", "nfc is not writeable");
                    return false;
                }

                // Check if there's enough space on the tag for the message
                int size = message.toByteArray().length;
                Log.e("Message size", String.valueOf(size));
                Log.e("Max Size", String.valueOf(ndef.getMaxSize()));

                if(ndef.getMaxSize() < size) {
                    //DialogUtils.displayErrorDialog(context, R.string.nfcBadSpaceErrorTitle, R.string.nfcBadSpaceError);
                    Log.e("NFC-error", "there is not enough space to write");

                    NdefFormatable format = NdefFormatable.get(tag);
                    format.connect();
                    format.format(message);

                    return false;
                }

                /*try {
                    // Write the data to the tag
                    ndef.writeNdefMessage(message);

                    Log.e("NFC-error", "data written");
                    //DialogUtils.displayInfoDialog(context, R.string.nfcWrittenTitle, R.string.nfcWritten);
                    return true;
                } catch (TagLostException tle) {
                    Log.e("NFC-error", "Tag Lost exception");
                    //DialogUtils.displayErrorDialog(context, R.string.nfcTagLostErrorTitle, R.string.nfcTagLostError);
                    return false;
                } catch (IOException ioe) {
                    Log.e("NFC-error", "IOException");
                    //DialogUtils.displayErrorDialog(context, R.string.nfcFormattingErrorTitle, R.string.nfcFormattingError);
                    return false;
                } catch (FormatException fe) {
                    Log.e("NFC-error", "Format Exception");
                    //DialogUtils.displayErrorDialog(context, R.string.nfcFormattingErrorTitle, R.string.nfcFormattingError);
                    return false;
                }*/

                NdefFormatable formatable = NdefFormatable.get(tag);

                if (formatable != null) {
                    try {
                        formatable.connect();

                        try {
                            formatable.format(message);
                        }
                        catch (Exception e) {
                            // let the user know the tag refused to format
                            Log.e("NFC-error", "Tag refused to format");
                        }
                    }
                    catch (Exception e) {
                        // let the user know the tag refused to connect
                        Log.e("NFC-error", "Tag refused to connect");
                    }
                    finally {
                        formatable.close();
                    }
                }
                else {
                    // let the user know the tag cannot be formatted
                    /*Log.e("NFC-error", "Tag cannot be formatted");*/
                    Ndef ndefTag = Ndef.get(tag);
                    try {
                        ndefTag.writeNdefMessage(message);
                        Log.e("Here", "Here");
                        return true;
                    }catch (TagLostException tle){
                        Log.e("NFC-error-ndef", "Tag lost exception");
                    }catch(IOException ioe){
                        Log.e("NFC-error-ndef", "IOException occures");
                    }catch(FormatException fe){
                        Log.e("NFC-error-ndef", "Format Exception");
                    }catch (Exception e){
                        Log.e("NFC-error-ndef", "Major Exception");
                    }
                }

                // If the tag is not formatted, format it with the message
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if(format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        Log.e("NFC-error-format", "NDEF Message written");
                        //DialogUtils.displayInfoDialog(context, R.string.nfcWrittenTitle, R.string.nfcWritten);
                        return true;
                    } catch (TagLostException tle) {
                        Log.e("NFC-error-format", "tag lost exception");
                        //DialogUtils.displayErrorDialog(context, R.string.nfcTagLostErrorTitle, R.string.nfcTagLostError);
                        return false;
                    } catch (IOException ioe) {
                        Log.e("NFC-error-format", "IOException");
                        //DialogUtils.displayErrorDialog(context, R.string.nfcFormattingErrorTitle, R.string.nfcFormattingError);
                        return false;
                    } catch (FormatException fe) {
                        Log.e("NFC-error-format", "FormatException");
                        //DialogUtils.displayErrorDialog(context, R.string.nfcFormattingErrorTitle, R.string.nfcFormattingError);
                        return false;
                    }
                } else {
                    Log.e("NFC-error", "format is null");
                    //DialogUtils.displayErrorDialog(context, R.string.nfcNoNdefErrorTitle, R.string.nfcNoNdefError);
                    return false;
                }
            }
        } catch(Exception e) {
            Log.e("NFC-error", "outermost error");
            //DialogUtils.displayErrorDialog(context, R.string.nfcUnknownErrorTitle, R.string.nfcUnknownError);
        }

        return false;
    }
}