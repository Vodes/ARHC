package pw.vodes.animerename;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import pw.vodes.animerename.cli.MkvInfoParser;

public class TagUtil {
	
	public static void fixTagging(File file) {
		var tracks = MkvInfoParser.parse(file);
		var full = getPossibleFullSubs(tracks);
		Sys.out("Full: " + full.name);
		var sign = getPossibleSignSubs(tracks);
		Sys.out("Sign: " + sign.name);
	}
	
	public static Track getPossibleFullSubs(List<Track> tracks) {
		for(var track : tracks) {
			if(track.type.equalsIgnoreCase("subtitles")) {
				if((track.lang.equalsIgnoreCase("eng") || track.lang.equalsIgnoreCase("jpn") || track.lang.equalsIgnoreCase("und")) 
						&& !StringUtils.containsIgnoreCase(track.name, "sign") && !StringUtils.containsIgnoreCase(track.name, "song")) {
					return track;
				}
			}
		}
		return null;
	}
	
	public static Track getPossibleSignSubs(List<Track> tracks) {
		for(var track : tracks) {
			if(track.type.equalsIgnoreCase("subtitles")) {
				if(track.lang.equalsIgnoreCase("zxx")) {
					return track;
				}
				if(track.lang.equalsIgnoreCase("eng") 
						&& (StringUtils.containsIgnoreCase(track.name, "sign") || StringUtils.containsIgnoreCase(track.name, "song") || track.forced_flag)) {
					return track;
				}
			}
		}
		return null;
	}
	
	public static class Track {
		
		public int number, channels = 0;
		public String name = "", lang = "und", type = "", codec_id = "";
		public boolean default_flag = false, forced_flag = false;
		
		public Track(int number) {
			this.number = number;
		}
	}

}
