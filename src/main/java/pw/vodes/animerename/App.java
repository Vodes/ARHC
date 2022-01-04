package pw.vodes.animerename;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;

import com.dgtlrepublic.anitomyj.AnitomyJ;
import com.dgtlrepublic.anitomyj.Element;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;

import pw.vodes.animerename.cli.CommandLineUtil;
import pw.vodes.animerename.cli.MkvInfoParser;
import pw.vodes.animerename.ui.MainWindow;

public class App 
{
	
	public static MainWindow window;
	public static ArrayList<Token> tokens = new ArrayList<>();
	public static List<File> currentFiles = new ArrayList<File>();
	
	public static final String DEFAULT_TEMPLATE = "[%release_group%] %anime_title% - %season_number_s%%episode_number_e%", DEFAULT_TITLE_TEMPLATE = "%anime_title% - %season_number_s%%episode_number_e%";
	
	//CLI Options
	private static final String dry_run_st = " Tool goes into dry-run mode if neither of rename, mkvt, hardlink or fixtags have been specified.";
	private static Option dirOption = new Option("d", "dir", true, "Specifies the directory of files you want to work with.");
	private static Option templateOption = new Option("t", "template", true, "Specifies the template used for renaming.\nDefault: " + DEFAULT_TEMPLATE);
	private static Option titleTemplateOption = new Option("tt", "title_template", true, "Specifies the template used for mkv titles.\nDefault: " + DEFAULT_TITLE_TEMPLATE);
	private static Option renameOption = new Option("rn", "rename", false, "Enables renaming." + dry_run_st);
	private static Option titleOption = new Option("mkvt", "mkvtitles", false, "Enables mkv title setting." + dry_run_st);
	private static Option hardlinkOption = new Option("hl", "hardlink", false, "Enables hardlinking." + dry_run_st);
	private static Option fixtagsOption = new Option("ft", "fixtags", false, "Enables tagfixing." + dry_run_st);
	
    public static void main(String[] args){
    	addTokens();
    	if(Desktop.isDesktopSupported() && args.length < 1) {
    		FlatArcDarkIJTheme.setup();
    		window = new MainWindow();
    		window.frame.setVisible(true);
    		window.updateTable();	
    	} else {
    		var cliOptions = new Options();
    		cliOptions.addOption(dirOption);
    		cliOptions.addOption(templateOption);
    		cliOptions.addOption(titleTemplateOption);
    		cliOptions.addOption(renameOption);
    		cliOptions.addOption(hardlinkOption);
    		cliOptions.addOption(fixtagsOption);
    		cliOptions.addOption(titleOption);
    		
            CommandLineParser parser = new DefaultParser();
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(115);
            CommandLine cmd = null;//not a good practice, it serves it purpose 

            try {
                cmd = parser.parse(cliOptions, args);
            } catch (ParseException e) {
                System.out.println(e.getMessage());
                formatter.printHelp(" ", cliOptions);
				String keys = "%anime_title%\n%episode_title%\n%episode_number%\n%episode_number_e%\n%season_number%\n%season_number_s%\n%release_group%";
                System.out.println("\n\nPossible Template Tokens: \n" + keys);
                System.exit(1);
            }
            
            if(cmd.hasOption(renameOption) && cmd.hasOption(hardlinkOption)) {
            	Sys.out("Both renaming and hardlinking at the same time is not supported!", "error");
            	System.exit(1);
            } else if(cmd.hasOption(hardlinkOption) && cmd.hasOption(fixtagsOption)) {
            	Sys.out("You have both hardlinking and tag fixing enabled. Tag fixing modifies the original files.", "warn");
            	Sys.out("If you know what you're doing, feel free to wait 10 seconds to continue, otherwise please abort.", "warn");
            	try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
            
            var dir = cmd.getOptionValue(dirOption, System.getProperty("user.dir"));
            var dir_file = new File(dir);
            if(cmd.hasOption(dirOption)) {
            	if(!dir_file.exists() || !dir_file.isDirectory() || dir_file.listFiles().length < 1) {
            		Sys.out("Please provide a valid & not empty directory!", "error");
            		System.exit(1);
            	}
            }
            var template = cmd.getOptionValue(templateOption, DEFAULT_TEMPLATE);
            var title_template = cmd.getOptionValue(titleTemplateOption, DEFAULT_TITLE_TEMPLATE);
            
            if(!cmd.hasOption(renameOption) && !cmd.hasOption(hardlinkOption) && !cmd.hasOption(fixtagsOption) && !cmd.hasOption(titleOption)) {
            	dryRun(dir_file, template, title_template);
            } else {
            	cliRun(dir_file, cmd.hasOption(renameOption), cmd.hasOption(hardlinkOption), cmd.hasOption(fixtagsOption), cmd.hasOption(titleOption), template, title_template);
            }
    	}
    }
    
    private static void dryRun(File dir, String template, String title_template) {
    	for(var file : dir.listFiles()) {
			if(FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("mkv")) {
				TagUtil.fixTagging(file);
				return;
//				System.out.println(file.getName() + "\n");
//				System.out.println("Would rename/hardlink to: " + doTokenReplace(file.getName(), template) + ".mkv");
//				System.out.println("Would change title to: " + doTokenReplace(file.getName(), title_template) + "\n\n");
			}
    	}
    	
    }
    
    private static void cliRun(File dir, boolean rename, boolean hardlink, boolean fixtags, boolean changeTitle, String template, String title_template) {
    	for(var file : dir.listFiles()) {
    		if(changeTitle) {
				String command = String.format("mkvpropedit \"%s\" --edit info --set title=\"%s\"", file.getAbsolutePath(), doTokenReplace(file.getName(), title_template));
				ArrayList<String> commands = new ArrayList<>();
				commands.add(command);
				try {
					CommandLineUtil.runCommand(commands, true).waitFor();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
    		}
    		
    		if(fixtags) {
    			
    		}
    		
    		if(rename) {
    			
    		}
    	}
    }
    
    private static void addTokens() {
    	tokens.add(new Token("%episode_number%", "kElementEpisodeNumber"));
    	tokens.add(new Token("%episode_number_e%", "kElementEpisodeNumber"));
    	tokens.add(new Token("%episode_title%", "kElementEpisodeTitle"));
    	tokens.add(new Token("%season_number%", "kElementAnimeSeason"));
    	tokens.add(new Token("%season_number_s%", "kElementAnimeSeason"));
    	tokens.add(new Token("%anime_title%", "kElementAnimeTitle"));
    	tokens.add(new Token("%release_group%", "kElementReleaseGroup"));
    }
    
    public static String doTokenReplace(String inputFile, String template) {
    	String returnSt = template;
    	List<Element> aniElements = AnitomyJ.parse(inputFile);
    	for(Token token : tokens) {
    		String replacement = token.getValue(aniElements);
    		returnSt = returnSt.replaceAll("(?i)" + token.name, replacement);
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
