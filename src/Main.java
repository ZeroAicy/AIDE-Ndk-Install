
import android.content.Context;
import android.os.Build;
import android.system.ErrnoException;
import io.github.zeroaicy.util.ContextUtil;
import io.github.zeroaicy.util.FileUtil;
import io.github.zeroaicy.util.IOUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.io.FileInputStream;
import android.text.TextUtils;

public class Main {

	public static File getAideNdkInstallDir(Context context) {
		return new File(getNoBackupFilesDir(context), getNdkSupportVersionFileName());
	}

	public static final File getNoBackupFilesDir(Context context) {
		if (Build.VERSION.SDK_INT >= 21) {
			return context.getNoBackupFilesDir();
		}
		return createDir(new File(context.getApplicationInfo().dataDir, "no_backup"));
	}

	private static synchronized File createDir(File file) {
		if (file.exists() || file.mkdirs()) {
			return file;
		}
		if (file.exists()) {
			return file;
		}
		return null;
	}

	public static String getNdkSupportVersionFileName() {
		return "ndksupport-" + getNdkVersion();
	}

	private static int getNdkVersion() {
		int ndkVersion = Build.CPU_ABI.contains("x86") ? 0x65f03102 : 0x65f03101;
		return Build.VERSION.SDK_INT >= 26 ? ndkVersion + 2 : ndkVersion;
	}

	public static final String busyboxResourceName = "/data/busybox";
	public static final String ndkInstallShellResourceName = "/data/ndk-install.sh";

	// Ndk 版本名称 例如 r24 r27b
	// $NDK_VERSION_NAME环境变量
	static String NDK_VERSION_NAME; // 勿动
	// Ndk 版本代码 例如 24.0.8215888 27.1.12297006
	// $NDK_VERSION_CODE环境变量
	static String NDK_VERSION_CODE = "27.1.12297006"; // 勿动

	// 
	// Ndk包路径 需要特定的Ndk.zip包
	// Zip 根目录为 android-ndk-xxx 例如 android-ndk-r24 android-ndk-r27b
	// 唯一主要修改的
	static String NdkZipFilePath = "/storage/emulated/0/.MyAicy/源码备份/AIDE+/AIDE+Ndk/android-sdk/ndk/android-ndk-r27b-aarch64.zip";

	public static void main(String[] args) throws Exception {
		// 如果安装多个 Ndk，在此处赋值
		// NdkZipFilePath = "/storage/emulated/0/.MyAicy/源码备份/AIDE+/NDK-R24/android-ndk-r24-aarch64.zip";

		File ndkZipFile = new File(NdkZipFilePath);
		if (!ndkZipFile.exists()) {
			System.out.println("NdkZip文件不存,在请更改NdkZipFilePath变量");
			return;
		}

		Context context = ContextUtil.getContext();

		File filesDir = context.getFilesDir();

		// busybox 安装目录
		final File busyboxInstallDir;
		// $HOME变量
		final File homeDir;

		// 兼容AIDE Pro
		if ("aidepro.top".equals(context.getPackageName())) {
			homeDir = new File(filesDir, "framework");
			busyboxInstallDir = new File(filesDir, "usr/bin/applets");
		} else {
			initProotEnv(context);
			homeDir = new File(filesDir, "home");
			// 防止覆盖 Termux版的 usr/bin目录
			busyboxInstallDir = new File(filesDir + "/usrx/bin");
		}

		if (homeDir.isFile()) {
			homeDir.delete();
		}
		if (!homeDir.exists()) {
			homeDir.mkdirs();
		}
		// busybox 安装路径

		if (busyboxInstallDir.isFile()) {
			busyboxInstallDir.delete();
		} else {
			// 删除 busyboxInstallDir目录
			FileUtil.deleteFolder(busyboxInstallDir);
		}
		
		// 创建文件夹
		if (!busyboxInstallDir.exists()) {
			busyboxInstallDir.mkdirs();
		}

		String homeDirPath = homeDir.getAbsolutePath();

		// 计算 NDK_VERSION_CODE 与 NDK_VERSION_NAME
		System.out.println("解析Ndk版本信息...");
		ZipFile zipFile = new ZipFile(ndkZipFile);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry zipEntry = entries.nextElement();
			String name = zipEntry.getName();
			int rootZipEntryNameEnd = name.indexOf('/');

			if (rootZipEntryNameEnd > 0) {
				name = name.substring(0, rootZipEntryNameEnd);
			}
			if (name.startsWith("android-ndk-") && !name.contains("/")) {
				Main.NDK_VERSION_NAME = name.substring("android-ndk-".length());
				System.out.printf("NDK_VERSION_NAME = %s\n", NDK_VERSION_NAME);
				break;
			}
			System.out.println(name);
		}
		ZipEntry sourceProperties = zipFile.getEntry("android-ndk-" + Main.NDK_VERSION_NAME + "/source.properties");
		if (sourceProperties != null) {
			for (String line : IOUtils.readLines(zipFile.getInputStream(sourceProperties))) {
				if (line.startsWith("Pkg.Revision = ")) {
					NDK_VERSION_CODE = line.substring("Pkg.Revision = ".length());
					System.out.printf("NDK_VERSION_CODE = %s\n", Main.NDK_VERSION_CODE);
					break;
				}
			}
		}
		zipFile.close();

