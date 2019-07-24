package com.citsgbt.mobile.gateway.config.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

/**
 * Created by gary.fu on 2016/9/18.
 */
public class EncryptionUtils {

	private static final Logger logger = LoggerFactory.getLogger(EncryptionUtils.class);

	public static final String ALGORITHM_DES = "DES";
	public static final String ALGORITHM_AES = "AES";

	public static final String ALGORITHM_SHA_256 = "SHA-256";
	public static final String ALGORITHM_SHA_384 = "SHA-384";
	public static final String ALGORITHM_SHA_512 = "SHA-512";
	public static final String ALGORITHM_MD5 = "MD5";

	private static String encryptStr(String content, String password) {
		byte[] results = encrypt(content, password);
		return Base64.encodeBase64String(results);
	}

	private static byte[] encrypt(String content, String password) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.setSeed(password.getBytes());
			kgen.init(128, secureRandom);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			byte[] byteContent = content.getBytes(StandardCharsets.UTF_8);
			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
			return cipher.doFinal(byteContent); // 加密
		} catch (Exception e) {
			logger.error("AES加密失败", e);
		}
		return new byte[0];
	}

	private static String decryptStr(String encryptContent, String password) {
		String result = "";
		try {
			byte[] results = decrypt(Base64.decodeBase64(encryptContent), password);
			result = new String(results, StandardCharsets.UTF_8);
		} catch (Exception e) {
			logger.error("解密失败", e);
		}
		return result;
	}

	/**
	 * 解密
	 *
	 * @param encryptContent 待解密内容
	 * @param password       解密密钥
	 * @return
	 */
	private static byte[] decrypt(byte[] encryptContent, String password) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.setSeed(password.getBytes());
			kgen.init(128, secureRandom);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
			return cipher.doFinal(encryptContent); // 加密
		} catch (Exception e) {
			logger.error("AES解密失败", e);
		}
		return new byte[0];
	}

	/**
	 * SHA加密
	 *
	 * @param algorithm - SHA-256, SHA512, MD5
	 * @return
	 * @throws Exception
	 */
	public static String irreversibleEncrypt(String srcString, String algorithm) {
		try {
			byte[] data = srcString.getBytes();
			MessageDigest sha = MessageDigest.getInstance(algorithm);
			sha.update(data);
			return new HexBinaryAdapter().marshal(sha.digest());
		} catch (NoSuchAlgorithmException e) {
			return srcString;
		}
	}


	public static String irreversibleEncryptWithSalt(String srcString, String salt, String algorithm) {
		return irreversibleEncrypt(srcString.concat(salt), algorithm);
	}

	public static String generateJwtToken(String companyCode, String loginName, String pass) {
		long time = System.currentTimeMillis();
		String sign = StringUtils.EMPTY;
		try {
			JWSSigner signer = new MACSigner(pass);
			JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
					.claim("companyCode", companyCode)
					.claim("loginName", loginName)
					.claim("timestamp", time).build();
			SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
			signedJWT.sign(signer);
			sign = signedJWT.serialize();
		} catch (Exception e) {
			logger.error("生成JWTToken失败", e);
		}
		return sign;
	}

	public static void main(String[] args) throws Exception {
		String timestamp = String.valueOf(new Date().getTime());
		String content = "publish,billy.yuan@citsgbt.com";
		String password = timestamp + "Password1";
		logger.debug(timestamp);
		int encryptTimes = 1;
		String result = content;
		for (int i = 0; i < encryptTimes; i++) {
			result = encryptStr(result, password);
		}
		logger.debug(result);
		String name = result;
		for (int i = 0; i < encryptTimes; i++) {
			name = decryptStr(name, password);
		}
		logger.debug(name);
		SecureRandom random = new SecureRandom();
		byte[] sharedSecret = new byte[32];
		random.nextBytes(sharedSecret);
		String pass = Base64.encodeBase64String(sharedSecret);
		logger.debug(pass);
		long time = System.currentTimeMillis();
		JWSSigner signer = new MACSigner("eW8kKwkV2Kvea0jJQRbQSKiAspUS3zQHIe+8+UqS9e0=");
		JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
				.claim("companyCode", "publish")
				.claim("loginName", "billy.yuan@citsgbt.com")
				.claim("timestamp", time).build();
		SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
		signedJWT.sign(signer);
		String sign = signedJWT.serialize();
		logger.debug(sign);
	}
}
