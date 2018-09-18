package igrek.songbook.dagger;

import android.support.v7.app.AppCompatActivity;

import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;

public class DaggerIoc {
	
	private static FactoryComponent appComponent;
	
	private static Logger logger = LoggerFactory.getLogger();
	
	private DaggerIoc() {
	}
	
	public static void init(AppCompatActivity activity) {
		logger.info("Initializing Dagger IOC container...");
		appComponent = DaggerFactoryComponent.builder()
				.factoryModule(new FactoryModule(activity))
				.build();
	}
	
	public static FactoryComponent getFactoryComponent() {
		return appComponent;
	}
	
	/**
	 * only for testing purposes
	 */
	public static void setFactoryComponent(FactoryComponent component) {
		appComponent = component;
	}
}
