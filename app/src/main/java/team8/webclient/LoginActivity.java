package team8.webclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Key;
import java.security.spec.KeySpec;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public class LoginActivity extends AppCompatActivity {
    EditText login;
    EditText password;
    String username;
    Button btn_login;
    Button btn_tosignup;
    TextView answer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = (EditText) findViewById(R.id.editText_login);
        password = (EditText) findViewById(R.id.editText_loginPassword);
        username = String.valueOf(login.getText());
        answer = (TextView) findViewById(R.id.textView_login);
        btn_login = (Button) findViewById(R.id.button_login);
        btn_tosignup = (Button) findViewById(R.id.button_tosignup);
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
        btn_tosignup.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getBaseContext(), SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    public static String GET(String url){
        char[] c_arr = Storage.getPassword().toCharArray();
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);

                //JSonObject Reader erstellen und die Daten extrahieren
                JSONObject reader = new JSONObject(result);
                String salt_masterkeyString = reader.getString("salt_masterkey");
                String privkey_user_encString = reader.getString("privkey_user_enc");
                String pubkey_userString = reader.getString("pubkey_user");
                byte[] salt_masterkey = salt_masterkeyString.getBytes();
                byte[] privkey_user_enc = privkey_user_encString.getBytes();
                byte[] pubkey_user = pubkey_userString.getBytes();

                //masterkey bilden
                SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                KeySpec keySpec = new PBEKeySpec(c_arr, salt_masterkey, 1000, 256);
                SecretKey masterkey = secretKeyFactory.generateSecret(keySpec);

                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.WRAP_MODE, masterkey);
                //Fehler Login
                Key privkey_user_key = cipher.unwrap(privkey_user_enc, "AES", Cipher.SECRET_KEY);
                byte[] privkey_user = privkey_user_key.getEncoded();
                Storage.setPrivkey(privkey_user);
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
        private class HttpAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {

                return GET(params[0]);
            }
            // onPostExecute displays the results of the AsyncTask.
            @Override
            protected void onPostExecute(String result) {

                if(result.equals("")){
                    Toast.makeText(getBaseContext(), "The user don't exists, please signup!", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
                    answer.setText(result);
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    intent.putExtra("username", login.getText());
                    startActivity(intent);
                }
            }
        }
}
