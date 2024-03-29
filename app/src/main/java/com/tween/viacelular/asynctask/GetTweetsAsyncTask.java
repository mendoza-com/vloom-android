package com.tween.viacelular.asynctask;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.activities.CardViewActivity;
import com.tween.viacelular.models.Land;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.Migration;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.User;
import com.tween.viacelular.utils.ApiConnection;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import io.realm.Realm;

/**
 * Manejador para obtener vía api contenido de Twitter sobre la empresa
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar) on 15/06/2016
 */
public class GetTweetsAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog	progress;
	private Context			context;
	private boolean			displayDialog	= false;
	private String			companyId		= "";

	public GetTweetsAsyncTask(final Context context, final boolean displayDialog, final String companyId)
	{
		this.context		= context;
		this.displayDialog	= displayDialog;
		this.companyId		= companyId;
	}

	protected void onPreExecute()
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

				progress = new MaterialDialog.Builder(context)
					.title(R.string.landing_card_loading_header)
					.cancelable(false)
					.content(R.string.social)
					.progress(true, 0)
					.show();
			}

			Migration.getDB(context);
		}
		catch(Exception e)
		{
			Utils.logError(context, "GetTweetsAsyncTask:onPreExecute - Exception:", e);
		}
	}

	@Override
	protected String doInBackground(Void... params)
	{
		String result = "";

		try
		{
			Realm realm	= Realm.getDefaultInstance();
			User user	= realm.where(User.class).findFirst();

			if(user != null)
			{
				if(StringUtils.isIdMongo(user.getUserId()))
				{
					SharedPreferences preferences	= context.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
					String url						= ApiConnection.COMPANIES_SOCIAL.replace(Suscription.KEY_API, companyId)+"/"+user.getUserId();
					JSONObject jsonResult			= new JSONObject(ApiConnection.getRequest(url, context, preferences.getString(Common.KEY_TOKEN, ""), "", Common.TIMEOUT_API));
					result							= ApiConnection.checkResponse(context, jsonResult);
					final Suscription suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
					int notificationId				= preferences.getInt(Common.KEY_LAST_MSGID, 0);
					String dateText					= context.getString(R.string.social_date);

					if(result.equals(ApiConnection.OK))
					{
						if(!jsonResult.isNull(Common.KEY_CONTENT))
						{
							if(!jsonResult.getJSONObject(Common.KEY_CONTENT).isNull(Common.KEY_DATA) && jsonResult.getJSONObject(Common.KEY_CONTENT).getBoolean("success"))
							{
								String date		= jsonResult.getJSONObject(Common.KEY_CONTENT).getJSONObject(Common.KEY_DATA).getString("date");
								notificationId	= notificationId+1;
								final Message message	= new Message();
								message.setMsgId(String.valueOf(notificationId));
								message.setMsg(jsonResult.getJSONObject(Common.KEY_CONTENT).getJSONObject(Common.KEY_DATA).getString("tweet"));
								message.setCompanyId(suscription.getCompanyId());
								message.setChannel(suscription.getName());
								message.setType(context.getString(R.string.social_high));
								message.setStatus(Message.STATUS_READ);
								message.setPhone(preferences.getString(User.KEY_PHONE, ""));
								message.setCountryCode(preferences.getString(Land.KEY_API, ""));
								message.setFlags(Message.FLAGS_PUSH);
								message.setCreated(System.currentTimeMillis());
								message.setDeleted(Common.BOOL_NO);
								message.setKind(Message.KIND_TWITTER);
								message.setLink(jsonResult.getJSONObject(Common.KEY_CONTENT).getJSONObject(Common.KEY_DATA).getString("background"));//Momentaneo hasta tener link-preview
								message.setSubMsg(jsonResult.getJSONObject(Common.KEY_CONTENT).getJSONObject(Common.KEY_DATA).getString("description"));
								message.setSocialId(jsonResult.getJSONObject(Common.KEY_CONTENT).getJSONObject(Common.KEY_DATA).getString("id"));
								message.setSocialName(jsonResult.getJSONObject(Common.KEY_CONTENT).getJSONObject(Common.KEY_DATA).getString("name"));
								message.setSocialAccount(jsonResult.getJSONObject(Common.KEY_CONTENT).getJSONObject(Common.KEY_DATA).getString("twitter"));
								message.setSocialShares(jsonResult.getJSONObject(Common.KEY_CONTENT).getJSONObject(Common.KEY_DATA).getInt("retweets"));
								message.setSocialLikes(jsonResult.getJSONObject(Common.KEY_CONTENT).getJSONObject(Common.KEY_DATA).getInt("favs"));
								SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
								SimpleDateFormat old = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy",Locale.ENGLISH);
								old.setLenient(true);

								Date date2 = null;
								try
								{
									date2 = old.parse(date);
								}
								catch(ParseException e)
								{
									Utils.logError(context, "GetTweetsAsyncTask:doInBackground:parseDate - ParseException:", e);
								}

								message.setSocialDate(dateText.replace("dd/mm/yyyy", sdf.format(date2)));
								realm.executeTransaction(new Realm.Transaction()
								{
									@Override
									public void execute(Realm realm)
									{
										realm.copyToRealmOrUpdate(message);
										suscription.setLastSocialUpdated(System.currentTimeMillis());
									}
								});
								SharedPreferences.Editor editor = preferences.edit();
								editor.putInt(Common.KEY_LAST_MSGID, notificationId);
								editor.apply();
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "GetTweetsAsyncTask:doInBackground - Exception:", e);
		}

		return result;
	}

	@Override
	protected void onPostExecute(String s)
	{
		super.onPostExecute(s);
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

			new ConfirmReadingAsyncTask(context, false, companyId, "", Message.STATUS_READ).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			Intent intent = new Intent(context, CardViewActivity.class);
			intent.putExtra(Common.KEY_ID, companyId);
			context.startActivity(intent);
		}
		catch(Exception e)
		{
			Utils.logError(context, "GetTweetsAsyncTask:onPostExecute - Exception:", e);
		}
	}
}