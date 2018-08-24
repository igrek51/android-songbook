package igrek.songbook.dagger;

import android.app.Application;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import javax.inject.Inject;

import igrek.songbook.BuildConfig;
import igrek.songbook.MainApplication;
import igrek.songbook.dagger.base.DaggerTestComponent;
import igrek.songbook.dagger.base.TestComponent;
import igrek.songbook.dagger.base.TestModule;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, application = MainApplication.class)
public class SimpleDaggerInjectionTest {
	
	@Inject
	Application application;
	
	@Before
	public void setUp() {
		MainApplication application = (MainApplication) RuntimeEnvironment.application;
		
		TestComponent component = DaggerTestComponent.builder()
				.factoryModule(new TestModule(application))
				.build();
		
		DaggerIoc.setFactoryComponent(component);
		
		component.inject(this);
	}
	
	@Test
	public void testApplicationInjection() {
		System.out.println("injected application: " + application.toString());
	}
	
}
