package com.tween.viacelular.models;

import com.tween.viacelular.utils.ApiConnection;
import com.tween.viacelular.utils.StringUtils;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Modelo para persistencia de empresas
 * Created by Tween (David Figueroa davo.figueroa@tween.com.ar) on 03/02/2016
 */
public class Suscription extends RealmObject
{
	@PrimaryKey
	@Index
	private String				companyId;
	private String				name;
	private String				countryCode;
	private String				industryCode;
	private String				industry;
	private int					type;
	private String				image;
	private String				colorHex;
	private String				fromNumbers;
	private String				keywords;
	private String				unsuscribe;
	private String				url;
	private String				phone;
	private String				msgExamples;
	private String				identificationKey;
	private int					dataSent;
	private String				identificationValue;
	private String				about;
	private int					status;
	private int					silenced;
	private int					blocked;
	private String				email;
	private int					receive;
	private int					suscribe;
	private int					follower;
	private int					gray;
	private RealmList<Message>	messages; //Relación de 0 a muchos con los mensajes
	private Long				lastSocialUpdated;
	private String				twitter;

	@Ignore
	public static final int TYPE_AUTOGENERATED			= 0;
	@Ignore
	public static final int TYPE_NEWUSER				= 1;
	@Ignore
	public static final int TYPE_FREE_REGISTERED		= 2;
	@Ignore
	public static final int TYPE_PRO_REGISTERED			= 3;
	@Ignore
	public static final int TYPE_BLOCK					= 4;
	@Ignore
	public static final int TYPE_ONG					= 5;
	@Ignore
	public static final int TYPE_FOLDER					= 6;
	@Ignore
	public static final int STATUS_ACTIVE				= 0;
	@Ignore
	public static final int STATUS_INACTIVE				= 1;
	@Ignore
	public static final int STATUS_BLOCKED				= 2;
	@Ignore
	public static final String NUMBER_FREE				= "free";
	@Ignore
	public static final String NUMBER_PAYOUT			= "payout";
	@Ignore
	public static final String KEY_API					= "companyId";
	@Ignore
	public static final String KEY_SILENCED				= "silenced";
	@Ignore
	public static final String KEY_BLOCKED				= "blocked";
	@Ignore
	public static final String KEY_IMAGE				= "image";
	@Ignore
	public static final String KEY_COLOR				= "colorHex";
	@Ignore
	public static final String KEY_NUMBERS				= "fromNumbers";
	@Ignore
	public static final String KEY_KEYWORDS				= "keywords";
	@Ignore
	public static final String KEY_UNSUSCRIBE			= "unsuscribe";
	@Ignore
	public static final String KEY_INDUSTRYCODE			= "industryCode";
	@Ignore
	public static final String KEY_INDUSTRY				= "industry";
	@Ignore
	public static final String KEY_EMPLOYEES			= "employees";
	@Ignore
	public static final String KEY_RECEIVE				= "receive";
	@Ignore
	public static final String KEY_FROM					= "from";
	@Ignore
	public static final String KEY_SUSCRIBE				= "suscribe";
	@Ignore
	public static final String KEY_URL					= "url";
	@Ignore
	public static final String KEY_MSGEXAMPLES			= "msgExamples";
	@Ignore
	public static final String KEY_ABOUT				= "about";
	@Ignore
	public static final String KEY_IDENTIFICATIONKEY	= "identificationKey";
	@Ignore
	public static final String KEY_IDENTIFICATIONVALUE	= "identificationValue";
	@Ignore
	public static final String KEY_DATASENT				= "dataSent";
	@Ignore
	public static final String KEY_FOLLOWER				= "follower";
	@Ignore
	public static final String KEY_GRAY					= "gray";
	@Ignore
	public static final String KEY_LASTSOCIALUPDATED	= "lastSocialUpdated";
	@Ignore
	public static final String KEY_TWITTER				= "twitter";
	@Ignore
	public static final String KEY_DEFAULTTWITTER		= "VloomApp";
	@Ignore
	public static final String COMPANY_ID_VC			= "vc1";
	@Ignore
	public static final String COMPANY_ID_VC_LONG		= "viacelular";
	@Ignore
	public static final String COMPANY_ID_VC_MONGO		= "57029b3381576d943c9dc9d3";
	@Ignore
	public static final String COMPANY_ID_VC_MONGOOLD	= "561fa8d734dea37a1dc73908";
	@Ignore
	public static final String COMPANY_ID_WEBVC			= "55a53d38b62e923a18351a4d";
	@Ignore
	public static final String DEFAULT_SENDER			= "26100";
	@Ignore
	public static final String ICON_APP					= ApiConnection.CLOUDFRONT_S3+"vloom-ar/vloom-ar@3x.png";

