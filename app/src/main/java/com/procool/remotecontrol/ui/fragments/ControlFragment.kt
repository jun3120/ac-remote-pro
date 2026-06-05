package com.procool.remotecontrol.ui.fragments

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.procool.remotecontrol.data.IrHandler
import com.procool.remotecontrol.data.storage.MmkvStorage
import com.procool.remotecontrol.databinding.FragmentControlBinding

class ControlFragment : Fragment() {

    private var _binding: FragmentControlBinding? = null
    private val binding get() = _binding!!
    private val irHandler = IrHandler()

    private var currentTemp = 26
    private var currentModeIndex = 1
    private var currentFanIndex = 0
    private val modes = arrayOf("制热", "制冷", "除湿", "送风", "自动")
    private val fanSpeeds = arrayOf("自动", "低", "中", "高")

    private var devicePath: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFirstDevice()
        setupControls()
    }

    private fun loadFirstDevice() {
        val devices = MmkvStorage.getDevices()
        if (devices.isNotEmpty()) {
            val device = devices.first()
            devicePath = device.codePath
            binding.controlDeviceName.text = device.displayName
            irHandler.load(device.codePath, device.categoryId, device.subCategory)
        } else {
            binding.controlDeviceName.text = "未添加设备"
        }
    }

    private fun setupControls() {
        // Temperature SeekBar
        binding.tempSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentTemp = 16 + progress
                binding.controlTemperature.text = "${currentTemp}°C"
                if (fromUser) {
                    val targetTemp = currentTemp
                    // Adjust irHandler temp to match
                    if (fromUser && devicePath != null) {
                        vibrate()
                    }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Mode card
        binding.cardMode.setOnClickListener {
            currentModeIndex = (currentModeIndex + 1) % modes.size
            binding.modeValue.text = modes[currentModeIndex]
            irHandler.changeMode()
            vibrate()
        }

        // Fan card
        binding.cardFan.setOnClickListener {
            currentFanIndex = (currentFanIndex + 1) % fanSpeeds.size
            binding.fanValue.text = fanSpeeds[currentFanIndex]
            irHandler.changeFanSpeed()
            vibrate()
        }

        // Swing switch
        binding.swingSwitch.setOnCheckedChangeListener { _, _ ->
            irHandler.toggleSwing()
            vibrate()
        }

        // Power button
        binding.powerButton.setOnClickListener {
            val ok = irHandler.togglePower()
            if (ok) {
                val state = if (irHandler.isOn()) "已开启" else "已关闭"
                binding.powerButton.text = state
                vibrate()
            }
        }
    }

    private fun vibrate() {
        val vib = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val mgr = requireContext().getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            mgr.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            requireContext().getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
        }
        vib.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun onResume() {
        super.onResume()
        // Refresh device list
        loadFirstDevice()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        irHandler.close()
        _binding = null
    }
}
