/**
 * @author dawson dong
 */

package com.arrg.app.uapplock.util.kisstools.helper;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;
import android.os.Looper;

import com.arrg.app.uapplock.util.kisstools.utils.FileUtil;
import com.arrg.app.uapplock.util.kisstools.utils.LogUtil;
import com.arrg.app.uapplock.util.kisstools.utils.MediaUtil;
import com.arrg.app.uapplock.util.kisstools.utils.StringUtil;
import com.arrg.app.uapplock.util.kisstools.utils.TimeUtil;
import com.arrg.app.uapplock.util.kisstools.utils.ToastUtil;

import java.lang.Thread.UncaughtExceptionHandler;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class CrashHelper implements UncaughtExceptionHandler {

	public static final String TAG = "CrashHelper";

	@SuppressLint("NewApi")
	public static final void init(Application application) {
		if (Thread.getDefaultUncaughtExceptionHandler() instanceof CrashHelper) {
			return;
		}

		CrashHelper helper = new CrashHelper();
		Thread.setDefaultUncaughtExceptionHandler(helper);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		final String name = thread.getName();

		String crashMessage = StringUtil.stringify(ex);
		StringBuilder builder = new StringBuilder();
		builder.append("crash in thread " + name);
		builder.append("crash detail message:\n");
		builder.append(crashMessage);

		long time = System.currentTimeMillis();
		String format = TimeUtil.FILE_FORMAT;
		String fileName = TimeUtil.format(time, format) + ".txt";
		String crashFolder = MediaUtil.getFileDir("crash_log");
		String filePath = crashFolder + "/" + fileName;
		LogUtil.e(TAG, builder.toString());
		FileUtil.write(filePath, builder.toString());
		showMessage("application cashed!");
		try {
			Thread.sleep(2500);
		} catch (Exception e) {
		}
		System.exit(1);
	}

	private void showMessage(final String message) {
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				ToastUtil.show(message);
				Looper.loop();
			}
		}.start();
	}
}
