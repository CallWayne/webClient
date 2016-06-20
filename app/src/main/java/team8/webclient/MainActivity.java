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
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
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
import java.util.Date;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class MainActivity extends AppCompatActivity {

    Button btn_send;
    Button btn_receive;
    Button btn_delete;
    EditText deleteAccount;
    EditText receiveMsg;
    EditText sendUsername;
    EditText sendMsg;
    EditText sendRecipient;
    String result;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_send = (Button) findViewById(R.id.button_send);
        btn_receive = (Button) findViewById(R.id.button_receive);
        btn_delete = (Button) findViewById(R.id.button_delete);
        deleteAccount = (EditText) findViewById(R.id.editText_deleteAccount);
        receiveMsg = (EditText) findViewById(R.id.editText_receive);
        sendUsername = (EditText) findViewById(R.id.editText_sendUsername);
        sendMsg = (EditText) findViewById(R.id.editText_send_Msg);
        sendRecipient = (EditText) findViewById(R.id.editText_sendRecipient);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendAsyncTask sendAsyncTask = new SendAsyncTask();
                //sendAsyncTask.execute("http://10.0.2.2:3000/"+sendUsername.getText()+"/msg", sendMsg.getText().toString(), sendUsername.getText().toString()), sendRecipient.getText().toString()
            }
        });

        btn_receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReceiveAsyncTask receiveAsyncTask = new ReceiveAsyncTask();
                receiveAsyncTask.execute("http://10.0.2.2:3000/" + deleteAccount.getText());
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteAsyncTask deleteAsyncTask = new DeleteAsyncTask();
                deleteAsyncTask.execute("http://10.0.2.2:3000/" + receiveMsg.getText() + "/msg");
            }
        });
    }

    public static String POSTdeleteAccount(String url) {
        InputStream inputStream = null;
        String result = "";
        try {
            HttpClient httpclient = new DefaultHttpClient();

            HttpDelete httpDelete = new HttpDelete(url);

            HttpResponse httpResponse = httpclient.execute(httpDelete);

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

    public static String getPubKey(String url) {
        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpGet httpGet = new HttpGet(url);

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpGet);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    public static String POSTMessage(String posturl, String postmsg, String postusername, String postrecipient) {
        String url = posturl;
        String msg = postmsg;
        byte[] msgbyte = msg.getBytes();
        String username = postusername;
        String recipient = postrecipient;
        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpDelete httpDelete = new HttpDelete(url);

            String json = "";

            byte[] nachricht = msg.getBytes();
            String pubKey = getPubKey("http://10.0.2.2:3000/" + recipient + "/pubkey");
            byte[] pubkey_recipient = pubKey.getBytes();

            //KeyPair bilden
            KeyGenerator kg = null;

            //KeyPairGenerator erzeugen --> Algorithmus: RSA 2048
            kg = KeyGenerator.getInstance("AES");
            SecureRandom securerandom = new SecureRandom();
            byte bytes[] = new byte[20];
            securerandom.nextBytes(bytes);
            kg.init(128, securerandom);
            SecretKey key_recipient_secret = kg.generateKey();
            byte[] key_recipient = key_recipient_secret.getEncoded();

            //Initialisierungsvektor erzeugen
            final Random r = new SecureRandom();
            byte[] iv = new byte[16];
            r.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.WRAP_MODE, key_recipient_secret);
            //byte[] privkey_user_enc = cipher.wrap(msgbyte, pubkey_recipient, iv);
            //Nachricht verschlüsseln
            // byte[] cipher = encryptAESCBC(nachricht, pubkey_recipient, iv, key_recipient_secret);
            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpDelete);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if (inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
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

    private class DeleteAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return POSTdeleteAccount(params[0]);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(intent);
        }
    }

    private class ReceiveAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return null;//GETMessage(params[0], params[1]);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(intent);
        }
    }

    private class SendAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return null; //POSTMessage(params[0], params[1], params[2]);
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getBaseContext(), LoginActivity.class);
            startActivity(intent);

        }
    }
}
