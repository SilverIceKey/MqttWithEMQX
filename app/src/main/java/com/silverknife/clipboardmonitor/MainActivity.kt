package com.silverknife.clipboardmonitor

import android.app.Activity
import android.content.Intent
import android.os.Bundle


class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this@MainActivity, ClipboardControlService::class.java)
        startService(intent)
    }

    override fun onStart() {
        super.onStart()
        finish()
    }
}