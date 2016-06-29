package team8.webclient;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.common.api.GoogleApiClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class SignupActivity extends AppCompatActivity {
    Button btn_signup;
    EditText signup;
    String username;
    EditText password;
    String passwordString;
    Button btn_tologin;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        signup = (EditText) findViewById(R.id.editText_signup);
        password = (EditText) findViewById(R.id.editText_password);
        btn_signup = (Button) findViewById(R.id.button_signup);
        btn_tologin = (Button) findViewById(R.id.button_tologin);
        //wenn der Signup Button geklickt wird
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (signup != null) {
                    HttpAsyncTask httpAsyncTask = new HttpAsyncTask();
                    url = "http://10.0.2.2:3000/" + signup.getText().toString();
                    username = signup.getText().toString();
                    passwordString = password.getText().toString();
                    //Starten des AsyncTask
                    httpAsyncTask.execute(url, username, passwordString);
                }

            }
        });
        //wenn der Login Button geklickt wird
        btn_tologin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    //Methode zum Registrieren des Users
    public static String POST(String url, String username, char[] password) {
        InputStream inputStream = null;
        String result = "";
        char[] PASSWORD = password;
        int iterations = 10000;
        try {

            HttpClient httpclient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost(url);

            String json = "";

            //salt_masterkey bilden
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            byte[] salt = new byte[64];
            sr.nextBytes(salt);
            String saltString = Base64.encodeToString(salt, Base64.DEFAULT);
            //masterkey aus salt_masterkey und password bilden
            SecretKeyFactory kf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec specs = new PBEKeySpec(password, salt, 10000, 256);
            SecretKey key = kf.generateSecret(specs);
            byte[] masterkey = key.getEncoded();

            //KeyPair bilden
            KeyPairGenerator kpg = null;

            //KeyPairGenerator erzeugen
            kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.genKeyPair();
            //publicKey bilden
            Key publicKey = kp.getPublic();
            byte[] pubKeyByte = publicKey.getEncoded();
            String pubKeyString =  Base64.encodeToString(pubKeyByte, Base64.DEFAULT);
            //privateKey bilden
            Key privateKey = kp.getPrivate();
            byte [] privKeyByte = privateKey.getEncoded();

            //verschlüsselten privkey_user_enc bilden
            SecretKey masterkey_enc = new SecretKeySpec(masterkey, 0, masterkey.length, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, masterkey_enc);
            byte [] privkey_user_enc = cipher.doFinal(privateKey.getEncoded());
            String privkeyGeheim =  Base64.encodeToString(privkey_user_enc, Base64.DEFAULT);

            //JSONObject füllen
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("login", username);
            jsonObject.accumulate("salt_masterkey", saltString);
            jsonObject.accumulate("pubkey_user", pubKeyString);
            jsonObject.accumulate("privkey_user_enc", privkeyGeheim);

            json = jsonObject.toString();

            StringEntity se = new StringEntity(json);

            httpPost.setEntity(se);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            HttpResponse httpResponse = httpclient.execute(httpPost);

            inputStream = httpResponse.getEntity().getContent();

            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;

    }

    //AsyncTask für das Registrieren
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String password = params[2];
            char[] c_arr = password.toCharArray();
            return POST(params[0], params[1], c_arr);
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(intent);
        }
    }
}
