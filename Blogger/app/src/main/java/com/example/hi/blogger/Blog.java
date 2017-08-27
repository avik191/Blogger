package com.example.hi.blogger;

/**
 * Created by HI on 24-Aug-17.
 */

public class Blog
{
	String title,description,url,uid,username,image,postkey;

	int timestamp,views;

	public int getViews()
	{
		return views;
	}

	public void setViews(int views)
	{
		this.views = views;
	}

	public int getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(int timestamp)
	{
		this.timestamp = timestamp;
	}

	public String getTitle()
	{
		return title;
	}

	public String getUid()
	{
		return uid;
	}

	public String getPostkey()
	{
		return postkey;
	}

	public void setPostkey(String postkey)
	{
		this.postkey = postkey;
	}

	public String getImage()
	{
		return image;
	}

	public void setImage(String image)
	{
		this.image = image;
	}

	public void setUid(String uid)
	{
		this.uid = uid;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}
}
