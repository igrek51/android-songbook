package igrek.songbook;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Instrumented test, which will execute on an Android device.
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class InstrumentationTest {
	
	@Test
	public void test_navigationMenuShows() {
		// Context of the app under test.
		Context appContext = InstrumentationRegistry.getTargetContext();
		
		assertThat(appContext.getPackageName()).isEqualTo("igrek.songbook");
	}
}
