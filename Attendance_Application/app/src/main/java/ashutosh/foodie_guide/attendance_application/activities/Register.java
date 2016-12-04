package ashutosh.foodie_guide.attendance_application.activities;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import ashutosh.foodie_guide.attendance_application.R;
import ashutosh.foodie_guide.attendance_application.helpers.AESHelper;

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
        String MESSAGE = et.getText().toString();
        Log.e("message", "This is the MESSAGE: " + MESSAGE);

        String normalText = MESSAGE;
        String normalTextEnc;
        try {
            normalTextEnc = AESHelper.encrypt(seedValue, normalText);
            Log.e("AES", "this is the encrpted text: "+normalTextEnc);

            String normalTextDec = AESHelper.decrypt(seedValue, normalTextEnc);
            Log.e("AES", "this is the decrypted tex: "+normalTextDec);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}