		if (TextUtils.isEmpty(Main.NDK_VERSION_NAME)) {
			System.out.println("未能解析出Ndk版本信息 -> Main.NDK_VERSION_NAME");
			System.out.println("请使用符合的 Ndk包");
			return;
		}

		// 解压busybox
		System.out.println("写入并安装busybox...");
		writeBusybox(busyboxInstallDir);

		// 安装脚本路径
		System.out.println("写入ndk-install.sh...");
		File ndkInstallFile = new File(homeDir, "ndk-install.sh");
		writeNdkInstallFile(ndkInstallFile);

		// 写入cmake
		System.out.println("写入cmake...");
		writeResource("android-sdk/cmake/cmake-3.10.2-android-aarch64.zip",
					  new File(homeDirPath, "android-sdk/cmake/cmake-3.10.2-android-aarch64.zip"));
		writeResource("android-sdk/cmake/cmake-3.18.1-android-aarch64.zip",
					  new File(homeDirPath, "android-sdk/cmake/cmake-3.18.1-android-aarch64.zip"));
		writeResource("android-sdk/cmake/cmake-3.22.1-android-aarch64.zip",
					  new File(homeDirPath, "android-sdk/cmake/cmake-3.22.1-android-aarch64.zip"));
		writeResource("android-sdk/cmake/cmake-3.25.1-android-aarch64.zip",
					  new File(homeDirPath, "android-sdk/cmake/cmake-3.25.1-android-aarch64.zip"));

		// 复制Ndk
		System.out.println("复制Ndk到安装缓存区...");
		FileInputStream ndkZipFileInputStream = new FileInputStream(ndkZipFile);
		FileOutputStream ndkZipFileOutputStream = new FileOutputStream(
			new File(homeDir, String.format("android-ndk-%s-aarch64.zip", NDK_VERSION_NAME)));
		IOUtils.streamTransfer(ndkZipFileInputStream, ndkZipFileOutputStream, true);

		// 运行脚本 
		runNdkInstallShellFile(homeDirPath, busyboxInstallDir, ndkInstallFile);

		// 添加运行权限

		System.out.println("添加执行权限...");
		Runtime.getRuntime().exec("chmod -R 777 " + homeDirPath);

		// link_aide_ndk
		// 使用 ln 链接 Ndk 可以复用gradle ndk

