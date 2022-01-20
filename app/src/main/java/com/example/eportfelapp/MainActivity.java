package com.example.eportfelapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private ImageView weekBtnImageView, todayBtnImageView, budgetBtnImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weekBtnImageView = findViewById(R.id.weekBtnImageView);
        budgetBtnImageView = findViewById(R.id.budgetBtnImageView);
        todayBtnImageView = findViewById(R.id.todayBtnImageView);

        budgetBtnImageView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this,BudgetActivity.class);
            startActivity(intent);
        }
    });

        todayBtnImageView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, TodaySpendingActivity.class);
            startActivity(intent);
        }
    });

        weekBtnImageView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this, WeekSpendingActivity.class);
            startActivity(intent);
        }
    });

}


}