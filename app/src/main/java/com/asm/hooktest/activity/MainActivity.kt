package com.asm.hooktest.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.asm.hooktest.R
import android.content.Intent
import android.view.View
import android.widget.Button
import com.asm.hooktest.activity.PluginActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.tv_start_plugin_activity).setOnClickListener { v: View? ->
            val intent = Intent(this@MainActivity, PluginActivity::class.java)
            startActivity(intent)
        }
    }
}