	public Suscription()
	{
	}

	public Suscription(	final String companyId, final String name, final String countryCode, final String industryCode, final String industry, final int type, final String image,
						final String colorHex, final String fromNumbers, final String keywords, final String unsuscribe, final String url, final String phone, final String msgExamples,
						final String identificationKey, final int dataSent, final String identificationValue, final String about, final int status, final int silenced, final int blocked,
						final String email, final int receive, final int suscribe, final int follower, final int gray, final String twitter)
	{
		this.companyId				= companyId;
		this.name					= name;
		this.countryCode			= countryCode;
		this.industryCode			= industryCode;
		this.industry				= industry;
		this.type					= type;
		this.image					= image;
		this.colorHex				= colorHex;
		this.fromNumbers			= fromNumbers;
		this.keywords				= keywords;
		this.unsuscribe				= unsuscribe;
		this.url					= url;
		this.phone					= phone;
		this.msgExamples			= msgExamples;
		this.identificationKey		= identificationKey;
		this.dataSent				= dataSent;
		this.identificationValue	= identificationValue;
		this.about					= about;
		this.status					= status;
		this.silenced				= silenced;
		this.blocked				= blocked;
		this.email					= email;
		this.receive				= receive;
		this.suscribe				= suscribe;
		this.follower				= follower;
		this.gray					= gray;
		this.twitter				= twitter;
	}

