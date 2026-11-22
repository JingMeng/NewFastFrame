package com.example.chat;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.example.chat.ui.SplashActivity;

/**
 * 椤圭洰鍚嶇О:    NewFastFrame
 * 鍒涘缓浜?        闄堥敠鍐? * 鍒涘缓鏃堕棿:    2017/10/7      23:36
 * QQ:             1981367757
 */

public class TestChatActivity extends AppCompatActivity{


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_chat);

    }

    public void chat(View view){
        Intent intent=new Intent(this, SplashActivity.class);
        startActivity(intent);
    }


}

