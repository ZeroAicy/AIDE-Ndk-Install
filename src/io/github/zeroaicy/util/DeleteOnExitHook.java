package io.github.zeroaicy.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * This class holds a set of filenames to be deleted on VM exit through a shutdown hook.
 * A set is used both to prevent double-insertion of the same file as well as offer
 * quick removal.
 */


public class DeleteOnExitHook {
    private static LinkedHashSet<String> files = new LinkedHashSet<>();
    static {
        // BEGIN Android-changed: Use Runtime.addShutdownHook() rather than SharedSecrets.
        Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					log("run-运行");
					runHooks();
				}

			});
        // END Android-changed: Use Runtime.addShutdownHook() rather than SharedSecrets.
    }

    private DeleteOnExitHook() {}


	private static void log(String v){
		Log.d("DeleteOnExitHook", v);
	}
	public static synchronized void addAll(File[] fileArray) {
        if(files == null) {
            // DeleteOnExitHook is running. Too late to add a file
            throw new IllegalStateException("Shutdown in progress");
        }
		for( File f : fileArray){
			if( f != null){
				files.add(f.getAbsolutePath());
			}
		}

    }
	
	public static synchronized void add(String file) {
        if(files == null) {
            // DeleteOnExitHook is running. Too late to add a file
            throw new IllegalStateException("Shutdown in progress");
        }
		files.add(file);
		
    }
	private static void runHooks() {
		log("runHooks-开始");
		
        LinkedHashSet<String> theFiles;

        synchronized (DeleteOnExitHook.class) {
            theFiles = files;
            files = null;
        }

        ArrayList<String> toBeDeleted = new ArrayList<String>(theFiles);

        // reverse the list to maintain previous jdk deletion order.
        // Last in first deleted.
        Collections.reverse(toBeDeleted);
		
        for (String filename : toBeDeleted) {
            (new File(filename)).delete();
        }
		log("runHooks-END");
		
    }
}

