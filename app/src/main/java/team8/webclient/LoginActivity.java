package team8.webclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.spec.KeySpec;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class LoginActivity extends AppCompatActivity {
    EditText login;
    EditText password;
    String username;
    Button btn_login;
    Button btn_tosignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = (EditText) findViewById(R.id.editText_login);
        password = (EditText) findViewById(R.id.editText_loginPassword);
        username = String.valueOf(login.getText());
        btn_login = (Button) findViewById(R.id.button_login);
        btn_tosignup = (Button) findViewById(R.id.button_tosignup);

        //Beim Klicken des Login Buttons
        btn_login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(login != null) {
                    Storage.setUsername(login.getText().toString());
                    Storage.setPassword(password.getText().toString());
                    HttpAsyncTask httpAsyncTask = new HttpAsyncTask();
                    httpAsyncTask.execute("http://10.0.2.2:3000/"+login.getText());
                }

            }
        });

        //Beim Klicken des Signup Buttons
        btn_tosignup.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getBaseContext(), SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    //Methode für das Login
    public static String GET(String url){
        char[] password = Storage.getPassword().toCharArray();
        InputStream inputStream = null;
        String result = "";
        try {

            HttpClient httpclient = new DefaultHttpClient();

            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            inputStream = httpResponse.getEntity().getContent();

            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);

                //JSonObject Reader erstellen und die Daten extrahieren
                JSONObject reader = new JSONObject(result);
                String salt_masterkeyString = reader.getString("salt_masterkey");
                byte[] salt_masterkey = Base64.decode(salt_masterkeyString, Base64.DEFAULT);
                String privkey_user_encString = reader.getString("privkey_user_enc");
                String pubkey_userString = reader.getString("pubkey_user");
                byte[] privkey_user_enc = privkey_user_encString.getBytes();

                //masterkey wiederherstellen aus password und salt_masterkey
                SecretKeyFactory kf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                KeySpec specs = new PBEKeySpec(password, salt_masterkey, 10000, 256);
                SecretKey key = kf.generateSecret(specs);
                byte[] masterkey = key.getEncoded();

                //privateKey entschlüsseln
                SecretKey masterkey_enc = new SecretKeySpec(masterkey, 0, masterkey.length, "AES");
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, masterkey_enc);
                byte[] privkeyGeheim = Base64.decode(privkey_user_enc, Base64.DEFAULT);
                byte[] privKey = cipher.doFinal(privkeyGeheim);
                Storage.setPrivkey(privKey);
                Storage.setPubkey(pubkey_userString);

            }
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

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

    //AsyncTask für das Login
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            return GET(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {

            if(result.equals("")){
                Toast.makeText(getBaseContext(), "The user don't exists, please signup!", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getBaseContext(), "Login succesfull!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
            }
        }
    }
}
