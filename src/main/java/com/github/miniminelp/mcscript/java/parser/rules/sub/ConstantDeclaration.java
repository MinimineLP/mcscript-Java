/**
 *
 */
package com.github.miniminelp.mcscript.java.parser.rules.sub;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.github.miniminelp.mcscript.java.parser.Content;
import com.github.miniminelp.mcscript.java.parser.Parsable;
import com.github.miniminelp.mcscript.java.parser.ParsedObject;
import com.github.miniminelp.mcscript.java.parser.SubParsingExecutor;
import com.github.miniminelp.mcscript.java.parser.rules.ParserRule;

/**
 * @author Minimine
 * @since 0.0.1
 * @version 0.0.4
 *
 */
public class ConstantDeclaration extends ParserRule {

	/**
	 * @see com.github.miniminelp.mcscript.java.parser.rules.ParserRule#apply()
	 */
	@Override
	public boolean apply() {
		
		if(!parsable.isActualWord())return false;
		
		String keyword = parsable.actualWord();
		int line = parsable.getLine();

		if(!keyword.equals("const")) return false;
			
		parsable.skipWord();
		parsable.skipIgnored();
			
		if(!parsable.isActualWord())throwErr("Declaration of "+keyword+" without a name");
		String name = parsable.actualWord();
		
		parsable.skipWord();
		parsable.skipIgnored();
		
		while(parsable.actual().equals(".")||parsable.actual().equals("[")) {
			if(parsable.actual().equals(".")) {
				parsable.skip();
				parsable.skipIgnored();
				if(!parsable.isActualWord())throwErr("Needing word");
				name+="."+parsable.actualWord();
				parsable.skipWord();
				parsable.skipIgnored();
			}
			else {
				String x = replaceLast(parseStatement().replaceFirst("\\[", ""), "\\]", "");
				name += "."+fixStatement(x);
				parsable.skipIgnored();
			}
		}
			
		if(parsable.actual().equals("=")) {
				
			parsable.skip();
			parsable.skipIgnored();
			
			Object val = parseValue();
			
			Content c = new Content("constdeclaration", new Object[]{
				name, val,
			}, line);
			
			parsed.add(c);
				
			return true;
				
		} else {
			throwErr("A const must get a value");
		}
		
		return false;
	}

	/**
	 * @return the parsed value
	 */
	public Object parseValue() {
		
		Object[] s = parseValue(parsable);
		
		parsable = (Parsable) s[0];
		Object val = s[1];
		
		return val;
	}
	

