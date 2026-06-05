package com.procool.remotecontrol.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.procool.remotecontrol.ui.fragments.*

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> DeviceFragment()
        1 -> ControlFragment()
        2 -> HistoryFragment()
        3 -> SettingsFragment()
        else -> DeviceFragment()
    }
}
