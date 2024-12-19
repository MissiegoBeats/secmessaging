package com.secmes.secmessaging;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.URLDecoder;

public class URLEncoderUtils {
    // Codifica la clave pública en URL Encoding
    public static String encodeToUrl(String publicKey) throws UnsupportedEncodingException {
        // Codifica la clave pública a URL encoding
        return URLEncoder.encode(publicKey, "UTF-8");
    }

    // Decodifica de vuelta a la clave pública original
    public static String decodeFromUrl(String encodedPublicKey) throws UnsupportedEncodingException {
        // Decodifica la clave pública codificada en URL
        return URLDecoder.decode(encodedPublicKey, "UTF-8");
    }
}
