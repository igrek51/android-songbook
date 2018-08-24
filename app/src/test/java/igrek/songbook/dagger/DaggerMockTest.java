package igrek.songbook.dagger;


import org.junit.Test;

import igrek.songbook.dagger.base.BaseDaggerTest;

public class DaggerMockTest extends BaseDaggerTest {
	
	@Test
	public void testLoggerMock() {
		logger.info("Hello dupa");
	}
	
}