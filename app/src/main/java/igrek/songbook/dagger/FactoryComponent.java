package igrek.songbook.dagger;

import javax.inject.Singleton;

import dagger.Component;
import igrek.songbook.activity.MainActivity;
import igrek.songbook.service.activity.ActivityController;
import igrek.songbook.service.activity.AppInitializer;
import igrek.songbook.service.activity.OptionSelectDispatcher;
import igrek.songbook.service.filesystem.ExternalCardService;
import igrek.songbook.service.filesystem.FilesystemService;
import igrek.songbook.service.screen.ScreenService;
import igrek.songbook.service.state.AppStateService;

/**
 * Dagger will be injecting to those classes
 */
@Singleton
@Component(modules = {FactoryModule.class})
public interface FactoryComponent {
	
	void inject(MainActivity there);
	
	void inject(ExternalCardService there);
	void inject(FilesystemService there);
	void inject(AppInitializer there);
	void inject(ActivityController there);
	void inject(ScreenService there);
	void inject(AppStateService there);
	void inject(OptionSelectDispatcher there);
	
}