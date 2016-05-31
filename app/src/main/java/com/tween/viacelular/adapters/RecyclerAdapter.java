package com.tween.viacelular.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.tween.viacelular.R;
import com.tween.viacelular.models.User;
import com.tween.viacelular.utils.Common;
import com.tween.viacelular.utils.StringUtils;
import io.realm.Realm;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>
{
	private static final int	TYPE_HEADER				= 0;
	private static final int	TYPE_ITEM				= 1;
	public static final int		HOME_SELECTED			= 1;
	public static final int		SUSCRIPTION_SELECTED	= 2;
	public static final int		SETTINGS_SELECTED		= 3;
	public static final int		FEEDBACK_SELECTED		= 4;
	private int					mIcons[]				= {R.drawable.ic_inbox_black_24dp, 0, 0, 0};//Agregamos la pantalla Feedback
	private String				mNavTitles[];
	private String				name;
	private int					profile;
	private String				email;
	private int					selected;
	private int					color;
	private String				phone;

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		public int			HolderId;
		public TextView		textView;
		public ImageView	imageView;
		public ImageView	profile;
		public View			div;
		public TextView		Name;
		public TextView		email;

		public ViewHolder(View itemView, int ViewType)
		{
			super(itemView);

			if(ViewType == TYPE_ITEM)
			{
				textView	= (TextView) itemView.findViewById(R.id.rowText);
				imageView	= (ImageView) itemView.findViewById(R.id.rowIcon);
				div			= itemView.findViewById(R.id.div);
				HolderId	= 1;
				textView.setClickable(true);
			}
			else
			{
				Name		= (TextView) itemView.findViewById(R.id.name);
				email		= (TextView) itemView.findViewById(R.id.email);
				profile		= (ImageView) itemView.findViewById(R.id.circleView);
				HolderId	= 0;
			}
		}
	}

	public RecyclerAdapter(String[] title, int selected, int color, Context context)
	{
		try
		{
			//Modificaciones para contemplar migración a Realm
			Realm realm		= Realm.getDefaultInstance();
			User user		= realm.where(User.class).findFirst();
			String name		= "";
			String email	= "";
			String phone	= "";

			if(user != null)
			{
				if(StringUtils.isNotEmpty(user.getFirstName()))
				{
					name = user.getFirstName() + " ";
				}

				if(StringUtils.isNotEmpty(user.getLastName()))
				{
					name = name + user.getLastName();
				}

				if(StringUtils.isNotEmpty(user.getEmail()))
				{
					email = user.getEmail();
				}

				if(StringUtils.isNotEmpty(user.getPhone()))
				{
					phone = user.getPhone();
				}
			}

			this.name		= name;
			this.email		= email;
			this.profile	= R.mipmap.ic_launcher;
			this.mNavTitles	= title;
			this.selected	= selected;
			this.color		= color;
			this.phone		= phone;
		}
		catch(Exception e)
		{
			System.out.println("RecyclerAdapter:constructor - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		try
		{
			if(viewType == TYPE_ITEM)
			{
				View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);
				return new ViewHolder(v, viewType);
			}
			else
			{
				if(viewType == TYPE_HEADER)
				{
					View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.header, parent, false);
					return new ViewHolder(v, viewType);
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("RecyclerAdapter.ViewHolder:onCreateViewHolder - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override
	public void onBindViewHolder(RecyclerAdapter.ViewHolder holder, final int position)
	{
		try
		{
			if(holder.HolderId == 1)
			{
				holder.textView.setText(mNavTitles[position - 1]);

				if(mIcons[position - 1] != 0)
				{
					holder.imageView.setImageResource(mIcons[position - 1]);
				}
				else
				{
					holder.imageView.setVisibility(android.widget.ImageView.GONE);
				}

				if(position != 1)
				{
					holder.div.setVisibility(android.view.View.GONE);
				}

				if(position == selected)
				{
					holder.textView.setSelected(true);
					holder.imageView.setSelected(true);
					holder.textView.setTextColor(color);
					holder.imageView.setColorFilter(color);
				}
				else
				{
					holder.textView.setSelected(false);
					holder.imageView.setSelected(false);
				}
			}
			else
			{
				holder.profile.setImageResource(profile);
				String initial = "";
				//Agregado para mostrar siempre el celular debajo del nombre o email según de cual dato dispongamos

				if(StringUtils.isNotEmpty(name))
				{
					holder.Name.setText(name);
					holder.Name.setVisibility(android.widget.TextView.VISIBLE);
					initial = name;
				}
				else
				{
					if(StringUtils.isNotEmpty(email))
					{
						holder.Name.setText(email);
						holder.Name.setVisibility(android.widget.TextView.VISIBLE);
						initial = email;
					}
					else
					{
						holder.Name.setVisibility(android.widget.TextView.GONE);
					}
				}

				if(StringUtils.isEmpty(initial))
				{
					initial = phone;
				}

				holder.email.setText(phone);
				TextDrawable drawable = TextDrawable.builder().buildRound(StringUtils.getInitials(initial), R.color.button);
				holder.profile.setImageDrawable(drawable);
			}
		}
		catch(Exception e)
		{
			System.out.println("RecyclerAdapter:onBindViewHolder - Exception: " + e);

			if(Common.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public int getItemCount()
	{
		//Agregado para evitar excepciones por referencia de lista null
		if(mNavTitles != null)
		{
			return mNavTitles.length + 1;
		}
		else
		{
			return 1;
		}
	}

	@Override
	public int getItemViewType(int position)
	{
		if(isPositionHeader(position))
		{
			return TYPE_HEADER;
		}

		return TYPE_ITEM;
	}

	private boolean isPositionHeader(int position)
	{
		return position == 0;
	}
}