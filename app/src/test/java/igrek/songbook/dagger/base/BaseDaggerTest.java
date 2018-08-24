package igrek.songbook.dagger.base;

import android.app.Activity;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import javax.inject.Inject;

import igrek.songbook.BuildConfig;
import igrek.songbook.MainApplication;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, application = MainApplication.class, manifest = "src/main/AndroidManifest.xml", packageName = "igrek.songbook")
public abstract class BaseDaggerTest {
	
	@Inject
	protected Activity activity;
	
	@Inject
	protected Logger logger;
	
	
	@Before
	public void setUp() {
		MainApplication application = (MainApplication) RuntimeEnvironment.application;
		
		TestComponent component = DaggerTestComponent.builder()
				.factoryModule(new TestModule(application))
				.build();
		
		DaggerIoc.setFactoryComponent(component);
		
		injectThis(component);
	}
	
	protected void injectThis(TestComponent component) {
		component.inject(this);
	}
}
