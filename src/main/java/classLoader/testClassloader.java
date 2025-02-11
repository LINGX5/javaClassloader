package classLoader;

import java.lang.reflect.Method;

public class testClassloader extends ClassLoader{
    // 定义类名称
    public static String CLASS_NAME = "HelloWorld";
    // 定义类字节码
    public static byte[] CLASS_BYTES = new byte[]{-54, -2, -70, -66, 0, 0, 0, 61, 0, 17, 10, 0, 2,
            0, 3, 7, 0, 4, 12, 0, 5, 0, 6, 1, 0, 16, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47,
            79, 98, 106, 101, 99, 116, 1, 0, 6, 60, 105, 110, 105, 116, 62, 1, 0, 3, 40, 41, 86,
            8, 0, 8, 1, 0, 13, 72, 101, 108, 108, 111, 44, 32, 87, 111, 114, 108, 100, 33, 7, 0,
            10, 1, 0, 10, 72, 101, 108, 108, 111, 87, 111, 114, 108, 100, 1, 0, 4, 67, 111, 100,
            101, 1, 0, 15, 76, 105, 110, 101, 78, 117, 109, 98, 101, 114, 84, 97, 98, 108, 101, 1,
            0, 10, 104, 101, 108, 108, 111, 119, 111, 114, 108, 100, 1, 0, 20, 40, 41, 76, 106, 97,
            118, 97, 47, 108, 97, 110, 103, 47, 83, 116, 114, 105, 110, 103, 59, 1, 0, 10, 83, 111,
            117, 114, 99, 101, 70, 105, 108, 101, 1, 0, 15, 72, 101, 108, 108, 111, 87, 111, 114,
            108, 100, 46, 106, 97, 118, 97, 0, 33, 0, 9, 0, 2, 0, 0, 0, 0, 0, 2, 0, 1, 0, 5, 0, 6,
            0, 1, 0, 11, 0, 0, 0, 29, 0, 1, 0, 1, 0, 0, 0, 5, 42, -73, 0, 1, -79, 0, 0, 0, 1, 0, 12,
            0, 0, 0, 6, 0, 1, 0, 0, 0, 1, 0, 1, 0, 13, 0, 14, 0, 1, 0, 11, 0, 0, 0, 27, 0, 1, 0, 1,
            0, 0, 0, 3, 18, 7, -80, 0, 0, 0, 1, 0, 12, 0, 0, 0, 6, 0, 1, 0, 0, 0, 3, 0, 1, 0, 15, 0,
            0, 0, 2, 0, 16};
    // 重写findClass方法
    public Class<?> findClass(String name) throws ClassNotFoundException {
        // 如果类名称匹配
        if (CLASS_NAME.equals(name)) {
            // 返回类字节码
            return defineClass(name, CLASS_BYTES, 0, CLASS_BYTES.length);
        }
        // 否则调用父类方法
        return super.findClass(name);
    }

    public static void main(String[] args) {
        // 创建自定义类加载器
        testClassloader loader = new testClassloader();
        try{
            // 使用加载器加载HelloWorld类
            Class<?> aClass = loader.loadClass(CLASS_NAME);
            // 利用类反射创建HelloWorld对象
            Object instance = aClass.getDeclaredConstructor().newInstance();
            // 反射获取helloworld方法
            Method method = instance.getClass().getMethod("helloworld");
            // 反射调用helloworld方法
            String str = (String)method.invoke(instance);
            System.out.println(str);

        }catch (Exception e){
            System.out.println(e);
        }
    }

}