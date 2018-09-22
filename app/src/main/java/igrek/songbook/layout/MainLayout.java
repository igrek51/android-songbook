package igrek.songbook.layout;

import android.view.View;

public interface MainLayout {
	
	void showLayout(View layout);
	
	LayoutState getLayoutState();
	
	int getLayoutResourceId();
	
	void onBackClicked();
	
	void onLayoutExit();
	
}
