package igrek.songbook.dagger;

import igrek.songbook.MainApplication;

public class DaggerIoc {
	
	private static FactoryComponent appComponent;
	
	private DaggerIoc() {
	}
	
	public static void init(MainApplication application) {
		appComponent = DaggerFactoryComponent.builder()
				.factoryModule(new FactoryModule(application))
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
