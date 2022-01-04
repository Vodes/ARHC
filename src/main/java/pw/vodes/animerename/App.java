package pw.vodes.animerename;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.dgtlrepublic.anitomyj.AnitomyJ;
import com.dgtlrepublic.anitomyj.Element;
import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.FlatArcIJTheme;

import pw.vodes.animerename.ui.MainWindow;

public class App 
{
	
	public static MainWindow window;
	public static ArrayList<Token> tokens = new ArrayList<>();
	public static List<File> currentFiles = new ArrayList<File>();
	
    public static void main(String[] args){
    	addTokens();
    	if(Desktop.isDesktopSupported() && args.length < 1) {
    		FlatArcDarkIJTheme.setup();
    		window = new MainWindow();
    		window.frame.setVisible(true);
    		window.updateTable();	
    	} else {
    		
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
