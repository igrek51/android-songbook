package igrek.songbook.service.database;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import javax.inject.Inject;

import igrek.songbook.R;
import igrek.songbook.dagger.DaggerIoc;
import igrek.songbook.logger.Logger;
import igrek.songbook.logger.LoggerFactory;

public class SongsDbService {
	
	@Inject
	Activity activity;
	
	private SongsDbHelper dbHelper;
	private Logger logger = LoggerFactory.getLogger();
	
	public SongsDbService() {
		DaggerIoc.getFactoryComponent().inject(this);
		init();
	}
	
	public void init() {
		logger.debug("initializing database");
		String dbName = "android.resource://" + activity.getPackageName() + "/" + R.raw.songs;
		dbHelper = new SongsDbHelper(activity, dbName);
	}
	
	public void read() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		String[] projection = {
				"*"
		};
		
		Cursor cursor = db.query("songs",   // The table to query
				projection,             // The array of columns to return (pass null to get all)
				null,              // The columns for the WHERE clause
				null,          // The values for the WHERE clause
				null,                   // don't group the rows
				null,                   // don't filter by row groups
				null               // The sort order
		);
		
		while (cursor.moveToNext()) {
			long itemId = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
			logger.debug(itemId);
		}
		cursor.close();
	}
}
