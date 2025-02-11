package BCEL;

import classLoader.Class2Bytes;
import org.apache.bcel.classfile.Utility;
import org.apache.bcel.util.ClassLoader;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class testBCELClassLoader {
    /**
     * 将class文件转成字节数组，并使用BCEL进行编码
     * @param classFile
     * @return
     * @throws IOException
     */
    public String BCELencode(File classFile) throws Exception {
        Class2Bytes class2Bytes = new Class2Bytes();
        byte[] bytes = class2Bytes.class2bytes(classFile);
//        System.out.println(Arrays.toString(bytes));
        String ClassName = "$$BCEL$$" +  Utility.encode(bytes, true);
        return ClassName;
    }
    /**
     * 使用BCEL类加载器加载恶意类
     * @param ClassName
     * @throws Exception
     */
    public void testClassLoader(String ClassName) throws Exception {
        // 创建BCEL类加载器
        ClassLoader BCELClassLoader = new ClassLoader();
        // 加载获得类实例，同时执行恶意代码 evilClass  1,2,3三种反射实例化类的方法都是可行的
        // 1
        /*Class.forName(ClassName, true, BCELClassLoader);*/
        // 2
        /*Class<?> clazz = BCELClassLoader.loadClass(ClassName);
        clazz.getDeclaredConstructor().newInstance();*/
        // 3
        BCELClassLoader.loadClass(ClassName).newInstance();
    }
    public static void main(String[] args) throws Exception {

        testBCELClassLoader testBCELClassLoader = new testBCELClassLoader();
        String ClassName = testBCELClassLoader.BCELencode(new File("src/main/java/BCEL/evilClass.class"));
        System.out.println(ClassName);
        testBCELClassLoader.testClassLoader(ClassName);
    }
}