	public String getName()
	{
		if(name != null)
		{
			return name;
		}
		else
		{
			return "";
		}
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getIndustry()
	{
		if(industry != null)
		{
			return industry;
		}
		else
		{
			return "";
		}
	}
	
	public void setIndustry(String industry)
	{
		this.industry = industry;
	}
	
	public String getEmail()
	{
		if(email != null)
		{
			return email;
		}
		else
		{
			return "";
		}
	}
	
	public String getIndustryCode()
	{
		if(industryCode != null)
		{
			return industryCode;
		}
		else
		{
			return "";
		}
	}
	
	public void setIndustryCode(String industryCode)
	{
		this.industryCode = industryCode;
	}
	
	public void setEmail(String email)
	{
		this.email = email;
	}
	
	public String getFromNumbers()
	{
		//Adaptación a nuevo formato de jsonArray
		if(fromNumbers != null)
		{
			if(StringUtils.isNotEmpty(fromNumbers))
			{
				return fromNumbers;
			}
			else
			{
				return "[]";
			}
		}
		else
		{
			return "[]";
		}
	}
	
	public int getSuscribe()
	{
		return suscribe;
	}
	
	public void setSuscribe(final int suscribe)
	{
		this.suscribe = suscribe;
	}
	
	public String getUrl()
	{
		if(url != null)
		{
			return url;
		}
		else
		{
			return "";
		}
	}
	
	public void setUrl(final String url)
	{
		this.url = url;
	}
	
	public String getPhone()
	{
		if(phone != null)
		{
			return phone;
		}
		else
		{
			return "";
		}
	}
	
	public void setPhone(final String phone)
	{
		this.phone = phone;
	}
	
	public String getMsgExamples()
	{
		if(msgExamples != null)
		{
			if(StringUtils.isNotEmpty(msgExamples))
			{
				return msgExamples;
			}
			else
			{
				return "[]";
			}
		}
		else
		{
			return "[]";
		}
	}
	
	public void setMsgExamples(final String msgExamples)
	{
		this.msgExamples = msgExamples;
	}
	
	public void setFromNumbers(String fromNumbers)
	{
		this.fromNumbers = fromNumbers;
	}
	
	public int getSilenced()
	{
		return silenced;
	}
	
	public void setSilenced(final int silenced)
	{
		this.silenced = silenced;
	}
	
	public int getBlocked()
	{
		return blocked;
	}
	
	public void setBlocked(final int blocked)
	{
		this.blocked = blocked;
	}
	
	public int getReceive()
	{
		return receive;
	}
	
	public void setReceive(final int receive)
	{
		this.receive = receive;
	}
	
	public String getImage()
	{
		if(image != null)
		{
			return image;
		}
		else
		{
			return "";
		}
	}
	
	public void setImage(final String image)
	{
		this.image = image;
	}
	
	public String getColorHex()
	{
		if(colorHex != null)
		{
			return colorHex;
		}
		else
		{
			return "";
		}
	}
	
	public void setColorHex(final String colorHex)
	{
		this.colorHex = colorHex;
	}
	
	public String getKeywords()
	{
		if(keywords != null)
		{
			return keywords;
		}
		else
		{
			return "";
		}
	}
	
	public void setKeywords(final String keywords)
	{
		this.keywords = keywords;
	}
	
	public String getUnsuscribe()
	{
		if(unsuscribe != null)
		{
			return unsuscribe;
		}
		else
		{
			return "";
		}
	}
	
	public void setUnsuscribe(final String unsuscribe)
	{
		this.unsuscribe = unsuscribe;
	}
	
	public int getType()
	{
		return type;
	}
	
	public void setType(int type)
	{
		this.type = type;
	}
	
	public int getStatus()
	{
		return status;
	}
	
	public void setStatus(int status)
	{
		this.status = status;
	}
	
	public String getCountryCode()
	{
		if(countryCode != null)
		{
			return countryCode;
		}
		else
		{
			return "";
		}
	}
	
	public void setCountryCode(String countryCode)
	{
		this.countryCode = countryCode;
	}
	
	public String getCompanyId()
	{
		if(companyId != null)
		{
			return companyId;
		}
		else
		{
			return "";
		}
	}
	
	public void setCompanyId(String companyId)
	{
		this.companyId = companyId;
	}
	
	public String getAbout()
	{
		if(about != null)
		{
			return about;
		}
		else
		{
			return "";
		}
	}
	
	public void setAbout(final String about)
	{
		this.about = about;
	}
	
	public String getIdentificationKey()
	{
		if(identificationKey != null)
		{
			return identificationKey;
		}
		else
		{
			return "";
		}
	}
	
	public void setIdentificationKey(final String identificationKey)
	{
		this.identificationKey = identificationKey;
	}
	
	public int getDataSent()
	{
		return dataSent;
	}
	
	public void setDataSent(final int dataSent)
	{
		this.dataSent = dataSent;
	}
	
	public String getIdentificationValue()
	{
		if(identificationValue != null)
		{
			return identificationValue;
		}
		else
		{
			return "";
		}
	}
	
	public void setIdentificationValue(final String identificationValue)
	{
		this.identificationValue = identificationValue;
	}

	public int getFollower()
	{
		return follower;
	}

	public void setFollower(final int follower)
	{
		this.follower = follower;
	}

	public int getGray()
	{
		return gray;
	}

	public void setGray(final int gray)
	{
		this.gray = gray;
	}

	public RealmList<Message> getMessages()
	{
		return messages;
	}

	public void setMessages(final RealmList<Message> messages)
	{
		this.messages = messages;
	}

	public Long getLastSocialUpdated()
	{
		return lastSocialUpdated;
	}

	public void setLastSocialUpdated(final Long lastSocialUpdated)
	{
		this.lastSocialUpdated = lastSocialUpdated;
	}

	public String getTwitter()
	{
		return twitter;
	}

	public void setTwitter(final String twitter)
	{
		this.twitter = twitter;
	}
}