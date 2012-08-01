package de.itagile.isilmelindwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.Time;
import android.view.View;
import android.widget.RemoteViews;
import de.itagile.isilmelindwidget.IsilmelindDbOpenHelper.ScheduleTable;
import de.itagile.isilmelindwidget.IsilmelindDbOpenHelper.OccupationTable;

public class IsilmelindWidgetProvider extends AppWidgetProvider {

	private static final String AUTHORITY = "de.itagile.isilmelind.contentprovider";

	private static final String OCCUPATIONS_BASE_PATH = "occupations";

	private static final String SCHEDULES_BASE_PATH = "schedules";

	public static final Uri OCCUPATIONS_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + OCCUPATIONS_BASE_PATH);

	public static final Uri SCHEDULES_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + SCHEDULES_BASE_PATH);

	public static final String BUTTON_CLICKED_ACTION = "buttonClickedAction";

	public static final String INTEND_OCCUPATION_ID = "intendButtonId";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		Cursor cursor = context.getContentResolver().query(
				OCCUPATIONS_CONTENT_URI, null, null, null, null);

		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.main);
		ComponentName widget = new ComponentName(context,
				IsilmelindWidgetProvider.class);

		String buttonText = "";
		int[] buttonIds = { R.id.button1, R.id.button2, R.id.button3,
				R.id.button4, R.id.button5, R.id.button6 };
		for (int buttonId : buttonIds) {
			if (buttonId == R.id.button1 && cursor.moveToFirst()
					|| cursor.moveToNext()) {
				buttonText = cursor.getString(cursor
						.getColumnIndex(OccupationTable.COLUMN_NAME));
				int occupationId = cursor.getInt(cursor
						.getColumnIndex(OccupationTable._ID));

				remoteViews.setTextViewText(buttonId, buttonText);

				Intent clickIntent = new Intent(context,
						IsilmelindWidgetProvider.class);
				clickIntent.setAction(BUTTON_CLICKED_ACTION);
				clickIntent.putExtra(INTEND_OCCUPATION_ID, occupationId);

				PendingIntent pendingIntent = PendingIntent.getBroadcast(
						context, buttonId, clickIntent, 0);
				remoteViews.setOnClickPendingIntent(buttonId, pendingIntent);
			} else {
				remoteViews.setInt(buttonId, "setVisibility", View.INVISIBLE);
			}
			appWidgetManager.updateAppWidget(buttonId, remoteViews);

		}
		appWidgetManager.updateAppWidget(widget, remoteViews);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(BUTTON_CLICKED_ACTION)) {

			String whereClause = ScheduleTable.COLUMN_START
					+ " is not null and " + ScheduleTable.COLUMN_END
					+ " is null";

			Cursor cursor = context.getContentResolver().query(
					SCHEDULES_CONTENT_URI, null, whereClause, null, null);

			if (cursor.moveToFirst()) {
				Time endTime = new Time();
				endTime.setToNow();
				
				int unfinishedScheduleId = cursor.getInt(cursor.getColumnIndex(ScheduleTable._ID));
				ContentValues values = new ContentValues();
				values.put(ScheduleTable.COLUMN_END, endTime.format2445());
				
				String whereIdClause =  ScheduleTable._ID + " = " + unfinishedScheduleId;
				context.getContentResolver().update(SCHEDULES_CONTENT_URI, values, whereIdClause, null);
			}

			Time startTime = new Time();
			startTime.setToNow();

			int occupationId = intent.getIntExtra(INTEND_OCCUPATION_ID, 0);

			ContentValues values = new ContentValues();
			values.put(ScheduleTable.COLUMN_START, startTime.format2445());
			values.put(ScheduleTable.COLUMN_OCCUPATION_ID, occupationId);
			context.getContentResolver().insert(SCHEDULES_CONTENT_URI,
					values);
		} else {
			super.onReceive(context, intent);
		}
	}

}
