package com.bwssystems.HABridge;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.Map.Entry;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bwssystems.HABridge.api.hue.HueError;
import com.bwssystems.HABridge.api.hue.HueErrorResponse;
import com.bwssystems.HABridge.api.hue.WhitelistEntry;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import spark.Request;

public class BridgeSecurity {
	private static final Logger log = LoggerFactory.getLogger(BridgeSecurity.class);
	private static final String USER_SESSION_ID = "user";
	private static final String DEPRACATED_INTERNAL_USER = "thehabridgeuser";
	private static final String TEST_USER_TYPE = "test_ha_bridge";
    private static final byte[] SALT = {
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
        };
	private char[] habridgeKey;
	private String execGarden;
    private BridgeSecurityDescriptor securityDescriptor;
	private boolean settingsChanged;

	public BridgeSecurity(char[] theKey, String theExecGarden) {
		habridgeKey = theKey;
		execGarden = theExecGarden;
		securityDescriptor = null;
		settingsChanged = false;
	}

	public void setSecurityData(String theData) {
		String anError = null;
		if(theData != null && !theData.isEmpty()) {
			try {
				securityDescriptor = new Gson().fromJson(decrypt(theData), BridgeSecurityDescriptor.class);
			} catch (JsonSyntaxException e) {
				anError = e.getMessage();
			} catch (GeneralSecurityException e) {
				anError = e.getMessage();
			} catch (IOException e) {
				anError = e.getMessage();
			}
			if(anError != null)
				log.warn("Cound not get security data, using default security (none): " + anError);
		}
		
		if(theData == null || anError != null) {
			securityDescriptor = new BridgeSecurityDescriptor();
		}
	}

	public String getSecurityDescriptorData() throws UnsupportedEncodingException, GeneralSecurityException {
		return encrypt(new Gson().toJson(securityDescriptor));
	}
	
	public boolean isUseLinkButton() {
		return securityDescriptor.isUseLinkButton();
	}

	public String setPassword(User aUser) throws IOException {
		String error = null;
		if(aUser != null) {
			error = aUser.validate();
			if(error == null) {
				if(securityDescriptor.getUsers() != null) {
					User theUser = securityDescriptor.getUsers().get(aUser.getUsername());
					if(theUser != null) {
						theUser.setPassword(aUser.getPassword());
						theUser.setPassword2(null);
						settingsChanged = true;
					}
					else
						error = "User not found";
				}
				else
					error = "User not found";
			}
		}
		else
			error = "invalid user object given";
		
		return error;
	}

	public String addUser(User aUser) throws IOException {
		String error = null;
		if(aUser != null) {
			error = aUser.validate();
			if(error == null) {
				if(securityDescriptor.getUsers() == null)
					securityDescriptor.setUsers(new HashMap<String, User>());
				if(securityDescriptor.getUsers().get(aUser.getUsername()) == null) {
					securityDescriptor.getUsers().put(aUser.getUsername(), aUser);
					settingsChanged = true;
				}
				else
					error = "Invalid request";
			}
		}
		else
			error = "invalid user object given";
		
		return error;
	}

	public String delUser(User aUser) throws IOException {
		String error = null;
		if(aUser != null) {
				if(securityDescriptor.getUsers() != null) {
				if(securityDescriptor.getUsers().get(aUser.getUsername()) != null) {
					securityDescriptor.getUsers().remove(aUser.getUsername());
					settingsChanged = true;
				}
				else
					error = "User not found";
				}
		}
		else
			error = "invalid user object given";
		
		return error;
	}

	public String getExecGarden() {
		return execGarden;
	}
	public void setUseLinkButton(boolean useThis) {
		securityDescriptor.setUseLinkButton(useThis);
		settingsChanged = true;
	}

	public boolean isSecureHueApi() {
		return securityDescriptor.isSecureHueApi();
	}
	
	public void setSecureHueApi(boolean theState) {
		securityDescriptor.setSecureHueApi(theState);
	}
	public SecurityInfo getSecurityInfo() {
		SecurityInfo theInfo = new SecurityInfo();
		theInfo.setUseLinkButton(isUseLinkButton());
		theInfo.setSecureHueApi(isSecureHueApi());
		theInfo.setSecure(isSecure());
		return theInfo;
	}
	public LoginResult validatePassword(User targetUser) throws IOException {
		LoginResult result = new LoginResult();
		if(targetUser != null && targetUser.getUsername() != null) {
			if(securityDescriptor.getUsers() != null && securityDescriptor.getUsers().get(targetUser.getUsername()) != null) {
				User theUser = securityDescriptor.getUsers().get(targetUser.getUsername());
				if(theUser.getPassword() != null) {
					theUser.setPassword2(targetUser.getPassword());
					if(theUser.validatePassword()) {
						theUser.setPassword2(null);
						result.setUser(targetUser);
					}
					else
						result.setError("user or password not correct");
				} else {
					result.setError("input password is not set....");
				}
			}
			else
				result.setError("user or password not correct");
		}
		else
			result.setError("input user not given");
		return result;
	}
	
