package cloud.ohiyou.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AesUtils {
    // Base64 字符表
    private static final String BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    public static String decrypted(String encryptedPage) throws Exception {
        // 提取 raw_key 数组
        int[] rawKeyInts = extractRawKey(encryptedPage);
        // 提取 encrypted 字符串
        String encrypted = extractEncrypted(encryptedPage);
        // 提取 tag 数组
        int[] tagInts = extractArray(encryptedPage, "tag");
        // 提取 iv 数组
        int[] ivInts = extractArray(encryptedPage, "iv");


        byte[] rawKey = toSignedByteArray(rawKeyInts);
        byte[] iv = toSignedByteArray(ivInts);
        byte[] tag = toSignedByteArray(tagInts);
        byte[] encryptedData = hexStringToByteArray(encrypted);

        // 获取加密方式
        String decryptionMode = getDecryptionMode(encryptedPage);

        // 合并加密数据和tag
        byte[] cipherText = new byte[encryptedData.length + tag.length];
        System.arraycopy(encryptedData, 0, cipherText, 0, encryptedData.length);
        System.arraycopy(tag, 0, cipherText, encryptedData.length, tag.length);

        byte[] decryptedData = decrypt(decryptionMode, rawKey, iv, cipherText);

        System.out.println("Decrypted text: " + new String(decryptedData).trim());


//        // 如果没有 tag 则使用CTR 加密
//        if (tag.length <= 0) {
//            return decryptAESCTR(new SecretKeySpec(rawKey, "AES"), new IvParameterSpec(iv), encryptedData);
//        } else {
//
//        // 合并加密数据和tag
//        byte[] cipherText = new byte[encryptedData.length + tag.length];
//        System.arraycopy(encryptedData, 0, cipherText, 0, encryptedData.length);
//        System.arraycopy(tag, 0, cipherText, encryptedData.length, tag.length);
//
//        // 使用AES-GCM解码
//        SecretKey secretKey = new SecretKeySpec(rawKey, "AES");
//        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv); // 128为tag长度
//
//        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
//        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);
//
//        byte[] decryptedData = cipher.doFinal(cipherText);
//
//        // 输出解码后的数据
//        String result = new String(decryptedData, "UTF-8");
//        System.out.println("Decrypted data: " + result);

        return null;
    }

    private static String getDecryptionMode(String encryptedPage) {
        // 通过正则匹配数组
        String regex = "var\\s+(\\w+)\\s*=\\s*\\[\\s*(['\"][^'\"]*['\"]\\s*(,\\s*['\"][^'\"]*['\"]\\s*)*)\\];";
        // 编译模式
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(encryptedPage);

        // 查找匹配项,匹配第二个数组
        // 查找匹配项
        int matchCount = 0;
        while (matcher.find()) {
            matchCount++;
            if (matchCount == 2) { // 匹配第二个
                String variableName = matcher.group(1); // 捕获变量名
                String arrayContent = matcher.group(2); // 捕获数组内容

                // 解析数组内容
                String[] elements = arrayContent.split(",\\s*");
                // 通过base64解密数组，获取加密方式
                String decryptionModes = "";
                List<String> decryptionModesList = new ArrayList<>();
                for (String element : elements) {
                    String decodedString = atob(element);
                    decryptionModesList.add(decodedString);
                    // 加密格式是 AES-XXX 我需要匹配AES开头,但是我需要 XXX 的数据
                    if (decodedString.startsWith("AES-")) {
                        decryptionModes = decodedString.substring(4);
                    }
                }

                // 匹配AES加密方式
                System.out.println(decryptionModes);
                return decryptionModes;
            }
        }
        return null;
    }


    // 提取 raw_key 数组
    public static int[] extractRawKey(String signPageCode) {
        Pattern pattern = Pattern.compile("var raw_key=\\[([0-9,\\s]+)\\];");
        Matcher matcher = pattern.matcher(signPageCode);
        if (matcher.find()) {
            String[] rawKeyString = matcher.group(1).split(",");
            int[] rawKey = new int[rawKeyString.length];
            for (int i = 0; i < rawKeyString.length; i++) {
                rawKey[i] = Integer.parseInt(rawKeyString[i].trim());
            }
            return rawKey;
        }
        return new int[0]; // 返回空数组如果没有找到
    }

    // 提取 encrypted 字符串
    public static String extractEncrypted(String signPageCode) {
        Pattern pattern = Pattern.compile("var encrypted=\"(.*?)\";");
        Matcher matcher = pattern.matcher(signPageCode);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return ""; // 如果没有找到，返回空字符串
    }

    // 提取 tag 和 iv 数组（Uint8Array）
    public static int[] extractArray(String signPageCode, String varName) {
        Pattern pattern = Pattern.compile("var " + varName + "=\\[([0-9,\\s]+)\\];");
        Matcher matcher = pattern.matcher(signPageCode);
        if (matcher.find()) {
            String[] byteArrayString = matcher.group(1).replace("[", "").replace("]", "").split(",");
            int[] rawKey = new int[byteArrayString.length];
            for (int i = 0; i < byteArrayString.length; i++) {
                rawKey[i] = Integer.parseInt(byteArrayString[i].trim());
            }
            return rawKey;
        }
        return new int[0]; // 返回空数组如果没有找到
    }

    // 将整数数组转换为有符号字节数组
    public static byte[] toSignedByteArray(int[] input) {
        byte[] result = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            result[i] = (byte) input[i]; // 自动处理超出范围的值
        }
        return result;
    }

    // 将十六进制字符串转换为字节数组
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


    // 使用 AES-CTR 解密数据
    public static String decryptAESCTR(SecretKey key, IvParameterSpec ivSpec, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] decryptedData = cipher.doFinal(data);

        // 输出解码后的数据
        String result = new String(decryptedData, "UTF-8");
        System.out.println("Decrypted data: " + result);
        return result;
    }


    // 使用 AES-CBC 解密数据
    public static byte[] decryptAESCBC(SecretKey key, IvParameterSpec ivSpec, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        return cipher.doFinal(data);
    }

    // 动态解密
    public static byte[] decrypt(String mode, byte[] key, byte[] iv, byte[] encryptedData) throws Exception {
        if ("GCM".equalsIgnoreCase(mode)) {
            return decryptGCM(key, iv, encryptedData);
        }

        // 其他模式处理 (如 CBC、ECB 等)
        String transformation = "AES/" + mode + "/PKCS5Padding";
        Cipher cipher = Cipher.getInstance(transformation);
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

        if ("ECB".equalsIgnoreCase(mode)) {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
        } else {
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        }
        return cipher.doFinal(encryptedData);
    }

    // 处理GCM模式
    private static byte[] decryptGCM(byte[] key, byte[] iv, byte[] encryptedData) throws Exception {
        // 分离密文和 Tag
        int tagLength = 16; // 认证标签的长度（字节）
        int ciphertextLength = encryptedData.length - tagLength;

        byte[] ciphertext = new byte[ciphertextLength];
        byte[] tag = new byte[tagLength];

        System.arraycopy(encryptedData, 0, ciphertext, 0, ciphertextLength);
        System.arraycopy(encryptedData, ciphertextLength, tag, 0, tagLength);

        // 配置 GCM 参数
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(tagLength * 8, iv);

        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

        // 解密密文
        return cipher.doFinal(ciphertext);
    }


    public static String atob(String base64String) {
        // 去掉 '
        base64String = base64String.replace("'", "");
        // 去除尾部的 '='
        base64String = base64String.replaceAll("=+$", "");
        StringBuilder decoded = new StringBuilder();

        int buffer = 0;
        int bitsCount = 0;

        for (char c : base64String.toCharArray()) {
            // 查找字符在 Base64 字符表中的索引
            int index = BASE64_CHARS.indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException("Invalid Base64 character: " + c);
            }

            // 将索引值存入 buffer
            buffer = (buffer << 6) | index;
            bitsCount += 6;

            // 当 buffer 中的位数达到或超过 8 位时，提取一个字节
            if (bitsCount >= 8) {
                bitsCount -= 8;
                int byteValue = (buffer >> bitsCount) & 0xFF; // 取高 8 位
                decoded.append((char) byteValue); // 转换为字符
            }
        }

        return decoded.toString();
    }


}
