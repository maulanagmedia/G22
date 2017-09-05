package gmedia.net.id.gmediatvclient.Adapter;

/**
 * Created by Shin on 3/21/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
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

import gmedia.net.id.gmediatvclient.ClientActivity;
import gmedia.net.id.gmediatvclient.R;
import gmedia.net.id.gmediatvclient.RemoteScreen;
import gmedia.net.id.gmediatvclient.Utils.SelectedServer;
import gmedia.net.id.gmediatvclient.Utils.ServiceUtils;

public class ConnectionListAdapter extends RecyclerView.Adapter<ConnectionListAdapter.MyViewHolder> {

    private Context context;
    private List<CustomItem> masterList;
    public static int position = 0;
    private ItemValidation iv = new ItemValidation();
    public static int selectedPosition = 0;
    private String TAG = "Adapter";
    private int hostPort;
    private String hostIP;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public CardView cvContainer;
        public LinearLayout llContainer;
        public TextView tvItem1, tvItem2;

        public MyViewHolder(View view) {

            super(view);
            cvContainer = (CardView) view.findViewById(R.id.cv_container);
            llContainer = (LinearLayout) view.findViewById(R.id.ll_container);
            tvItem1 = (TextView) view.findViewById(R.id.tv_item1);
            tvItem2 = (TextView) view.findViewById(R.id.tv_item2);
        }
    }

    public ConnectionListAdapter(Context context, List masterlist){
        this.context = context;
        this.masterList = masterlist;
    }

    public void addMoreData(CustomItem item){

        masterList.add(item);
        notifyDataSetChanged();
    }

    public void clearData(){

        masterList = new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cv_list_server, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final CustomItem cli = masterList.get(position);

        holder.tvItem1.setText((masterList.size() > 1) ? "Gmedia TV " + (position + 1): "Gmedia TV");
        holder.tvItem2.setText(cli.getItem1() +" : "+cli.getItem3());

        holder.llContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*SelectedServer.host = cli.getItem1();
                SelectedServer.name = cli.getItem2();
                SelectedServer.port = cli.getItem3();
                SelectedServer.type = cli.getItem4();*/

                try {
                    connectToHost(cli);
                }catch (Exception e){
                    Snackbar.make(((Activity)context).findViewById(android.R.id.content), "TV Sedang Offline",
                            Snackbar.LENGTH_INDEFINITE).setAction("OK",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                }
                            }).show();

                    masterList.remove(position);
                    notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return masterList.size();
    }

    private void connectToHost(CustomItem item) {

        InetAddress hostAddress = null;
        try {
            hostAddress = InetAddress.getByName(item.getItem1());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        hostIP = item.getItem1();
        hostPort = iv.parseNullInteger(item.getItem3());

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
            jsonData.put("type", "request_connection");
            jsonData.put("host", item.getItem1());
            jsonData.put("name", item.getItem2());
            jsonData.put("port", item.getItem3());
            jsonData.put("typeconnection", item.getItem4());

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
                    socket = new Socket(hostIP, hostPort);
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

                try {
                    SelectedServer.host = jsonData.getString("host");
                    SelectedServer.name = jsonData.getString("name");
                    SelectedServer.port = jsonData.getString("port");
                    SelectedServer.type = jsonData.getString("typeconnection");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(context, RemoteScreen.class);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            } else {

                if(linked){

                    Snackbar.make(((Activity)context).findViewById(android.R.id.content), "TV telah terkoneksi device lain",
                            Snackbar.LENGTH_INDEFINITE).setAction("OK",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                }
                            }).show();
                }else{
                    Snackbar.make(((Activity)context).findViewById(android.R.id.content), "Tidak dapat terkoneksi ke TV",
                            Snackbar.LENGTH_INDEFINITE).setAction("OK",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                }
                            }).show();
                }
            }
        }
    }

}