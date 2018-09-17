package igrek.songbook.wip;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import igrek.songbook.activity.MainActivity;
import igrek.songbook.dagger.AndroidTestComponent;
import igrek.songbook.dagger.DaggerAndroidTestComponent;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.dagger.FactoryModule;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;
import igrek.songbook.settings.preferences.PreferencesService;

/**
 * Instrumented test, which will execute on an Android device.
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 * A test for a Work-in-progress-features
 */
@RunWith(AndroidJUnit4.class)
public class WIPFeatureTest {
	
	private Logger logger = LoggerFactory.getLogger();
	
	@Rule
	public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class);
	
	@Inject
	Activity activity;
	@Inject
	PreferencesService preferencesService;
	
	@Before
	public void setUpDagger() {
		MainActivity activity = rule.getActivity();
		AndroidTestComponent component = DaggerAndroidTestComponent.builder()
				.factoryModule(new FactoryModule(activity))
				.build();
		DaggerIoc.setFactoryComponent(component);
		component.inject(this);
		logger.warn("====== Running Android Instrumentation Test: WIPFeatureTest ======");
	}
	
	@Test
	//@Ignore
	public void testWipFeature() {
		
		preferencesService.clear();
		
	}
	
}
