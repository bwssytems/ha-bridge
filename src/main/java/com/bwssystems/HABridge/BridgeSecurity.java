package com.bwssystems.HABridge;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class BridgeSecurity {
	private static final Logger log = LoggerFactory.getLogger(BridgeSecurity.class);
	private char[] habridgeKey;
    private static final byte[] SALT = {
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
            (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
        };
    private BridgeSecurityDescriptor securityDescriptor;

	public BridgeSecurity(char[] theKey, String theData) {
		habridgeKey = theKey;
		securityDescriptor = null;
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

	public void setPassword(String aPassword) throws IOException {
		if(aPassword != null) {
			securityDescriptor.setUiPassword(String.valueOf(base64Decode(aPassword)));
			securityDescriptor.setPasswordSet(true);
		} else {
			securityDescriptor.setUiPassword(null);
			securityDescriptor.setPasswordSet(false);
		}
		securityDescriptor.setSettingsChanged(true);
	}

	public void setExecGarden(String theGarden) {
		securityDescriptor.setExecGarden(theGarden);
		securityDescriptor.setSettingsChanged(true);
	}

	public String getExecGarden() {
		return securityDescriptor.getExecGarden();
	}
	public void setUseLinkButton(boolean useThis) {
		securityDescriptor.setUseLinkButton(useThis);
		securityDescriptor.setSettingsChanged(true);
	}

	public boolean validatePassword(String targetPassword) throws IOException {
		if(securityDescriptor.isPasswordSet()) {
			if(securityDescriptor.getUiPassword().equals(String.valueOf(base64Decode(targetPassword)))) 
				return true;
		} else {
			log.warn("validating password when password is not set....");
			return true;
		}
		return false;
	}
	
	public boolean isSecure() {
		return securityDescriptor.isPasswordSet();
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
}
