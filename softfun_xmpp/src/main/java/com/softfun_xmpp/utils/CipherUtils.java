/**
 * Copyright 2014 Zhenguo Jin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.softfun_xmpp.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * 加密与解密的工具类
 *
 */
public final class CipherUtils {

    /**
     * MD5加密
     * <br>http://stackoverflow.com/questions/1057041/difference-between-java-and-php5-md5-hash
     * <br>http://code.google.com/p/roboguice/issues/detail?id=89
     *
     * @param string 源字符串
     * @return 加密后的字符串
     */
    public static String md5(String string) {
        byte[] hash = null;
        try {
            hash = MessageDigest.getInstance("MD5").digest(
                    string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    private static byte[] key = {0x63, 0x79, 0x68, 0x67, 0x30, 0x32, 0x33, 0x34};
    /**
     * 返回可逆算法DES的密钥
     *前8字节将被用来生成密钥
     * @return 生成的密钥
     * @throws Exception
     */
    public static Key getKey() {
        try {
            DESKeySpec des = new DESKeySpec(key);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            return keyFactory.generateSecret(des);
        } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据指定的密钥及算法，将字符串进行解密。
     *
     * @param data      要进行解密的数据，它是由原来的byte[]数组转化为字符串的结果。
     * @param key       密钥。
     * @return 解密后的结果。它由解密后的byte[]重新创建为String对象。如果解密失败，将返回null。
     * @throws Exception
     */
    public static String decrypt(String data, Key key) {
        String result = null;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            result = new String(cipher.doFinal(StringUtils
                    .hexStringToByteArray(data)), "utf8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    /**
     * 根据指定的密钥及算法对指定字符串进行可逆加密。
     *
     * @param data      要进行加密的字符串。
     * @param key       密钥。
     * @return 加密后的结果将由byte[]数组转换为16进制表示的数组。如果加密过程失败，将返回null。
     */
    public static String encrypt(String data, Key key){
        String s = null;
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            s = StringUtils.byteArrayToHexString(cipher.doFinal(data.getBytes("utf8")));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return s;
    }
}
