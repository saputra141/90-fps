package com.ultra.emulator

import android.graphics.Bitmap

class EmulatorEngine {
    companion object {
        init {
            System.loadLibrary("ultraemulator")
        }
    }

    external fun initCore()
    external fun loadROM(romData: ByteArray)
    external fun renderFrame(bitmap: Bitmap)
}
