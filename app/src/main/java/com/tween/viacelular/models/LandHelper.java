package com.tween.viacelular.models;

import android.app.Activity;
import com.tween.viacelular.R;
import com.tween.viacelular.utils.Common;
import org.json.JSONArray;
import org.json.JSONObject;
import io.realm.Realm;

/**
 * Created by davidfigueroa on 31/3/16.
 */
public abstract class LandHelper
{
	public static void debug(Land Land)
	{
		if(Land != null)
		{
			System.out.println("\nLand - code: " + Land.getCode());
			System.out.println("Land - name: " + Land.getName());
			System.out.println("Land - isoCode: " + Land.getIsoCode());
			System.out.println("Land - format: " + Land.getFormat());
			System.out.println("Land - minLength: " + Land.getMinLength());
			System.out.println("Land - maxLength: " + Land.getMaxLength());
		}
		else
		{
			System.out.println("\nLand: null");
		}
	}

	public static void parseList(JSONArray jsonArray)
	{
		try
		{
			Realm realm = Realm.getDefaultInstance();

			//Modificación para contemplar nuevo estándar y reestructuración de api
			for(int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject jsonObject	= jsonArray.getJSONObject(i);
				String name				= "";
				String code				= "";
				String isoCode			= "";
				String format			= "";
				String minLength		= "";
				String maxLength		= "";

				if(jsonObject.has(Common.KEY_NAME))
				{
					if(!jsonObject.isNull(Common.KEY_NAME))
					{
						name = jsonObject.getString(Common.KEY_NAME);
					}
				}
				else
				{
					if(jsonObject.has(Common.KEY_DISPLAYNAME))
					{
						if(!jsonObject.isNull(Common.KEY_DISPLAYNAME))
						{
							name = jsonObject.getString(Common.KEY_DISPLAYNAME);
						}
					}
				}

				if(jsonObject.has(Land.KEY_CODE))
				{
					if(!jsonObject.isNull(Land.KEY_CODE))
					{
						code = jsonObject.getString(Land.KEY_CODE);
					}
				}

				if(jsonObject.has(Land.KEY_ISOCODE))
				{
					if(!jsonObject.isNull(Land.KEY_ISOCODE))
					{
						isoCode = jsonObject.getString(Land.KEY_ISOCODE);
					}
				}

				if(jsonObject.has(Land.KEY_FORMAT))
				{
					if(!jsonObject.isNull(Land.KEY_FORMAT))
					{
						format = jsonObject.getString(Land.KEY_FORMAT);
					}
				}

				if(jsonObject.has(Land.KEY_MINLENGTH))
				{
					if(!jsonObject.isNull(Land.KEY_MINLENGTH))
					{
						minLength = jsonObject.getString(Land.KEY_MINLENGTH);
					}
				}

				if(jsonObject.has(Land.KEY_MAXLENGHT))
				{
					if(!jsonObject.isNull(Land.KEY_MAXLENGHT))
					{
						maxLength = jsonObject.getString(Land.KEY_MAXLENGHT);
					}
				}

				Land Land = new Land(code, name, isoCode, format, minLength, maxLength);
				realm.beginTransaction();
				realm.copyToRealmOrUpdate(Land);
				realm.commitTransaction();
			}
		}
		catch(Exception e)
		{
			System.out.println("Land - Exception: " + e);
			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public static void parseArray(Activity activity)
	{
		String[] arrayCountries	= activity.getResources().getStringArray(R.array.countries);
		String[] arrayCodes		= activity.getResources().getStringArray(R.array.codes);
		String[] arrayIsoCodes	= activity.getResources().getStringArray(R.array.isoCodes);
		String[] formats		= activity.getResources().getStringArray(R.array.formats);
		String[] minLength		= activity.getResources().getStringArray(R.array.minLength);
		String[] maxLength		= activity.getResources().getStringArray(R.array.maxLength);
		Realm realm				= Realm.getDefaultInstance();

		for(int i = 0; i < arrayCountries.length; i++)
		{
			Land Land = new Land(arrayCodes[i], arrayCountries[i], arrayIsoCodes[i], formats[i], minLength[i], maxLength[i]);
			realm.beginTransaction();
			realm.copyToRealmOrUpdate(Land);
			realm.commitTransaction();
		}
	}
}