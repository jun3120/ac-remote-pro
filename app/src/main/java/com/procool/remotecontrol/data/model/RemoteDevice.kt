package com.procool.remotecontrol.data.model

data class RemoteDevice(
    val codePath: String = "",
    val categoryId: Int = 1,
    val subCategory: Int = 1,
    val brandName: String = "",
    val customName: String = "",
    val savedAt: Long = System.currentTimeMillis()
) {
    val displayName: String get() = if (customName.isNotEmpty()) customName else brandName
}

data class UsageRecord(
    val deviceCodePath: String = "",
    val action: String = "",
    val value: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
