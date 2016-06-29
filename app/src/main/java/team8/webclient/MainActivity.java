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
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    Button btn_send;
    Button btn_receive;
    Button btn_delete;
    Button btn_logout;
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
        sendMsg = (EditText) findViewById(R.id.editText_send_Msg);
        sendRecipient = (EditText) findViewById(R.id.editText_sendRecipient);
        btn_logout = (Button) findViewById(R.id.button_logout);

        //wenn der Button Logout gedrückt wird
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Storage.deleteNachrichten();
                Toast.makeText(getBaseContext(), "Logout", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        //wenn der Button Senden gedrückt wird
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendAsyncTask sendAsyncTask = new SendAsyncTask();
                sendAsyncTask.execute("http://10.0.2.2:3000/"+Storage.getUsername()+"/msg", sendMsg.getText().toString(), sendRecipient.getText().toString());
            }
        });

        //wenn der Button Empfangen gedrückt wird
        btn_receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReceiveAsyncTask receiveAsyncTask = new ReceiveAsyncTask();
                receiveAsyncTask.execute("http://10.0.2.2:3000/"+ Storage.getUsername()+"/msg");
            }
        });

        //wenn der Button Löschen gedrückt wird
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteAsyncTask deleteAsyncTask = new DeleteAsyncTask();
                deleteAsyncTask.execute("http://10.0.2.2:3000/" + Storage.getUsername());
            }
        });
    }

    //Methode zum Löschen des Users
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

    //Methode zum abholen des PublicKeys
    public static String getPubKey(String url) {
        InputStream inputStream = null;
        String result = "";
        try {

            HttpClient httpclient = new DefaultHttpClient();

            HttpGet httpGet = new HttpGet(url);

            HttpResponse httpResponse = httpclient.execute(httpGet);

            inputStream = httpResponse.getEntity().getContent();

            if (inputStream != null){
                result = convertInputStreamToString(inputStream);
                JSONObject reader = new JSONObject(result);
                result = reader.getString("pubkey_user");
            }
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    //Methode zum Empfangen von Nachrichten
    public static String GETMessage(String url) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        ArrayList<Message> msg= new ArrayList<Message>();
        InputStream inputStream = null;
        String result = "";
        if(!url.endsWith("?"))
            url += "?";
            List<NameValuePair> params = new LinkedList<NameValuePair>();

            //Unix Timestamp erzeugen
            long unixTime = System.currentTimeMillis() / 1000L;
            String strTime = Long.toString(unixTime);

            //Username vom eingeloggten User besorgen
            String username = Storage.getUsername();

            //Timestamp und Username an die URL als Parameter hinzufügen
            params.add(new BasicNameValuePair("timestamp", strTime));
            params.add(new BasicNameValuePair("login", username));

            //PrivateKey wiederherstellen
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Storage.getPrivkey());
            KeyFactory generator = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = generator.generatePrivate(privateKeySpec);

            //Digitale Signatur sig_service erstellen
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(privateKey);
            String Umschlag = new String(strTime+username);
            sig.update(Umschlag.getBytes());
            byte[] sig_service = sig.sign();
            String sig_serviceString = Base64.encodeToString(sig_service, Base64.DEFAULT);

            //Sig_service an die URL als Parameter hinzufügen
            params.add(new BasicNameValuePair("sig_service", sig_serviceString));

            String paramString = URLEncodedUtils.format(params, "utf-8");

            url += paramString;
        try {

            HttpClient httpclient = new DefaultHttpClient();

            HttpGet httpGet = new HttpGet(url);

            HttpResponse httpResponse = httpclient.execute(httpGet);

            inputStream = httpResponse.getEntity().getContent();

            //JSON Objekt empfangen und auslesen und in einem Message Objekt speichern
            if (inputStream != null){
                result = convertInputStreamToString(inputStream);
                JSONArray reader = new JSONArray(result);
                for(int i=0; reader.length()>i; i++){
                    byte[] sender = reader.getJSONObject(i).getString("sender").getBytes();
                    byte[] content_enc = Base64.decode(reader.getJSONObject(i).getString("content_enc"), Base64.DEFAULT);
                    byte[] iv = Base64.decode(reader.getJSONObject(i).getString("iv"), Base64.DEFAULT);
                    byte[] key_recipient_enc = Base64.decode(reader.getJSONObject(i).getString("key_recipient_enc"), Base64.DEFAULT);
                    byte[] sig_recipient = Base64.decode(reader.getJSONObject(i).getString("sig_recipient"), Base64.DEFAULT);
                    byte[] created_at = reader.getJSONObject(i).getString("created_at").getBytes();
                    Message message1 = new Message(sender, content_enc, iv, key_recipient_enc, sig_recipient, created_at);
                    msg.add(message1);
                }

                //einzelne Message Objekte auslesen und entschlüsseln
                for(int i = 0; msg.size()>i; i++){
                    Message message2= msg.get(i);
                    byte[] sender = message2.getSender();
                    String senderString = new String(sender);
                    byte[] content_enc = message2.getContent_enc();
                    byte[] created_at = message2.getCreated_at();
                    byte[] key_recipient_enc = message2.getKey_recipient_enc();
                    byte[] iv = message2.getIv();

                    //Pubkey des Senders abholen
                    String pubKey = getPubKey("http://10.0.2.2:3000/" + senderString + "/pubkey");
                    byte[] pubKeyByte = Base64.decode(pubKey, Base64.DEFAULT);
                    //Pubkey erstellen als Key
                    PublicKey masterkey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubKeyByte));

                    //Signaur des Empfängers
                    byte[] sig_recipient = message2.getSig_recipient();
                    String digiSignature = new String(senderString + Base64.encodeToString(content_enc, Base64.DEFAULT) + Base64.encodeToString(iv, Base64.DEFAULT) + Base64.encodeToString(key_recipient_enc, Base64.DEFAULT));

                    //Signatur aus masterkey erstellen
                    Signature s1 = Signature.getInstance("SHA256withRSA");
                    s1.initVerify(masterkey);
                    s1.update(digiSignature.getBytes());

                    //Signatur prüfen
                    if(s1.verify(sig_recipient)){
                        //PrivateKey herstellen
                        EncodedKeySpec privateKeySpec1 = new PKCS8EncodedKeySpec(Storage.getPrivkey());
                        KeyFactory generator1 = KeyFactory.getInstance("RSA");
                        PrivateKey privateKey1 = generator1.generatePrivate(privateKeySpec1);

                        //key_recipient_enc mit dem PrivateKey entschlüssen
                        Cipher cipher1 = Cipher.getInstance("RSA");
                        cipher1.init(Cipher.DECRYPT_MODE, privateKey1);
                        byte[] key_recipient = cipher1.doFinal(key_recipient_enc);

                        //ivSpec erstellen
                        IvParameterSpec ivSpec = new IvParameterSpec(iv);

                        //key_recipient_enc erstellen
                        SecretKey key_recipient_enc1 = new SecretKeySpec(key_recipient, 0, key_recipient.length, "AES");

                        //content_enc mit key_recipient und ivSpec entschlüssen
                        Cipher cipher2 = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        cipher2.init(Cipher.DECRYPT_MODE, key_recipient_enc1, ivSpec);
                        byte[] clearTextByte = cipher2.doFinal(content_enc);
                        String nachricht = new String(clearTextByte);
                        Storage.setNachrichten(nachricht);
                    }
                }
            }
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return null;
    }

    //Methode für das versenden von Nachrichten
    public static String POSTMessage(String posturl, String postmsg, String postrecipient) {
        String url = posturl;
        String msg = postmsg;
        byte[] msgbyte = msg.getBytes();
        String username = Storage.getUsername();
        String recipient = postrecipient;
        InputStream inputStream = null;
        String result = "";
        try {

            HttpClient httpclient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost(url);

            String json = "";

            //Inhalt der Nachricht abrufen
            byte[] nachricht = msg.getBytes();

            //PublicKey vom Empfänger abholen
            String pubKey = getPubKey("http://10.0.2.2:3000/" + recipient + "/pubkey");
            byte[] pubKeyByte = Base64.decode(pubKey, Base64.DEFAULT);

            //KeyPair bilden
            KeyGenerator kg = null;

            //KeyPairGenerator erzeugen
            kg = KeyGenerator.getInstance("AES");
            SecureRandom securerandom = new SecureRandom();
            byte bytes[] = new byte[20];
            securerandom.nextBytes(bytes);
            kg.init(128, securerandom);
            SecretKey key_recipient_secret = kg.generateKey();
            byte[] key_recipient = key_recipient_secret.getEncoded();

            //Initialisierungsvektor erzeugen
            byte[] iv = null;
            IvParameterSpec ivspec = null;
            String ivString = "";
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            iv = new byte[16];
            sr.nextBytes(iv);
            ivString = Base64.encodeToString(iv, Base64.DEFAULT);
            ivspec = new IvParameterSpec(iv);

            //Nachricht verschlüssen mit key_recipient_secret und ivSpec
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key_recipient_secret, ivspec);
            byte [] clearText = msg.getBytes("UTF-8");
            byte [] content_enc = cipher.doFinal(clearText);
            String content_encString = Base64.encodeToString(content_enc, Base64.DEFAULT);

            //Verschlüsselung des Key_Recipient_Secret mithilfe von pubkey_recipient
            PublicKey masterkey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubKeyByte));
            Cipher cipherkey = Cipher.getInstance("RSA");
            cipherkey.init(Cipher.ENCRYPT_MODE, masterkey);
            byte [] key_recipient_enc = cipherkey.doFinal(key_recipient);
            String key_recipient_encString = Base64.encodeToString(key_recipient_enc, Base64.DEFAULT);

            //Digitale Signatur erstellen mit SHA-256 und privkey_user über Identität, Cipher, IV, key_recipient_enc
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Storage.getPrivkey());
            KeyFactory generator = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = generator.generatePrivate(privateKeySpec);
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(privateKey);
            String innererUmschlag = new String(username + content_encString + ivString + key_recipient_encString);
            sig.update(innererUmschlag.getBytes());
            byte[] sig_recipient = sig.sign();
            String sig_recipientString = Base64.encodeToString(sig_recipient, Base64.DEFAULT);

            //Timestamp erzeugen
            long unixTime = System.currentTimeMillis() / 1000L;
            String strTime = Long.toString(unixTime);


            //Digitale Signatur  mit Hilfe SHA-256 und privkey_user über innerenUmschlag, timestamp, Empfänger
            Signature sig1 = Signature.getInstance("SHA256withRSA");
            sig1.initSign(privateKey);
            String außererUmschlag = new String(sig_recipientString + strTime + recipient);
            sig.update(außererUmschlag.getBytes());
            byte[] sig_service = sig.sign();
            String sig_serviceString = Base64.encodeToString(sig_service, Base64.DEFAULT);

            //JSON Object zusammenbauen
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("login", username);
            jsonObject.accumulate("content_enc", content_encString);
            jsonObject.accumulate("iv", ivString);
            jsonObject.accumulate("key_recipient_enc", key_recipient_encString);
            jsonObject.accumulate("sig_service", sig_serviceString);
            jsonObject.accumulate("sig_recipient", sig_recipientString);
            jsonObject.accumulate("recipient", recipient);
            jsonObject.accumulate("timestamp", strTime);

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

    //AsyncTask für das Löschen des User
    private class DeleteAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return POSTdeleteAccount(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);
        }
    }

    //AsyncTask fürs Empfangen
    private class ReceiveAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                return GETMessage(params[0]);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (SignatureException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result == "Did not work!"){
                Toast.makeText(getBaseContext(), "Keine Nachrichten die letzen 5 Minuten", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getBaseContext(), "Data Receive!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getBaseContext(), ChatActivity.class);
                startActivity(intent);
            }

        }
    }

    //AsyncTask fürs Versenden
    private class SendAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            return POSTMessage(params[0], params[1], params[2]);
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);

        }
    }
}
