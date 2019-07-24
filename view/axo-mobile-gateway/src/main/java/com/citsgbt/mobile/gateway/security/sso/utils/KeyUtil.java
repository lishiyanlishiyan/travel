package com.citsgbt.mobile.gateway.security.sso.utils;

import com.citsgbt.mobile.gateway.security.sso.vo.SAMLConfig;
import net.shibboleth.utilities.java.support.security.KeyNotFoundException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.UsageType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

/**
 * KeyUtil_老AXO
 */
public class KeyUtil {

	private static final Log logger = LogFactory.getLog(KeyUtil.class);

	private KeyUtil() {

	}

	/**
	 * @throws CertificateException
	 * @throws IOException
	 * @throws Exception
	 */
	public static KeyPair getKeyPair(SAMLConfig samlConfig) throws CertificateException, IOException {
		return new KeyPair(getPublicKey(samlConfig.getCertPath(), samlConfig.getCertType()), getPrivateKey(samlConfig.getKeyPath(), samlConfig.getKeyType(), samlConfig.getKeyAlias(), samlConfig.getKeyPassword()));

	}

	/**
	 * 获取私有密钥
	 *
	 * @param keyPath
	 * @param keyType
	 * @param alias
	 * @param password
	 * @return
	 * @throws Exception
	 *
	 * David.Jiang 2016-3-25
	 * 此处设计上有疑义，猜测当初Louis的做法是用DSA的私钥，所以keypath放的是私钥文件路径
	 * 如果要使用DSA以外的私钥，如：RSA等，keypath放的是keystore的路径；keytype也是keystore的类型，如：pkcs12，jks等；alias放的是私钥在keystore中的别名；password是打开keystore文件的密码
	 */
	private static PrivateKey getPrivateKey(String keyPath, String keyType, String alias, String password) {
		PrivateKey key = null;
		File keyFile = new File(keyPath);
		try  (FileInputStream stream = new FileInputStream(keyFile)) {
			if (keyType.equals("DSA")) {
				byte[] keyBytes;
				keyBytes = new byte[stream.available()];
				if (stream.read(keyBytes) == -1) {
					throw new KeyNotFoundException("密钥文件未找到！");
				}
				KeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
				KeyFactory keyFactory;
				keyFactory = KeyFactory.getInstance("DSA");
				key = keyFactory.generatePrivate(keySpec);
			} else if (StringUtils.isNotEmpty(keyPath)) {
				KeyStore ks = getKeyStore(keyPath, keyType, password);
				key = (PrivateKey) ks.getKey(alias, password.toCharArray());
			}
		} catch (Exception e) {
			logger.error(ExceptionUtils.getFullStackTrace(e));
		}
		return key;
	}

	/**
	 * 获取公有密钥
	 *
	 * @param certPath
	 * @param certType
	 * @return
	 * @throws CertificateException
	 * @throws IOException
	 */
	private static PublicKey getPublicKey(String certPath, String certType) throws CertificateException, IOException {
		Certificate certificate = getCertificate(certPath, certType);
		return certificate.getPublicKey();
	}

	public static Certificate getCertificate(String certPath, String certType) throws CertificateException, IOException {
		CertificateFactory certificateFactory = CertificateFactory.getInstance(certType);
		try (FileInputStream in = new FileInputStream(certPath)) {
			return certificateFactory.generateCertificate(in);
		}

	}

	public static PublicKey getPublicKeyFromCert(SAMLConfig samlConfig) throws CertificateException, IOException {
		PublicKey publicKey;
		CertificateFactory cf = CertificateFactory.getInstance(samlConfig.getCertType());
		try (FileInputStream fileInputStream = new FileInputStream(samlConfig.getCertPath())) {
			Certificate c1 = cf.generateCertificate(fileInputStream);
			((X509Certificate) c1).checkValidity(new Date());
			publicKey = c1.getPublicKey();
			return publicKey;
		}
	}

	/**
	 * 获取keyStore
	 *
	 * @param keyPath
	 * @param keyType
	 * @param password
	 * @return
	 * @throws Exception
	 */
	private static KeyStore getKeyStore(String keyPath, String keyType, String password) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
		try(FileInputStream is = new FileInputStream(keyPath)) {
			KeyStore ks = KeyStore.getInstance(keyType);
			ks.load(is, password.toCharArray());
			return ks;
		}
	}


	public static Credential getCredential(String privateKeyPath, String privateKeyType, String privateKeyAlias, String privateKeyPassword, String publicKeyCertPath, String publicKeyCertType) throws CertificateException, IOException {
		PrivateKey privateKey = getPrivateKey(privateKeyPath, privateKeyType, privateKeyAlias, privateKeyPassword);
		PublicKey publicKey = getPublicKey(publicKeyCertPath, publicKeyCertType);
		BasicCredential basicCredential = new BasicCredential();
		basicCredential.setUsageType(UsageType.UNSPECIFIED);
		basicCredential.setPrivateKey(privateKey);
		basicCredential.setPublicKey(publicKey);
		return basicCredential;
	}
}
