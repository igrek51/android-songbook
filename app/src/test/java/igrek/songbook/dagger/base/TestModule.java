package igrek.songbook.dagger.base;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

import org.mockito.Mockito;

import igrek.songbook.MainApplication;
import igrek.songbook.dagger.FactoryModule;
import igrek.songbook.logger.Logger;
import igrek.songbook.mock.LoggerMock;

public class TestModule extends FactoryModule {
	
	public TestModule(MainApplication application) {
		super(application);
	}
	
	@Override
	protected Logger provideLogger() {
		return new LoggerMock();
	}
	
	@Override
	protected AppCompatActivity provideAppCompatActivity() {
		return Mockito.mock(AppCompatActivity.class);
	}
	
	@Override
	protected Activity provideActivity() {
		return provideAppCompatActivity();
	}
	
}
