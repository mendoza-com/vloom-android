package com.tween.viacelular.asynctask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.tween.viacelular.R;
import com.tween.viacelular.activities.CodeActivity;
import com.tween.viacelular.activities.VerifyCodeActivity;
import com.tween.viacelular.models.ConnectedAccount;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.User;
import com.tween.viacelular.models.UserHelper;
import com.tween.viacelular.utils.ApiConnection;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Locale;
import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Manejador para registrar ante api el usuario de la app
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar)
 */
public class RegisterPhoneAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog	progress;
	private Activity		activity;
	private String			phone;
	private boolean			needRedirect	= true;
	private boolean			freePass		= false;

	public RegisterPhoneAsyncTask(Activity activity, String phone)
	{
		this.activity	= activity;
		this.phone		= phone;
	}

	public RegisterPhoneAsyncTask(Activity activity, String phone, boolean needRedirect)
	{
		this.activity		= activity;
		this.phone			= phone;
		this.needRedirect	= needRedirect;
	}

	public RegisterPhoneAsyncTask(boolean freePass, Activity activity, String phone)
	{
		this.freePass	= freePass;
		this.activity	= activity;
		this.phone		= phone;
	}

	protected void onPreExecute()
	{
		try
		{
			if(needRedirect)
			{
				if(progress != null)
				{
					if(progress.isShowing())
					{
						progress.cancel();
					}
				}

				progress = new MaterialDialog.Builder(activity)
					.title(R.string.register_dialog)
					.cancelable(false)
					.content(R.string.please_wait)
					.progress(true, 0)
					.show();
			}
		}
		catch(Exception e)
		{
			Utils.logError(activity, "RegisterPhoneAsyncTask:onPreExecute - Exception:", e);
		}
	}

	@Override
	protected String doInBackground(Void... params)
	{
		String result = "";

		try
		{
			//Modificación para contemplar migración a Realm
			SharedPreferences preferences		= activity.getApplicationContext().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			Realm realm							= Realm.getDefaultInstance();
			final RealmResults<User> users		= realm.where(User.class).findAll();
			String gcmId						= preferences.getString(User.KEY_GCMID, "");
			realm.executeTransaction(new Realm.Transaction()
			{
				@Override
				public void execute(Realm realm)
				{
					users.deleteAllFromRealm();
				}
			});

			if(StringUtils.isEmpty(gcmId))
			{
				gcmId = FirebaseInstanceId.getInstance().getToken();

				if(StringUtils.isEmpty(gcmId))
				{
					gcmId = User.FAKE_GCMID_EMULATOR;
				}

				preferences.edit().putString(User.KEY_GCMID, gcmId).apply();
			}

			//Se quitó lo referido a isp para obtener desde clase sin consultar a la db
			JSONObject jsonSend		= new JSONObject();
			JSONObject info			= new JSONObject();
			JSONObject jsonResult;
			String email			= preferences.getString(User.KEY_EMAIL, "");

			if(StringUtils.isEmpty(email))
			{
				ConnectedAccount connectedAccount = realm.where(ConnectedAccount.class).equalTo(Common.KEY_TYPE, ConnectedAccount.TYPE_GOOGLE).findFirst();

				if(connectedAccount != null)
				{
					email = connectedAccount.getName();
				}
			}

			if(StringUtils.isNotEmpty(email))
			{
				info.put(User.KEY_EMAIL, email);
			}

			if(StringUtils.isNotEmpty(preferences.getString(Land.KEY_API, "")))
			{
				info.put(Land.KEY_API, preferences.getString(Land.KEY_API, ""));
			}

			//Modificado para obtener operadora sin consultar en la db
			if(StringUtils.isNotEmpty(Utils.getCarrierName(activity.getApplicationContext())))
			{
				info.put(User.KEY_CARRIER, Utils.getCarrierName(activity.getApplicationContext()));
			}

			//Agregado para enviar Sistema Operativo
			info.put("os", "android");
			info.put("countryLanguage", Locale.getDefault().getLanguage()+"-"+Locale.getDefault().getCountry());

			//Agregado para trackear datos del usuario en Fabric
			Fabric.with(activity, new Crashlytics());
			Crashlytics.setUserEmail(email);
			Crashlytics.setUserIdentifier(phone);

			//TODO Probablemente en algún momento sea necesario agregar la info del device del usuario
			jsonSend.put(User.KEY_PHONE, phone);

			//Modificación para incoporar api de llamada desde esta misma AsyncTask
			if(needRedirect)
			{
				jsonSend.put(User.KEY_GCMID, gcmId);
				jsonSend.put(Common.KEY_INFO, info);

				jsonResult = new JSONObject(ApiConnection.request(ApiConnection.USERS, activity, ApiConnection.METHOD_POST, preferences.getString(Common.KEY_TOKEN, ""),
											jsonSend.toString()));
			}
			else
			{
				jsonResult = new JSONObject(ApiConnection.request(ApiConnection.CALLME, activity, ApiConnection.METHOD_POST, preferences.getString(Common.KEY_TOKEN, ""),
											jsonSend.toString()));
			}

			result = ApiConnection.checkResponse(activity.getApplicationContext(), jsonResult);

			if(result.equals(ApiConnection.OK) && needRedirect)
			{
				JSONObject jsonData = jsonResult.getJSONObject(Common.KEY_CONTENT);

				if(jsonData != null)
				{
					SharedPreferences.Editor editor = preferences.edit();
					editor.putString(User.FAKE_USER, jsonData.toString());
					User userParsed = UserHelper.parseJSON(jsonData, false, null);

					if(userParsed != null)
					{
						editor.putString(User.KEY_PHONE, phone);

						if(StringUtils.isNotEmpty(userParsed.getUserId()))
						{
							editor.putString(User.KEY_API, userParsed.getUserId());
						}

						editor.putBoolean(Common.KEY_PREF_LOGGED, true);
						editor.putBoolean(Common.KEY_PREF_CHECKED, false);
						//Agregado para resetear el contador de llamadas
						editor.putBoolean(Common.KEY_PREF_CALLME, true);
						editor.putInt(Common.KEY_PREF_CALLME_TIMES, 0);
						result = ApiConnection.OK;
					}
					else
					{
						if(needRedirect)
						{
							result = activity.getString(R.string.response_invalid);
						}
					}

					editor.apply();
				}
				else
				{
					result = activity.getString(R.string.response_invalid);
				}
			}
		}
		catch(JSONException e)
		{
			Utils.logError(activity, "RegisterPhoneAsyncTask:doInBackground - JSONException:", e);
		}
		catch(Exception e)
		{
			Utils.logError(activity, "RegisterPhoneAsyncTask:doInBackground - Exception:", e);
		}

		return result;
	}

	@Override
	protected void onPostExecute(String result)
	{
		try
		{
			//Modificación para no mostrar error al no conectar api de tts y evitar solicitar llamadas innecesarias
			if(needRedirect)
			{
				if(progress != null)
				{
					if(progress.isShowing())
					{
						progress.cancel();
					}
				}

				if(result.equals(ApiConnection.OK) && freePass)
				{
					Intent intent = new Intent(activity.getApplicationContext(), VerifyCodeActivity.class);
					activity.startActivity(intent);
					activity.finish();
				}
				else
				{
					if(result.equals(ApiConnection.OK))
					{
						Intent intent = new Intent(activity.getApplicationContext(), CodeActivity.class);
						activity.startActivity(intent);
						activity.finish();
					}
					else
					{
						Toast.makeText(activity.getApplicationContext(), result, Toast.LENGTH_LONG).show();
					}
				}
			}
			else
			{
				//Agregado para limitar a dos llamdas únicamente
				SharedPreferences preferences	= activity.getApplicationContext().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor	= preferences.edit();
				int times						= preferences.getInt(Common.KEY_PREF_CALLME_TIMES, 0);
				editor.putInt(Common.KEY_PREF_CALLME_TIMES, times + 1);

				if(times >= 2)
				{
					editor.putBoolean(Common.KEY_PREF_CALLME, false);
				}

				editor.apply();
			}
		}
		catch(Exception e)
		{
			Utils.logError(activity, "RegisterPhoneAsyncTask:onPostExecute - Exception:", e);
		}

		super.onPostExecute(result);
	}
}