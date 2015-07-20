package ru.rian.riamessenger.model;

import android.content.ContentValues;
import android.database.Cursor;
import java.util.Date;


public class MessageContainer {

	private long id;
	private long userId;
	private long chatId;
	private Date created;
	private boolean readed;
	private boolean sended;
	private String body;
	private String subject;

	private MessageContainer(long id, long userId, long chatId, long created
			, boolean readed, boolean sended
			, String body, String subject){
		this.id			= id;
		this.userId		= userId;
		this.chatId		= chatId;
		this.created	= new Date(created);
		this.readed		= readed;
		this.sended		= sended;
		this.body		= body;
		this.subject	= subject;
	}
	public MessageContainer(long userId, long chatId, long created
			, boolean readed, boolean sended
			, String body, String subject){
		this(-1, userId,chatId, created
				, readed, sended
				, body, subject);
	}
}
