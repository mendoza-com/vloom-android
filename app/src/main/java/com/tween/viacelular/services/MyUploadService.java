package com.tween.viacelular.services;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tween.viacelular.asynctask.AttachAsyncTask;
import com.tween.viacelular.interfaces.CallBackListener;
import com.tween.viacelular.models.Message;
import com.tween.viacelular.utils.ApiConnection;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import io.realm.Realm;

/**
 * Servicio para subir fotos a Firebase
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar) on 27/12/2016
 */
public class MyUploadService extends MyBaseTaskService
{
	public static final String	ACTION_UPLOAD		= "action_upload";
	public static final String	UPLOAD_COMPLETED	= "upload_completed";
	public static final String	UPLOAD_ERROR		= "upload_error";
	public static final String	EXTRA_FILE_URI		= "extra_file_uri";
	public static final String	EXTRA_DOWNLOAD_URL	= "extra_download_url";
	public String				linkOne				= "";
	public String				linkTwo				= "";
	public String				linkThree			= "";
	public String				comment				= "";
	private StorageReference	mStorageRef;
	private int					field				= 1;
	private Uri					fileUri, downloadUri;

	@Override
	public void onCreate()
	{
		super.onCreate();
		mStorageRef = FirebaseStorage.getInstance().getReference();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if(ACTION_UPLOAD.equals(intent.getAction()))
		{
			fileUri	= intent.getParcelableExtra(EXTRA_FILE_URI);
			String id	= intent.getStringExtra(Common.KEY_ID);
			uploadFromUri(fileUri, id);
		}

		return START_REDELIVER_INTENT;
	}

	// [START upload_from_uri]
	private void uploadFromUri(final Uri fileUri, final String id)
	{
		try
		{
			final Realm realm	= Realm.getDefaultInstance();
			Message message		= realm.where(Message.class).equalTo(Message.KEY_API, id).findFirst();
			String fileName		= id;

			if(!StringUtils.isIdMongo(id))
			{
				fileName = String.valueOf(System.currentTimeMillis());
			}

			if(message != null)
			{
				if(StringUtils.isNotEmpty(message.getAttached()) && StringUtils.isNotEmpty(message.getAttachedTwo()))
				{
					//Lastone
					fileName	+= "/image3";
					field		= 3;
				}
				else
				{
					if(StringUtils.isNotEmpty(message.getAttached()))
					{
						//Second
						fileName	+= "/image2";
						field		= 2;
					}
					else
					{
						//First
						fileName	+= "/image1";
						field		= 1;
					}
				}

				taskStarted();
				final StorageReference photoRef = mStorageRef.child(ApiConnection.FIREBASE_CHILD).child(fileName);
				photoRef.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
				{
					@Override
					public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
					{
						String url	= "";
						downloadUri	= taskSnapshot.getMetadata().getDownloadUrl();
						
						if(downloadUri != null)
						{
							url = downloadUri.toString();
						}
						
						final String downloadUrl = url;

						//Persist url in Message
						realm.executeTransactionAsync(new Realm.Transaction()
						{
							@Override
							public void execute(Realm bgRealm)
							{
								Message message	= bgRealm.where(Message.class).equalTo(Message.KEY_API, id).findFirst();

								if(message != null)
								{
									switch(field)
									{
										case 1:
											linkOne = downloadUrl;
											message.setAttached(downloadUrl);
											message.setUri(fileUri.toString());
										break;

										case 2:
											linkTwo = downloadUrl;
											message.setAttachedTwo(downloadUrl);
											message.setUriTwo(fileUri.toString());
										break;

										case 3:
											linkThree = downloadUrl;
											message.setAttachedThree(downloadUrl);
											message.setUriThree(fileUri.toString());
										break;
									}

									comment = message.getNote();
								}
							}
						}, new Realm.Transaction.OnSuccess()
						{
							@Override
							public void onSuccess()
							{
								new AttachAsyncTask(getApplicationContext(), false, id, comment, linkOne, linkTwo, linkThree, new CallBackListener()
								{
									@Override
									public void invoke()
									{
										redirect();
									}
								}).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
							}
						});
					}
				})
				.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception exception)
					{
						broadcastUploadFinished(null, fileUri);
						taskCompleted();
					}
				});
			}
		}
		catch(Exception e)
		{
			Utils.logError(null, "MyUploadService:onOptionsItemSelected - Exception:", e);
		}
	}

	public void redirect()
	{
		broadcastUploadFinished(downloadUri, fileUri);
		taskCompleted();
	}

	/**
	 * Broadcast finished upload (success or failure).
	 * @return true if a running receiver received the broadcast.
	 */
	private boolean broadcastUploadFinished(@Nullable Uri downloadUrl, @Nullable Uri fileUri)
	{
		boolean success = downloadUrl != null;
		String action = success ? UPLOAD_COMPLETED : UPLOAD_ERROR;
		Intent broadcast = new Intent(action).putExtra(EXTRA_DOWNLOAD_URL, downloadUrl).putExtra(EXTRA_FILE_URI, fileUri);
		return LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(broadcast);
	}

	public static IntentFilter getIntentFilter()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPLOAD_COMPLETED);
		filter.addAction(UPLOAD_ERROR);
		return filter;
	}
}