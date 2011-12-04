package com.mine.oauth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

public class OAuthHelper {
	private OAuthConsumer mConsumer;
	private OAuthProvider mProvider;
	private String mCallbackUrl;
	private final String mScope = "https://mail.google.com/";
	private final String mCallback = "mine-activity://mine-vibration.com/";
	 
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
}