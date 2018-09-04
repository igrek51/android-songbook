package igrek.songbook.dagger;

import javax.inject.Singleton;

import dagger.Component;
import igrek.songbook.wip.WIPFeatureTest;

@Singleton
@Component(modules = {FactoryModule.class})
public interface AndroidTestComponent extends FactoryComponent {
	
	// enable dagger injection in tests
	void inject(WIPFeatureTest there);
	
}