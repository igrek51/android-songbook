package igrek.songbook.dagger;

import android.app.Activity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import javax.inject.Inject;

import igrek.songbook.activity.MainActivity;
import igrek.songbook.dagger.base.DaggerTestComponent;
import igrek.songbook.dagger.base.TestComponent;
import igrek.songbook.dagger.base.TestModule;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class DaggerInjectionTest {
	
	@Inject
	Activity activity;
	
	@Before
	public void setUp() {
		MainActivity activity = Robolectric.setupActivity(MainActivity.class);
		
		TestComponent component = DaggerTestComponent.builder()
				.factoryModule(new TestModule(activity))
				.build();
		
		DaggerIoc.setFactoryComponent(component);
		
		component.inject(this);
	}
	
	@Test
	public void testActivityInjection() {
		assertThat(activity).isNotNull();
		System.out.println("injected activity: " + activity.toString());
	}
	
}
