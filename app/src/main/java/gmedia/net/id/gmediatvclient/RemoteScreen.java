package gmedia.net.id.gmediatvclient;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import gmedia.net.id.gmediatvclient.Adapter.ConnectionListAdapter;
import gmedia.net.id.gmediatvclient.Utils.SelectedServer;
import gmedia.net.id.gmediatvclient.Utils.ServiceUtils;

public class RemoteScreen extends AppCompatActivity {

    private ImageView ivLeft, ivUp, ivRight, ivDown;
    private LinearLayout llOk, llMenu, llBack;
    private ItemValidation iv = new ItemValidation();
    private InetAddress hostAddress;
    private int hostPort;
    private final String TAG = "remote";
    private LinearLayout ll0, ll1, ll2, ll3, ll4, ll5, ll6, ll7, ll8, ll9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_screen);

        initUI();
    }

    private void initUI() {

        ivLeft = (ImageView) findViewById(R.id.turn_left);
        ivUp = (ImageView) findViewById(R.id.turn_up);
        ivRight = (ImageView) findViewById(R.id.turn_right);
        ivDown = (ImageView) findViewById(R.id.turn_down);
        llOk =(LinearLayout) findViewById(R.id.turn_ok);
        llMenu =(LinearLayout) findViewById(R.id.turn_menu);
        llBack =(LinearLayout) findViewById(R.id.turn_back);
        ll0 =(LinearLayout) findViewById(R.id.turn_0);
        ll1 =(LinearLayout) findViewById(R.id.turn_1);
        ll2 =(LinearLayout) findViewById(R.id.turn_2);
        ll3 =(LinearLayout) findViewById(R.id.turn_3);
        ll4 =(LinearLayout) findViewById(R.id.turn_4);
        ll5 =(LinearLayout) findViewById(R.id.turn_5);
        ll6 =(LinearLayout) findViewById(R.id.turn_6);
        ll7 =(LinearLayout) findViewById(R.id.turn_7);
        ll8 =(LinearLayout) findViewById(R.id.turn_8);
        ll9 =(LinearLayout) findViewById(R.id.turn_9);

        ivLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToHost("21");
            }
        });

        ivUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToHost("19");
            }
        });

        ivRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToHost("22");
            }
        });

        ivDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToHost("20");
            }
        });

        llOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToHost("23");
            }
        });

        llMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToHost("32");
            }
        });

        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToHost("4");
            }
        });

        ll0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToHost("7");
            }
        });

        ll1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToHost("8");
            }
        });

        ll2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToHost("9");
            }
        });

        ll3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToHost("10");
            }
        });

        ll4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToHost("11");
            }
        });

        ll5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToHost("12");
            }
        });

        ll6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToHost("13");
            }
        });

        ll7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToHost("14");
            }
        });

        ll8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToHost("15");
            }
        });

        ll9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToHost("16");
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: "+ keyCode);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        try {
            clearConnectedHost();
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        try {
            clearConnectedHost();
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void connectToHost(String keyCode) {

        try {
            hostAddress = InetAddress.getByName(SelectedServer.host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        hostPort = iv.parseNullInteger(SelectedServer.port);

        if (hostAddress == null) {
            Log.e(TAG, "Host Address is null");
            return;
        }

        String ipAddress = getMyIPAddress(true);
        JSONObject jsonData = new JSONObject();

        try {
            jsonData.put("request", ServiceUtils.REQUEST_CONNECT_CLIENT);
            jsonData.put("ipAddress", ipAddress);
            jsonData.put("keyCode", keyCode);
            jsonData.put("type", "");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "can't put request");
            return;
        }

        new SocketServerTask().execute(jsonData);
    }

    private static String getMyIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;
                        //Log.d(TAG, "getIPAddress: "+ addr.getHostName());
                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }

    private class SocketServerTask extends AsyncTask<JSONObject, Void, Void> {
        private JSONObject jsonData;
        private boolean success;

        @Override
        protected Void doInBackground(JSONObject... params) {
            Socket socket = null;
            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;
            jsonData = params[0];

            try {

                // Create a new Socket instance and connect to host
                try {
                    socket = new Socket(SelectedServer.host, hostPort);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());

                // transfer JSONObject as String to the server
                dataOutputStream.writeUTF(jsonData.toString());
                Log.i(TAG, "waiting for response from host");

                // Thread will wait till server replies
                String response = dataInputStream.readUTF();
                if (response != null && response.equals("Connection Accepted")) {
                    success = true;
                } else {
                    success = false;
                }

            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            } finally {

                // close socket
                if (socket != null) {
                    try {
                        Log.i(TAG, "closing the socket");
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // close input stream
                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // close output stream
                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (success) {
                //Toast.makeText(ClientActivity.this, "Connection Done", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "success remote: "+ result);
            } else {
                Log.d(TAG, "failed remote: "+ result);
                //Toast.makeText(ClientActivity.this, "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void clearConnectedHost() {

        InetAddress hostAddress = null;

        try {
            hostAddress = InetAddress.getByName(SelectedServer.host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        hostPort = iv.parseNullInteger(SelectedServer.port);

        if (hostAddress == null) {
            Log.e(TAG, "Host Address is null");
            return;
        }

        String ipAddress = getMyIPAddress(true);
        JSONObject jsonData = new JSONObject();

        try {
            jsonData.put("request", ServiceUtils.REQUEST_CONNECT_CLIENT);
            jsonData.put("ipAddress", ipAddress);
            jsonData.put("keyCode", "");
            jsonData.put("type", "clear_connection");

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "can't put request");
            return;
        }

        new SocketServerTask2().execute(jsonData);
    }

    private class SocketServerTask2 extends AsyncTask<JSONObject, Void, Void> {

        private JSONObject jsonData;
        private boolean success;
        private boolean linked = false;

        @Override
        protected Void doInBackground(JSONObject... params) {
            Socket socket = null;
            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;
            jsonData = params[0];

            try {

                // Create a new Socket instance and connect to host
                try {
                    socket = new Socket(SelectedServer.host, hostPort);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());

                // transfer JSONObject as String to the server
                dataOutputStream.writeUTF(jsonData.toString());
                Log.i(TAG, "waiting for response from host");

                // Thread will wait till server replies
                String response = dataInputStream.readUTF();
                if (response != null && response.equals("1")) {
                    success = true;
                } else {
                    success = false;
                    linked = true;
                }

            } catch (IOException e) {
                e.printStackTrace();
                success = false;
                linked = false;
            } finally {

                // close socket
                if (socket != null) {
                    try {
                        Log.i(TAG, "closing the socket");
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // close input stream
                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // close output stream
                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            if (success) {

                Log.d(TAG, "Clear Connection succes : "+result);
            } else {
                Log.d(TAG, "Clear Connection failed : "+result);
            }
        }
    }

    @Override
    public void onBackPressed() {

        try {
            clearConnectedHost();
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onBackPressed();
    }
}
