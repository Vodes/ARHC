package pw.vodes.animerename;

import java.awt.Desktop;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;

import com.dgtlrepublic.anitomyj.AnitomyJ;
import com.dgtlrepublic.anitomyj.Element;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;

import pw.vodes.animerename.TagUtil.Track;
import pw.vodes.animerename.cli.CommandLineUtil;
import pw.vodes.animerename.cli.MkvInfoWrapper;
import pw.vodes.animerename.ui.MainWindow;

public class App 
{
	
	public static MainWindow window;
	public static ArrayList<Token> tokens = new ArrayList<>();
	public static List<File> currentFiles = new ArrayList<File>();
	public static boolean isCLI = false;
	
	public static final String DEFAULT_TEMPLATE = "%release_group_b% %anime_title% - %season_number_s%%episode_number_e%", DEFAULT_TITLE_TEMPLATE = "%anime_title% - %season_number_s%%episode_number_e%";
	
	//CLI Options
	private static Options cliOptions = new Options();
	private static CommandLine cmd;
	private static final String dry_run_st = " Tool goes into dry-run mode if neither of rename, mkvt, hardlink or fixtags have been specified.";
	private static Option fileOption = new Option("f", "file", true, "Specifies a single file to edit.");
	private static Option dirOption = new Option("d", "dir", true, "Specifies the directory of files you want to work with.\nDefault is the current working dir.");
	private static Option templateOption = new Option("t", "template", true, "Specifies the template used for renaming.\nDefault: " + DEFAULT_TEMPLATE);
	private static Option titleTemplateOption = new Option("tt", "title_template", true, "Specifies the template used for mkv titles.\nDefault: " + DEFAULT_TITLE_TEMPLATE);
	private static Option renameOption = new Option("rn", "rename", false, "Enables renaming");
	private static Option titleOption = new Option("mkvt", "mkvtitles", false, "Enables mkv title setting");
	private static Option hardlinkOption = new Option("hl", "hardlink", false, "Enables hardlinking");
	private static Option fixtagsOption = new Option("ft", "fixtags", false, "Enables tagfixing");
	private static Option convertOption = new Option("ac", "audioconvert", false, "Enables the conversion of lossless audio to opus");
	private static Option episodeOffsetOp = new Option("eo", "episodeoffset", true, "Set a number offset for the parsed episode.\n%episode_number_a% ignores this because it's for absolute numbering");
	
