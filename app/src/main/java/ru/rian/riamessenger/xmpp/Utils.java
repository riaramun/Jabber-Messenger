package ru.rian.riamessenger.xmpp;

/**
 * Created by grigoriy on 26.06.15.
 */
public class Utils {
	public static String extractJid(String recepient){
		int pos = recepient.indexOf("/");
		if(pos >= 0){
			return recepient.substring(0,pos);
		}
		return recepient;
	}

	public static String extractResource(String recepient){
		int pos = recepient.indexOf("/");
		if(pos >= 0){
			return recepient.substring(pos+1);
		}
		return "";
	}
}
