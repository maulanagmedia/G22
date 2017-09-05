package gmedia.net.id.gmediatvclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import gmedia.net.id.gmediatvclient.Service.RemoteService;

public class MainActivity extends AppCompatActivity {

    private Button btnServer;
    private Button btnClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
    }

    private void initUI() {

        btnServer = (Button) findViewById(R.id.btn_server);
        btnClient = (Button) findViewById(R.id.btn_client);

        btnServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, ServerActivity.class);
                startActivity(intent);
            }
        });

        btnClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, ClientActivity.class);
                startActivity(intent);
            }
        });

        //startService(new Intent(MainActivity.this, RemoteService.class));
    }
}
