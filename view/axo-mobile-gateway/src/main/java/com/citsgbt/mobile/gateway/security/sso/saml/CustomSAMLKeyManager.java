package com.citsgbt.mobile.gateway.security.sso.saml;

import com.citsgbt.mobile.core.lang.exception.BusinessException;
import com.citsgbt.mobile.gateway.security.sso.utils.KeyUtil;
import com.citsgbt.mobile.gateway.security.sso.utils.SSOUtils;
import com.citsgbt.mobile.gateway.security.sso.vo.LoginConfig;
import com.citsgbt.mobile.gateway.security.sso.vo.SAMLConfig;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.slf4j.Logger;
import org.springframework.security.saml.key.EmptyKeyManager;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * key manager
 */
class CustomSAMLKeyManager implements KeyManager {

	private static final Logger logger = getLogger(CustomSAMLKeyManager.class);

	private KeyManager defaultKeyManager = new EmptyKeyManager();

	private Map<String, KeyManager> keyManagerMap = new ConcurrentHashMap<>();

	private KeyManager getOrCreateKeyManager() {
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (loginConfig instanceof SAMLConfig) {
			SAMLConfig samlConfig = (SAMLConfig) loginConfig;
			String keyType = samlConfig.getKeyType();
			String keyPath = samlConfig.getKeyPath();
			if (!"DSA".equalsIgnoreCase(samlConfig.getKeyType()) && StringUtils.isNotEmpty(keyPath)) {
				String key = StringUtils.join(samlConfig.getCompanyCode(), "/", samlConfig.getKeyType(), "/", samlConfig.getKeyPath());
				if (!keyManagerMap.containsKey(key)) {
					synchronized (keyManagerMap) {
						if (!keyManagerMap.containsKey(key)) {
							String storePass = samlConfig.getKeyPassword();
							Map<String, String> passwords = new HashMap<>();
							passwords.put(samlConfig.getKeyAlias(), samlConfig.getKeyPassword());
							String defaultKey = samlConfig.getKeyAlias();
							KeyStore keyStore = initialize(keyPath, storePass, keyType);
							keyManagerMap.put(key, new JKSKeyManager(keyStore, passwords, defaultKey));
						}
					}
				}
				return keyManagerMap.get(key);
			}
		}
		return getDefaultKeyManager();
	}

	private KeyStore initialize(String storeFile, String storePass, String storeType) {
		try (InputStream inputStream = new FileInputStream(storeFile)) {
			KeyStore ks = KeyStore.getInstance(storeType);
			ks.load(inputStream, storePass == null ? null : storePass.toCharArray());
			return ks;
		} catch (Exception e) {
			throw new BusinessException("Error initializing keystore", e);
		}
	}

	@Override
	public Credential getCredential(String keyName) {
		LoginConfig loginConfig = SSOUtils.getCurrentLoginConfig();
		if (loginConfig instanceof SAMLConfig) {
			SAMLConfig samlConfig = (SAMLConfig) loginConfig;
			String keyType = samlConfig.getKeyType();
			if ("DSA".equalsIgnoreCase(keyType)) {
				try {
					KeyPair keyPair = KeyUtil.getKeyPair(samlConfig);
					return SecurityHelper.getSimpleCredential(keyPair.getPublic(), keyPair.getPrivate());
				} catch (Exception e) {
					logger.error("获取失败", e);
				}
			}
		}
		return getOrCreateKeyManager().getCredential(keyName);
	}

	@Override
	public Credential getDefaultCredential() {
		return getCredential(null);
	}

	@Override
	public String getDefaultCredentialName() {
		return getOrCreateKeyManager().getDefaultCredentialName();
	}

	@Override
	public Set<String> getAvailableCredentials() {
		return getOrCreateKeyManager().getAvailableCredentials();
	}

	@Override
	public X509Certificate getCertificate(String alias) {
		return getOrCreateKeyManager().getCertificate(alias);
	}

	@Override
	public Iterable<Credential> resolve(CriteriaSet criteria) throws SecurityException {
		return getOrCreateKeyManager().resolve(criteria);
	}

	@Override
	public Credential resolveSingle(CriteriaSet criteria) throws SecurityException {
		return getOrCreateKeyManager().resolveSingle(criteria);
	}

	private KeyManager getDefaultKeyManager() {
		return defaultKeyManager;
	}

	public void setDefaultKeyManager(KeyManager defaultKeyManager) {
		this.defaultKeyManager = defaultKeyManager;
	}

	public Map<String, KeyManager> getKeyManagerMap() {
		return keyManagerMap;
	}

	public void setKeyManagerMap(Map<String, KeyManager> keyManagerMap) {
		this.keyManagerMap = keyManagerMap;
	}
}
