package igrek.songbook.service.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SongsDbHelper extends SQLiteOpenHelper {
	
	public static final int DATABASE_VERSION = 1;
	
	public SongsDbHelper(Context context, String databaseName) {
		super(context, databaseName, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
	
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}