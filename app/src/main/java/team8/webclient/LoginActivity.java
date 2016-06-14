package team8.webclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.*;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    EditText login;
    Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = (EditText) findViewById(R.id.editText_login);
        btn_login = (Button) findViewById(R.id.button_login);
        btn_login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(login != null) {
                    String url ="http://localhost:3000/"+login.getText();
                    // Request a string response from the provided URL.
                    JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Intent i = new Intent(LoginActivity.this, ChatActivity.class);
                                    try {
                                        KryptoChat kC = (KryptoChat) getApplication();
                                        kC.setPubkey(response.getString("pubkey_user"));
                                        kC.setPrivkey_user_enc(response.getString("privkey_user_enc"));
                                        kC.setUserName(login.getText().toString());
                                        kC.setSalt_masterkey(response.getString("salt_masterkey"));
                                        startActivity(i);
                                    } catch (Exception e) {e.printStackTrace();}
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
            }
        });
    }
}
