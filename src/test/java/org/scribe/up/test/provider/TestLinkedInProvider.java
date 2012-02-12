/*
  Copyright 2012 Jérôme Leleu

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.scribe.up.test.provider;

import junit.framework.TestCase;

import org.scribe.model.Token;
import org.scribe.up.credential.OAuthCredential;
import org.scribe.up.profile.UserProfile;
import org.scribe.up.provider.impl.LinkedInProvider;
import org.scribe.up.test.util.PrivateData;
import org.scribe.up.test.util.SingleUserSession;
import org.scribe.up.test.util.WebHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

/**
 * This class tests the LinkedInProvider by simulating a complete authentication.
 * 
 * @author Jérôme Leleu
 * @since 1.0.0
 */
public class TestLinkedInProvider extends TestCase {
    
    private static final Logger logger = LoggerFactory.getLogger(TestLinkedInProvider.class);
    
    public void testProvider() throws Exception {
        // init provider
        LinkedInProvider linkedinProvider = new LinkedInProvider();
        linkedinProvider.setKey(PrivateData.get("linkedin.key"));
        linkedinProvider.setSecret(PrivateData.get("linkedin.secret"));
        linkedinProvider.setCallbackUrl(PrivateData.get("callbackUrl"));
        linkedinProvider.setName("testLinkedIn");
        linkedinProvider.init();
        
        // authorization url
        SingleUserSession testSession = new SingleUserSession();
        String authorizationUrl = linkedinProvider.getAuthorizationUrl(testSession);
        logger.debug("authorizationUrl : {}", authorizationUrl);
        WebClient webClient = WebHelper.newClient();
        HtmlPage loginPage = webClient.getPage(authorizationUrl);
        HtmlForm form = loginPage.getFormByName("oauthAuthorizeForm");
        HtmlTextInput sessionKey = form.getInputByName("session_key");
        sessionKey.setValueAttribute(PrivateData.get("linkedin.login"));
        HtmlPasswordInput sessionPassword = form.getInputByName("session_password");
        sessionPassword.setValueAttribute(PrivateData.get("linkedin.password"));
        HtmlSubmitInput submit = form.getInputByName("authorize");
        HtmlPage callbackPage = submit.click();
        String callbackUrl = callbackPage.getUrl().toString();
        logger.debug("callbackUrl : {}", callbackUrl);
        
        OAuthCredential credential = linkedinProvider.extractCredentialFromParameters(WebHelper
            .extractParametersFromUrl(callbackUrl));
        // access token
        Token accessToken = linkedinProvider.getAccessToken(testSession, credential);
        logger.debug("accessToken : {}", accessToken);
        // user profile
        UserProfile userProfile = linkedinProvider.getUserProfile(accessToken);
        logger.debug("userProfile : {}", userProfile);
        assertEquals(PrivateData.get("linkedin.id"), userProfile.getId());
        assertEquals(PrivateData.get("linkedin.attributeValue1"),
                     userProfile.getAttributes().get(PrivateData.get("linkedin.attributeName1")));
        assertEquals(PrivateData.get("linkedin.nbAttributes"), "" + userProfile.getAttributes().size());
    }
}