package pw.vodes.animerename;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import pw.vodes.animerename.cli.MkvInfoWrapper;
import pw.vodes.animerename.cli.MkvPropEditWrapper;

public class TagUtil {
	
	public static void fixTagging(File file, String title) {
		List<Track> tracks = MkvInfoWrapper.parse(file);
		List<Track> editedTracks = new ArrayList<Track>();
		
		Sys.out("--------------------------\nFixing tags: " + file.getName());
		
		//Set Full to default and no-forced
		Track full = getPossibleFullSubs(tracks);
		if(full != null) {
			Sys.out(String.format("Full Subs | ID: %d | Name: %s | Lang: %s", full.number, full.name, full.lang));
			full.lang = "eng";
			full.default_flag = true;
			full.forced_flag = false;
			editedTracks.add(full);
		}
		
		//Set Sign Track to no-default and forced
		Track sign = getPossibleSignSubs(tracks);
		if(sign != null) {
			Sys.out(String.format("Signs Subs | ID: %d | Name: %s | Lang: %s", sign.number, sign.name, sign.lang));
			sign.lang = "eng";
			sign.default_flag = false;
			sign.forced_flag = true;
			editedTracks.add(sign);
		}
		
		Track germSign = getPossibleGermanSignSubs(tracks);
		if(germSign != null) {
			Sys.out(String.format("German Signs Subs | ID: %d | Name: %s | Lang: %s", germSign.number, germSign.name, germSign.lang));
			germSign.lang = "ger";
			germSign.default_flag = false;
			germSign.forced_flag = true;
			editedTracks.add(germSign);
		}
		
		//Set first japanese audio track to default
		boolean hadFirst = false;
		for(Track track : tracks) {
			if(track.type.equalsIgnoreCase("audio")) {
				if(track.lang.equalsIgnoreCase("jpn") || track.lang.equalsIgnoreCase("jp") || track.lang.equalsIgnoreCase("ja")) {
					if(!hadFirst) {
						Sys.out(String.format("Jap Audio | ID: %d | Name: %s | Codec: %s", track.number, track.name, track.codec_id));
						track.default_flag = true;
						track.forced_flag = false;
						if(StringUtils.containsAny(track.name.toLowerCase(), "flac", "truehd") && track.codec_id.toLowerCase().contains("opus")) {
							if(track.name.toLowerCase().contains("flac")) {
								track.name = track.name.replaceAll("(?i)flac", "Opus");
							} else {
								track.name = track.name.replaceAll("(?i)truehd", "Opus");
							}
						}
						editedTracks.add(track);
						hadFirst = true;
						continue;
					}
				}
				Sys.out(String.format("Other Audio | ID: %d | Name: %s | Codec: %s | Lang: %s", track.number, track.name, track.codec_id, track.lang));
				track.default_flag = false;
				track.forced_flag = false;
				if(StringUtils.containsAny(track.name.toLowerCase(), "flac", "truehd") && track.codec_id.toLowerCase().contains("opus")) {
					if(track.name.toLowerCase().contains("flac")) {
						track.name = track.name.replaceAll("(?i)flac", "Opus");
					} else {
						track.name = track.name.replaceAll("(?i)truehd", "Opus");
					}
				}
				editedTracks.add(track);
			} else if(track.type.equalsIgnoreCase("subtitles")) {
				if(full != null && full.number == track.number) {
					continue;
				}
				if(sign != null && sign.number == track.number) {
					continue;
				}
				if(germSign != null && germSign.number == track.number) {
					continue;
				}
				Sys.out(String.format("Other Subs | ID: %d | Name: %s | Codec: %s | Lang: %s", track.number, track.name, track.codec_id, track.lang));
				track.default_flag = false;
				track.forced_flag = false;
				editedTracks.add(track);
			}
		}
		Sys.out("Running mkvpropedit...");
		MkvPropEditWrapper.editTrackMetadata(file, editedTracks, title);
		Sys.out("Done.\n--------------------------");
	}
	
	public static Track getPossibleFullSubs(List<Track> tracks) {
		for(Track track : tracks) {
			if(track.type.equalsIgnoreCase("subtitles")) {
				if(track.lang.equalsIgnoreCase("eng") || track.lang.equalsIgnoreCase("en") || track.lang.equalsIgnoreCase("jp") || track.lang.equalsIgnoreCase("ja") || track.lang.equalsIgnoreCase("jpn") || track.lang.equalsIgnoreCase("und")) {
					if((!StringUtils.containsIgnoreCase(track.name, "sign") && !StringUtils.containsIgnoreCase(track.name, "song")) || StringUtils.containsIgnoreCase(track.name, "Full"))
						return track;
				}
			}
		}
		return null;
	}
	
	public static Track getPossibleGermanSignSubs(List<Track> tracks) {
		for(Track track : tracks) {
			if(track.type.equalsIgnoreCase("subtitles")) {
				if(track.lang.equalsIgnoreCase("ger") || track.lang.equalsIgnoreCase("de")) {
					if(StringUtils.containsIgnoreCase(track.name, "sign") || StringUtils.containsIgnoreCase(track.name, "song") || StringUtils.containsIgnoreCase(track.name, "forc") || track.forced_flag) {
						return track;
					}
				}
			}
		}
		return null;
	}
	
	public static Track getPossibleSignSubs(List<Track> tracks) {
		for(Track track : tracks) {
			if(track.type.equalsIgnoreCase("subtitles")) {
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
	
	public static List<Track> getLosslessAudioTracks(List<Track> tracks){
		List<Track> audioTracks = new ArrayList<>();
		for(Track track : tracks) {
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
