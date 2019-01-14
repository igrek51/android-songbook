package igrek.songbook.system;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.info.logger.Logger;
import igrek.songbook.info.logger.LoggerFactory;

public class PermissionService {
	
	private Logger logger = LoggerFactory.INSTANCE.getLogger();
	
	@Inject
	Activity activity;
	
	public PermissionService() {
		DaggerIoc.getFactoryComponent().inject(this);
	}
	
	public boolean isStoragePermissionGranted() {
		if (Build.VERSION.SDK_INT >= 23) {
			if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
				// Permission is granted
				return true;
			} else {
				// Permission is revoked
				ActivityCompat.requestPermissions(activity, new String[]{
						Manifest.permission.WRITE_EXTERNAL_STORAGE
				}, 1);
				return false;
			}
		} else { //permission is automatically granted on sdk<23 upon installation
			// Permission is granted
			return true;
		}
	}
	
	private void onPermissionGranted(@NotNull String permission) {
		logger.info("permission " + permission + " has been granted");
	}
	
	private void onPermissionDenied(@NotNull String permission) {
		logger.warn("permission " + permission + " has been denied");
	}
	
	public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
		if (grantResults.length > 0) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				onPermissionGranted(permissions[0]);
			} else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
				onPermissionDenied(permissions[0]);
			}
		}
	}
}
