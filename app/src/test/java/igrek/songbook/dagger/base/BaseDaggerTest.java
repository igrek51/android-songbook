package igrek.songbook.dagger.base;

import android.app.Activity;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import javax.inject.Inject;

import igrek.songbook.activity.MainActivity;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;

@RunWith(RobolectricTestRunner.class)
public abstract class BaseDaggerTest {
	
	@Inject
	protected Activity activity;
	
	@Inject
	protected Logger logger;
	
	@Before
	public void setUp() {
		MainActivity activity = Robolectric.setupActivity(MainActivity.class);
		
		TestComponent component = DaggerTestComponent.builder()
				.factoryModule(new TestModule(activity))
				.build();
		
		DaggerIoc.setFactoryComponent(component);
		
		injectThis(component);
	}
	
	protected void injectThis(TestComponent component) {
		component.inject(this);
	}
}
