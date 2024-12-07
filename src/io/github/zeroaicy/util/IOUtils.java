package io.github.zeroaicy.util;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class IOUtils {

	private static final String TAG = "IOUtils";


	public static void streamTransfer( InputStream input, OutputStream output ) throws IOException {
		streamTransfer(input, output, false);
	}

	public static void streamTransfer( InputStream input, OutputStream output, boolean autoClose ) throws IOException {
		try {
			byte[] data = new byte[4096];
			int read;
			while ( ( read = input.read(data) ) > 0 ) {
				output.write(data, 0, read);
			}
		}
		finally {
			if( autoClose ){
				close(input);
				close(output);
			}
		}
	}

	public static byte[] readAllBytes( InputStream inputStream ) throws IOException {
		return readAllBytes(inputStream, true);
	}

	public static byte[] readAllBytes( InputStream inputStream, boolean autoClose ) throws IOException {
		byte[] data = new byte[4096];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int count;
		while ( ( count = inputStream.read(data) ) > 0 ) {
			baos.write(data, 0, count);
		}
		byte[] readAllBytes = baos.toByteArray();
		baos.close();
		if ( autoClose ) {
			inputStream.close();
		}
		return readAllBytes;
	}

	public static void close( AutoCloseable autoCloseable ) {
		try {
			if ( autoCloseable != null ) autoCloseable.close();
		}
		catch (Throwable e) {}
	}

	public static List<String> readLines( InputStream input ) {
		ArrayList<String> lines = new ArrayList<>();
		readLines(new InputStreamReader(input), lines, true);
		return lines;
	}
	public static void readLines( InputStream input, Collection<String> lines ) {
		readLines(new InputStreamReader(input), lines, true);
	}

	public static void readLines( InputStream input, Collection<String> lines, boolean autoClose ) {
		readLines(new InputStreamReader(input), lines, autoClose);
	}

	public static void readLines( Reader reader, Collection<String> lines ) {
		readLines(new BufferedReader(reader), lines, true);
	}

	public static void readLines( Reader reader, Collection<String> lines, boolean autoClose ) {
		readLines(new BufferedReader(reader), lines, autoClose);
	}

	public static void readLines( BufferedReader bufferedReader, Collection<String> lines, boolean autoClose ) {
		try {
			String readLine;
			while ( ( readLine = bufferedReader.readLine() ) != null ) {
				lines.add(readLine);
			}
		}
		catch (Exception e) {}

		finally {
			if ( autoClose ) {
				IOUtils.close(bufferedReader);
			}
		}
	}

	public static void writeLines( Collection<String> lines, String outputPath ) {
		try {
			writeLines(lines, new FileOutputStream(outputPath), true);
		}
		catch (FileNotFoundException e) {
			Log.w(TAG, String.format("outputPath: %s 没有发现", outputPath));
		}
	}
	public static void writeLines( Collection<String> lines, File outputFile ) {
		try {
			writeLines(lines, new FileOutputStream(outputFile), true);
		}
		catch (FileNotFoundException e) {
			Log.w(TAG, String.format("outputFile: %s 没有发现", outputFile.getAbsolutePath()));
		}
	}
	public static void writeLines( Collection<String> lines, OutputStream output ) {
		writeLines(lines, output, true);
	}

	public static void writeLines( Collection<String> lines, OutputStream output, boolean autoClose ) {
		if ( output == null ) {
			Log.w(TAG, "output is null");
		}
		try {
			for ( String line : lines ) {
				output.write(line.getBytes());
				output.write('\n');
			}
		}
		catch (Throwable e) {}
		finally {
			IOUtils.close(output);
		}
	}
}
