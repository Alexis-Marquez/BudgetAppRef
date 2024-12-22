package budgetapprefactored.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import budgetapprefactored.config.EncryptionKeyProvider;
import org.springframework.stereotype.Service;

@Service
public class EncryptionUtil {
    private static final String ALGORITHM = "AES";

    private static final EncryptionKeyProvider encryptionKeyProvider = new EncryptionKeyProvider();
    private static final SecretKey secretKey = encryptionKeyProvider.getSecretKey();

    public static String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    public static String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        return new String(cipher.doFinal(decodedData));
    }
}
