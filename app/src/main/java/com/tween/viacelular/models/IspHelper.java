package com.tween.viacelular.models;

import android.content.Context;
import android.telephony.TelephonyManager;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import org.json.JSONObject;
import io.realm.Realm;

/**
 * Manejador para tratamiento y parseo de datos sobre la conexión del dispositivo
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar) on 31/03/2016
 */
public abstract class IspHelper
{
	public static void debug(Isp isp)
	{
		if(isp != null)
		{
			System.out.println("\nIsp - query: " + isp.getQuery());
			System.out.println("Isp - as: " + isp.getAs());
			System.out.println("Isp - status: " + isp.getStatus());
			System.out.println("Isp - country: " + isp.getCountry());
			System.out.println("Isp - countryCode: " + isp.getCountryCode());
			System.out.println("Isp - region: " + isp.getRegion());
			System.out.println("Isp - regionName: " + isp.getRegionName());
			System.out.println("Isp - city: " + isp.getCity());
			System.out.println("Isp - zip: " + isp.getZip());
			System.out.println("Isp - lat: " + isp.getLat());
			System.out.println("Isp - lon: " + isp.getLon());
			System.out.println("Isp - timezone: " + isp.getTimezone());
			System.out.println("Isp - isp: " + isp.getIsp());
			System.out.println("Isp - org: " + isp.getOrg());
			System.out.println("Isp - operatorNet: " + isp.getOperatorNet());
			System.out.println("Isp - operatorSim: " + isp.getOperatorSim());
			System.out.println("Isp - countryNet: " + isp.getCountryNet());
			System.out.println("Isp - countrySim: " + isp.getCountrySim());
		}
		else
		{
			System.out.println("\nIsp: null");
		}
	}

