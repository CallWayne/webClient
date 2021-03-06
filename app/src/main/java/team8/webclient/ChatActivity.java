package team8.webclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ChatActivity extends AppCompatActivity {
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        list = (ListView) findViewById(R.id.listView);
        //Auflistung der Nachrichten
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                Storage.getNachrichten() );

        list.setAdapter(arrayAdapter);
    }

    //Methode für das zurücksetzen der Nachrichten
    protected void onDestroy() {
        super.onDestroy();
        Storage.deleteNachrichten();
    }

}

