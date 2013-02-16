package org.ccnx.ccn.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.logging.Level;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.impl.support.Log;
import org.ccnx.ccn.io.CCNVersionedInputStream;
import org.ccnx.ccn.io.NoMatchingContentFoundException;
import org.ccnx.ccn.io.RepositoryVersionedOutputStream;
import org.ccnx.ccn.io.content.ConfigSlice;
import org.ccnx.ccn.io.content.ContentDecodingException;
import org.ccnx.ccn.profiles.VersioningProfile;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

/**
 * FileSync command-line utility
 * Author: Jared Lindblom <lindblom@cs.ucla.edu>
 * 
 * NOTE: Error/Exception reporting is suppressed by default in this tool
 * for applications that rely on its standard output! Please set suppressReporting to false
 * if Error/Exception reporting is desired.
 */
public class filesync implements Usage {
	static String[] OkArgs = {"-slice", "-getSnapshot", "-setSnapshot"};
	static Usage u = new filesync();
	
	/** Suppress Error/Exception reporting in this tool (for applications that rely on its standard output) */
	static final boolean suppressReporting = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Log.setDefaultLevel(Level.WARNING);

		if (args.length > 3 || args.length < 1)
			u.usage("");

		try {
			switch(args[0])
			{
			case "-slice":
				if(args.length != 3)
					u.usage("");

				createSlice(args[1], args[2]);
				break;
			case "-getLatestSnapshot":
				if(args.length != 2)
					u.usage("");

				getSnapshot(args[1]);
				break;
			case "-putSnapshot":
				if(args.length != 2)
					u.usage("");

				putSnapshot(args[1]);
				break;
			default:
				u.usage("");
				break;
			}
		} catch (MalformedContentNameStringException e) {
			if( !suppressReporting )
				e.printStackTrace();
			System.exit(1);
		} catch (ContentDecodingException e) {
			if( !suppressReporting )
				e.printStackTrace();
			System.exit(1);
		} catch (NoMatchingContentFoundException e) {
			if( !suppressReporting )
				e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			if( !suppressReporting )
				e.printStackTrace();
			System.exit(1);
		}

		System.exit(0);
	}

	public static void createSlice(String topologyString, String prefixString) throws MalformedContentNameStringException, ContentDecodingException, IOException {

		/** Get CCN Handle */
		CCNHandle handle = CCNHandle.getHandle();

		/** Validate Topology */
		ContentName topology = ContentName.fromNative(topologyString);

		/** Validate Prefix */
		ContentName prefix = ContentName.fromNative(prefixString);

		/** Create Slice */
		ConfigSlice.checkAndCreate(topology, prefix, null, handle);

		/** Close CCNHandle */
		handle.close();
	}

	public static void getSnapshot (String prefixString) throws MalformedContentNameStringException, NoMatchingContentFoundException, IOException
	{
		/** Get CCN Handle */
		CCNHandle handle = CCNHandle.getHandle();

		/** Validate Snapshot */
		ContentName snapshot = ContentName.fromNative(prefixString + "/snapshot");

		/** Find Latest Version */
		Interest interest = VersioningProfile.latestVersionInterest(snapshot, null, null);

		/** Get Latest Version */
		BufferedInputStream bufferedInputStream = new BufferedInputStream(new CCNVersionedInputStream(interest.name()));

		/** Create Output Stream */
		PrintStream printStream = new PrintStream(System.out);

		/** Write to StdOut */
		byte[] buffer = new byte[1024];
		int bytesRead;

		while ((bytesRead = bufferedInputStream.read(buffer)) >= 0) {
			printStream.write(buffer, 0, bytesRead);
		}

		/** Close OutputStream */
		printStream.close();

		/** Close InputStream */
		bufferedInputStream.close();

		/** Close CCNHandle */
		handle.close();
	}

	public static void putSnapshot (String prefixString) throws MalformedContentNameStringException, IOException {

		/** Get CCN Handle */
		CCNHandle handle = CCNHandle.getHandle();

		/** Validate Snapshot */
		ContentName snapshot = ContentName.fromNative(prefixString + "/snapshot");

		/** Version Snapshot */
		ContentName versionedSnapshot = VersioningProfile.updateVersion(snapshot);

		/** Create Versioned Output Stream */
		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new RepositoryVersionedOutputStream(versionedSnapshot, handle));

		/** Read from Stdin */
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

		/** Read One Line */
		int i;
		while ((i = bufferedReader.read()) >= 0)
		{
			bufferedOutputStream.write(i);

			/** If newline, stop */
			if (i == '\n')
				break;
		}

		/** Close BufferedReader */
		bufferedReader.close();

		/** Close BufferedOutputStream */
		bufferedOutputStream.close();

		/** Close CCNHandle */
		handle.close();
	}

	public void usage(String extraUsage) {
		if( !suppressReporting )
			System.out.println("usage: filesync " + extraUsage + "[-slice] <topo> <prefix> || [-getSnapshot] <prefix> || [-putSnapshot] <prefix>");
		System.exit(1);
	}
}
