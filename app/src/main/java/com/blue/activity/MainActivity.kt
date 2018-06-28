package com.blue.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.blue.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityMainBinding.inflate(layoutInflater).apply { setContentView(root) }
    }
}