	/**
	 * @param parsable the parsable {@link Parsable}
	 * @return an {@link Object} containing [{@link Parsable}]
	 */
	public Object[] parseValue(Parsable parsable) {
		
		Object val;
		
		if(parsable.isActualNumber()) {
		
			val = parsable.actualNumber();
			parsable.skipNumber();
					
		} else if(parsable.actual().equals("\"")){
			
			StatementParseResult res = parseStatement(parsable);
			parsable = res.getParsable();
			String s = res.getResult();
			val = s;//replaceLast(s.replaceFirst("\\\"", ""), "\\\"", "");
			
		} else if (parsable.actual().equals("'")) {

			StatementParseResult res = parseStatement(parsable);
			parsable = res.getParsable();
			String s = res.getResult();
			val = "\""+replaceLast(s.replaceFirst("'", ""), "'", "")+"\"";
				
		} else if (parsable.actual().equals("{")) {

			StatementParseResult tr = parseStatement(parsable);
			parsable = tr.getParsable();
			String r = tr.getResult();
			String s = replaceLast(r.replaceFirst("\\{", ""),"\\}","");
			Parsable p = new Parsable(s);
			
			HashMap<String, Object> object = new HashMap<String,Object>();
			
			while(p.hasNext()) {
				p.skipIgnored();
				if(!p.isActualWord())throwErr("Needing word",p,file);
				String key = p.actualWord();
				p.skipWord();
				p.skipIgnored();
				if(!p.actual().equals(":"))throwErr("Needing \":\"",p,file);
				p.skip();
				p.skipIgnored();
				Object[] res = parseValue(p);
				p = (Parsable) res[0];
				Object value = res[1];
				object.put(key,value);
				p.skipIgnored();
				if(p.actual().equals(","))p.skip();
				p.skipIgnored();
			}
			
			val = object;
			
		} else if (parsable.actual().equals("[")) {

			StatementParseResult tr = parseStatement(parsable);
			parsable = tr.getParsable();
			String r = tr.getResult();
			String s = replaceLast(r.replaceFirst("\\[", ""),"\\]","");
			
			Parsable p = new Parsable(s);
			
			HashMap<String, Object> object = new HashMap<String,Object>();
			
			int key = 0;
			
			while(p.hasNext()) {
				p.skipIgnored();
				Object[] res = parseValue(p);
				p = (Parsable) res[0];
				Object value = res[1];
				object.put(key+"",value);
				p.skipIgnored();
				if(p.actual().equals(","))p.skip();
				p.skipIgnored();
				key++;
			}
			
			val = object;
				
		} 
		
		else if(parsable.isActualWord()&&parsable.actualWord().equals("modal")) {
			parsable.skipWord();
			parsable.skipIgnored();
			
			if(!parsable.actual().equals("("))throwErr("Can't create a modal without '()' ",parsable,file);
			
			parsable.skip();
				
			List<String> arguments = new LinkedList<String>();
				
			// Now get arguments (if exists)
			if(!parsable.actual().equals(")")) {
				
				String arg = "";
						
				while(!parsable.actual().equals(")")){
					
					if(!parsable.has(parsable.getPosition()))
						throwErr("Unclosed method definition",parsable,file);
							
					if(parsable.actual().equals(",")) {
						if(arg.equals(""))throwErr("Empty required method argument",parsable,file);
						arguments.add(arg);
						arg = "";
					}
							
					else arg += parsable.actual();
					parsable.skip();
					
				}
					
				if(arg.equals(""))throwErr("Empty required method argument",parsable,file);
				arguments.add(arg);
			}
					
			parsable.skip();
			parsable.skipIgnored();
			
			if(!parsable.actual().equals("{")) throwErr("Method without content");

			StatementParseResult tr = parseStatement(parsable);
			parsable = tr.getParsable();
			String r = tr.getResult();
			String statement = replaceLast(r.replaceFirst("\\{", ""),"\\}","");
			
			Parsable subp = new Parsable(statement);
			SubParsingExecutor exec = new SubParsingExecutor(subp, file);
			ParsedObject res = exec.parse();
			
			if(!res.getMethods().isEmpty())throwErr("Can't define a method in a method");
			
			Content action = new Content("methoddefine", new Object[]{"(Unknown)", arguments, res}, parsable.getLine());
			val = action;
		}
		
		else {
			int distanceToSemicolon = parsable.distanceToNext(";".charAt(0));
			int distanceToLinebreak = parsable.distanceToNext(LINESEPERATOR.charAt(0));
			int distanceToComma = parsable.distanceToNext(LINESEPERATOR.charAt(0));
				
			if(distanceToSemicolon > distanceToLinebreak&&distanceToSemicolon > distanceToComma) {
				val = parsable.getToNext(LINESEPERATOR.charAt(0));
				parsable.skipToNext(LINESEPERATOR.charAt(0));
			}
			else if(distanceToComma > distanceToLinebreak&&distanceToSemicolon < distanceToComma) {
				val = parsable.getToNext(LINESEPERATOR.charAt(0));
				parsable.skipToNext(LINESEPERATOR.charAt(0));
			}
			else {
				val = parsable.getToNext(";".charAt(0));
				parsable.skipToNext(";".charAt(0));
			}
		}
		return new Object[] {parsable,val};
	}
}
