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



public class LoginActivity extends AppCompatActivity {
    EditText login;
    String username;
    Button btn_login;
    Button btn_tosignup;
    TextView answer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = (EditText) findViewById(R.id.editText_login);
        username = String.valueOf(login.getText());
        answer = (TextView) findViewById(R.id.textView_login);
        btn_login = (Button) findViewById(R.id.button_login);
        btn_tosignup = (Button) findViewById(R.id.button_tosignup);
        btn_login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(login != null) {
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
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
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

                if(result.equals("400")){
                    Toast.makeText(getBaseContext(), "Bad Request 400!", Toast.LENGTH_LONG).show();
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
