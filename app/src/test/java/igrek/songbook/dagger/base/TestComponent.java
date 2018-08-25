package igrek.songbook.dagger.base;

import javax.inject.Singleton;

import dagger.Component;
import igrek.songbook.dagger.DaggerInjectionTest;
import igrek.songbook.dagger.FactoryComponent;
import igrek.songbook.dagger.FactoryModule;

@Singleton
@Component(modules = {FactoryModule.class})
public interface TestComponent extends FactoryComponent {
	
	// to use dagger injection in tests
	void inject(DaggerInjectionTest there);
	
	void inject(BaseDaggerTest there);
	
}