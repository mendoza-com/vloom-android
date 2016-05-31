package com.tween.viacelular.asynctask;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.data.ApiConnection;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.LandHelper;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import io.realm.Realm;
import io.realm.RealmResults;

public class CountryAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog	progress;
	private Activity		activity;
	private boolean			displayDialog	= false;

	public CountryAsyncTask(Activity activity, boolean displayDialog)
	{
		this.activity		= activity;
		this.displayDialog	= displayDialog;
	}

	protected void onPreExecute()
	{
		try
		{
			//Agregado para no mostrar dialogo por llamada en cadena
			if(displayDialog)
			{
				if(progress != null)
				{
					if(progress.isShowing())
					{
						progress.cancel();
					}
				}

				progress = new MaterialDialog.Builder(activity)
					.title(R.string.progress_dialog)
					.cancelable(false)
					.content(R.string.please_wait)
					.progress(true, 0)
					.show();
			}
		}
		catch(Exception e)
		{
			System.out.println("CountryAsyncTask - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	protected String doInBackground(Void... params)
	{
		String result = "";

		try
		{
			//Modificaciones para contemplar migración a Realm
			Realm realm						= Realm.getDefaultInstance();
			RealmResults<Land> countries	= realm.where(Land.class).findAll();
			realm.beginTransaction();
			countries.deleteAllFromRealm();
			realm.commitTransaction();
			SharedPreferences preferences	= activity.getApplicationContext().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			JSONObject jsonResult			= new JSONObject(ApiConnection.request(ApiConnection.COUNTRIES, activity, ApiConnection.METHOD_GET, preferences.getString(Common.KEY_TOKEN, ""), ""));
			result							= ApiConnection.checkResponse(activity.getApplicationContext(), jsonResult);
			boolean parseLocal				= true;
			JSONObject jsonData				= null;
			JSONArray arrayKey				= null;

			if(result.equals(ApiConnection.OK))
			{
				jsonData = jsonResult.getJSONObject(Common.KEY_DATA);

				if(jsonData != null)
				{
					//Renombre de key para contemplar nuevo estándar en api get countries
					if(jsonData.has(Common.KEY_DATA))
					{
						arrayKey = jsonData.getJSONArray(Common.KEY_DATA);

						if(arrayKey != null)
						{
							if(arrayKey.length() > 0)
							{
								result		= ApiConnection.OK;
								LandHelper.parseList(arrayKey);
								parseLocal	= false;
							}
						}
					}
				}
			}

			//Modificación para contemplar caso backup en el que no esté disponible la api aún
			if(parseLocal)
			{
				String json = ApiConnection.loadJSONFromAsset(activity, "GETcountries.json");

				if(StringUtils.isNotEmpty(json))
				{
					jsonData	= new JSONObject(json);
					arrayKey	= jsonData.getJSONArray(Common.KEY_DATA);
					result		= ApiConnection.OK;
					LandHelper.parseList(arrayKey);
				}
				else
				{
					LandHelper.parseArray(activity);
				}
			}
		}
		catch(JSONException e)
		{
			System.out.println("CountryAsyncTask - JSONException: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
		catch(Exception e)
		{
			System.out.println("CountryAsyncTask - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return result;
	}

	@Override
	protected void onPostExecute(String result)
	{
		try
		{
			if(displayDialog)
			{
				if(progress != null)
				{
					if(progress.isShowing())
					{
						progress.cancel();
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("CountryAsyncTask - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		super.onPostExecute(result);
	}
}