	public static void parseJSON(JSONObject json, Context context, boolean update)
	{
		try
		{
			String jAs			= "";
			String jStatus		= "";
			String jCountry		= "";
			String jCountryCode	= "";
			String jRegion		= "";
			String jRegionName	= "";
			String jCity		= "";
			String jZip			= "";
			String jLat			= "";
			String jLon			= "";
			String jTimezone	= "";
			String jIsp			= "";
			String jOrg			= "";
			String jQuery		= "";
			String jOpNet		= "";
			String jOpSim		= "";
			String jCoNet		= "";
			String jCoSim		= "";

			if(json != null)
			{
				if(json.has(Isp.KEY_AS))
				{
					if(StringUtils.isNotEmpty(json.getString(Isp.KEY_AS)))
					{
						jAs = json.getString(Isp.KEY_AS);
					}
				}

				if(json.has(Common.KEY_STATUS))
				{
					if(StringUtils.isNotEmpty(json.getString(Common.KEY_STATUS)))
					{
						jStatus = json.getString(Common.KEY_STATUS);
					}
				}

				if(json.has(Isp.KEY_COUNTRY))
				{
					if(StringUtils.isNotEmpty(json.getString(Isp.KEY_COUNTRY)))
					{
						jCountry = json.getString(Isp.KEY_COUNTRY);
					}
				}

				if(json.has(Land.KEY_API))
				{
					if(StringUtils.isNotEmpty(json.getString(Land.KEY_API)))
					{
						jCountryCode = json.getString(Land.KEY_API);
					}
				}

				if(json.has(Isp.KEY_REGION))
				{
					if(StringUtils.isNotEmpty(json.getString(Isp.KEY_REGION)))
					{
						jRegion = json.getString(Isp.KEY_REGION);
					}
				}

				if(json.has(Isp.KEY_REGIONNAME))
				{
					if(StringUtils.isNotEmpty(json.getString(Isp.KEY_REGIONNAME)))
					{
						jRegionName = json.getString(Isp.KEY_REGIONNAME);
					}
				}

				if(json.has(Isp.KEY_CITY))
				{
					if(StringUtils.isNotEmpty(json.getString(Isp.KEY_CITY)))
					{
						jCity = json.getString(Isp.KEY_CITY);
					}
				}

				if(json.has(Isp.KEY_ZIP))
				{
					if(StringUtils.isNotEmpty(json.getString(Isp.KEY_ZIP)))
					{
						jZip = json.getString(Isp.KEY_ZIP);
					}
				}

				if(json.has(Isp.KEY_LAT))
				{
					if(StringUtils.isNotEmpty(json.getString(Isp.KEY_LAT)))
					{
						jLat = json.getString(Isp.KEY_LAT);
					}
				}

				if(json.has(Isp.KEY_LON))
				{
					if(StringUtils.isNotEmpty(json.getString(Isp.KEY_LON)))
					{
						jLon = json.getString(Isp.KEY_LON);
					}
				}

				if(json.has(Isp.KEY_TIMEZONE))
				{
					if(StringUtils.isNotEmpty(json.getString(Isp.KEY_TIMEZONE)))
					{
						jTimezone = json.getString(Isp.KEY_TIMEZONE);
					}
				}

				if(json.has(Isp.KEY_ISP))
				{
					if(StringUtils.isNotEmpty(json.getString(Isp.KEY_ISP)))
					{
						jIsp = json.getString(Isp.KEY_ISP);
					}
				}

				if(json.has(Isp.KEY_ORG))
				{
					if(StringUtils.isNotEmpty(json.getString(Isp.KEY_ORG)))
					{
						jOrg = json.getString(Isp.KEY_ORG);
					}
				}

				if(json.has(Isp.KEY_QUERY))
				{
					if(StringUtils.isNotEmpty(json.getString(Isp.KEY_QUERY)))
					{
						jQuery = json.getString(Isp.KEY_QUERY);
					}
				}
			}

			//Obtenemos la operadora
			TelephonyManager manager	= (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			Isp isp;

			if(manager != null)
			{
				jOpNet	= manager.getNetworkOperatorName().toUpperCase();
				jOpSim	= manager.getSimOperatorName().toUpperCase();
				jCoNet	= manager.getNetworkCountryIso().toUpperCase();
				jCoSim	= manager.getSimCountryIso().toUpperCase();
			}

			//Agregado para probar en emuladores
			if(jOpNet.equals("ANDROID") || jOpSim.equals("ANDROID"))
			{
				jOpNet	= "PERSONAL";
				jOpSim	= "PERSONAL";
			}

			//Diferenciamos si actualizamos, insertamos o mantenemos
			Realm realm	= Realm.getDefaultInstance();
			realm.beginTransaction();

			if(update)
			{
				isp = realm.where(Isp.class).findFirst();

				if(StringUtils.isNotEmpty(jAs) && StringUtils.isNotEmpty(isp.getAs()))
				{
					if(!isp.getAs().equals(jAs))
					{
						isp.setAs(jAs);
					}
				}

				if(StringUtils.isNotEmpty(jRegionName) && StringUtils.isNotEmpty(isp.getRegionName()))
				{
					if(!isp.getRegionName().equals(jRegionName))
					{
						isp.setRegionName(jRegionName);
					}
				}

				if(StringUtils.isNotEmpty(jRegion) && StringUtils.isNotEmpty(isp.getRegion()))
				{
					if(!isp.getRegion().equals(jRegion))
					{
						isp.setRegion(jRegion);
					}
				}

				if(StringUtils.isNotEmpty(jCity) && StringUtils.isNotEmpty(isp.getCity()))
				{
					if(!isp.getCity().equals(jCity))
					{
						isp.setCity(jCity);
					}
				}

				if(StringUtils.isNotEmpty(jZip) && StringUtils.isNotEmpty(isp.getZip()))
				{
					if(!isp.getZip().equals(jZip))
					{
						isp.setZip(jZip);
					}
				}

				if(StringUtils.isNotEmpty(jLat) && StringUtils.isNotEmpty(isp.getLat()))
				{
					if(!isp.getLat().equals(jLat))
					{
						isp.setLat(jLat);
					}
				}

				if(StringUtils.isNotEmpty(jLon) && StringUtils.isNotEmpty(isp.getLon()))
				{
					if(!isp.getLon().equals(jLon))
					{
						isp.setLon(jLon);
					}
				}

				if(StringUtils.isNotEmpty(jTimezone) && StringUtils.isNotEmpty(isp.getTimezone()))
				{
					if(!isp.getTimezone().equals(jTimezone))
					{
						isp.setTimezone(jTimezone);
					}
				}

				if(StringUtils.isNotEmpty(jIsp) && StringUtils.isNotEmpty(isp.getIsp()))
				{
					if(!isp.getIsp().equals(jIsp))
					{
						isp.setIsp(jIsp);
					}
				}

				if(StringUtils.isNotEmpty(jOrg) && StringUtils.isNotEmpty(isp.getOrg()))
				{
					if(!isp.getOrg().equals(jOrg))
					{
						isp.setOrg(jOrg);
					}
				}

				isp.setUpdated(System.currentTimeMillis());
			}
			else
			{
				isp	= new Isp(	jQuery, jAs, jStatus, jCountry, jCountryCode, jRegion, jRegionName, jCity, jZip, jLat, jLon, jTimezone, jIsp, jOrg, jOpNet, jOpSim, jCoNet, jCoSim,
								System.currentTimeMillis());
			}

			realm.copyToRealmOrUpdate(isp);
			realm.commitTransaction();
		}
		catch(Exception e)
		{
			Utils.logError(context, "Isp:parseJSON - Exception:", e);
		}
	}
}