/*************************************************************************
 * MineMessageVibrator is an Android App that provides vibrate and 
 * reminder functions for SMS, MMS, Gmail, etc. 
 * Copyright (C) 2010  Lei YU
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ************************************************************************/

package com.mine.oauth;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;

import oauth.signpost.OAuth;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

public class OAuthHelper implements Serializable {
	private static final long serialVersionUID = 4308192229877317644L;
	private CommonsHttpOAuthConsumer mConsumer;
	private CommonsHttpOAuthProvider mProvider;
	private String mCallbackUrl;
	private final String mScope = "https://mail.google.com/";
	private final String mCallback = "mine-activity://mine-vibration/";
	private static final String mSaveLoadFile = "OAuthHelper";

	public OAuthHelper(String consumerKey, String consumerSecret,
			String scope, String callbackUrl)
	  throws UnsupportedEncodingException {
		scope = mScope;
		callbackUrl = mCallback;
	    mConsumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
	    mProvider = new CommonsHttpOAuthProvider(
	            "https://www.google.com/accounts/OAuthGetRequestToken?scope="
	            + URLEncoder.encode(scope, "utf-8"),
	            "https://www.google.com/accounts/OAuthGetAccessToken",
	            "https://www.google.com/accounts/OAuthAuthorizeToken?hd=default");
	    //mProvider.setOAuth10a(true);
	    mCallbackUrl = (callbackUrl == null ? OAuth.OUT_OF_BAND : callbackUrl);
	}

	public String getRequestToken()
		throws OAuthMessageSignerException, OAuthNotAuthorizedException,
				OAuthExpectationFailedException, OAuthCommunicationException {
	    String authUrl = mProvider.retrieveRequestToken(mConsumer, mCallbackUrl);
	    return authUrl;
	}

	public String[] getAccessToken(String verifier)
		throws OAuthMessageSignerException, OAuthNotAuthorizedException,
				OAuthExpectationFailedException, OAuthCommunicationException {
	    mProvider.retrieveAccessToken(mConsumer, verifier);
	    return new String[] {
	            mConsumer.getToken(), mConsumer.getTokenSecret()
	    };
	}

	public static OAuthHelper load(Context context) {
		OAuthHelper helper = null;
		try {
			FileInputStream fis;
			fis = context.openFileInput(mSaveLoadFile);
			ObjectInputStream is = new ObjectInputStream(fis);
			helper = (OAuthHelper) is.readObject();
			helper.mProvider.setHttpClient(new DefaultHttpClient());
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return helper;
	}

	public static void save(Context context, OAuthHelper helper) {
		try {
			FileOutputStream fos = context.openFileOutput(mSaveLoadFile,
					Context.MODE_PRIVATE);
			ObjectOutputStream os = new ObjectOutputStream(fos);

			os.writeObject(helper);
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

