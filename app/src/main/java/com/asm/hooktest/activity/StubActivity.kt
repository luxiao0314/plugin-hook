package com.asm.hooktest.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.asm.hooktest.R


class StubActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stub)
    }
}