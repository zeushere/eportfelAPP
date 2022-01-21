package com.example.eportfelapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private CardView budgetCardView, todayCardView;

    private ImageView weekBtnImageView, todayBtnImageView, budgetBtnImageView, monthBtnImageView, analyticsImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        todayCardView = findViewById(R.id.todayCardView);
        budgetCardView = findViewById(R.id.budgetCardView);
        weekBtnImageView = findViewById(R.id.weekBtnImageView);
        monthBtnImageView = findViewById(R.id.monthBtnImageView);
        budgetBtnImageView = findViewById(R.id.budgetBtnImageView);
        todayBtnImageView = findViewById(R.id.todayBtnImageView);
        analyticsImageView = findViewById(R.id.analyticsImageView);
        budgetBtnImageView.setOnClickListener(new View.OnClickListener() {


            @Override
        public void onClick(View view) {
            Intent intent = new Intent(MainActivity.this,BudgetActivity.class);
            startActivity(intent);
        }
    });

        todayCardView.setOnClickListener(new View.OnClickListener() {
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
            intent.putExtra("type","week");
            startActivity(intent);
        }
    });

        monthBtnImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WeekSpendingActivity.class);
                intent.putExtra("type","month");
                startActivity(intent);
            }
        });

        analyticsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ChooseAnalyticActivity.class);
                startActivity(intent);
            }
        });

}


}