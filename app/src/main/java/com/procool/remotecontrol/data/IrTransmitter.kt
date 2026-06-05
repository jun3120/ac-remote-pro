package com.procool.remotecontrol.data

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Build

object IrTransmitter {
    fun emit(context: Context, pattern: IntArray): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return "系统版本过低"
        }

        val manager = context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
            ?: return "设备无红外功能"

        if (!manager.hasIrEmitter()) return "无红外发射器"

        return try {
            manager.transmit(pattern[0], pattern.copyOfRange(1, pattern.size))
            null
        } catch (e: Exception) {
            "发送失败: ${e.message}"
        }
    }
}
