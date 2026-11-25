package com.s22winemu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.s22winemu.app.data.DriverRepository
import com.s22winemu.app.ui.S22WinEmuApp
import com.s22winemu.app.ui.theme.S22WinEmuTheme

class MainActivity : ComponentActivity() {
    private lateinit var driverRepository: DriverRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        driverRepository = DriverRepository(applicationContext)

        setContent {
            S22WinEmuTheme {
                S22WinEmuApp(repository = driverRepository)
            }
        }
    }
}
