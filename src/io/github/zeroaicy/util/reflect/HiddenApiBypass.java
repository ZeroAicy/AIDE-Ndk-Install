



package io.github.zeroaicy.util.reflect;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import io.github.zeroaicy.util.Log;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import android.app.Application;

public class HiddenApiBypass {

	private static Object sVmRuntime;
	private static Method setHiddenApiExemptionsMethod;

	static  {
		try {
			if (!initializedFromEirv()) {
				boolean initializedFromZeroAicy = initializedFromZeroAicy();
				Log.d("HiddenApiBypass initializedFromZeroAicy", initializedFromZeroAicy);
			}
			else {
				Log.d("HiddenApiBypass initializedFromEirv OK");

			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private static boolean initializedFromZeroAicy() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			try {
				Method forName = Class.class.getDeclaredMethod("forName", String.class);
				Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);

				Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
				Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
				HiddenApiBypass.setHiddenApiExemptionsMethod = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});
				HiddenApiBypass.sVmRuntime = getRuntime.invoke(null);
				return false;
			} catch (final Throwable e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	/*
	 * @author eirv
	 */
	private static final boolean initializedFromEirv() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            try {
				UnsafeX unsafe = UnsafeX.getUnsafe();

				//assert unsafe.unsafe != null;

                Method[] stubs = Compiler.class.getDeclaredMethods();

				Method first = stubs[0];
                Method second = stubs[1];
                long size = unsafe.getLong(second, 24) - unsafe.getLong(first, 24);

                int addrSize = unsafe.addressSize();

                long methods = unsafe.getLong(Class.forName("dalvik.system.VMRuntime"), 48);

				long count = addrSize == 8 ? unsafe.getLong(methods) : unsafe.getInt(methods);

				methods += addrSize;

				for (long j = 0, done = 0; count > j; j++) {
                    long method = j * size + methods;
                    unsafe.putLong(first, 24, method);
                    String name = first.getName();

                    if (!"getRuntime".equals(name) && !"setHiddenApiExemptions".equals(name)) continue;
                    // 我下面这几行是把方法移出隐藏 api 列表
                    // first 方法可以直接调用的

                    long addr = method + 4;
                    int acc = unsafe.getInt(addr);

                    unsafe.putInt(addr, acc | 0x10000000);

                    if (++done == 2) break;
                }


				Class<?> vmRuntimeClass = Class.forName("dalvik.system.VMRuntime");

				Method getRuntime = vmRuntimeClass.getDeclaredMethod("getRuntime");

				HiddenApiBypass.setHiddenApiExemptionsMethod = vmRuntimeClass.getDeclaredMethod("setHiddenApiExemptions", String[].class);
				HiddenApiBypass.sVmRuntime = getRuntime.invoke(null);

            } catch (final Throwable e) {
				Log.d(TAG, "initializedFromEirv", e);            }
        }
		return true;
    }
	public static boolean setHiddenApiExemptions(String... signaturePrefixes) {

		//Log.d(TAG, "setHiddenApiExemptions被调用路径", new Throwable());

		if (HiddenApiBypass.sVmRuntime == null || HiddenApiBypass.setHiddenApiExemptionsMethod == null) {
			return false;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			try {
				HiddenApiBypass.setHiddenApiExemptionsMethod.invoke(HiddenApiBypass.sVmRuntime, new Object[]{signaturePrefixes});
				return true;
			} catch (final Throwable e) {
				Log.d(TAG, "setHiddenApiExemptions", e);
				return false;
			}
		}
		return true;
	}

	private static final String TAG = "HiddenApiBypass";

	private static final Set<String> signaturePrefixes = new HashSet<>();
	public static boolean addHiddenApiExemptions(String... signaturePrefixes) {
        HiddenApiBypass.signaturePrefixes.addAll(Arrays.asList(signaturePrefixes));
        String[] strings = new String[HiddenApiBypass.signaturePrefixes.size()];
        HiddenApiBypass.signaturePrefixes.toArray(strings);

        return setHiddenApiExemptions(strings);
    }
	public static boolean clearHiddenApiExemptions() {
        HiddenApiBypass.signaturePrefixes.clear();
        return setHiddenApiExemptions();
    }
}
