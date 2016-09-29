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

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加密与解密的工具类
 *
 */
public final class CipherUtils {
    /**
     * 算法
     */
    private static final String ALGORITHM = "DES";
    /**
     * DES公钥（应该是随机的8位）
     */
    public static final String ENCRYPT_KEY = "1a2b3c4d";
    /**
     * MD5加密
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

    /**
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     */

    /**
     * DES的向量键
     */
    private static byte[] key = {0x63, 0x79, 0x68, 0x67, 0x30, 0x32, 0x33, 0x34};
    /**
     * @param encryptString
     *            需要加密的明文
     * @param encryptKey
     *            DES公钥
     * @return 加密后的密文
     * @throws Exception
     */
    public static String encryptDES(String encryptString, String encryptKey)
            throws Exception {
        // 实例化IvParameterSpec对象，使用指定的初始化向量
        IvParameterSpec zeroIv = new IvParameterSpec(key);
        // 实例化SecretKeySpec类，根据字节数组来构造SecretKey
        SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes(), ALGORITHM);
        // 创建密码器
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        // 用秘钥初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
        // 执行加密操作
        byte[] encryptedData = cipher.doFinal(encryptString.getBytes());

        return Base64.encodeToString(encryptedData, Base64.DEFAULT);
    }

    /****
     * @param decrypString
     *            密文
     * @param decryptKey
     *            DES公钥
     * @return 解密后的明文
     * @throws Exception
     */
    public static String decryptDES(String decrypString, String decryptKey)
            throws Exception {
        byte[] byteMi = Base64.decode(decrypString, Base64.DEFAULT);
        // 实例化IvParameterSpec对象，使用指定的初始化向量
        IvParameterSpec zeroIv = new IvParameterSpec(key);
        // 实例化SecretKeySpec类，根据字节数组来构造SecretKey
        SecretKeySpec key = new SecretKeySpec(decryptKey.getBytes(), ALGORITHM);
        // 创建密码器
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        // 用秘钥初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
        // 执行解密操作
        byte[] decryptedData = cipher.doFinal(byteMi);
        return new String(decryptedData);
    }



    //    /**
//     * 返回可逆算法DES的密钥
//     *前8字节将被用来生成密钥
//     * @return 生成的密钥
//     * @throws Exception
//     */
//    public static Key getKey() {
//        try {
//            DESKeySpec des = new DESKeySpec(key);
//            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
//            return keyFactory.generateSecret(des);
//        } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    /**
//     * 根据指定的密钥及算法，将字符串进行解密。
//     *
//     * @param data      要进行解密的数据，它是由原来的byte[]数组转化为字符串的结果。
//     * @param key       密钥。
//     * @return 解密后的结果。它由解密后的byte[]重新创建为String对象。如果解密失败，将返回null。
//     * @throws Exception
//     */
//    public static String decrypt(String data, Key key) {
//        String result = null;
//        try {
////            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            Cipher cipher = Cipher.getInstance(DES);
//            cipher.init(Cipher.DECRYPT_MODE, key);
//            result = new String(cipher.doFinal(StringUtils
//                    .hexStringToByteArray(data)), "utf8");
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//        }
//        return result;
//    }
//
//    /**
//     * 根据指定的密钥及算法对指定字符串进行可逆加密。
//     *
//     * @param data      要进行加密的字符串。
//     * @param key       密钥。
//     * @return 加密后的结果将由byte[]数组转换为16进制表示的数组。如果加密过程失败，将返回null。
//     */
//    public static String encrypt(String data, Key key){
//        String s = null;
//        try {
//            //            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//            Cipher cipher = Cipher.getInstance(DES);
//            cipher.init(Cipher.ENCRYPT_MODE, key);
//            s = StringUtils.byteArrayToHexString(cipher.doFinal(data.getBytes("utf8")));
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//        }
//        return s;
//    }
}
