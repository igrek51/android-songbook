package igrek.songbook.dagger.base;

import android.support.v7.app.AppCompatActivity;

import igrek.songbook.dagger.FactoryModule;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.mock.LoggerMock;

public class TestModule extends FactoryModule {
	
	public TestModule(AppCompatActivity activity) {
		super(activity);
	}
	
	@Override
	protected Logger provideLogger() {
		return new LoggerMock();
	}
	
}
