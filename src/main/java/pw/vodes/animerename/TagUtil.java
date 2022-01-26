package pw.vodes.animerename;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import pw.vodes.animerename.cli.MkvInfoWrapper;
import pw.vodes.animerename.cli.MkvPropEditWrapper;

public class TagUtil {
	
	public static void fixTagging(File file) {
		List<Track> tracks = MkvInfoWrapper.parse(file);
		List<Track> editedTracks = new ArrayList<Track>();
		
		//Set Full to default and no-forced
		Track full = getPossibleFullSubs(tracks);
		if(full != null) {
			full.lang = "eng";
			full.default_flag = true;
			full.forced_flag = false;
			editedTracks.add(full);
		}
		
		//Set Sign Track to no-default and forced
		Track sign = getPossibleSignSubs(tracks);
		if(sign != null) {
			sign.lang = "eng";
			sign.default_flag = false;
			sign.forced_flag = true;
			editedTracks.add(sign);
		}
		
		//Set first japanese audio track to default
		boolean hadFirst = false;
		for(Track track : tracks) {
			if(track.type.equalsIgnoreCase("audio")) {
				if(track.lang.equalsIgnoreCase("jpn") || track.lang.equalsIgnoreCase("jp")) {
					if(!hadFirst) {
						track.default_flag = hadFirst = true;
						track.forced_flag = false;
						editedTracks.add(track);
						continue;
					}
				}
				track.default_flag = false;
				track.forced_flag = false;
				editedTracks.add(track);
			}
		}
		
		MkvPropEditWrapper.editTrackMetadata(file, editedTracks);
	}
	
	public static Track getPossibleFullSubs(List<Track> tracks) {
		for(Track track : tracks) {
			if(track.type.equalsIgnoreCase("subtitles")) {
				if(track.lang.equalsIgnoreCase("eng") || track.lang.equalsIgnoreCase("en") || track.lang.equalsIgnoreCase("jp") || track.lang.equalsIgnoreCase("jpn") || track.lang.equalsIgnoreCase("und")) {
					if((!StringUtils.containsIgnoreCase(track.name, "sign") && !StringUtils.containsIgnoreCase(track.name, "song")) || StringUtils.containsIgnoreCase(track.name, "Full"))
						return track;
				}
			}
		}
		return null;
	}
	
	public static Track getPossibleSignSubs(List<Track> tracks) {
		for(Track track : tracks) {
			if(track.type.equalsIgnoreCase("subtitles")) {
//				System.out.println(track.name + ", " + track.lang + ", " + (StringUtils.containsIgnoreCase(track.name, "sign") || StringUtils.containsIgnoreCase(track.name, "song") || track.forced_flag));
				if(track.lang.equalsIgnoreCase("zxx")) {
					return track;
				}
				if((track.lang.equalsIgnoreCase("eng") || track.lang.equalsIgnoreCase("en") || track.lang.equalsIgnoreCase("und"))
						&& (StringUtils.containsIgnoreCase(track.name, "sign") || StringUtils.containsIgnoreCase(track.name, "song") || track.forced_flag)) {
					return track;
				}
			}
		}
		return null;
	}
	
	// Codecs: A_FLAC, A_PCM, A_DTS, A_TRUEHD
	public static List<Track> getLosslessAudioTracks(File file){
		List<Track> audioTracks = new ArrayList<>();
		for(Track track : MkvInfoWrapper.parse(file)) {
			if(track.type.equalsIgnoreCase("audio")) {
				if(StringUtils.containsAny(track.codec_id.toLowerCase(), "A_FLAC".toLowerCase(), "A_PCM".toLowerCase(), "A_DTS".toLowerCase(), "A_TRUEHD".toLowerCase())) {
					audioTracks.add(track);
				}
			}
		}
		return audioTracks;
	}
	
	public static class Track {
		
		public int number, channels = 0, sub_id_ffmpeg = -1, audio_id_ffmpeg = -1;
		public String name = "", lang = "eng", type = "", codec_id = "";
		public boolean default_flag = false, forced_flag = false;
		
		public Track(int number) {
			this.number = number;
		}
	}

}
