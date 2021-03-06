package cn.yo2.aquarium.pocketvoa;

import org.acra.CrashReportingApplication;
import org.acra.ErrorReporter;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import cn.yo2.aquarium.logutils.MyLog;
import cn.yo2.aquarium.pocketvoa.parser.IDataSource;

public class App extends CrashReportingApplication {
	private static final String CLASSTAG = App.class.getSimpleName();

	private static final String DEFAULT_CHARSET = "utf-8";
	
	public static final String SITE_HOST = "http://aquarium-apps.heroku.com/";
	public static final String URL_MY_APPS = SITE_HOST + "my_apps.html";
	public static final String URL_CHECK_UPGRADE = SITE_HOST + "pocketvoa-version";
	public static final String URL_DOWNLOAD_URL = SITE_HOST + "download/";
	

	public SharedPreferences mSharedPreferences;
	
	@Override
	public String getFormId() {
		
		return "dFRJZDI3QWN0bnN6emRBb2tnMWRwSEE6MQ";
	}
	
	@Override
	public Bundle getCrashResources() {
	    Bundle result = new Bundle();
	    result.putInt(RES_NOTIF_TICKER_TEXT, R.string.crash_notif_ticker_text);
	    result.putInt(RES_NOTIF_TITLE, R.string.crash_notif_title);
	    result.putInt(RES_NOTIF_TEXT, R.string.crash_notif_text);
	    result.putInt(RES_NOTIF_ICON, android.R.drawable.stat_notify_error); // optional. default is a warning sign
	    result.putInt(RES_DIALOG_TEXT, R.string.crash_dialog_text);
	    result.putInt(RES_DIALOG_ICON, android.R.drawable.ic_dialog_info); //optional. default is a warning sign
	    result.putInt(RES_DIALOG_TITLE, R.string.crash_dialog_title); // optional. default is your application name 
	    result.putInt(RES_DIALOG_COMMENT_PROMPT, R.string.crash_dialog_comment_prompt); // optional. when defined, adds a user text field input with this text resource as a label
	    result.putInt(RES_DIALOG_OK_TOAST, R.string.crash_dialog_ok_toast); // optional. Displays a Toast when the user accepts to send a report ("Thank you !" for example)
	    return result;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		MyLog.initLog("Pocket VOA", true, false);

		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		// since version 1.1.0 remove multiple datasource support.
//		mDataSource = getDataSourceFromPrefs(mSharedPreferences);
		
		mDataSource = getDefaultDataSource();
		mDataSource.init(getMaxCountFromPrefs(mSharedPreferences));
		
		ErrorReporter.getInstance().addCustomData("version", getVersionName());
	}
	
	private String getVersionName() {
		return getString(R.string.app_version);
	}

	public final ResponseHandler mResponseHandler = new ResponseHandler(
			DEFAULT_CHARSET);

	public final ListGenerator mListGenerator = new ListGenerator(
			mResponseHandler);

	public final PageGenerator mPageGenerator = new PageGenerator(
			mResponseHandler);

	public IDataSource mDataSource;

	public Integer getMaxCountFromPrefs(SharedPreferences sharedPreferences) {
		return Integer.valueOf(sharedPreferences.getString(
				getString(R.string.prefs_list_count_key), "10"));
	}

	private IDataSource getDefaultDataSource() {
		return new cn.yo2.aquarium.pocketvoa.parser.voa51.Voa51DataSource();
	}

	public IDataSource getDataSourceFromPrefs(
			SharedPreferences sharedPreferences) {
		String datasourceStr = sharedPreferences.getString(
				getString(R.string.prefs_datasource_key), "");
//		Log.d(CLASSTAG, "datasource prefs -- " + datasourceStr);
		IDataSource dataSource = null;
		if (TextUtils.isEmpty(datasourceStr)) {
			dataSource = getDefaultDataSource();

		} else {
			try {
				dataSource = (IDataSource) Class.forName(datasourceStr)
						.newInstance();
			} catch (IllegalAccessException e) {
				Log.e(CLASSTAG, "Error in create DataSource", e);
				dataSource = getDefaultDataSource();
			} catch (InstantiationException e) {
				Log.e(CLASSTAG, "Error in create DataSource", e);
				dataSource = getDefaultDataSource();
			} catch (ClassNotFoundException e) {
				Log.e(CLASSTAG, "Error in create DataSource", e);
				dataSource = getDefaultDataSource();
			}
		}

		dataSource.init(getMaxCountFromPrefs(mSharedPreferences));

		return dataSource;
	}
	
	public static DefaultHttpClient createHttpClient() {
		HttpParams params = new BasicHttpParams();

        // Turn off stale checking.  Our connections break all the time anyway,
        // and it's not worth it to pay the penalty of checking every time.
        HttpConnectionParams.setStaleCheckingEnabled(params, false);

        // Default connection and socket timeout of 20 seconds.  Tweak to taste.
        HttpConnectionParams.setConnectionTimeout(params, 20 * 1000);
        HttpConnectionParams.setSoTimeout(params, 20 * 1000);
        HttpConnectionParams.setSocketBufferSize(params, 8192);

        HttpClientParams.setRedirecting(params, true);

        // Set the specified user agent and register standard protocols.
        HttpProtocolParams.setUserAgent(params, "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1.5) Gecko/20091102 Firefox/3.5.5 GTB6 (.NET CLR 3.5.30729)");
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http",
                PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https",
                SSLSocketFactory.getSocketFactory(), 443));

        ClientConnectionManager manager =
                new ThreadSafeClientConnManager(params, schemeRegistry);
        
        return new DefaultHttpClient(manager, params);
	}
	
//	private static final int CONN_TIME_OUT = 1000 * 20; // millis
//	private static final int READ_TIME_OUT = 1000 * 20; // millis
//	private static final int MAX_TOTAL_CONN = 10;
	
//	private DefaultHttpClient setupHttpClient() {
//		HttpParams params = new BasicHttpParams();
//		HttpConnectionParams.setConnectionTimeout(params, CONN_TIME_OUT);
//		HttpConnectionParams.setSoTimeout(params, READ_TIME_OUT);
//		ConnManagerParams.setMaxTotalConnections(params, MAX_TOTAL_CONN);
//		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
//
//		SchemeRegistry schemeRegistry = new SchemeRegistry();
//		schemeRegistry.register(new Scheme("http", PlainSocketFactory
//				.getSocketFactory(), 80));
//		schemeRegistry.register(new Scheme("https", SSLSocketFactory
//				.getSocketFactory(), 443));
//
//		ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(
//				params, schemeRegistry);
//
//		DefaultHttpClient client = new DefaultHttpClient(cm, params);
//
//		client.addRequestInterceptor(new HttpRequestInterceptor() {
//
//			public void process(HttpRequest request, HttpContext context)
//					throws HttpException, IOException {
//				request.addHeader("User-Agent",
//								"Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1.5) Gecko/20091102 Firefox/3.5.5 GTB6 (.NET CLR 3.5.30729)");
//
//			}
//		});
//		return client;
//	}

}
