package com.procool.remotecontrol

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.procool.remotecontrol.databinding.ActivityMainBinding
import com.procool.remotecontrol.ui.adapter.MainPagerAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pager = binding.viewPager
        val tabs = binding.tabLayout

        pager.adapter = MainPagerAdapter(this)
        pager.offscreenPageLimit = 3

        TabLayoutMediator(tabs, pager) { tab, position ->
            tab.text = when (position) {
                0 -> "我的设备"
                1 -> "遥控器"
                2 -> "统计"
                3 -> "设置"
                else -> ""
            }
        }.attach()
    }
}
