package igrek.songbook.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.service.activity.ActivityController;
import igrek.songbook.service.activity.AppInitializer;
import igrek.songbook.service.activity.OptionSelectDispatcher;
import igrek.songbook.service.activity.SystemKeyDispatcher;

public class MainActivity extends AppCompatActivity {
	
	@Inject
	AppInitializer appInitializer;
	@Inject
	ActivityController activityController;
	@Inject
	OptionSelectDispatcher optionSelectDispatcher;
	@Inject
	SystemKeyDispatcher systemKeyDispatcher;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DaggerIoc.getFactoryComponent().inject(this);
		appInitializer.init();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		activityController.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		activityController.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return optionSelectDispatcher.optionsSelect(item.getItemId()) || super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (systemKeyDispatcher.onKeyBack())
				return true;
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (systemKeyDispatcher.onKeyMenu())
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	}
}
