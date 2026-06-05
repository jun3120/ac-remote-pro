package com.procool.remotecontrol.data.storage

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
import com.procool.remotecontrol.data.model.RemoteDevice
import com.procool.remotecontrol.data.model.UsageRecord

object MmkvStorage {
    private lateinit var kv: MMKV
    private val gson = Gson()

    private const val KEY_DEVICES = "saved_remotes"
    private const val KEY_USAGE = "usage_records"

    fun init(context: Context) {
        MMKV.initialize(context)
        kv = MMKV.defaultMMKV()
    }

    fun getDevices(): List<RemoteDevice> {
        val json = kv.decodeString(KEY_DEVICES, "[]") ?: "[]"
        val type = object : TypeToken<List<RemoteDevice>>() {}.type
        return try { gson.fromJson(json, type) } catch (_: Exception) { emptyList() }
    }

    fun addDevice(device: RemoteDevice) {
        val devices = getDevices().toMutableList()
        devices.add(0, device)
        saveDevices(devices)
    }

    fun removeDevice(codePath: String) {
        val devices = getDevices().filter { it.codePath != codePath }
        saveDevices(devices)
    }

    fun updateDeviceName(codePath: String, name: String) {
        val devices = getDevices().map {
            if (it.codePath == codePath) it.copy(customName = name) else it
        }
        saveDevices(devices)
    }

    fun getUsageRecords(deviceCodePath: String): List<UsageRecord> {
        val json = kv.decodeString(KEY_USAGE, "[]") ?: "[]"
        val type = object : TypeToken<List<UsageRecord>>() {}.type
        val all = try { gson.fromJson<List<UsageRecord>>(json, type) } catch (_: Exception) { emptyList() }
        return all.filter { it.deviceCodePath == deviceCodePath }
    }

    fun addUsageRecord(record: UsageRecord) {
        val records = getUsageRecords("").toMutableList()
        records.add(record)
        kv.encode(KEY_USAGE, gson.toJson(records.takeLast(500)))
    }

    fun getTotalActions(deviceCodePath: String): Int {
        return getUsageRecords(deviceCodePath).size
    }

    private fun saveDevices(devices: List<RemoteDevice>) {
        kv.encode(KEY_DEVICES, gson.toJson(devices))
    }
}
