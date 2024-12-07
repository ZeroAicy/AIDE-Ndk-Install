package io.github.zeroaicy.util;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class OpenFile{
	private File mFile;
	private OpenFile(String filePath){
		this(new File(filePath));
	}
	public OpenFile(File mFile){
		this.mFile = mFile;
	}
	public static OpenFile open(String filePath){
		return new OpenFile(filePath);
	}
	public static OpenFile open(File file){
		return new OpenFile(file);
	}

	public List<String> list(){
		List<String> list = new ArrayList<String>();
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(mFile)));
			String line;
			while((line = br.readLine()) != null){
				list.add(line);
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		return list;
	}
	public void write(List<String> list){
		File parentFile = mFile.getParentFile();
		if ( !parentFile.exists() ){
			parentFile.mkdirs();
		}
		try{
			PrintWriter printWriter = new PrintWriter(mFile);
			int size = list.size();
			for(int i = 0; i < size; i++){
				printWriter.println(list.get(i));
			}
			printWriter.flush();
			printWriter.close();
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}
	}

	public OpenFile getOutOpenFile(String input, String out){
		return open(mFile.getAbsolutePath().replace(input, out));
	}
}

