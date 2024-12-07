package io.github.zeroaicy.util.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class UnsafeX{
	private static Class<?> UnsafeClass;

	private static Method getUnsafe;

	private static Method addressSize;

	private static Method getInt;
	private static Method getInt1;

	private static Method getLong;
	private static Method getLong1;


	private static Method putInt;
	private static Method putInt1;

	private static Method putLong;
	private static Method putObject;


	 static{
		try{
			
			UnsafeClass = Class.forName("sun.misc.Unsafe");
			getUnsafe = UnsafeClass.getDeclaredMethod("getUnsafe");

			addressSize = UnsafeClass.getDeclaredMethod("addressSize");

			getInt = UnsafeClass.getDeclaredMethod("getInt", long.class);
			getInt1 = UnsafeClass.getDeclaredMethod("getInt", Object.class, long.class);

			getLong = UnsafeClass.getDeclaredMethod("getLong", long.class);
			getLong1 = UnsafeClass.getDeclaredMethod("getLong", Object.class, long.class);

			putInt = UnsafeClass.getDeclaredMethod("putInt", long.class, int.class);
			putInt1 = UnsafeClass.getDeclaredMethod("putInt", Object.class, long.class, int.class);

			putLong = UnsafeClass.getDeclaredMethod("putLong", Object.class, long.class, long.class);
			putObject = UnsafeClass.getDeclaredMethod("putObject", Object.class, long.class, Object.class);

		}
		catch (Throwable e){

		}
	}

	Object unsafe;
	private UnsafeX(Object unsafe){
		this.unsafe = unsafe;
	}

	public int addressSize() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		return (int)addressSize.invoke(this.unsafe);

	}

	public int getInt(long address) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		return (int)getInt.invoke(this.unsafe, address);
	}

	public int getInt(Object obj, long offset) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		return (int)getInt1.invoke(this.unsafe, obj, offset);
	}

	public long getLong(long address) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		return (int)getLong.invoke(this.unsafe, address);
	}

	public long getLong(Object obj, long offset) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		return (int)getLong1.invoke(this.unsafe, obj, offset);
	}

	public static UnsafeX getUnsafe() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		return new UnsafeX(getUnsafe.invoke(null));
	}

	public void putInt(long address, int x) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		putInt.invoke(this.unsafe, address, x);
	}

	public void putInt(Object obj, long offset, int newValue) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		putInt1.invoke(this.unsafe, obj, offset, newValue);
	}

	public void putLong(Object obj, long offset, long newValue) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		putLong.invoke(this.unsafe, obj, offset, newValue);
	}

	public void putObject(Object obj, long offset, Object newValue) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		putObject.invoke(this.unsafe, obj, offset, newValue);
	}



}
