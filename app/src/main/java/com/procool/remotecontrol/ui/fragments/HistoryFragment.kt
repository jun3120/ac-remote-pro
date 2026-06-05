package com.procool.remotecontrol.ui.fragments

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.procool.remotecontrol.data.storage.MmkvStorage
import com.procool.remotecontrol.databinding.FragmentHistoryBinding
import kotlin.math.max

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadStats()
    }

    private fun loadStats() {
        val devices = MmkvStorage.getDevices()
        if (devices.isEmpty()) return

        val device = devices.first()
        val records = MmkvStorage.getUsageRecords(device.codePath)
        val totalActions = records.size

        binding.statTotalActions.text = "$totalActions"
        binding.statFavTemp.text = "26°C" // Default

        // Draw chart
        val chartView = ChartView(requireContext(), records.map { it.timestamp })
        binding.chartContainer.addView(chartView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ChartView(context: Context, private val timestamps: List<Long>) : View(context) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#7C4DFF")
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#307C4DFF")
        style = Paint.Style.FILL
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#424242")
        strokeWidth = 1f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (timestamps.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()
        val pad = 40f
        val chartW = w - pad * 2
        val chartH = h - pad * 2

        // Draw axis lines
        canvas.drawLine(pad, h - pad, w - pad, h - pad, axisPaint)
        canvas.drawLine(pad, pad, pad, h - pad, axisPaint)

        // Bin timestamps into 7 days
        val now = System.currentTimeMillis()
        val dayMs = 86400000L
        val bins = IntArray(7)
        for (ts in timestamps) {
            val daysAgo = ((now - ts) / dayMs).toInt()
            if (daysAgo in 0..6) bins[6 - daysAgo]++
        }

        val maxVal = max(bins.maxOrNull() ?: 1, 1).toFloat()
        val stepX = chartW / 6f

        val path = Path()
        val fillPath = Path()

        for (i in bins.indices) {
            val x = pad + i * stepX
            val y = pad + chartH * (1f - bins[i] / maxVal)
            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h - pad)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(pad + 6 * stepX, h - pad)
        fillPath.close()

        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(path, linePaint)

        // Draw dots
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#7C4DFF")
            style = Paint.Style.FILL
        }
        for (i in bins.indices) {
            val x = pad + i * stepX
            val y = pad + chartH * (1f - bins[i] / maxVal)
            canvas.drawCircle(x, y, 6f, dotPaint)
        }
    }
}
