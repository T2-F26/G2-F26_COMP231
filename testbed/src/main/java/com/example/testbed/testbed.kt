package com.example.testbed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.testbed.medclock.MedClockTestbed
import com.example.testbed.ui.theme.MedClockTheme

class testbed : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedClockTheme {
                MedClockTestbed()
            }
        }
    }
}

