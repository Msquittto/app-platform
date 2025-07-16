/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.apikey.utils;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * PBKDF2 算法。
 *
 * @author 李智超
 * @since 2024-12-09
 */
public class Pbkdf2Util {
    /**
     * 默认的加密用的填充。
     */
    public static final String DEFAULT_CIPHER_AND_PADDING = "PBKDF2WithHmacSHA256";

    /**
     * 计算摘要。
     *
     * @param hashAlgorithm 表示哈希算法的 {@link String}。
     * @param password 表示口令的 {@link char[]}。
     * @param salt 表示盐值的 {@link String}。
     * @param iterations 表示迭代次数的 {@link int}。
     * @param keyLength 表示生成密文长度的 {@link int}。
     * @return 表示密文的 {@link byte[]}。
     * @throws NoSuchAlgorithmException 表示没有算法的异常。
     * @throws InvalidKeySpecException 表示非法 key 的异常。
     */
    public static byte[] digest(String hashAlgorithm, char[] password, String salt, int iterations, int keyLength)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory kFactory = SecretKeyFactory.getInstance(hashAlgorithm);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password, salt.getBytes(StandardCharsets.UTF_8), iterations, keyLength);
        SecretKey secretKey = kFactory.generateSecret(pbeKeySpec);
        return secretKey.getEncoded();
    }

    /**
     * 建议口令单项哈希算法。
     * 要求迭代次数至少10000次，有性能约束的产品最少1000次，输出秘钥的长度最少256比特(32字节)，其中盐值和迭代次数不需要加密保存。
     *
     * @param password 表示用户输入口令的 {@link String}。
     * @param salt 表示盐值的 {@link String}。
     * @return 表示密文的 {@link byte[]}。
     * @throws SecurityException 表示安全异常。
     */
    public static byte[] pbkdf2ForPass(String password, String salt) throws SecurityException {
        // 默认采用PBKDF2WithHmacSHA256算法，迭代次数10000次，输出密文的字节长度256。
        try {
            return digest(DEFAULT_CIPHER_AND_PADDING, password.toCharArray(), salt, 10000, 256);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new SecurityException("Apikey encrypted error.");
        }
    }

    /**
     * 建议 PBKDF2 算法字符串密文保存格式: AlgID_Salt_IterCount_CipherData。
     *
     * @param password 示用户输入口令的 {@link String}。
     * @param salt 表示盐值的 {@link String}。
     * @return 表示密文的 {@link String}。
     */
    public static String pbkdf2ForPassStandard(String password, String salt) {
        byte[] digest = pbkdf2ForPass(password, salt);
        // Base64编码字符串形式
        return Base64.getEncoder().encodeToString(digest);
    }
}
