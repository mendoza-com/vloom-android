package com.tween.viacelular.utils;

import android.content.Context;
import android.text.format.DateFormat;
import com.tween.viacelular.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Utilidad para tratamiento de fechas y timestamps
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar) on 11/06/2015
 */
public class DateUtils
{
	public static final long oneDayTs			= (24 * 60 * 60 * 1000);
	private static final int SECOND_MILLIS		= 1000;
	private static final int MINUTE_MILLIS		= 60 * SECOND_MILLIS;
	private static final int HOUR_MILLIS		= 60 * MINUTE_MILLIS;
	public static final int DAY_MILLIS			= 24 * HOUR_MILLIS;
	public static final int LOW_FREQUENCY		= 90 * MINUTE_MILLIS;
	public static final int MEAN_FREQUENCY		= 30 * MINUTE_MILLIS;
	public static final int HIGH_FREQUENCY		= 10 * MINUTE_MILLIS;
	public static final int VERYHIGH_FREQUENCY	= 5 * MINUTE_MILLIS;

	public static String getTimeFromTs(long time, Context ctx)
	{
		//Agregado para prevenir excepciones
		try
		{
			if(time < 1000000000000L)
			{
				//if timestamp given in seconds, convert to millis
				time *= 1000;
			}

			long now = System.currentTimeMillis();
			if(time > now || time <= 0)
			{
				return ctx.getString(R.string.tomorrow);
			}

			// TODO: localize
			final long diff = now - time;
			if(diff < MINUTE_MILLIS)
			{
				return ctx.getString(R.string.now);
			}
			else
			{
				if(diff < 2 * MINUTE_MILLIS)
				{
					return ctx.getString(R.string.one_minute);
				}
				else
				{
					if(diff < 50 * MINUTE_MILLIS)
					{
						return diff / MINUTE_MILLIS + " " + ctx.getString(R.string.minutes);
					}
					else
					{
						if(diff < 90 * MINUTE_MILLIS)
						{
							return ctx.getString(R.string.one_hour);
						}
						else
						{
							if(diff < 24 * HOUR_MILLIS)
							{
								return diff / HOUR_MILLIS + " " + ctx.getString(R.string.hours);
							}
							else
							{
								if(diff < 48 * HOUR_MILLIS)
								{
									return ctx.getString(R.string.yesterday);
								}
								else
								{
									if(diff / DAY_MILLIS < 7)
									{
										if(diff / DAY_MILLIS < 2)
										{
											return diff / DAY_MILLIS + " " + ctx.getString(R.string.day);
										}
										else
										{
											return diff / DAY_MILLIS + " " + ctx.getString(R.string.days);
										}
									}
									else
									{
										if(diff / DAY_MILLIS >= 7 && diff / DAY_MILLIS < 30)
										{
											if(diff / DAY_MILLIS < 14)
											{
												return (int) ((diff / DAY_MILLIS) / 7) + " " + ctx.getString(R.string.week);
											}
											else
											{
												return (int) ((diff / DAY_MILLIS) / 7) + " " + ctx.getString(R.string.weeks);
											}
										}
										else
										{
											if(diff / DAY_MILLIS >= 30 && diff / DAY_MILLIS < 365)
											{
												if(diff / DAY_MILLIS < 60)
												{
													return (int) ((diff / DAY_MILLIS) / 30) + " " + ctx.getString(R.string.month);
												}
												else
												{
													return (int) ((diff / DAY_MILLIS) / 30) + " " + ctx.getString(R.string.months);
												}
											}
											else
											{
												if(diff / DAY_MILLIS < 730)
												{
													return (int) ((diff / DAY_MILLIS) / 365) + " " + ctx.getString(R.string.year);
												}
												else
												{
													return (int) ((diff / DAY_MILLIS) / 365) + " " + ctx.getString(R.string.years);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			Utils.logError(ctx, "DateUtils:getTimeFromTs - Exception:", e);
		}

		return "";
	}

	public static String getTime(Context context)
	{
		//Agregado para prevenir excepciones
		try
		{
			Long tsLong = System.currentTimeMillis() / 1000;
			return tsLong.toString();
		}
		catch(Exception e)
		{
			Utils.logError(context, "DateUtils:getTime - Exception:", e);
		}

		return "";
	}

	/**
	 * <h1>Fecha de Teléfono</h1>
	 * <p>Devuelve la fecha y hora actual del teléfono</p>
	 *
	 * @return Fecha y hora en formato "dd/MM/yyyy HH:mm:ss"
	 */
	public static String getDateTimePhone(Context context)
	{
		//Agregado para prevenir excepciones
		try
		{
			Calendar cal		= new GregorianCalendar();
			Date date			= cal.getTime();
			SimpleDateFormat df	= new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
			return df.format(date);
		}
		catch(Exception e)
		{
			Utils.logError(context, "DateUtils:getDateTimePhone - Exception:", e);
		}

		return "";
	}

	/**
	 * Calcula si es necesario actualizar un recurso dependiendo de la frecuencia asociada
	 * @param date
	 * @param frequency
	 * @return
	 */
	public static boolean needUpdate(Long date, int frequency, Context context)
	{
		try
		{
			if(date != null)
			{
				final long diff	= System.currentTimeMillis() - date;

				if(diff > frequency)
				{
					return true;
				}
			}
			else
			{
				return true;
			}
		}
		catch(Exception e)
		{
			Utils.logError(context, "DateUtils:needUpdate - Exception:", e);
		}

		return false;
	}

	/**
	 * Devuelve el date formateado para el timestamp recibido
	 * @param time
	 * @return
	 */
	public static String getDateFromTs(long time, Context context)
	{
		//Agregado para prevenir excepciones
		try
		{
			Calendar cal	= Calendar.getInstance();
			cal.setTimeInMillis(time);
			return DateFormat.format("dd/MM/yyyy HH:mm:ss", cal).toString();
		}
		catch(Exception e)
		{
			Utils.logError(context, "DateUtils:getDateFromTs - Exception:", e);
		}

		return "";
	}

	/**
	 * <h1>Fecha de Teléfono</h1>
	 * <p>Devuelve la fecha (SIN HORA) actual del teléfono</p>
	 *
	 * @return Fecha en formato "dd/MM/yyyy"
	 */
	public static String getDatePhone(Context context)
	{
		//Agregado para prevenir excepciones
		try
		{
			Calendar cal		= new GregorianCalendar();
			Date date	= cal.getTime();
			SimpleDateFormat df	= new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
			return df.format(date);
		}
		catch(Exception e)
		{
			Utils.logError(context, "DateUtils:getDateTimePhone - Exception:", e);
		}

		return "";
	}
}