package com.procool.remotecontrol.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.procool.remotecontrol.R
import com.procool.remotecontrol.data.RemoteWebApi
import com.procool.remotecontrol.data.model.RemoteDevice
import com.procool.remotecontrol.data.storage.MmkvStorage
import com.procool.remotecontrol.databinding.FragmentDeviceBinding
import com.procool.remotecontrol.databinding.ItemDeviceBinding
import com.procool.remotecontrol.gesture.GestureHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeviceFragment : Fragment() {

    private var _binding: FragmentDeviceBinding? = null
    private val binding get() = _binding!!
    private val adapter = DeviceListAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.deviceRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.deviceRecycler.adapter = adapter

        adapter.onItemClick = { device ->
            // Navigate to control fragment would be handled by main activity
        }

        binding.fabAdd.setOnClickListener {
            showBrandPicker()
        }

        loadDevices()
    }

    private fun loadDevices() {
        val devices = MmkvStorage.getDevices()
        adapter.submitList(devices)
    }

    private fun showBrandPicker() {
        // Domestic brands only: 格力、美的、海尔、奥克斯、TCL、海信、长虹
        val domesticBrands = listOf(
            "格力" to 1, "美的" to 2, "海尔" to 3,
            "奥克斯" to 4, "TCL" to 5, "海信" to 6, "长虹" to 7
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("选择品牌")
            .setItems(domesticBrands.map { it.first }.toTypedArray()) { _, which ->
                val (name, id) = domesticBrands[which]
                startPairing(name, id)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun startPairing(brandName: String, brandId: Int) {
        lifecycleScope.launch {
            try {
                val indexes = withContext(Dispatchers.IO) {
                    RemoteWebApi.listRemotes(1, brandId)
                }
                if (indexes.isEmpty()) {
                    Toast.makeText(requireContext(), "未找到该品牌数据", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Auto-test first remote
                for ((i, idx) in indexes.withIndex()) {
                    val binData = withContext(Dispatchers.IO) {
                        RemoteWebApi.downloadBin(idx.remoteMap)
                    }
                    val file = java.io.File(requireContext().filesDir, "remote_${idx.remoteMap}.bin")
                    file.writeBytes(binData)

                    // Save successful match
                    MmkvStorage.addDevice(
                        RemoteDevice(
                            codePath = file.absolutePath,
                            categoryId = 1,
                            subCategory = idx.subCategory,
                            brandName = brandName,
                            customName = "${brandName}空调"
                        )
                    )
                    loadDevices()
                    Toast.makeText(requireContext(), "已添加 ${brandName}空调", Toast.LENGTH_SHORT).show()
                    break
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "添加失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadDevices()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class DeviceListAdapter : RecyclerView.Adapter<DeviceListAdapter.VH>() {

        var onItemClick: ((RemoteDevice) -> Unit)? = null
        private var items = listOf<RemoteDevice>()

        fun submitList(list: List<RemoteDevice>) {
            items = list
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(binding)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class VH(private val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(device: RemoteDevice) {
                binding.deviceName.text = device.displayName
                binding.deviceStatus.text = "已保存 · ${device.brandName}"
                binding.root.setOnClickListener { onItemClick?.invoke(device) }
            }
        }
    }
}
