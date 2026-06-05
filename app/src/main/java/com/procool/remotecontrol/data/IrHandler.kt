package com.procool.remotecontrol.data

import android.util.Log
import com.procool.remotecontrol.ProApp
import com.procool.remotecontrol.data.model.UsageRecord
import com.procool.remotecontrol.data.storage.MmkvStorage
import net.irext.decode.sdk.IRDecode
import net.irext.decode.sdk.bean.ACStatus
import net.irext.decode.sdk.utils.Constants
import kotlinx.coroutines.*

class IrHandler {

    private val irDecode = IRDecode.getInstance()
    private var devicePath: String = ""
    private var loaded = false

    private var acTemp = Constants.ACTemperature.TEMP_24.value
    private var acPower = Constants.ACPower.POWER_ON.value

    companion object {
        private const val TAG = "IrHandler"
        const val CMD_POWER = 0
        const val CMD_MODE = 1
        const val CMD_TEMP_UP = 2
        const val CMD_TEMP_DN = 3
        const val CMD_FAN_SPEED = 9
        const val CMD_SWING = 10
    }

    fun load(codePath: String, categoryId: Int, subCategory: Int): Boolean {
        if (loaded) close()

        val file = java.io.File(codePath)
        Log.d(TAG, "Opening IR file: $codePath exists=${file.exists()}")

        val result = irDecode.openFile(categoryId, subCategory, codePath)
        if (result != 0) {
            Log.e(TAG, "IR decode init failed: code=$result")
            return false
        }

        devicePath = codePath
        loaded = true
        return true
    }

    fun transmit(keyCode: Int, status: ACStatus? = null): Boolean {
        if (!loaded) return false

        val acStatus = status ?: ACStatus(
            acPower, Constants.ACMode.MODE_COOL.value, acTemp,
            Constants.ACWindSpeed.SPEED_AUTO.value, Constants.ACSwing.SWING_ON.value,
            0, 0, 0, 0
        )

        val pattern = irDecode.decodeBinary(keyCode, acStatus)
        if (pattern.isEmpty()) return false

        val error = IrTransmitter.emit(ProApp.instance, pattern)
        if (error != null) {
            Log.e(TAG, "Transmit error: $error")
            return false
        }

        // Record usage asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            MmkvStorage.addUsageRecord(
                UsageRecord(
                    deviceCodePath = devicePath,
                    action = mapKeyCodeToName(keyCode),
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        return true
    }

    fun togglePower(): Boolean {
        acPower = if (acPower == Constants.ACPower.POWER_ON.value)
            Constants.ACPower.POWER_OFF.value
        else
            Constants.ACPower.POWER_ON.value
        return transmit(CMD_POWER)
    }

    fun increaseTemp(): Int {
        acTemp += 1
        if (!transmit(CMD_TEMP_UP)) acTemp -= 1
        return acTemp + 16
    }

    fun decreaseTemp(): Int {
        acTemp -= 1
        if (!transmit(CMD_TEMP_DN)) acTemp += 1
        return acTemp + 16
    }

    fun changeMode(): Boolean = transmit(CMD_MODE)
    fun changeFanSpeed(): Boolean = transmit(CMD_FAN_SPEED)
    fun toggleSwing(): Boolean = transmit(CMD_SWING)

    fun getCelsius(): Int = acTemp + 16
    fun isOn(): Boolean = acPower == Constants.ACPower.POWER_ON.value
    fun getCodePath(): String = devicePath

    fun close() {
        if (loaded) {
            irDecode.closeBinary()
            loaded = false
        }
    }

    private fun mapKeyCodeToName(keyCode: Int): String = when (keyCode) {
        CMD_POWER -> "power"
        CMD_MODE -> "mode"
        CMD_TEMP_UP, CMD_TEMP_DN -> "temperature"
        CMD_FAN_SPEED -> "fan_speed"
        CMD_SWING -> "swing"
        else -> "unknown"
    }
}
