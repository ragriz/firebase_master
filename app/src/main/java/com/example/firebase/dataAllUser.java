package com.example.firebase;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class dataAllUser extends RecyclerView.Adapter<dataAllUser.ViewHolder>{
    private ArrayList<dataUser> AlDataUser;
    private LayoutInflater layoutInflater;
    public static RecyclerViewClickListener itemListener;
    public dataAllUser(Context context, ArrayList<dataUser> AlDataUser, RecyclerViewClickListener itemListener){
        this.AlDataUser = AlDataUser;
        layoutInflater = LayoutInflater.from(context);
        this.itemListener = itemListener;
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView tvNama, tvEmail;
        //public ProgressBar progressBar;
        public ViewHolder(View view){
            super(view);
            tvNama = view.findViewById(R.id.tvNama_InflaterDataUser);
            tvEmail = view.findViewById(R.id.tvEmail_InflaterDataUser);
            view.setOnClickListener(this);
        }
        @Override
        public void onClick(View v){
            itemListener.recylerViewListClicked(v, this.getAdapterPosition());
        }
    }
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.inflater_data_user, parent, false);
        int height = parent.getMeasuredHeight()/4;
        itemView.setMinimumHeight(height);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final dataUser user = AlDataUser.get(position);
        holder.tvNama.setText(user.getNama());
        holder.tvEmail.setText(user.getEmail());
    }
    @Override
    public int getItemCount(){
        return AlDataUser.size();
    }
    public interface RecyclerViewClickListener{
        void recylerViewListClicked(View v, int position);
    }
    public dataUser getItem(int position) {
        return AlDataUser.get(position);
    }
}
