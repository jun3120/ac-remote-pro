package com.procool.remotecontrol.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.HttpURLConnection
import java.net.URL

data class RemoteIndexInfo(
    val remoteMap: Int,
    val name: String = "",
    val subCategory: Int = 1
)

object RemoteWebApi {
    private const val BASE = "https://irext.net/app/api"
    private const val KEY = "bb00f5882b850f455e489a54a129fbb0"
    private const val SECRET = "687fd3dfb34847d8a7ee8f3e37e5047d"
    private val gson = Gson()

    fun listRemotes(categoryId: Int, brandId: Int): List<RemoteIndexInfo> {
        val conn = URL("$BASE/listRemotes?categoryId=$categoryId&brandId=$brandId").openConnection() as HttpURLConnection
        conn.setRequestProperty("appKey", KEY)
        conn.setRequestProperty("appSecret", SECRET)
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        val json = conn.inputStream.bufferedReader().use { it.readText() }
        conn.disconnect()
        val type = object : TypeToken<List<RemoteIndexInfo>>() {}.type
        return try { gson.fromJson(json, type) ?: emptyList() } catch (_: Exception) { emptyList() }
    }

    fun downloadBin(remoteMap: Int): ByteArray {
        val conn = URL("$BASE/downloadBin?remoteMap=$remoteMap").openConnection() as HttpURLConnection
        conn.setRequestProperty("appKey", KEY)
        conn.setRequestProperty("appSecret", SECRET)
        conn.connectTimeout = 15_000
        conn.readTimeout = 15_000
        val data = conn.inputStream.use { it.readBytes() }
        conn.disconnect()
        return data
    }
}
