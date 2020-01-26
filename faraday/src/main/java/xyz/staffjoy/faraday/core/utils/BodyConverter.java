package xyz.staffjoy.faraday.core.utils;

import static java.nio.charset.Charset.forName;

/*
 *
 * String与Byte[]转换
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:48 2019/12/29
 *
 */
public class BodyConverter {
    public static String convertBodyToString(byte[] body) {
        if (body == null) {
            return null;
        }
        return new String(body, forName("UTF-8"));
    }

    public static byte[] convertStringToBody(String body) {
        if (body == null) {
            return null;
        }
        return body.getBytes(forName("UTF-8"));
    }
}
