package igrek.songbook.settings;

import android.widget.SeekBar;
import android.widget.TextView;

import io.reactivex.subjects.PublishSubject;

public class SeekbarController {
	
	private SeekBar seekBar;
	private TextView label;
	private float currentValue;
	private float min;
	private float max;
	PublishSubject<Float> valueSubject = PublishSubject.create();
	
	public SeekbarController(SeekBar seekBar, TextView label, float currentValue, float min, float max) {
		this.seekBar = seekBar;
		this.label = label;
		this.currentValue = currentValue;
		this.min = min;
		this.max = max;
		
		setValue(currentValue);
		updateLabel(currentValue);
		
		seekBar.setMax(100);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				float value = getValue();
				updateLabel(value);
				valueSubject.onNext(getValue());
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
	}
	
	
	public String generateLabelText(float value) {
		return "";
	}
	
	private void updateLabel(float value) {
		label.setText(generateLabelText(value));
	}
	
	public void setValue(float value) {
		this.currentValue = value;
		float progress = (value - min) * seekBar.getMax() / (max - min);
		if (progress < 0)
			progress = 0;
		if (progress > 100)
			progress = 100;
		seekBar.setProgress((int) progress);
	}
	
	public float getValue() {
		int progress = seekBar.getProgress();
		float value = min + (max - min) * progress / seekBar.getMax();
		return value;
	}
	
	public PublishSubject<Float> getValueSubject() {
		return valueSubject;
	}
}
