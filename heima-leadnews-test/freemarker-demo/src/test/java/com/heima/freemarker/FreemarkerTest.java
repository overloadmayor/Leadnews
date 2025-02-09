package com.heima.freemarker;

import com.heima.freemarker.entity.Student;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@SpringBootTest(classes = FreemarkerDemoApplication.class)
//@SpringBootTest
@RunWith(SpringRunner.class)

public class FreemarkerTest {
    @Autowired
    private Configuration configuration;

    @Test
    public void test() throws IOException, TemplateException {
        Template template = configuration.getTemplate("02-list.ftl");

        /**
         * 合成方法:
         * 两个参数
         * 第一个参数:模型数据
         *  第二个参数：输出流
         */
        template.process(getData(),new FileWriter("D:/桌面/list.html"));
    }

    private Map getData()
    {
        Map<String,Object>map=new HashMap<>();

        Student stu1 = new Student();
        stu1.setName("小强");
        stu1.setAge(18);
        stu1.setMoney(1000.86f);
        stu1.setBirthday(new Date());

        //小红对象模型数据
        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMoney(200.1f);
        stu2.setAge(19);

        //将两个对象模型数据存放到List集合中
        List<Student> stus = new ArrayList<>();
        stus.add(stu1);
        stus.add(stu2);

        //向model中存放List集合数据
//        model.addAttribute("stus",stus);
        map.put("stus",stus);

        //map数据
        Map<String,Student> stumap = new HashMap<>();
        stumap.put("stu1",stu1);
        stumap.put("stu2",stu2);
//        model.addAttribute("stuMap",map);
        map.put("stuMap",stumap);
        //日期
//        model.addAttribute("today",new Date());
        map.put("today",new Date());
        //长数值
//        model.addAttribute("point",26658498499L);
        map.put("point",26658498499L);
        return map;
    }
}
