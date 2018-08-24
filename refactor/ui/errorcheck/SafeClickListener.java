package igrek.todotree.ui.errorcheck;


import android.view.View;

public abstract class SafeClickListener implements View.OnClickListener {
	
	@Override
	public void onClick(View var1) {
		try {
			onClick();
		} catch (Throwable t) {
			UIErrorHandler.showError(t);
		}
	}
	
	public abstract void onClick() throws Throwable;
	
}
