package com.heima.tess4j;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;

public class Application {
    //识别图片中的文字
    public static void main(String[] args) throws TesseractException {
        //创建实例
        ITesseract tesseract = new Tesseract();
        //设置字体库路径
        tesseract.setDatapath("D:/HeiMaNews/tessdata/");
        //设置语言 --> 简体中文
        tesseract.setLanguage("chi_sim");

        File file = new File("C:/Users/86134/Pictures/Snipaste_2024-10-15_22-16-23.png");
        //识别图片
        String result = tesseract.doOCR(file);
        System.out.println("识别的结果："+result);
    }
}
