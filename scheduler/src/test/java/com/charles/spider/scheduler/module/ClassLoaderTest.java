package com.charles.spider.scheduler.module;

import com.charles.spider.scheduler.config.Config;
import org.junit.Test;
import org.xeustechnologies.jcl.JarClassLoader;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClassLoaderTest {

    @Test
    public void test() throws MalformedURLException, ClassNotFoundException, FileNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Path path =Paths.get(Config.INIT_DATA_PATH,"common/custom-module-test-1.0-SNAPSHOT.jar");

        SpiderClassLoader loader = new SpiderClassLoader(this.getClass().getClassLoader());

        loader.addJar(path);


        Class cls = loader.loadClass("com.charles.spider.scheduler.module.GroovyModuleAgent");
        Class common = loader.loadClass("com.sct.test.Utils");


        System.out.println(cls.getClassLoader());
        System.out.println(common.getClassLoader());

        Method method = common.getMethod("test");

        method.invoke(null);
        int a =0;

        //loader.
        //System.out.println(System.getProperty("java.class.path"));




    }
}