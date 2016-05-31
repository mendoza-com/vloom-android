package com.tween.viacelular.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import com.appboy.Appboy;
import com.tween.viacelular.R;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.Utils;

public class SuggestionsActivity extends AppCompatActivity
{
	private boolean firstTime = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_suggestions);
			if(Utils.checkSesion(this, Common.ANOTHER_SCREEN))
			{
				Toolbar toolBar				= (Toolbar) findViewById(R.id.toolBarSuggestion);
				final Intent intentRecive	= getIntent();
				setSupportActionBar(toolBar);
				setTitle(getString(R.string.suggestions_atitle));
				toolBar.setNavigationIcon(R.drawable.back);

				toolBar.setNavigationOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(final View v)
					{
						begin(v);
					}
				});

				if(intentRecive != null)
				{
					if(intentRecive.hasExtra(Common.KEY_FIRSTTIME))
					{
						firstTime = intentRecive.getBooleanExtra(Common.KEY_FIRSTTIME, false);
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("SuggestionsActivity:onCreate - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onBackPressed()
	{
		try
		{
			begin(null);
		}
		catch(Exception e)
		{
			System.out.println("SuggestionsActivity:onBackPressed - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void begin(View view)
	{
		try
		{
			//Agregado para auto-suscribir companies
			if(firstTime)
			{
				BlockedActivity.modifySubscriptions(getApplicationContext(), Common.BOOL_YES, true, "");
			}
		}
		catch(Exception e)
		{
			System.out.println("SuggestionsActivity:begin - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void onStart()
	{
		//Agregado para evitar excepciones
		try
		{
			super.onStart();

			if(!Common.DEBUG)
			{
				Appboy.getInstance(SuggestionsActivity.this).openSession(SuggestionsActivity.this);
			}
		}
		catch(Exception e)
		{
			System.out.println("SuggestionsActivity:onStart - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	public void onStop()
	{
		//Agregado para evitar excepciones
		try
		{
			super.onStop();

			if(!Common.DEBUG)
			{
				Appboy.getInstance(SuggestionsActivity.this).closeSession(SuggestionsActivity.this);
			}
		}
		catch(Exception e)
		{
			System.out.println("SuggestionsActivity:onStop - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
}