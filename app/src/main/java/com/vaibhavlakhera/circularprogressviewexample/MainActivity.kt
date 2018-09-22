package com.vaibhavlakhera.circularprogressviewexample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val colors by lazy { resources.getIntArray(R.array.colors) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnUpdate.setOnClickListener {
            progressView.setProgress(Random().nextInt(progressView.getTotal() - 1) + 1)
        }

        seekBarTotal.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                progressView.setTotal(progress)
                tvTotal.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnTotalColor.setOnClickListener {
            progressView.setTotalColor(colors[Random().nextInt(colors.size)])
        }

        seekBarTotalWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                progressView.setTotalWidth(progress.toFloat())
                tvTotalWidth.text = String.format("%d DP", progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                progressView.setProgress(progress, false)
                tvProgress.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnProgressColor.setOnClickListener {
            progressView.setProgressColor(colors[Random().nextInt(colors.size)])
        }

        seekBarProgressWidth.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                progressView.setProgressWidth(progress.toFloat())
                tvProgressWidth.text = String.format("%d DP", progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        cbProgressRoundCap.setOnCheckedChangeListener { _, isChecked ->
            progressView.setProgressRoundCap(isChecked)
        }

        cbProgressTextEnabled.setOnCheckedChangeListener { _, isChecked ->
            progressView.setProgressTextEnabled(isChecked)
        }

        ArrayAdapter.createFromResource(
                this,
                R.array.progress_text_types,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerProgressTextType.adapter = adapter
        }
        spinnerProgressTextType.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                progressView.setProgressTextType(position)
            }
        }

        seekBarProgressTextSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                progressView.setProgressTextSize(progress.toFloat())
                tvProgressTextSize.text = String.format("%d SP", progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnProgressTextColor.setOnClickListener {
            progressView.setProgressTextColor(colors[Random().nextInt(colors.size)])
        }

        btnFillColor.setOnClickListener {
            progressView.setFillColor(colors[Random().nextInt(colors.size)])
        }

        seekBarStartAngle.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                progressView.setStartAngle(progress.toFloat())
                tvStartAngle.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        cbAnimate.setOnCheckedChangeListener { _, isChecked ->
            progressView.setAnimate(isChecked)
        }

        val animateDurations = resources.getIntArray(R.array.animate_durations_int)
        ArrayAdapter.createFromResource(
                this,
                R.array.animate_durations_string,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerAnimateDuration.adapter = adapter
        }
        spinnerAnimateDuration.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                progressView.setAnimateDuration(animateDurations[position].toLong())
            }
        }
    }
}