package gmedia.net.id.gmediatvclient.Adapter;

/**
 * Created by Shin on 3/21/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.gmediatvclient.R;
import gmedia.net.id.gmediatvclient.Utils.SelectedServer;

public class ConnectionListAdapter extends RecyclerView.Adapter<ConnectionListAdapter.MyViewHolder> {

    private Context context;
    private List<CustomItem> masterList;
    public static int position = 0;
    private ItemValidation iv = new ItemValidation();
    public static int selectedPosition = 0;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public CardView cvContainer;
        public TextView tvItem1, tvItem2;

        public MyViewHolder(View view) {

            super(view);
            cvContainer = (CardView) view.findViewById(R.id.cv_container);
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

        holder.tvItem1.setText(cli.getItem2());
        holder.tvItem2.setText(cli.getItem1());

        holder.cvContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SelectedServer.host = cli.getItem1();
                SelectedServer.name = cli.getItem2();
                SelectedServer.port = cli.getItem3();
                SelectedServer.type = cli.getItem4();

                Toast.makeText(context, "Connected to: " + cli.getItem1(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return masterList.size();
    }

}