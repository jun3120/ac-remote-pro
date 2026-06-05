package com.procool.remotecontrol.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.procool.remotecontrol.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingsFeedback.setOnClickListener {
            Toast.makeText(requireContext(), "客服邮箱: support@example.com", Toast.LENGTH_SHORT).show()
        }
        binding.settingsPrivacy.setOnClickListener {
            Toast.makeText(requireContext(), "隐私政策即将上线", Toast.LENGTH_SHORT).show()
        }
        binding.settingsAbout.setOnClickListener {
            Toast.makeText(requireContext(), "空调遥控器马甲3 v1.0.0", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
