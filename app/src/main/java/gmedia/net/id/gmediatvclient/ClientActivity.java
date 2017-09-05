package gmedia.net.id.gmediatvclient;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gmedia.net.id.gmediatvclient.Adapter.ConnectionListAdapter;
import gmedia.net.id.gmediatvclient.Utils.DataTransferService;
import gmedia.net.id.gmediatvclient.Utils.ITransferable;
import gmedia.net.id.gmediatvclient.Utils.SelectedServer;
import gmedia.net.id.gmediatvclient.Utils.ServiceUtils;

import static android.R.attr.data;
import static android.R.attr.host;

public class ClientActivity extends AppCompatActivity {

    private InetAddress hostAddress;
    private int hostPort;
    private NsdManager mNsdManager;
    private final String TAG = "Client";
    private RecyclerView rvListConnection;
    private List<CustomItem> masterList = new ArrayList<>();
    private ItemValidation iv = new ItemValidation();
    private static final String REQUEST_CONNECT_CLIENT = "request-connect-client";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        masterList = new ArrayList<>();
        initUI();

        // NSD Stuff
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        mNsdManager.discoverServices(ServiceUtils.SERVICE_TYPE,
                NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);

    }

    private void initUI() {

        rvListConnection = (RecyclerView) findViewById(R.id.rv_list_connection);

        setListConnection(masterList);

    }

    private void setListConnection(List<CustomItem> listItem){

        rvListConnection.setAdapter(null);

        if(listItem != null){

            final ConnectionListAdapter menuAdapter = new ConnectionListAdapter(ClientActivity.this, listItem);

            final RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(ClientActivity.this, 1);
            rvListConnection.setLayoutManager(mLayoutManager);
//        rvListMenu.addItemDecoration(new NavMenu.GridSpacingItemDecoration(2, dpToPx(10), true));
            rvListConnection.setItemAnimator(new DefaultItemAnimator());
            rvListConnection.setAdapter(menuAdapter);
        }
    }

    @Override
    protected void onPause() {
        if (mNsdManager != null) {
            try {
                mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNsdManager != null) {

            try {
                mNsdManager.discoverServices(
                        ServiceUtils.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
            }catch (Exception e){
                e.printStackTrace();

            }
        }

    }

    @Override
    protected void onDestroy() {
        if (mNsdManager != null) {
            try {
                mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }


    NsdManager.DiscoveryListener mDiscoveryListener = new NsdManager.DiscoveryListener() {

        // Called as soon as service discovery begins.
        @Override
        public void onDiscoveryStarted(String regType) {
            Log.d(TAG, "Service discovery started");
        }

        @Override
        public void onServiceFound(NsdServiceInfo service) {
            // A service was found! Do something with it.
            Log.d(TAG, "Service discovery success : " + service);
            Log.d(TAG, "Host = "+ service.getServiceName());
            Log.d(TAG, "port = " + String.valueOf(service.getPort()));

            if (!service.getServiceType().equals(ServiceUtils.SERVICE_TYPE)) {
                // Service type is the string containing the protocol and
                // transport layer for this service.
                Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
            } else if (service.getServiceName().equals(ServiceUtils.SERVICE_NAME)) {
                // The name of the service tells the user what they'd be
                // connecting to. It could be "Bob's Chat App".

                Log.d(TAG, "Same machine ip: " + service.getHost());
                Log.d(TAG, "Same machine: " + ServiceUtils.SERVICE_NAME);
            } else {
                Log.d(TAG, "Diff Machine : " + service.getServiceName());
                // connect to the service and obtain serviceInfo
                /*try {
                    mNsdManager.resolveService(service, mResolveListener);
                }catch (Exception e){
                    e.printStackTrace();
                }*/
            }

            try {
                mNsdManager.resolveService(service, mResolveListener);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo service) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            Log.e(TAG, "service lost" + service);
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
            Log.i(TAG, "Discovery stopped: " + serviceType);
        }

        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            mNsdManager.stopServiceDiscovery(this);
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            mNsdManager.stopServiceDiscovery(this);
        }
    };

    NsdManager.ResolveListener mResolveListener = new NsdManager.ResolveListener() {

        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            // Called when the resolve fails. Use the error code to debug.
            Log.e(TAG, "Resolve failed " + errorCode);
            Log.e(TAG, "serivce = " + serviceInfo);
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            Log.d(TAG, "Resolve Succeeded. " + serviceInfo);

            if (serviceInfo.getServiceName().equals(ServiceUtils.SERVICE_NAME)) {
                Log.d(TAG, "Same IP.");
                addData(serviceInfo);
                return;
            }

            // Obtain port and IP
            hostPort = serviceInfo.getPort();
            hostAddress = serviceInfo.getHost();
        }
    };

    private void addData(NsdServiceInfo service){

        boolean alreadySaved = false;
        for (CustomItem item: masterList){

            String ipService = "";
            if(service.getHost() != null) ipService = service.getHost().getHostAddress();
            if(ipService.equals(item.getItem1()) && service.getServiceName().equals(item.getItem2())){

                alreadySaved = true;
                break;
            }
        }

        if(!alreadySaved){

            String ip = "";
            if(service.getHost() != null){
                ip = service.getHost().getHostAddress();
            }

            final CustomItem itemToAdd = new CustomItem(ip, service.getServiceName().toString(), String.valueOf(service.getPort()),service.getServiceType());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ConnectionListAdapter adapter = (ConnectionListAdapter) rvListConnection.getAdapter();
                    adapter.addMoreData(itemToAdd);
                }
            });
        }
    }

    // =============================== CLIENT RECEIVER ==============================================

    private void connectToHost() {

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
            jsonData.put("request", REQUEST_CONNECT_CLIENT);
            jsonData.put("ipAddress", ipAddress);
            jsonData.put("keyCode", "22");
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
                Toast.makeText(ClientActivity.this, "Connection Done", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ClientActivity.this, "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
