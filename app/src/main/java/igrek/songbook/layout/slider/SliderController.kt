package igrek.songbook.layout.slider

import android.widget.SeekBar
import android.widget.TextView

import io.reactivex.subjects.PublishSubject

open class SliderController(private val seekBar: SeekBar, private val label: TextView, currentValue: Float, private val min: Float, private val max: Float) {
    var valueSubject = PublishSubject.create<Float>()
        internal set

    var value: Float
        get() {
            val progress = seekBar.progress
            return min + (max - min) * progress / seekBar.max
        }
        set(value) {
            var progress = (value - min) * seekBar.max / (max - min)
            if (progress < 0)
                progress = 0f
            if (progress > 100000)
                progress = 100000f
            seekBar.progress = progress.toInt()
        }

    init {

        value = currentValue
        updateLabel(currentValue)

        seekBar.max = 100000
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val value = value
                updateLabel(value)
                valueSubject.onNext(value)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }


    open fun generateLabelText(value: Float): String {
        return ""
    }

    private fun updateLabel(value: Float) {
        label.text = generateLabelText(value)
    }
}
