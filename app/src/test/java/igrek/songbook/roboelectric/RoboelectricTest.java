package igrek.songbook.roboelectric;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import igrek.songbook.activity.MainActivity;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class RoboelectricTest {
	
	@Test
	public void testActivityCreation() {
		MainActivity activity = Robolectric.setupActivity(MainActivity.class);
		
		assertThat(activity).isNotNull();
		
		System.out.println("created activity: " + activity.toString());
	}
	
}