	public boolean isSecure() {
		return securityDescriptor.isSecure();
	}

	public boolean isSettingsChanged() {
		return settingsChanged;
	}

	public void setSettingsChanged(boolean settingsChanged) {
		this.settingsChanged = settingsChanged;
	}
	public Map<String, WhitelistEntry> getWhitelist() {
		return securityDescriptor.getWhitelist();
	}

	public HueError[] validateWhitelistUser(String aUser, String userDescription, boolean strict) {
		String validUser = null;
		boolean found = false;
		if (aUser != null && !aUser.equalsIgnoreCase("undefined") && !aUser.equalsIgnoreCase("null")
				&& !aUser.equalsIgnoreCase("") && !aUser.equals(DEPRACATED_INTERNAL_USER)) {
			if (securityDescriptor.getWhitelist() != null) {
				Set<String> theUserIds = securityDescriptor.getWhitelist().keySet();
				Iterator<String> userIterator = theUserIds.iterator();
				while (userIterator.hasNext()) {
					validUser = userIterator.next();
					if (validUser.equals(aUser)) {
						found = true;
						log.debug("validateWhitelistUser: found a user <" + aUser + ">");
					}
				}
			}

			if(!found && !strict) {
				log.debug("validateWhitelistUser: a user was not found and it is not strict rules <" + aUser + "> being created");
				newWhitelistUser(aUser, userDescription);
				
				found = true;
			}
		}
		
		if (!found) {
			log.debug("validateWhitelistUser: a user was not found and strict rules is set to: " + strict + "for user <" + aUser + ">");
			return HueErrorResponse.createResponse("1", "/api/" + aUser == null ? "" : aUser, "unauthorized user", null, null, null).getTheErrors();
		}

		return null;
	}
	
	private void newWhitelistUser(String aUser, String userDescription) {
		if (securityDescriptor.getWhitelist() == null) {
			securityDescriptor.setWhitelist(new HashMap<>());
		}
		if(userDescription == null)
			userDescription = "auto insert user";
		
		securityDescriptor.getWhitelist().put(aUser, WhitelistEntry.createEntry(userDescription));
		setSettingsChanged(true);
	}

	public String createWhitelistUser(String userDescription) {
		String aUser = getNewUserID();
		newWhitelistUser(aUser, userDescription);
		return aUser;
	}

	public void convertWhitelist(Map<String, WhitelistEntry> whitelist) {
		securityDescriptor.setWhitelist(whitelist);
	}

	private String getNewUserID() {
		UUID uid = UUID.randomUUID();
		StringTokenizer st = new StringTokenizer(uid.toString(), "-");
		String newUser = "";
		while (st.hasMoreTokens()) {
			newUser = newUser + st.nextToken();
		}

		return newUser;
	}
	
	public void removeTestUsers() {
		if (securityDescriptor.getWhitelist() != null) {
			Object anUser = securityDescriptor.getWhitelist().remove(DEPRACATED_INTERNAL_USER);
			if(anUser != null)
				setSettingsChanged(true);

		    Iterator<Entry<String, WhitelistEntry>> it = securityDescriptor.getWhitelist().entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry<String, WhitelistEntry> pair = it.next();
		        if(pair.getValue().getName().equals(TEST_USER_TYPE)) {
			        it.remove(); // avoids a ConcurrentModificationException
					setSettingsChanged(true);
		        }
		    }
		}
	}

	private String encrypt(String property) throws GeneralSecurityException, UnsupportedEncodingException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(habridgeKey));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return base64Encode(pbeCipher.doFinal(property.getBytes("UTF-8")));
    }

    private static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private String decrypt(String property) throws GeneralSecurityException, IOException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(habridgeKey));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8");
    }

    private static byte[] base64Decode(String property) throws IOException {
        return Base64.getDecoder().decode(property);
    }

    public void addAuthenticatedUser(Request request, User u) {
		request.session().attribute(USER_SESSION_ID, u);
		
	}

	public void removeAuthenticatedUser(Request request) {
		request.session().removeAttribute(USER_SESSION_ID);
		
	}

	public User getAuthenticatedUser(Request request) {
		User theUser = request.session().attribute(USER_SESSION_ID);
		if(theUser == null) {
			String authHeader = request.headers("Authorization");
			if(authHeader != null) {
			    byte[] authData;
				try {
					authData = base64Decode(authHeader.substring(6));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					return theUser;
				}
			    String[] credentials = new String(authData).split(":");
			    String username = credentials[0];
			    String password = credentials[1];
			    theUser = new User();
			    theUser.setUsername(username);
			    theUser.setPassword(password);
			    LoginResult theResult = null;
				try {
					theResult = validatePassword(theUser);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					return null;
				}
				if(theResult != null && theResult.getError() == null) {
					addAuthenticatedUser(request, theUser);
				}
			}
		}
		return theUser;
	}
}