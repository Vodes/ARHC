package pw.vodes.animerename.cli;

public class MkvPropEditWrapper {
	
	public static void setTrackMetadata(int track, String name, String lang, boolean default_flag, boolean forced_flag) {
		String name_part = name != null && !name.isEmpty() ? " --set name=\"" + lang + "\"" : "";
		String lang_part = lang != null && !lang.isEmpty() ? " --set language=" + lang : "";
		String default_part = " --set flag-default " + (default_flag ? "1" : "0");
		String forced_part = " --set flag-forced " + (forced_flag ? "1" : "0");

	}

}
