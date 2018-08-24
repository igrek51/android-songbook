package igrek.songbook.dagger.base;

import javax.inject.Singleton;

import dagger.Component;
import igrek.songbook.dagger.FactoryComponent;
import igrek.songbook.dagger.FactoryModule;
import igrek.songbook.dagger.SimpleDaggerInjectionTest;

@Singleton
@Component(modules = {FactoryModule.class})
public interface TestComponent extends FactoryComponent {
	
	// to use dagger injection in tests
	void inject(SimpleDaggerInjectionTest there);
	
	void inject(BaseDaggerTest there);
	
}