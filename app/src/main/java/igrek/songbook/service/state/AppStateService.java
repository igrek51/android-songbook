package igrek.songbook.service.state;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.domain.state.AppState;

public class AppStateService {
	
	private AppState state = AppState.FILE_LIST;
	
	public AppStateService() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public AppState getState() {
		return state;
	}
	
	public void setState(AppState state) {
		this.state = state;
	}
}
