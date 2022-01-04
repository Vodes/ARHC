package pw.vodes.animerename.cli;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import pw.vodes.animerename.TagUtil.Track;

public class MkvPropEditWrapper {
	
	public static void editTrackMetadata(File file, List<Track> tracks) {
		String command = String.format("mkvpropedit \"%s\"", file.getAbsolutePath());
		
		for(Track track : tracks) {
			String name_part = " --set name=\"" + track.name + "\"";
			String lang_part = " --set language=" + track.lang;
			String default_part = " --set flag-default=" + (track.default_flag ? "1" : "0");
			String forced_part = " --set flag-forced=" + (track.forced_flag ? "1" : "0");
			
			command += String.format(" --edit track:%d%s%s%s%s", track.number, name_part, lang_part, default_part, forced_part);
		}
//		System.out.println(command);
		try {
			CommandLineUtil.runCommand(Arrays.asList(command), true).waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
