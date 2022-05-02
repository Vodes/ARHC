package pw.vodes.animerename.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

public class CommandLineUtil {

	public static Process runCommand(List<String> commands, boolean silent) {
		Process process = null;
		ArrayList<String> commandList = new ArrayList<>();
		if (SystemUtils.IS_OS_WINDOWS) {
			commandList.add("cmd");
			commandList.add("/c");
//			commandList.add("start");
		} else if (SystemUtils.IS_OS_LINUX) {
			commandList.add("bash");
			commandList.add("-c");
		} else if (SystemUtils.IS_OS_MAC) {
			commandList.add("/bin/bash");
			commandList.add("-c");
		}
		commandList.addAll(commands);
		try {
			if(SystemUtils.IS_OS_WINDOWS) {
				// A Test of a weird filename encoding workaround
				File tempBat = File.createTempFile("propedit-", ".bat");
				FileUtils.write(tempBat, "chcp 65001" + "\n" + commands.get(0), "UTF-8");
				process = Runtime.getRuntime().exec("\"" + tempBat.getAbsolutePath() + "\"");
				tempBat.deleteOnExit();
			} else {
				process = new ProcessBuilder(commandList).start();
			}
			if(!silent) {
				new OutRedirector(process.getInputStream()).start();
				new OutRedirector(process.getErrorStream()).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return process;
	}
	
	public static class OutRedirector extends Thread {
		InputStream is;

		// reads everything from is until empty.
		public OutRedirector(InputStream is) {
			this.is = is;
		}

		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is, "UTF-8");
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null)
					System.out.println(line + "\n");
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}
