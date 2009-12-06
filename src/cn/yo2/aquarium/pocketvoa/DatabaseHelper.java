package cn.yo2.aquarium.pocketvoa;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DatabaseHelper {
	private static final String CLASSTAG = DatabaseHelper.class.getSimpleName();

	private static final String EXEC_SQL_PREFIX = "SQL EXEC -- ";

	private static final String DB_NAME = "pocketvoa.db";

	private static final int DB_VER = 1;

	static final String T_ARTICLES = "articles";

	static final String C_ID = Article.K_ID;
	static final String C_TYPE = Article.K_TYPE; 
	static final String C_SUBTYPE = Article.K_SUBTYPE;
	static final String C_TITLE = Article.K_TITLE;
	static final String C_DATE = Article.K_DATE;
	static final String C_URL = Article.K_URL;
	static final String C_MP3 = Article.K_MP3;

	static final String[] COLS = { C_ID, C_TYPE, C_SUBTYPE, C_TITLE, C_DATE, C_URL,
			C_MP3, };

	private static final String CREATE_TABLE_SQL = "CREATE TABLE " + T_ARTICLES
			+ "(" 
			+ C_ID      + " INTEGER PRIMARY KEY AUTOINCREMENT," 
			+ C_TYPE    + " TEXT," 
			+ C_SUBTYPE + " TEXT," 
			+ C_TITLE   + " TEXT," 
			+ C_DATE    + " INTEGER,"
			+ C_URL     + " TEXT," 
			+ C_MP3     + " TEXT" 
			+ ");";
	
	private static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS " + T_ARTICLES + ";";

	private DatabaseOpenHelper mDbHelper;
	private SQLiteDatabase mDb;
	private Context mCtx;

	public DatabaseHelper(Context context) {
		mCtx = context;
		mDbHelper = new DatabaseOpenHelper(mCtx);
	}

	public void open() {
		if (mDb == null)
			mDb = mDbHelper.getWritableDatabase();
	}

	public void close() {
		if (mDb != null) {
			mDb.close();
			mDb = null;
		}
	}
	
	public Cursor queryArticles(String type, String subtype) {
		String sel = null;
		String[] selArgs = null;
		if (type != null && subtype != null) {
			sel = C_TYPE + "=? and " + C_SUBTYPE + "=?";
			selArgs = new String[] {type, subtype};
		}
		else if (type != null) {
			sel = C_TYPE + "=?";
			selArgs = new String[] {type};
		}
		else if (subtype != null) {
			sel = C_SUBTYPE + "=?";
			selArgs = new String[] {subtype};
		}
		
		return mDb.query(T_ARTICLES, COLS, sel, selArgs, null, null, C_DATE + " desc");
	}
	
	/**
	 * 
	 * @param id
	 * @return the Article with the id or null if cannot be found
	 */
	public Article queryArticle(long id) {
		Article article = null;
		Cursor cursor = mDb.query(T_ARTICLES, COLS, C_ID + "=" + id, null, null, null, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				article = new Article();
				article.id = id;
				article.type = cursor.getString(cursor.getColumnIndex(C_TYPE));
				article.subtype = cursor.getString(cursor.getColumnIndex(C_SUBTYPE));
				article.title = cursor.getString(cursor.getColumnIndex(C_TITLE));
				article.date = cursor.getString(cursor.getColumnIndex(C_DATE));
				article.url = cursor.getString(cursor.getColumnIndex(C_URL));
				article.mp3 = cursor.getString(cursor.getColumnIndex(C_MP3));
			}
			cursor.close();
		}
		
		return article;
	}
	
	public long createArticle(String title, String date, String type, String subtype, String url, String mp3) {
		ContentValues values = new ContentValues();
		
		values.put(C_TITLE, title);
		values.put(C_DATE, date);
		values.put(C_TYPE, type);
		values.put(C_SUBTYPE, subtype);
		values.put(C_URL, url);
		values.put(C_MP3, mp3);
		
		return mDb.insert(T_ARTICLES, null, values);
	}
	
	public int deleteArticle(long id) {
		return mDb.delete(T_ARTICLES, C_ID + "=?", new String[]{String.valueOf(id)});
	}
	
	private static class DatabaseOpenHelper extends SQLiteOpenHelper {

		DatabaseOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VER);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				Log.d(CLASSTAG, EXEC_SQL_PREFIX + CREATE_TABLE_SQL);
				db.execSQL(CREATE_TABLE_SQL);
			} catch (SQLException e) {
				Log.e(CLASSTAG, "Error when create tables", e);
			}

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log
					.w(CLASSTAG, "Upgrading database from version "
							+ oldVersion + " to " + newVersion
							+ ", which will destroy all old data");
			try {
				Log.d(CLASSTAG, EXEC_SQL_PREFIX + DROP_TABLE_SQL);
				db.execSQL(DROP_TABLE_SQL);
			} catch (SQLException e) {
				Log.e(CLASSTAG, "Error when drop tables", e);
			}

			onCreate(db);
		}
	}
}

class Article {
	static final String K_ID      = "_id";
	static final String K_TYPE    = "type"; 
	static final String K_SUBTYPE = "subtype";
	static final String K_TITLE   = "title";
	static final String K_DATE    = "date";
	static final String K_URL     = "url";
	static final String K_MP3     = "mp3";
	static final String K_TEXT    = "text";
	
	Long id;
	String type;
	String subtype;
	String text;
	String mp3;
	String date;
	String url;
	String title;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((mp3 == null) ? 0 : mp3.hashCode());
		result = prime * result + ((subtype == null) ? 0 : subtype.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Article other = (Article) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (mp3 == null) {
			if (other.mp3 != null)
				return false;
		} else if (!mp3.equals(other.mp3))
			return false;
		if (subtype == null) {
			if (other.subtype != null)
				return false;
		} else if (!subtype.equals(other.subtype))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Article [date=" + date + ", id=" + id + ", mp3=" + mp3
				+ ", subtype=" + subtype + ", text=" + text + ", title="
				+ title + ", type=" + type + ", url=" + url + "]";
	}	
	
	
}