    public static void main(String[] args){
    	addTokens();
    	Console console = System.console();
    	if(console == null && Desktop.isDesktopSupported() && args.length < 1) {
    		FlatArcDarkIJTheme.setup();
    		window = new MainWindow();
    		window.frame.setVisible(true);
    		window.updateTable();	
    	} else {
    		isCLI = true;
    		cliOptions.addOption(fileOption);
    		cliOptions.addOption(dirOption);
    		cliOptions.addOption(templateOption);
    		cliOptions.addOption(titleTemplateOption);
    		cliOptions.addOption(renameOption);
    		cliOptions.addOption(hardlinkOption);
    		cliOptions.addOption(fixtagsOption);
    		cliOptions.addOption(titleOption);
    		cliOptions.addOption(convertOption);
    		cliOptions.addOption(episodeOffsetOp);
    		
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(115);

            try {
                cmd = new DefaultParser().parse(cliOptions, args);
                if(args.length < 1) {
                	throw new ParseException("");
                }
            } catch (ParseException e) {
                System.out.println(e.getMessage());
                formatter.printHelp(" ", cliOptions);
                System.out.println(dry_run_st);
                System.out.println("\n\nPossible Template Tokens: \n" + getTokenString());
                System.exit(1);
            }
            
            if(cmd.hasOption(renameOption) && cmd.hasOption(hardlinkOption)) {
            	Sys.out("Both renaming and hardlinking at the same time is not supported!", "error");
            	System.exit(1);
            } else if(cmd.hasOption(hardlinkOption) && cmd.hasOption(fixtagsOption)) {
            	Sys.out("Tag fixing and mkv title changes modify the original files.", "warn");
            	Sys.out("If you know what you're doing, feel free to wait 10 seconds to continue, otherwise please abort.", "warn");
            	try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
            
            String dir = cmd.getOptionValue(dirOption, System.getProperty("user.dir"));
            File in_file = new File(dir);
            if(cmd.hasOption(fileOption)) {
            	in_file = new File(cmd.getOptionValue(fileOption));
            	if(!in_file.exists() || in_file.isDirectory()) {
            		Sys.out("Please provide a valid file!", "error");
            		System.exit(1);
            	}
            } else if(cmd.hasOption(dirOption)) {
            	if(!in_file.exists() || !in_file.isDirectory() || in_file.listFiles().length < 1) {
            		Sys.out("Please provide a valid & not empty directory!", "error");
            		System.exit(1);
            	}
            }
            String template = cmd.getOptionValue(templateOption, DEFAULT_TEMPLATE);
            String title_template = cmd.getOptionValue(titleTemplateOption, DEFAULT_TITLE_TEMPLATE);
            
            if(!cmd.hasOption(renameOption) && !cmd.hasOption(hardlinkOption) && !cmd.hasOption(fixtagsOption) && !cmd.hasOption(titleOption) && !cmd.hasOption(convertOption)) {
            	dryRun(in_file, template, title_template);
            } else {
            	cliRun(in_file, cmd.hasOption(renameOption), cmd.hasOption(hardlinkOption), cmd.hasOption(fixtagsOption), cmd.hasOption(titleOption), cmd.hasOption(convertOption), template, title_template);
            }
    	}
    }
    
    private static void dryRun(File in, String template, String title_template) {
    	List<File> files = in.isDirectory() ? Arrays.asList(in.listFiles()) : Arrays.asList(in);
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	for(File file : files) {
			if(FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("mkv")) {
				System.out.println(file.getName() + "\n");
				System.out.println("Would rename/hardlink to: " + doTokenReplace(file.getName(), template) + ".mkv");
				System.out.println("Would change title to: " + doTokenReplace(file.getName(), title_template) + "\n\n");
				
				for(Track track : TagUtil.getLosslessAudioTracks(file)) {
					System.out.println("Lossless Audio Track: " + track.audio_id_ffmpeg + " | Name: " + track.name + " | Channels: " + track.channels);
				}
			}
    	}
    	
    }
    
    private static void cliRun(File in, boolean rename, boolean hardlink, boolean fixtags, boolean changeTitle, boolean audioCon, String template, String title_template) {
    	List<File> files = in.isDirectory() ? Arrays.asList(in.listFiles()) : Arrays.asList(in);

    	File tempdir = null;
    	try {
			tempdir = Files.createTempDirectory("arhc-conv").toFile();
			tempdir.deleteOnExit();
		} catch (IOException e2) {}
    	
    	for(File file : files) {
			if(!file.isFile() || !FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("mkv")) {
				continue;
			}
    		if(changeTitle) {
				String command = String.format("mkvpropedit \"%s\" --edit info --set title=\"%s\"", file.getAbsolutePath(), doTokenReplace(file.getName(), title_template));
				try {
					CommandLineUtil.runCommand(Arrays.asList(command), true).waitFor();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
    		}
    		
    		if(fixtags) {
    			TagUtil.fixTagging(file);
    		}
    		
    		if(audioCon) {
    			try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    			List<Track> tracks = TagUtil.getLosslessAudioTracks(file);
    			if(!tracks.isEmpty() && tempdir != null) {
        			String command = String.format("ffmpeg -i \"%s\" -map 0 -c copy", file.getAbsolutePath());
        			for(Track track : tracks) {
        				if(track.channels <= 2) {
        					command += String.format(" -c:a:%d libopus -b:a:%d 224k", track.audio_id_ffmpeg, track.audio_id_ffmpeg);
        				} else if(track.channels == 6) {
        					command += String.format(" -c:a:%d libopus -filter:a:%d \"channelmap=channel_layout=5.1\" -b:a:%d 420k", track.audio_id_ffmpeg, track.audio_id_ffmpeg, track.audio_id_ffmpeg);
        				} else if(track.channels == 8) {
        					command += String.format(" -c:a:%d libopus -filter:a:%d \"channelmap=channel_layout=7.1\" -b:a:%d 496k", track.audio_id_ffmpeg, track.audio_id_ffmpeg, track.audio_id_ffmpeg);
        				}
        			}
        			File out = new File(tempdir, file.getName());
        			command += String.format(" \"%s\"", out.getAbsolutePath());
        			try {
        				Sys.out("Converting: " + file.getName());
        				//Run conversion (ffmpeg)
						CommandLineUtil.runCommand(Arrays.asList(command), true).waitFor();
						// Wait a bit (safety first)
		    			try {Thread.sleep(1000);} catch (InterruptedException e) {}
        				Sys.out("Fixing metadata...");
						//Fix mkv metadata (due to ffmpeg fucking it up)
						CommandLineUtil.runCommand(Arrays.asList(String.format("mkvpropedit --add-track-statistics-tags \"%s\"", out.getAbsolutePath())), true).waitFor();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
        			if(out.exists() && out.length() > 1000) {
        				try {
							Files.move(out.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							e.printStackTrace();
						}
        			}
    			}
    		}
    		
			File out = new File(file.getParentFile(), doTokenReplace(file.getName(), template) + ".mkv");
    		if(rename) {
    			file.renameTo(out);
    		} else if(hardlink) {
    			try {
    				File outDir = new File(out.getParentFile(), "links");
    				outDir.mkdir();
					Files.createLink(new File(outDir, out.getName()).toPath(), file.toPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    	}
    }
    
    private static void addTokens() {
    	tokens.add(new Token("%episode_number%", "kElementEpisodeNumber"));
    	tokens.add(new Token("%episode_number_a%", "kElementEpisodeNumber"));
    	tokens.add(new Token("%episode_number_e%", "kElementEpisodeNumber"));
    	tokens.add(new Token("%episode_title%", "kElementEpisodeTitle"));
    	tokens.add(new Token("%season_number%", "kElementAnimeSeason"));
    	tokens.add(new Token("%season_number_s%", "kElementAnimeSeason"));
    	tokens.add(new Token("%anime_title%", "kElementAnimeTitle"));
    	tokens.add(new Token("%release_group%", "kElementReleaseGroup"));
    	tokens.add(new Token("%release_group_b%", "kElementReleaseGroup"));
    	tokens.add(new Token("%release_group_p%", "kElementReleaseGroup"));
    }
    
    public static String getTokenString() {
    	String s = "";
    	for(Token token : tokens) {
    		s += token.name + "\n";
    	}
    	return s.trim();
    }
    
    public static String doTokenReplace(String inputFile, String template) {
    	String returnSt = template;
    	List<Element> aniElements = AnitomyJ.parse(inputFile);
    	for(Token token : tokens) {
    		String replacement = token.getValue(aniElements);
    		if(token.name.equalsIgnoreCase("%episode_number_e%") || token.name.equalsIgnoreCase("%episode_number%")) {
    			if(isCLI) {
    				if(cmd.hasOption(episodeOffsetOp)) {
    					try {
							double offset = Double.parseDouble(cmd.getOptionValue(episodeOffsetOp));
							double episode = Double.parseDouble(replacement);
							
							DecimalFormat format = new DecimalFormat("0.#");
							episode += offset;
							if (episode > 0) {
								replacement = episode < 10 ? "0" + format.format(episode) : format.format(episode);
							}
						} catch (Exception e) {}
    				}
    			}
    		}
    		if(!replacement.isEmpty()) {
				if(token.name.endsWith("_e%")) {
					replacement = "E" + replacement;
				} else if(token.name.endsWith("_s%")) {
					replacement = "S" + replacement;
				} else if(token.name.endsWith("_b%")) {
					replacement = "[" + replacement + "]";
				} else if(token.name.endsWith("_p%")) {
					replacement = "(" + replacement + ")";
				}
    		}
    		returnSt = returnSt.trim().replaceAll("(?i)" + token.name, replacement);
    	}
    	return returnSt;
    }
    
    public static String getTokenOverride(String token) {
    	for(Token t : tokens) {
    		if(t.name.equalsIgnoreCase(token)) {
    			return t.override == null ? "" : t.override;
    		}
    	}
    	return "";
    }
    
    public static void setTokenOverride(String token, String value) {
    	for(Token t : tokens) {
    		if(t.name.equalsIgnoreCase(token)) {
    			t.override = value;
    			break;
    		}
    	}
    }
}
