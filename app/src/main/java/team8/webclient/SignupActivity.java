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
import javax.crypto.spec.*;


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
        btn_signup.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(signup != null) {
                    HttpAsyncTask httpAsyncTask = new HttpAsyncTask();
                    url = "http://10.0.2.2:3000/"+signup.getText().toString();
                    username = signup.getText().toString();
                    passwordString = password.getText().toString();
                    httpAsyncTask.execute(url, username, passwordString);
                }

            }
        });
        btn_tologin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    public static String POST(String url, String username, char[] password){
        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            //salt_masterkey bilden
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            byte[] salt = new byte[64];
            sr.nextBytes(salt);
            byte[] salt_masterkey = salt;
            String saltString = new String(salt_masterkey, "UTF-8");
            byte[] salt_masterkey1 = saltString.getBytes("UTF-8");

            SecretKeyFactory kf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec specs = new PBEKeySpec(password, salt_masterkey1, 10000, 256);
            SecretKey key = kf.generateSecret(specs);
            byte[] masterkey = key.getEncoded();

            //KeyPair bilden
            KeyPairGenerator kpg = null;

            //KeyPairGenerator erzeugen --> Algorithmus: RSA 2048
            kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.genKeyPair();
            Key publicKey = kp.getPublic();
            Key privateKey = kp.getPrivate();
            System.out.println(privateKey.toString());

            //generate privkey_user_enc
            SecretKey masterkey_enc = new SecretKeySpec(masterkey, 0, masterkey.length, "AES");
            byte[] encrypted = null;
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, masterkey_enc);
            encrypted = cipher.doFinal(privateKey.getEncoded());

            String privkeyGeheim = android.util.Base64.encodeToString(encrypted, android.util.Base64.DEFAULT);


            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("login", username);
            jsonObject.accumulate("salt_masterkey", salt_masterkey);
            jsonObject.accumulate("pubkey_user", publicKey.toString());
            jsonObject.accumulate("privkey_user_enc", privkeyGeheim);


            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();


            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;

    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String password = params[2];
            char[] c_arr = password.toCharArray();
            return POST(params[0], params[1], c_arr);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(intent);
        }
    }
}