		try {

			// AIDE Ndk安装路径
			File aideNdkInstallDir = getAideNdkInstallDir(context);
			aideNdkInstallDir.mkdirs();

			File installedFile = new File(aideNdkInstallDir, ".installed");
			// 重置AIDE NDK 安装完成标志
			installedFile.delete();

			System.out.println("以软链接方式[类似Windows的快捷方式] 安装AIDE NDK");

			String busyboxInstallDirPath = busyboxInstallDir.getAbsolutePath();

			File binDir = new File(aideNdkInstallDir, "bin");
			if (binDir.exists()) {
				// 软链接 不能递归删除
				if (Files.isSymbolicLink(binDir.toPath())) {
					binDir.delete();
				} else {
					// 因为是目录所以没删掉😓
					FileUtil.deleteFolder(binDir);
				}
			}

			String binDirPath = binDir.getAbsolutePath();
			// symlink时必须保证 newPath不存在 
			android.system.Os.symlink(busyboxInstallDirPath, binDirPath);

			File android_ndk_aide_dir = new File(aideNdkInstallDir, "android-ndk-aide");
			if (binDir.exists()) {
				// android_ndk_aide_dir也是是目录
				// 软链接 不能递归删除
				if (Files.isSymbolicLink(android_ndk_aide_dir.toPath())) {
					android_ndk_aide_dir.delete();
				} else {
					FileUtil.deleteFolder(android_ndk_aide_dir);
				}
			}
			android.system.Os.symlink(homeDirPath + "/android-sdk/ndk/" + NDK_VERSION_CODE,
									  android_ndk_aide_dir.getAbsolutePath());
			installedFile.createNewFile();
		} catch (ErrnoException e) {
			System.out.println("链接失败");
			e.printStackTrace();

			return;
		}

