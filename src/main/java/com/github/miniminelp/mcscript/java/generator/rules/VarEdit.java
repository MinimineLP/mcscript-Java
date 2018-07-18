/**
 *
 */
package com.github.miniminelp.mcscript.java.generator.rules;

import java.util.List;

import com.github.miniminelp.mcscript.java.generator.GeneratorRule;
import com.github.miniminelp.mcscript.java.parser.Util;

/**
 * @author Minimine
 * @since 0.0.1
 * @version 0.0.3
 *
 */
public class VarEdit extends GeneratorRule {

	/**
	 * @author Minimine
	 * @see com.github.miniminelp.mcscript.java.generator.GeneratorRule#generate()
	 */
	@Override
	public List<Object> generate() {
		
		 Object[] cont = (Object[]) content.getContent(); 
		 String[] varname = (String[]) cont[0];
		 String operator = (String) cont[1];
		 String[] val = (String[]) cont[2];
		 
		 if(Util.NUMBERS.contains(Character.toString(val[0].charAt(0))) && operator.equals("+")) 
			 return list("scoreboard players add "+varname[1]+" "+varname[0]+" "+val[0]);
		 
		 if(Util.NUMBERS.contains(Character.toString(val[0].charAt(0))) && operator.equals("-")) 
			 return list("scoreboard players remove "+varname[1]+" "+varname[0]+" "+val[0]);
		 
		 if(operator.equals("=")) {
			 if(Util.NUMBERS.contains(Character.toString(val[0].charAt(0)))) {
				 return list("scoreboard players set "+varname[1]+" "+varname[0]+" "+val[0]);
			 } else {
				 return list("scoreboard players operation "+varname[1]+" "+varname[0]+" = "+val[1]+" "+val[0]);
			 }
		 }
		 
		 return list("scoreboard players operation "+varname[1]+" "+varname[0]+" "+operator+"= "+val[1]+" "+val[0]);
	}
}
