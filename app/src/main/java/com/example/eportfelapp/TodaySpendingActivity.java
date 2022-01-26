package com.example.eportfelapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;
import org.joda.time.Weeks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class TodaySpendingActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private TextView totalAmountSpentOn;
    private ProgressBar progressBar;
    private ProgressDialog loader;
    private volatile boolean cancelled;
    private Button scanBtn;

    private FirebaseAuth mAuth;
    private String onlineUserId = "";
    private DatabaseReference expensesRef;
    private DatabaseReference productsRef;

    private TodayItemsAdapter todayItemsAdapter;
    private List<Data> myDataList;
    private List<Products> myProductsList;
    private String resultScan;

    int isPassed = 0;
    int lastProductId = 0;
    int needToUpdateProductInfo = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_spending);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Today Spending");
        totalAmountSpentOn = findViewById(R.id.totalAmountSpentOn);
        progressBar = findViewById(R.id.progressBar);
        scanBtn = findViewById(R.id.scanBtn);
        scanBtn.setOnClickListener(this);

        fab = findViewById(R.id.fab);
        loader = new ProgressDialog(this);


        mAuth = FirebaseAuth.getInstance();
        onlineUserId = mAuth.getCurrentUser().getUid();
        expensesRef = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        productsRef = FirebaseDatabase.getInstance().getReference("products");


        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        myDataList = new ArrayList<>();
        myProductsList = new ArrayList<>();
        todayItemsAdapter = new TodayItemsAdapter(TodaySpendingActivity.this, myDataList);
        recyclerView.setAdapter(todayItemsAdapter);

        readItems();


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItemSpentOn();
            }
        });
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {

                            hello();

                    }


                },
                10000,10000
        );


    }

    private void hello(){
        System.out.println("sey hello");
    }

    private void showToast() {
        Toast.makeText(this, "No product in the database", Toast.LENGTH_SHORT).show();
    }

    private void readProducts(String resultScan) {
        try {
            long result = Long.parseLong(resultScan);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("products");
            Query query = reference.orderByChild("barcode").equalTo(result);

            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Products product = new Products();
                    myProductsList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                        Products data = dataSnapshot.getValue(Products.class);
                        myProductsList.add(data);
                        product = data;
                    }

                    if (!myProductsList.isEmpty()) {
                        String Amount = String.valueOf(product.getAmount());
                        String Item = product.getItem();
                        String notes = product.getNotes();

                        loader.setMessage("adding a budget item");
                        loader.setCanceledOnTouchOutside(false);
                        loader.show();

                        String id = expensesRef.push().getKey();
                        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                        Calendar cal = Calendar.getInstance();
                        String date = dateFormat.format(cal.getTime());

                        MutableDateTime epoch = new MutableDateTime();
                        epoch.setDate(0);
                        DateTime now = new DateTime();
                        Weeks weeks = Weeks.weeksBetween(epoch, now);
                        Months months = Months.monthsBetween(epoch, now);

                        String itemNday = Item + date;
                        String itemNweek = Item + weeks.getWeeks();
                        String itemNmonth = Item + months.getMonths();


                        Data data = new Data(Item, date, id, itemNday, itemNweek, itemNmonth, Integer.parseInt(Amount), weeks.getWeeks(), months.getMonths(), notes);
                        expensesRef.child(id).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    isPassed = 1;
                                    Toast.makeText(TodaySpendingActivity.this, "Budget item added successfuly", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(TodaySpendingActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }

                                loader.dismiss();
                            }
                        });

                        todayItemsAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);

                        int totalAmount = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Map<String, Object> map = (Map<String, Object>) ds.getValue();
                            Object total = map.get("amount");
                            int pTotal = Integer.parseInt(String.valueOf(total));
                            totalAmount += pTotal;

                            totalAmountSpentOn.setText("Total Day's Spending: PLN " + totalAmount);

                        }

                    } else {

                        showToast();
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception ex) {
            showToast();
        }
    }

    private void readItems() {

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("date").equalTo(date);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myDataList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Data data = dataSnapshot.getValue(Data.class);
                    myDataList.add(data);
                }
                todayItemsAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                int totalAmount = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Map<String, Object> map = (Map<String, Object>) ds.getValue();
                    Object total = map.get("amount");
                    int pTotal = Integer.parseInt(String.valueOf(total));
                    totalAmount += pTotal;

                    totalAmountSpentOn.setText("Total Day's Spending: PLN " + totalAmount);
                }
                if (totalAmount == 0){
                    totalAmountSpentOn.setText("Total Day's Spending: PLN " + totalAmount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateAmountRealtime(){
        System.out.println("harszla");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("products");
        Query query = reference.orderByChild("id");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Products product = new Products();
                myProductsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    Products data = dataSnapshot.getValue(Products.class);
                    myProductsList.add(data);
                    product = data;
                    lastProductId = product.getId() + 1;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addItemSpentOn() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.input_layout, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final Spinner itemSpinner = myView.findViewById(R.id.itemspinner);
        final EditText amount = myView.findViewById(R.id.amount);
        final EditText note = myView.findViewById(R.id.note);
        final EditText barcodeInput = myView.findViewById(R.id.barcode);

        final Button cancel = myView.findViewById(R.id.cancel);
        final Button save = myView.findViewById(R.id.save);

        note.setVisibility(View.VISIBLE);
        barcodeInput.setVisibility(View.VISIBLE);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String Amount = amount.getText().toString();
                String Item = itemSpinner.getSelectedItem().toString();
                String notes = note.getText().toString();
                String barcode = barcodeInput.getText().toString();

                if (TextUtils.isEmpty(Amount)) {
                    amount.setError("Item is required!");
                    return;
                }

                if (Item.equals("Select item")) {
                    Toast.makeText(TodaySpendingActivity.this, "Select a valid item", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(notes)) {
                    note.setError("Note is required");
                    return;

                } else {
                    loader.setMessage("Adding a budget item");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();


                    String id = expensesRef.push().getKey();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("products");
                    Query query = reference.orderByChild("id");
                    Query queryBarcode = null;
                    if(!TextUtils.isEmpty(barcode))
                        queryBarcode = reference.orderByChild("barcode").equalTo(Long.parseLong(barcode));
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Products product = new Products();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                                Products data = dataSnapshot.getValue(Products.class);
                                product = new Products(data.getItem(), Integer.parseInt(Amount)+1, data.getId(), data.getNotes(), data.getBarcode());
                                productsRef.child(String.valueOf(product.getId())).setValue(product).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            System.out.println("hacziko");
                                        } else {
                                            System.out.println("kamilzyla7");
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });




                    if(!TextUtils.isEmpty(barcode)){
                        queryBarcode.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Products product = new Products();
                                myProductsList.clear();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {


                                        Products data = dataSnapshot.getValue(Products.class);
                                        myProductsList.add(data);
                                        product = data;
                                        if(product.getBarcode().equals(Long.parseLong(barcode))){
                                            needToUpdateProductInfo = 1;
                                            product = new Products(Item, Integer.parseInt(Amount), product.getId(), notes, Long.parseLong(barcode));
                                            productsRef.child(String.valueOf(product.getId())).setValue(product).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {

                                                        Toast.makeText(TodaySpendingActivity.this, "Product updated successfuly", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(TodaySpendingActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }

                                        break;

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
               }


                    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    Calendar cal = Calendar.getInstance();
                    String date = dateFormat.format(cal.getTime());

                    MutableDateTime epoch = new MutableDateTime();
                    epoch.setDate(0);
                    DateTime now = new DateTime();
                    Weeks weeks = Weeks.weeksBetween(epoch, now);
                    Months months = Months.monthsBetween(epoch, now);

                    String itemNday = Item + date;
                    String itemNweek = Item + weeks.getWeeks();
                    String itemNmonth = Item + months.getMonths();


                    Data data = new Data(Item, date, id, itemNday, itemNweek, itemNmonth, Integer.parseInt(Amount), weeks.getWeeks(), months.getMonths(), notes);
                    expensesRef.child(id).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(isPassed == 0){
                                if (task.isSuccessful()) {
                                    isPassed = 1;
                                    Toast.makeText(TodaySpendingActivity.this, "Budget item added successfuly", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(TodaySpendingActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }


                            if (isPassed == 1 && !TextUtils.isEmpty(barcode) && needToUpdateProductInfo == 0) {
                                isPassed = 0;
                                try {
                                    Products product = new Products(Item, Integer.parseInt(Amount), lastProductId, notes, Long.parseLong(barcode));
                                    productsRef.child(String.valueOf(lastProductId)).setValue(product).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(TodaySpendingActivity.this, "Product added successfuly", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(TodaySpendingActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } catch (Exception ex) {
                                    Toast.makeText(TodaySpendingActivity.this, "Barcode is not a number", Toast.LENGTH_SHORT).show();
                                }
                            }

                            loader.dismiss();
                        }
                    });
                }
                dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });


        dialog.show();
    }

    @Override
    public void onClick(View view) {
        scanCode();
    }

    private void scanCode() {

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(true);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning Code");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(result.getContents());
                builder.setTitle("Scanning Result");
                builder.setPositiveButton("Scan Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        scanCode();
                    }
                }).setNegativeButton("finish", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        resultScan = result.getContents();
                        readProducts(resultScan);

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                Toast.makeText(this, "No Results", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}