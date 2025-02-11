package classLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

public class Class2Bytes {
    public  byte[] class2bytes(File classFile) {
        try {FileInputStream fis = new FileInputStream(classFile);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int bytesRead = 0;
            byte[] bytes = new byte[4096];
            while ((bytesRead = fis.read(bytes, 0, 4096)) != -1) {
                baos.write(bytes, 0, bytesRead);
            }
            byte[] classBytes = baos.toByteArray();
            return classBytes;
        } catch (Exception e) {
            System.out.println("转换出错: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
