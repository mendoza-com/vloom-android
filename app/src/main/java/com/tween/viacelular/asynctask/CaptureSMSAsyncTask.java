package com.tween.viacelular.asynctask;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import com.afollestad.materialdialogs.MaterialDialog;
import com.tween.viacelular.R;
import com.tween.viacelular.data.ApiConnection;
import com.tween.viacelular.data.Country;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.models.SuscriptionHelper;
import com.tween.viacelular.models.User;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

public class CaptureSMSAsyncTask extends AsyncTask<Void, Void, String>
{
	private MaterialDialog	progress;
	private Activity		activity;
	private boolean			displayDialog	= true;

	public CaptureSMSAsyncTask(Activity activity, boolean displayDialog)
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

			final CompaniesAsyncTask task = new CompaniesAsyncTask(activity, false);
			task.execute();
		}
		catch(Exception e)
		{
			System.out.println("CaptureSMSAsyncTask - Exception: " + e);

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
			Realm realm						= Realm.getDefaultInstance();
			RealmResults<Message> messages	= realm.where(Message.class).equalTo(Common.KEY_TYPE, Message.TYPE_SMS).findAll();
			User user						= realm.where(User.class).findFirst();

			if(messages.size() > 0)
			{
				realm.beginTransaction();
				messages.deleteAllFromRealm();
				realm.commitTransaction();
			}

			SharedPreferences preferences	= activity.getApplicationContext().getSharedPreferences(Common.KEY_PREF, Context.MODE_PRIVATE);
			//Corrección de countryCode en Mensajes capturados
			String country					= preferences.getString(Country.KEY_API, "");

			if(user != null)
			{
				if(StringUtils.isNotEmpty(user.getCountryCode()))
				{
					country							= user.getCountryCode();
					SharedPreferences.Editor editor	= preferences.edit();
					editor.putString(Country.KEY_API, country);
					editor.apply();
				}
			}

			if(ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED)
			{
				Uri inboxURI = Uri.parse("content://sms/inbox");
				/**
				 * Available fields:
				 * @long _id
				 * @long thread_id
				 * @String address
				 * @String person
				 * @long date
				 * @long date_sent
				 * @int protocol
				 * @int read
				 * @int status
				 * @int type
				 * @String reply_path_present
				 * @String subject
				 * @String body
				 * @String service_center
				 * @int locked
				 * @int error_code
				 * @String creator
				 * @int seen
				 */
				String[] reqCols	= new String[]{"_id", "address", "body", "date", "date_sent", "read"};
				ContentResolver cr	= activity.getContentResolver();
				Cursor cursor		= cr.query(inboxURI, reqCols, null, null, null);

				if(cursor != null)
				{
					if(cursor.getCount() > 0)
					{
						cursor.moveToFirst();

						do
						{
							//de 3 a 7 caracteres en el address para empresas con número corto, pueden ser texto ejemplo "Personal"
							String address		= cursor.getString(cursor.getColumnIndexOrThrow("address")).replace("+", "");
							String body			= cursor.getString(cursor.getColumnIndexOrThrow("body"));
							String date			= cursor.getString(cursor.getColumnIndexOrThrow("date"));
							String date_sent	= cursor.getString(cursor.getColumnIndexOrThrow("date_sent"));
							int read			= cursor.getInt(cursor.getColumnIndexOrThrow("read"));

							//Se incorpora lectura de sms personales
							if(StringUtils.isPhoneNumber(address))
							{
								RealmResults<Message> notifications = null;

								if(date.equals(date_sent))
								{
									notifications = realm.where(Message.class).equalTo(Message.KEY_CHANNEL, address).equalTo(Common.KEY_TYPE, Message.TYPE_SMS)
														.equalTo(Message.KEY_CREATED, Long.valueOf(date)).findAll();
								}
								else
								{
									//Prueba antes se usaba un in con los dos valores de date
									notifications = realm.where(Message.class).equalTo(Message.KEY_CHANNEL, address).equalTo(Common.KEY_TYPE, Message.TYPE_SMS)
														.equalTo(Message.KEY_CREATED, Long.valueOf(date)).or().equalTo(Message.KEY_CREATED, Long.valueOf(date_sent)).findAll();
								}

								if(notifications.size() == 0)
								{
									Message notification = new Message();
									notification.setMsg(body);

									if(StringUtils.isNotEmpty(date_sent))
									{
										notification.setCreated(Long.valueOf(date_sent));
									}
									else
									{
										notification.setCreated(Long.valueOf(date));
									}

									SharedPreferences.Editor editor	= preferences.edit();
									editor.putInt(Common.KEY_LAST_MSGID, preferences.getInt(Common.KEY_LAST_MSGID, 1) + 1);
									editor.apply();
									notification.setMsgId(String.valueOf(preferences.getInt(Common.KEY_LAST_MSGID, 1)));
									Suscription client				= null;
									notification.setType(Message.TYPE_SMS);

									if(body.toUpperCase().contains(activity.getString(R.string.app_name).toUpperCase()) || body.toUpperCase().contains("VIACELULAR"))
									{
										//Optimización para evitar bucle por un registro
										notification.setCompanyId(Suscription.COMPANY_ID_VC_MONGO);
									}
									else
									{
										int coincidenceNumber	= 0;
										client					= null;
										//Re-estructuración para mejorar clasificación de sms
										//1- buscamos company con el número del que vino el sms
										RealmResults<Suscription> companiesWithNumber = realm.where(Suscription.class).contains(Suscription.KEY_NUMBERS, address, Case.INSENSITIVE).findAll();
										//2- verificamos coincidencias (companies que compartan el número)
										if(companiesWithNumber.size() > 1)
										{
											//Hay ocurrencias, revisaremos las keywords
											for(Suscription company : companiesWithNumber)
											{
												//3- verificamos si el mensaje contiene el nombre de la company
												if(body.toUpperCase().contains(company.getName().toUpperCase()))
												{
													client = company;
													break;
												}
												else
												{
													//4- verificamos si el mensaje contiene alguna de las keywords
													if(StringUtils.containsKeywords(body, company.getKeywords()))
													{
														client = company;
														break;
													}
												}
											}
										}
										else
										{
											if(companiesWithNumber.size() == 1)
											{
												//Encontró una única company
												client = companiesWithNumber.get(0);
											}
										}

										if(client != null)
										{
											notification.setCompanyId(client.getCompanyId());
										}
										else
										{
											client = realm.where(Suscription.class).equalTo(Common.KEY_NAME, address, Case.INSENSITIVE).findFirst();

											if(client == null)
											{
												//No existe este número corto en la db, generamos company fantasma
												client	= SuscriptionHelper.createPhantom(address, activity, country);
											}

											notification.setCompanyId(client.getCompanyId());
										}

										System.out.println("Para: "+address+" hay "+companiesWithNumber.size()+" coincidencias y se asigno: "+client.getName());
									}

									//Si el mensaje es personal el status es 6 - Personal
									if(StringUtils.isCompanyNumber(address))
									{
										if(read == 1)
										{
											notification.setStatus(Message.STATUS_READ);
										}
										else
										{
											notification.setStatus(Message.STATUS_RECEIVE);
										}
									}
									else
									{
										notification.setStatus(Message.STATUS_PERSONAL);
									}

									notification.setKind(Message.KIND_TEXT);
									notification.setChannel(address);
									notification.setCountryCode(country);
									notification.setFlags(Message.FLAGS_SMS);
									notification.setPhone(preferences.getString(User.KEY_PHONE, ""));
									realm.beginTransaction();
									realm.copyToRealmOrUpdate(notification);
									realm.commitTransaction();
								}
							}
						}
						while(cursor.moveToNext());
						cursor.close();
						result = ApiConnection.OK;

						//Agregado para evitar reproceso si el usuario hizo back en la pantalla de verificación del código
						SharedPreferences.Editor editor	= preferences.edit();
						editor.putBoolean(Common.KEY_PREF_CAPTURED, true);
						editor.apply();
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("CaptureSMSAsyncTask - Exception: " + e);

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

			//Agregado para enviar los sms recibidos a la api, se movió para chorear sin necesidad de validar (siempre que haya sms)
			if(result.equals(ApiConnection.OK))
			{
				final ConnectApiSMSAsyncTask task	= new ConnectApiSMSAsyncTask(activity, false);
				task.execute();
			}
		}
		catch(Exception e)
		{
			System.out.println("CaptureSMSAsyncTask - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		super.onPostExecute(result);
	}
}