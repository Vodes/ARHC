package pw.vodes.animerename.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import pw.vodes.animerename.Sys;
import pw.vodes.animerename.TagUtil;
import pw.vodes.animerename.TagUtil.Track;

public class MkvInfoWrapper {
	
	private static List<String> getOutput(Process p){
		ArrayList<String> out = new ArrayList<String>();
		InputStreamReader isr;
		try {
			isr = new InputStreamReader(p.getInputStream(), "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				out.add(line.contains("+") ? line.split("\\+")[1].trim() : line.trim());
			}
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}
	
	public static List<TagUtil.Track> parse(File file) {
		ArrayList<TagUtil.Track> tracks = new ArrayList<TagUtil.Track>();

		try {
			List<String> output = getOutput(CommandLineUtil.runCommand(Arrays.asList(String.format("mkvinfo \"%s\"", file.getAbsolutePath())), true));
			Track current = null;
//			System.out.println("\n\n");
			for(String line : output) {
//				System.out.println(line);
				if(StringUtils.startsWithIgnoreCase(line, "Track number")) {
					if(current != null) {
//						System.out.println("\n\n");
						tracks.add(current);
					}
					String numberSt = StringUtils.removeStartIgnoreCase(line, "Track number:").trim();
					if(numberSt.contains(" ")) {
						numberSt = numberSt.split(" ")[0];
					}
					current = new Track(Integer.parseInt(numberSt));
//					System.out.println("Track: " + current.number);
				}
				
				if(current != null) {
					if(StringUtils.startsWithIgnoreCase(line, "Track type")) {
						current.type = line.split(":")[1].trim();
//						System.out.println("Type: " + current.type);
					}
					if(StringUtils.startsWithIgnoreCase(line, "codec id")) {
						current.codec_id = line.split(":")[1].trim();
					}
					if(StringUtils.startsWithIgnoreCase(line, "language")) {
						current.lang = line.split(":")[1].trim();
//						System.out.println("Lang: " + current.lang);
					}
					if(StringUtils.startsWithIgnoreCase(line, "name")) {
						current.name = line.split(":")[1].trim();
//						System.out.println("Name: " + current.name);
					}
					if(StringUtils.startsWithIgnoreCase(line, "channels")) {
						current.channels = Integer.parseInt(line.split(":")[1].trim());
					}
					if(StringUtils.startsWithIgnoreCase(line, "default track")) {
						current.default_flag = Integer.parseInt(line.split(":")[1].trim()) > 0;
					}
					if(StringUtils.startsWithIgnoreCase(line, "forced track")) {
						current.forced_flag = Integer.parseInt(line.split(":")[1].trim()) > 0;
					}
					if (StringUtils.startsWithIgnoreCase(line, "EBML void") || StringUtils.startsWithIgnoreCase(line, "File name")) {
						tracks.add(current);
//						System.out.println("\n\n");
						break;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return tracks;
	}
}
