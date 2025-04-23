package io.github.zeroaicy.util;
import android.content.Context;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FileUtil {

	private static final String LogDir = "/CrashLog";

	public static String CrashLogPath;
	public static String LogCatPath;

	static {
		init();
	}

	public static void init() {
		try {
			FileUtil.CrashLogPath = getCrashLogPath(FileUtil.LogDir);
			FileUtil.LogCatPath = getLogCatPath(FileUtil.CrashLogPath);
		} catch (Throwable e) {
		}
	}

	public static String getLogCatPath(String crashLogPath) {

		StringBuilder logCatPathBuilder = new StringBuilder();
		logCatPathBuilder.append(crashLogPath);

		logCatPathBuilder.append(File.separator);

		String logcatFileName = ContextUtil.getProcessName();

		if (logcatFileName.contains(":")) {
			logcatFileName = logcatFileName.replace(":", "--");
		}
		logCatPathBuilder.append(logcatFileName);
		logCatPathBuilder.append(".txt");

		return logCatPathBuilder.toString();
	}

	private static String getCrashLogPath(String CrashDir) {
		///内置储存器
		boolean isExternalCacheDir = false;

		Context context = ContextUtil.getContext();
		File logRootDirectory = context.getExternalCacheDir();
		//new File(Environment.getExternalStorageDirectory(), "Download");

		try {
			boolean isGranted = false;
			// XXPermissions.isGranted(context, android.Manifest.permission.MANAGE_EXTERNAL_STORAGE);

			if (!isGranted) {
				// 更改为: /内置储存器/Android/data/${PackageName}/cache
				logRootDirectory = context.getExternalCacheDir();
				isExternalCacheDir = true;
			}
		} catch (Throwable e) {
			logRootDirectory = context.getExternalCacheDir();
			isExternalCacheDir = true;
		}

		String crashDir = logRootDirectory.getAbsolutePath() + CrashDir;

		if (!isExternalCacheDir) {
			crashDir += "/";
			crashDir += ContextUtil.getPackageName();
		}
		return crashDir;
	}

	public static List<String> Files2Strings(List<File> files) {
		List<String> strings = new ArrayList<String>();
		for (File file : files) {
			strings.add(file.getAbsolutePath());
		}
		return strings;
	}

	public static ArrayList<File> findJavaFile(String filePath) {
		return findFile(new File(filePath), ".java");
	}
	public static ArrayList<File> findJavaFile(File file) {
		return findFile(file, ".java");
	}

	//基于栈
	public static ArrayList<File> findFile(File mFile, String suffix) {
		ArrayList<File> mFiles = new ArrayList<File>();
		if (mFile.isFile()) {
			String name = mFile.getName();
			if (suffix == null || (name.endsWith(suffix) && !name.startsWith("."))) {
				mFiles.add(mFile);
			}
		}
		if (mFile.isDirectory()) {
			Stack<File> list = new Stack<>();
			list.push(mFile);//进栈
			while (!list.isEmpty()) {
				File file = list.pop();//出栈
				File[] listFiles = file.listFiles();

				if (file != null && file.isDirectory() && listFiles != null) {
					for (File file_temp : listFiles) {

						if (file_temp.isDirectory()) {
							//过滤隐藏文件夹
							if (!file_temp.getName().startsWith(".")) {
								list.push(file_temp);//进栈
							}
						} else if (file_temp.isFile()) {
							if (suffix == null || (file_temp.getName().toLowerCase().endsWith(suffix))) {
								mFiles.add(file_temp);//进栈
							}
						}
					}
				}
			}
		}
		return mFiles;
	}

	public static void copy(final String source, final String target, final boolean isCover) {
		try {
			Path sourcePath = Paths.get(source);
			Stream<Path> walk = Files.walk(sourcePath);
			walk.forEach(new Consumer<Path>() {
				@Override
				public void accept(Path path) {
					try {
						Path path2 = Paths.get(path.toString().replace(source, target));
						if (Files.isDirectory(path) && !Files.exists(path2)) {
							Files.createDirectory(path2, new FileAttribute[0]);
						} else if (Files.isRegularFile(path)) {
							if (Files.exists(path2) && (!isCover || Files.getLastModifiedTime(path)
									.compareTo(Files.getLastModifiedTime(path2)) < 0)) {
								return;
							}
							Files.copy(path, path2, StandardCopyOption.REPLACE_EXISTING);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (IOException e) {
		}
	}
	public static byte[] readAllBytes(InputStream inputStream) throws IOException {
		return readAllBytes(inputStream, true);
	}

	public static byte[] readAllBytes(InputStream inputStream, boolean autoClose) throws IOException {
		byte[] data = new byte[4096];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int count;
		while ((count = inputStream.read(data)) > 0) {
			baos.write(data, 0, count);
		}
		byte[] readAllBytes = baos.toByteArray();
		baos.close();
		if (autoClose) {
			inputStream.close();
		}
		return readAllBytes;
	}

	public static void deleteFolder(String folder) {
		deleteFolder(new File(folder));
	}

	public static void deleteFolder(File file) {
		deleteFolder(file, true);
	}

	public static void deleteFolder(File folder, boolean deleteRootDir) {
		if (folder.isFile()) {
			folder.delete();
		}
		File[] childFiles = folder.listFiles();

		if (childFiles != null) {
			for (File file : childFiles) {
				if (file.isDirectory()) {
					deleteFolder(file); // 递归删除子文件夹  
				} else {
					file.delete(); // 删除文件  
				}
			}
		}

		if (deleteRootDir) {
			// 删除空文件夹  
			folder.delete();
		}
	}

	public static void copyNotCover(String source, String target) {
		copy(source, target, false);
	}
}

