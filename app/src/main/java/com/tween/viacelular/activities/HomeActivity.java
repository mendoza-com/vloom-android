package com.tween.viacelular.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import com.appboy.Appboy;
import com.tween.viacelular.R;
import com.tween.viacelular.adapters.RecyclerAdapter;
import com.tween.viacelular.adapters.RecyclerItemClickListener;
import com.tween.viacelular.fragments.SwipeRefreshLayoutBasicFragment;
import com.tween.viacelular.models.Suscription;
import com.tween.viacelular.utils.AppRater;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import com.tween.viacelular.utils.Utils;
import io.realm.Realm;

public class HomeActivity extends AppCompatActivity
{
	private String							companyId	= "";
	private int								block		= Common.BOOL_NO;
	private boolean							refresh		= true;
	private SwipeRefreshLayoutBasicFragment	fragment;
	private DrawerLayout					drawer;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Realm realm = null;

		try
		{
			//Agregado para efecto de transición entre pantallas
			if(Common.API_LEVEL >= Build.VERSION_CODES.LOLLIPOP)
			{
				getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
				getWindow().setEnterTransition(new Explode());
				getWindow().setReenterTransition(new Explode());
				getWindow().setExitTransition(new Explode());
			}

			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_home);
			final CoordinatorLayout clayout = (CoordinatorLayout) findViewById(R.id.clSnack);

			//Agregado para permitir pull update del contenido
			if(savedInstanceState == null)
			{
				FragmentTransaction transaction	= getSupportFragmentManager().beginTransaction();
				fragment						= new SwipeRefreshLayoutBasicFragment();
				fragment.setHomeActivity(HomeActivity.this);
				fragment.setClayout(clayout);
				transaction.replace(R.id.rlFragment, fragment);
				transaction.commit();
			}

			if(Utils.checkSesion(this, Common.ANOTHER_SCREEN))
			{
				//Se quitó la configuración de ImageLoader para llamarse antes de ejectuarse
				final Intent intentRecive	= getIntent();

				if(intentRecive != null)
				{
					companyId	= intentRecive.getStringExtra(Common.KEY_ID);
					block		= intentRecive.getIntExtra(Suscription.KEY_BLOCKED, Common.BOOL_NO);
					refresh		= intentRecive.getBooleanExtra(Common.KEY_REFRESH, false);
				}

				Toolbar toolbar					= (Toolbar) findViewById(R.id.toolBar);
				setSupportActionBar(toolbar);
				RecyclerView mRecyclerView		= (RecyclerView) findViewById(R.id.RecyclerView);
				mRecyclerView.setHasFixedSize(true);
				ImageView ivIcon				= (ImageView) findViewById(R.id.ivIcon);
				RecyclerView.Adapter mAdapter	= null;

				//Se quitó la precarga de la imagen de VC blanca para procesarla en HomeAdapter

				mAdapter = new RecyclerAdapter(	Utils.getMenu(getApplicationContext()), RecyclerAdapter.HOME_SELECTED, ContextCompat.getColor(getApplicationContext(), R.color.accent),
												getApplicationContext());

				mRecyclerView.setAdapter(mAdapter);
				RecyclerView.LayoutManager mLayoutManager	= new LinearLayoutManager(this);
				mRecyclerView.setLayoutManager(mLayoutManager);
				drawer										= (DrawerLayout) findViewById(R.id.DrawerLayout);
				//Agregado para ocultar menú si está abierto y sino cerrar la app al hacer back
				drawer.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
				ActionBarDrawerToggle mDrawerToggle			= new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
				{
					@Override
					public void onDrawerOpened(View drawerView)
					{
						super.onDrawerOpened(drawerView);
					}

					@Override
					public void onDrawerClosed(View drawerView)
					{
						super.onDrawerClosed(drawerView);
					}
				};

				mDrawerToggle.syncState();
				//Cambio de contexto para redirigir desde el menú
				final Context context = getApplicationContext();
				mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(),
					new RecyclerItemClickListener.OnItemClickListener()
					{
						@Override
						public void onItemClick(View view, int position)
						{
							Utils.redirectMenu(context, position, RecyclerAdapter.HOME_SELECTED);
						}
					})
				);

				Utils.tintColorScreen(this, Common.COLOR_ACTION);

				if(StringUtils.isNotEmpty(companyId) && block == Common.BOOL_YES)
				{
					realm							= Realm.getDefaultInstance();
					final Suscription suscription	= realm.where(Suscription.class).equalTo(Suscription.KEY_API, companyId).findFirst();
					final String companyId			= suscription.getCompanyId();
					Snackbar snackBar				= Snackbar.make(clayout, getString(R.string.snack_blocked), Snackbar.LENGTH_LONG).setAction(getString(R.string.undo), new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							BlockedActivity.modifySubscriptions(getApplicationContext(), Common.BOOL_YES, false, companyId);
							fragment.refresh(false, false);
						}
					});

					Utils.setStyleSnackBar(snackBar, getApplicationContext());
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("HomeActivity:onCreate - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return super.onOptionsItemSelected(item);
	}

	protected void onResume()
	{
		super.onResume();
		//Agregado para recargar listado en caso de salir de la app
		try
		{
			if(Utils.checkSesion(this, Common.ANOTHER_SCREEN))
			{
				//Agregado para evitar solicitudes consecutivas a la api
				if(fragment != null)
				{
					if(refresh)
					{
						fragment.refresh(true, false);
					}
					else
					{
						fragment.refresh(false, false);
						refresh = true;
					}
				}
			}

			//Agregado para solicitar calificación en Play Store
			if(AppRater.launchApp(getApplicationContext()))
			{
				final Context context		= this;
				AlertDialog.Builder dialog	= new AlertDialog.Builder(this);
				dialog.setCancelable(false);
				dialog.setMessage(R.string.rate_dialog_message);
				dialog.setTitle(R.string.rate_dialog_title);
				dialog.setPositiveButton(R.string.rate_dialog_ok, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(final DialogInterface dialog, final int which)
					{
						AppRater.rateApp(context);
						try
						{
							context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
						}
						catch(Exception e)
						{
							context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
						}

						dialog.dismiss();
					}
				});
				dialog.setNegativeButton(R.string.rate_dialog_cancel, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(final DialogInterface dialog, final int which)
					{
						AppRater.delayRateApp(context);
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		}
		catch(Exception e)
		{
			System.out.println("HomeActivity:onResume - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	protected void onPause()
	{
		super.onPause();
	}

	@Override
	public void onBackPressed()
	{
		try
		{
			if(Common.API_LEVEL > Build.VERSION_CODES.JELLY_BEAN)
			{
				finish();
			}
			else
			{
				//Agregado para ocultar menú si está abierto y sino cerrar la app al hacer back
				if(drawer.isDrawerOpen(Gravity.LEFT))
				{
					drawer.closeDrawer(Gravity.LEFT);
				}
				else
				{
					super.onBackPressed();
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("HomeActivity:onBackPressed - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void onStart()
	{
		super.onStart();

		try
		{
			if(!Common.DEBUG)
			{
				Appboy.getInstance(HomeActivity.this).openSession(HomeActivity.this);
			}
		}
		catch(Exception e)
		{
			System.out.println("HomeActivity:onStart - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void onStop()
	{
		super.onStop();

		try
		{
			if(!Common.DEBUG)
			{
				Appboy.getInstance(HomeActivity.this).closeSession(HomeActivity.this);
			}
		}
		catch(Exception e)
		{
			System.out.println("HomeActivity:onStop - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
}