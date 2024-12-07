package io.github.zeroaicy.util;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class MD5Util{

    public static String stringMD5(String input){

        try{
			//拿到一个MD5转换器（如果想要SHA1参数换成”SHA1”）
            MessageDigest messageDigest =MessageDigest.getInstance("MD5");
			//输入的字符串转换成字节数组
            byte[] inputByteArray = input.getBytes();
			//inputByteArray是输入字符串转换得到的字节数组
            messageDigest.update(inputByteArray);
			//转换并返回结果，也是字节数组，包含16个元素
            byte[] resultByteArray = messageDigest.digest();
			//字符数组转换成字符串返回
            return byteArrayToHex(resultByteArray);
        }
		catch(NoSuchAlgorithmException e){
            return null;
        }
    }

	//将字节数组换成成16进制的字符串
    public static String byteArrayToHex(byte[] byteArray){

		//首先初始化一个字符数组，用来存放每个16进制字符
        char[] hexDigits = {'0','1','2','3','4','5','6','7','8','9', 'a','b','c','d','e','f' };

		//new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方））
        char[] resultCharArray =new char[byteArray.length * 2];

		//遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去
        int index = 0;

        for(byte b : byteArray){
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }

		//字符数组组合成字符串返回
        return new String(resultCharArray);
    }

    public static void main999(String[] args){
        Scanner input = new Scanner(System.in);
        while(true){
            System.out.println("请输入：");
            String in = input.nextLine();
            String value = MD5Util.stringMD5(in);
            System.out.println("加密前：" + in);
            System.out.println("加密后：" + value + "，长度：" + value.length());
        }
    }
}
