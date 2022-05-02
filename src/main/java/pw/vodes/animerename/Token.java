package pw.vodes.animerename;

import java.util.List;

import com.dgtlrepublic.anitomyj.Element;

public class Token {
	
	public String name, anitomyName;
	public String override;
	
	public Token(String name, String anitomyName) {
		this.name = name;
		this.anitomyName = anitomyName;
	}
	
	public String getValue(List<Element> elements) {
		if(override != null && !override.isEmpty()) {
			return override;
		}

		for(Element el : elements) {
//			System.out.println(el.getCategory() + " : " + el.getValue());
			if(el.getCategory().toString().equalsIgnoreCase(anitomyName)) {
//				if(name.endsWith("_e%")) {
//					return "E" + el.getValue();
//				} else if(name.endsWith("_s%")) {
//					return "S" + el.getValue();
//				} else if(name.endsWith("_b%")) {
//					return "[" + el.getValue() + "]";
//				} else if(name.endsWith("_p%")) {
//					return "(" + el.getValue() + ")";
//				} else {
					return el.getValue();
//				}
			}
		}
		return "";
	}

}
