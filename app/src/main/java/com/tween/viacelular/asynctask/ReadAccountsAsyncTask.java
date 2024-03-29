package com.tween.viacelular.asynctask;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.models.ConnectedAccount;
import com.tween.viacelular.models.User;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Manejador para actualización de cuentas asociadas al dispositivo
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar) on 17/06/2015
 */
public class ReadAccountsAsyncTask extends AsyncTask<Void, Void, Boolean>
{
	private MaterialDialog	progress;
	private Activity		activity;
	private boolean			displayDialog	= false;

	public ReadAccountsAsyncTask(Activity activity, boolean displayDialog)
	{
		this.activity		= activity;
		this.displayDialog	= displayDialog;
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
			Utils.logError(activity, "ReadAccountsAsyncTask:onPreExecute - Exception:", e);
		}
	}

	@Override
	protected Boolean doInBackground(Void... params)
	{
		try
		{
			if(ContextCompat.checkSelfPermission(activity, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED)
			{
				//Modificaciones para contemplar migración a Realm
				Realm realm						= Realm.getDefaultInstance();
				AccountManager accountManager	= AccountManager.get(activity);
				Account[] accounts				= accountManager.getAccounts();

				if(accounts.length > 0)
				{
					SharedPreferences preferences					= activity.getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
					final RealmResults<ConnectedAccount> results	= realm.where(ConnectedAccount.class).findAll();
					realm.executeTransaction(new Realm.Transaction()
					{
						@Override
						public void execute(Realm realm)
						{
							results.deleteAllFromRealm();
						}
					});

					for(final Account account : accounts)
					{
						realm.executeTransaction(new Realm.Transaction()
						{
							@Override
							public void execute(Realm realm)
							{
								realm.copyToRealmOrUpdate(new ConnectedAccount(account.name, account.type));
							}
						});

						if(account.type.equals(ConnectedAccount.TYPE_GOOGLE) && StringUtils.isEmpty(preferences.getString(User.KEY_EMAIL, "")))
						{
							SharedPreferences.Editor editor = preferences.edit();
							editor.putString(User.KEY_EMAIL, account.name);
							editor.apply();
						}
					}
				}
			}

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

			return true;
		}
		catch(Exception e)
		{
			Utils.logError(activity, "ReadAccountsAsyncTask:doInBackground - Exception:", e);
		}

		return false;
	}
}