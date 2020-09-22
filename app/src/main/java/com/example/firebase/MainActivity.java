package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements dataAllUser.RecyclerViewClickListener {
    //firebase setup
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("users");

    Query myQuery = myRef;

    //adapter and data setup
    dataAllUser rvAdapter;
    ArrayList<dataUser> AlDataUser;

    //view
    TextView tv;
    Button btnSimpan, btnCari;
    EditText etNama, etEmail, etId, etCari;
    ProgressBar progressBar;
    RecyclerView recyclerView;

    //handler
    Handler mHandler;
    int LOADING = 100; //pesan handler mengenai progress bar
    int PENCARIAN = 101; //pesan handler mode pencarian

    //variabel
    DataSnapshot currentData; //snapshot untuk
    String cari;
    boolean dataSiap = false;
    boolean modePencarian = false;
    Dialog mDialog, mDialogUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //view id
        tv = (TextView)findViewById(R.id.tv);
        etNama = (EditText)findViewById(R.id.etNama);
        etEmail = (EditText)findViewById(R.id.etEmail);
        etCari = (EditText)findViewById(R.id.etCari);
        etId = (EditText)findViewById(R.id.etId);
        btnSimpan = (Button)findViewById(R.id.btnSimpan);
        btnCari = (Button)findViewById(R.id.btnCari);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        recyclerView = (RecyclerView)findViewById(R.id.rvUser);

        //deklarasi dialog
        mDialog = new Dialog(this);
        mDialogUpdate = new Dialog(this);

        //adapter and data setup
        AlDataUser = new ArrayList<dataUser>(); //deklarasi arraylist
        rvAdapter = new dataAllUser(this, AlDataUser, this); //deklarasi adapter recyclerview
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this); //deklarasi layoutmanager untuk recyclerview
        ((LinearLayoutManager) manager).setOrientation(RecyclerView.VERTICAL); //menentukan orientasi layout manager
        recyclerView.setLayoutManager(manager); //menentukan layoutmanager pada recyclerview
        recyclerView.hasFixedSize(); //memperbaiki ukuran recyclerview
        recyclerView.setAdapter(rvAdapter); //menentukan adapter recyclerview

        //handler
        mHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what == LOADING){ //jika objek pesan adalah mengenai progressbar maka
                    if(msg.arg1==1){
                        dataSiap = true;
                        progressBar.setVisibility(View.GONE);
                    }else{
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }
                if(msg.what == PENCARIAN){ //jika objek pesan adalah mengenai pencarian maka
                    if(msg.arg1==1){
                        modePencarian = true;
                        //AlDataUser.clear();
                        //rvAdapter.notifyDataSetChanged();
                        Snackbar.make(findViewById(R.id.mConstraintLayout), "Pencarian aktif!", Snackbar.LENGTH_SHORT).show();
                    }else{

                    }
                }
            }
        };

        //listener button
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nama = etNama.getText().toString();
                String email = etEmail.getText().toString();
                String id = etId.getText().toString();
                if(!nama.equals("")&&!email.equals("")&&!id.equals("")){
                    if(dataSiap){ //memerika apakah data sudah dimuat
                        if(!currentData.hasChild(id)){ //jika child tidak ada yang sama didalam database maka
                            /*  id child(sebagai baris) pertama untuk membuat id, didalam id buat lagi child(sebagai kolom) */
                            myRef.child(id).child("nama").setValue(nama); //myRef.child(baris).child(data).setValue(isiData);
                            myRef.child(id).child("email").setValue(email); //myRef.child(baris).child(data).setValue(isiData);
                            etEmail.setText(""); //mengosongkan kembali edittext
                            etId.setText(""); //mengosongkan kembali edittext
                            etNama.setText(""); //mengosongkan kembali edittext
                            Snackbar.make(findViewById(R.id.mConstraintLayout), "Data ditambahkan!", Snackbar.LENGTH_SHORT).show();
                            etId.hasFocus(); //mengalihkan kursor ke editext id
                        }else{ //jika child sudah ada maka
                            Snackbar.make(findViewById(R.id.mConstraintLayout), "ID Sudah ada", Snackbar.LENGTH_SHORT).show();
                        }
                    }else{ //jika data belum selesai dimuat maka . . .
                        Snackbar.make(findViewById(R.id.mConstraintLayout), "Data belum siap!", Snackbar.LENGTH_SHORT).show();
                    }
                }else{ //jika salah satu edittext kosong maka . . .
                    Snackbar.make(findViewById(R.id.mConstraintLayout), "Nama dan email harus diisi!", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        btnCari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cari = etCari.getText().toString();
                if(!cari.equals("")){
                    mHandler.obtainMessage(PENCARIAN, 1, -1).sendToTarget();
                    recyclerViewPencarian(currentData);
                }else{
                    Snackbar.make(findViewById(R.id.mConstraintLayout), "Kotak pencarian harus diisi!", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        //listener database
        myQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentData = dataSnapshot; //dibutuhkan pada saat penambahan data
                if(!modePencarian){
                    recyclerViewUpdate(dataSnapshot); //perbarui data
                }else{
                    recyclerViewPencarian(dataSnapshot); //recyclerview mode pencarian
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(findViewById(R.id.mConstraintLayout), "Gagal memuat data", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void recyclerViewUpdate(DataSnapshot dataSnapshot){
        AlDataUser.clear(); //membersihkan arraylist
        for(DataSnapshot ds : dataSnapshot.getChildren()){ //perulangan pada saat data memuat child
            dataUser user = new dataUser(); //deklarasikan class data yang bersesuaian / sebagai wadah data
            user.setId(ds.getKey()); //setter untuk id
            String nama ="", email="";
            if(ds.hasChild("nama")){ //untuk menghindari error null
                nama = ds.child("nama").getValue().toString(); //mendapatkan value berdasarkan keychild
            }
            if(ds.hasChild("email")){ //untuk menghindari error null
                email = ds.child("email").getValue().toString(); //mendapatkan value kedua berdasarkan keychild
            }
            user.setNama(nama); //setter class data
            user.setEmail(email); //setter class data
            AlDataUser.add(user); //menambahkan class data pada arraylist
        }
        mHandler.obtainMessage(LOADING, 1, -1).sendToTarget(); //memberikan pesan kepada handler bahwa proses memuat telah selesai
        rvAdapter.notifyDataSetChanged(); //memberi tahu adapter recyclerview bahwa data telah diubah(lakukan refresh data)
    }

    private void recyclerViewPencarian(DataSnapshot dataSnapshot){
        AlDataUser.clear(); //membersihkan arraylist
        for(DataSnapshot ds : dataSnapshot.getChildren()) { //perulangan pada saat data memuat child
            boolean pencarianSesuai = false; //patokan bahwa data bersesuaian dengan pencarian
            dataUser user = new dataUser(); //deklarasikan class data yang bersesuaian / sebagai wadah data
            user.setId(ds.getKey()); //setter untuk id
            String nama ="", email="";
            if(ds.hasChild("nama")){ //untuk menghindari error null
                if(ds.child("nama").getValue().toString().contains(cari)){
                    //nama = ds.child("nama").getValue().toString(); //mendapatkan value berdasarkan keychild
                    pencarianSesuai = true;
                }
            }
            if(ds.hasChild("email")){ //untuk menghindari error null
                if(ds.child("email").getValue().toString().contains(cari)){
                    //email = ds.child("email").getValue().toString(); //mendapatkan value kedua berdasarkan keychild
                    pencarianSesuai = true;
                }
            }
            if(pencarianSesuai){
                nama = ds.child("nama").getValue().toString(); //mendapatkan value berdasarkan keychild
                email = ds.child("email").getValue().toString(); //mendapatkan value kedua berdasarkan keychild
                user.setNama(nama); //setter class data
                user.setEmail(email); //setter class data
                AlDataUser.add(user); //menambahkan class data pada arraylist
            }
        }
        rvAdapter.notifyDataSetChanged(); //memberi tahu adapter recyclerview bahwa data telah diubah(lakukan refresh data)
    }

    @Override
    public void recylerViewListClicked(View v, int position) {
        final dataUser user = AlDataUser.get(position);

        //Dialog
        Button btnUpdate, btnHapus;
        mDialog.setContentView(R.layout.inflater_dialog); //menentukan xml inflater
        btnHapus = (Button)mDialog.findViewById(R.id.btnHapusInflaterDialog);
        btnUpdate = (Button)mDialog.findViewById(R.id.btnUpdateInflaterDialog);
        //listener
        btnHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef.child(user.getId()).removeValue(); //menghapus data
                mDialog.dismiss(); //menutup dialog
                Snackbar.make(findViewById(R.id.mConstraintLayout), "ID "+user.getId()+" Telah dihapus", Snackbar.LENGTH_SHORT).show();
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogUpdate.show();
                mDialog.dismiss();
            }
        });
        mDialog.show();

        //Dialog Update
        mDialogUpdate.setContentView(R.layout.inflater_dialog_update); //menentukan inflater xml
        final EditText etNama = (EditText)mDialogUpdate.findViewById(R.id.etNamaInflaterDialogUpdate);
        final EditText etEmail = (EditText)mDialogUpdate.findViewById(R.id.etEmailInflaterDialogUpdate);
        etNama.setText(user.getNama()); //mengisi edittext sesuai dengan data
        etEmail.setText(user.getEmail()); //mengisi edittext sesuai dengan data
        Button btnUpdateDialog = (Button)mDialogUpdate.findViewById(R.id.btnUpdateInflaterDialogUpdate);
        btnUpdateDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nama = etNama.getText().toString();
                String email = etEmail.getText().toString();
                if(!nama.equals("")||!email.equals("")){ //mengecek salah satu editext kosong
                    myRef.child(user.getId()).child("nama").setValue(nama); //mengupdate nama
                    myRef.child(user.getId()).child("email").setValue(email); //mengupdate email
                    Snackbar.make(findViewById(R.id.mConstraintLayout), " Data telah diupdate!", Snackbar.LENGTH_SHORT).show();
                    mDialogUpdate.dismiss(); //menutup dialog
                }else{ //jika salah satu editext kosong maka
                    Snackbar.make(findViewById(R.id.mConstraintLayout), " Nama dan email tidak boleh kosong!", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }
}