		System.out.println("安装结束");
	}

	private static void runNdkInstallShellFile(String homeDirPath, File busyboxInstallDir, File ndkInstallFile) {
		ProcessBuilder processBuilder = new ProcessBuilder();
		Map<String, String> env = processBuilder.environment();

		env.put("HOME", homeDirPath);
		env.put("PATH", busyboxInstallDir.getAbsolutePath() + ":" + env.get("PATH"));
		env.put("NDK_VERSION_NAME", Main.NDK_VERSION_NAME);
		env.put("NDK_VERSION_CODE", Main.NDK_VERSION_CODE);
		// proot模式
		putCustomizeEnv(env);

		List<String> argsList = new ArrayList<>();
		argsList.add(ndkInstallFile.getAbsolutePath());

		// proot模式
		argsList = setupShellCommandArguments(argsList);


		processBuilder.command(argsList);
		processBuilder.redirectErrorStream(true);

		try {
			// 启动子进程  
			Process process = processBuilder.start();

			// 读取合并后的输出流  
			try {
				InputStream inputStream = process.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				String line;
				while ((line = reader.readLine()) != null) {
					// 处理每一行输出  
					System.out.println(line);
				}

				// 等待子进程结束  
				int exitCode = process.waitFor();
				System.out.println("Exited with code: " + exitCode);

				reader.close();
				inputStream.close();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			// 处理异常  
			e.printStackTrace();
		}
	}

	private static void writeNdkInstallFile(File ndkInstallFile) throws IOException {
		writeResource(Main.ndkInstallShellResourceName, ndkInstallFile);
		ndkInstallFile.setExecutable(true);
	}

	private static void writeBusybox(File busyboxInstallDir) throws IOException {
		FileUtil.deleteFolder(busyboxInstallDir);
		busyboxInstallDir.mkdirs();

		File busyboxFile = new File(busyboxInstallDir, "busybox");
		writeResource(Main.busyboxResourceName, busyboxFile);

		busyboxFile.setExecutable(true);

		installBusybox(busyboxInstallDir, busyboxFile);
	}

	private static void installBusybox(File busyboxInstallDir, File busyboxFile) throws IOException {
		
		if( !busyboxInstallDir.exists() ){
			busyboxInstallDir.mkdirs();
		}
		
		List<String> args = new ArrayList<>();

		args.add(busyboxFile.getAbsolutePath());

		args.add("--install");
		args.add("-s");
		args.add(".");
		
		ProcessBuilder processBuilder = new ProcessBuilder();

		// proot模式
		putCustomizeEnv(processBuilder.environment());
		
		args = setupShellCommandArguments(args);

		processBuilder.directory(busyboxInstallDir).command(args);

		processBuilder.redirectError(new File(busyboxInstallDir, "installLog.txt"));
		processBuilder.start();
	}

	public static void writeResource(String resourceName, File outputFile) throws IOException {
		outputFile.setReadable(true, false);
		outputFile.setWritable(true, false);

		File parentFile = outputFile.getParentFile();
		if (!parentFile.exists()) {
			parentFile.mkdirs();
		}
		if (outputFile.exists()) {
			outputFile.delete();
		}

		FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
		InputStream resourceAsStream = Main.class.getResourceAsStream(resourceName);
		IOUtils.streamTransfer(resourceAsStream, fileOutputStream, true);

		outputFile.setReadable(true, false);
		outputFile.setWritable(true, false);
		outputFile.setExecutable(true, false);

	}

	/**
	 * 解压zip
	 */
	public static void dealzip(File zipSourcePath, File targetFile) {
		//判断目标地址是否存在，如果没有就创建
		String targetPath = targetFile.getAbsolutePath();
		File pathFile = new File(targetPath);
		if (!pathFile.exists()) {
			pathFile.mkdirs();
		}
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(zipSourcePath, Charset.forName("UTF-8"));
			//若zip中含有中文名文件,换GBK
			//zip = new ZipFile(zipPath, Charset.forName("GBK"));
			//遍历里面的文件及文件夹
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory()) {
					//不解压目录
					continue;
				}
				String zipEntryName = entry.getName();
				//解压文件
				InputStream in = zipFile.getInputStream(entry);
				//也就是把这个文件加入到目标文件夹路径;
				File outFile = new File(targetFile, zipEntryName.replace('/', File.separatorChar));

				//不存在则创建文件路径
				File outParentFile = outFile.getParentFile();
				if (!outParentFile.exists()) {
					outParentFile.mkdirs();
				}
				//文件夹就不解压;
				if (outFile.isDirectory()) {
					//文件已存在，没有文件则删除文件夹
					String[] list = outFile.list();
					if (list != null && list.length > 0) {
						continue;
					}
					outFile.delete();
				}

				OutputStream out = new FileOutputStream(outFile);
				byte[] bf = new byte[2048];
				int len;
				while ((len = in.read(bf)) > 0) {
					out.write(bf, 0, len);
				}
				in.close();
				out.close();
			}
			zipFile.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	//  proot 模式
	static boolean PROOT_MODE;
	//proot路径
	public static String PROOT_PATH;
	///data/data/包名 路径
	public static String PACKAGE_NAME_PATH;
	// /linkerconfig/ld.config.txt路径
	public static String PROOT_TMP_DIR;

	private static void initProotEnv(Context currentPackageContext) {

		if (Main.PROOT_PATH != null) {
			return;
		}

		Main.PROOT_MODE = currentPackageContext.getApplicationInfo().targetSdkVersion > 28;

		if (Main.PROOT_PATH == null) {
			Main.PROOT_PATH = currentPackageContext.getApplicationInfo().nativeLibraryDir + "/libproot.so";
		}

		if (Main.PACKAGE_NAME_PATH == null) {
			Main.PACKAGE_NAME_PATH = currentPackageContext.getDataDir().getAbsolutePath();
		}

		Main.PROOT_TMP_DIR = new File(Main.PROOT_PATH).getParent();

		File cacheDirFile = new File(PACKAGE_NAME_PATH, "cache");
		if (!cacheDirFile.exists()) {
			cacheDirFile.mkdir();
		}
		File ld_config_txt_file = new File(cacheDirFile, "ld.config.txt");
		if (!ld_config_txt_file.exists() || ld_config_txt_file.length() == 0) {
			try {

				String inputPath = "/linkerconfig/ld.config.txt";

				InputStream input = new FileInputStream(inputPath);
				OutputStream output = new FileOutputStream(ld_config_txt_file);

				IOUtils.streamTransfer(input, output, true);

				ld_config_txt_file.setReadable(true, false);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	public static List<String> setupShellCommandArguments(List<String> arguments) {

		if (!Main.PROOT_MODE) {
			return arguments;
		}

		List<String> result = new ArrayList<>();

		String PACKAGE_NAME_PATH = Main.PACKAGE_NAME_PATH;
		//以proot方式启动
		result.add(Main.PROOT_PATH);

		result.add("--rootfs=/");
		// result.add("--bind=" + PACKAGE_NAME_PATH + ":/data/data/com.termux");
		// result.add("--bind=" + PACKAGE_NAME_PATH + ":/data/user/0/com.termux");

		result.add("--bind=" + PACKAGE_NAME_PATH + "/cache" + ":/linkerconfig");

		result.addAll(arguments);

		return result;
	}

	private static void putCustomizeEnv(Map<String, String> environment) {
		//为proot添加缓存路径 PROOT_TMP_DIR
		environment.put("PROOT_TMP_DIR", PROOT_TMP_DIR);
	}
}

