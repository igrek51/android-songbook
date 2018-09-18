package igrek.songbook.system;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;

public class PackageInfoService {
	
	@Inject
	AppCompatActivity activity;
	
	private Logger logger = LoggerFactory.getLogger();
	private String versionName;
	
	public PackageInfoService() {
		DaggerIoc.getFactoryComponent().inject(this);
		
		try {
			PackageInfo pInfo = activity.getPackageManager()
					.getPackageInfo(activity.getPackageName(), 0);
			versionName = pInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			logger.error(e);
		}
	}
	
	public String getVersionName() {
		return versionName;
	}
}