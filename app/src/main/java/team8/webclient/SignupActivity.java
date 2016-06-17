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
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


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
            byte[] salt_masterkey = SecureRandom.getSeed(64);

            //Aufruf PBKDF2 Funktion um masterkey aus password und salt_masterkey zu bilden
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec keySpec = new PBEKeySpec(password, salt_masterkey, 1000, 256);
            SecretKey masterkey = secretKeyFactory.generateSecret(keySpec);

            //KeyPair bilden
            KeyPairGenerator kpg = null;

            //KeyPairGenerator erzeugen --> Algorithmus: RSA 2048
            kpg = KeyPairGenerator.getInstance("RSA");
            SecureRandom securerandom = new SecureRandom();
            byte bytes[] = new byte[20];
            securerandom.nextBytes(bytes);
            kpg.initialize(2048, securerandom);

            //KeyPair erzeugen
            KeyPair kp = kpg.genKeyPair();

            //publickey und privatekey in Variablen speichern
            Key pubkey_user = kp.getPublic();
            Key privkey_user = kp.getPrivate();
            byte[] privateKeyByte = privkey_user.getEncoded();
            byte[] publicKeyByte = pubkey_user.getEncoded();
            String pubKeyStr = new String(Base64.encode(publicKeyByte, 0));
            String privKeyStr = new String(Base64.encode(privateKeyByte, 0));

            //privkey_user zu privkey_user_enc verschlüsseln
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.WRAP_MODE,masterkey);
            byte[] privkey_user_enc = cipher.wrap(privkey_user);


            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("username", username);
            jsonObject.accumulate("salt_masterkey", salt_masterkey);
            jsonObject.accumulate("pubkey_user", pubKeyStr);
            jsonObject.accumulate("privkey_user_enc", privkey_user_enc);


            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person);

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
            String url = params[0];
            String username = params[0];
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
