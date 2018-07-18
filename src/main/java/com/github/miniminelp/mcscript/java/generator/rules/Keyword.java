/**
 *
 */
package com.github.miniminelp.mcscript.java.generator.rules;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.github.miniminelp.mcscript.java.core.Main;
import com.github.miniminelp.mcscript.java.generator.GeneratorRule;
import com.github.miniminelp.mcscript.java.generator.rules.FileSet.FileEdit;
import com.github.miniminelp.mcscript.java.parser.ConstUseParser;
import com.github.miniminelp.mcscript.java.parser.Content;
import com.github.miniminelp.mcscript.java.parser.ParsedObject;
import com.github.miniminelp.mcscript.java.parser.StatementParser;

/**
 * @author Minimine
 * @since 0.0.1
 * @version 0.0.4
 *
 */
public class Keyword extends GeneratorRule {

	public static int whileid=0;
	
	/**
	 * @see com.github.miniminelp.mcscript.java.generator.GeneratorRule#generate()
	 */
	@Override
	public List<Object> generate() {
		return generateKeyword(content, true);
	}

	/**
	 * @param content the content {@link Content}
	 * @param ex should the keyword an execute? {@link Boolean}
	 * @return the generated content {@link List}?extends {@link Object}
	 * 
	 */
	public List<Object> generateKeyword(Content content, boolean ex) {
		
		FileEdit fe = FileEdit.getLastEdit();

		List<Object> contentlist = new LinkedList<Object>();
		
		Object[] cont = (Object[]) content.getContent();
		String keyword = (String) cont[0];
		String statement = (String)cont[1];
		ParsedObject action = (ParsedObject) cont[2];
		
		if(keyword.equals("then")) {
			if(statement != null)throwError("Expected action after then", content.getLine(), obj.getFile());
			if(action == null)throwError("Expected action after then", content.getLine(), obj.getFile());
			
			List<Object> generated = new LinkedList<Object>();
			
			for(Content a : action.getContent()) {
				Object[] slist = generate(a).toArray();
				
				for(Object sub : slist) {
					if(sub instanceof String) {
						String s = (String)sub;
						
						s = s.replaceAll("\n", "").replaceAll("\r", "");
						
						while(s.startsWith(" ")) {
							s=s.replaceFirst(" ", "");
						}
						if(s!=null&&!s.equals("")){
							generated.add("run " + s);
						}
					} else {
						generated.add(sub);
					}
				}
			}
			contentlist.addAll(generated);
		}
		
		if(" as at asat positioned align dimension rotated anchored".contains(" "+keyword+" ")) {
			
			if(statement==null)throwError("Missing statement for "+keyword+" block", content.getLine(), obj.getFile());
			if(action != null) {
				
				List<Object> generated = new LinkedList<Object>();
				
				for(Content a : action.getContent()) {
					Object[] slist = generate(a).toArray();
					
					for(Object sub : slist) {
						if(sub instanceof String) {
							String s = (String)sub;
							
							s = s.replaceAll("\n", "").replaceAll("\r", "");
							
							while(s.startsWith(" ")) {
								s=s.replaceFirst(" ", "");
							}
							if(s!=null&&!s.equals("")){
								generated.add("run " + s);
							}
						} else {
							generated.add(sub);
						}
					}
				}
				
				Object[] lines = generated.toArray(); 
				
				for(int i=0;i<lines.length;i++) {
					if(lines[i] instanceof String) {
						if(!lines[i].equals("")&&!((String)lines[i]).startsWith("#")){
							if(keyword.equals("asat"))lines[i] = "as "+statement+" at @s "+lines[i];
							if(keyword.equals("as"))lines[i] = "as "+statement+" "+lines[i];
							if(keyword.equals("at"))lines[i] = "at "+statement+" "+lines[i];
							if(keyword.equals("positioned"))lines[i] = "positioned "+statement+" "+lines[i];
							if(keyword.equals("align"))lines[i] = "align "+statement+" "+lines[i];
							if(keyword.equals("dimension"))lines[i] = "dimension "+statement+" "+lines[i];
							if(keyword.equals("rotated"))lines[i] = "rotated "+statement+" "+lines[i];
							if(keyword.equals("anchored"))lines[i] = "anchored "+statement+" "+lines[i];
							
							if(ex)lines[i]="execute "+lines[i];
						}
					}
				}
				contentlist.addAll(Arrays.asList(lines));
				
			}
			else {
				
				statement = fixSelector(statement);
				
				Content next = obj.peek();
				obj.skip();
				
				Object[] lines = generate(next).toArray();
				
				for(int i=0;i<lines.length;i++) {
					if(lines[i] instanceof String) {
						if(!lines[i].equals("")&&!((String)lines[i]).startsWith("#")){
							String the_statement = "";
							if(keyword.equals("asat"))the_statement = "as "+statement+" at @s";
							if(keyword.equals("as"))the_statement = "as "+statement;
							if(keyword.equals("at"))the_statement = "at "+statement;
							if(keyword.equals("positioned"))the_statement = "positioned "+statement;
							if(keyword.equals("align"))the_statement = "align "+statement;
							if(keyword.equals("dimension"))the_statement = "dimension "+statement;
							if(keyword.equals("rotated"))the_statement = "rotated "+statement;
							if(keyword.equals("anchored"))the_statement = "anchored "+statement;
							
							String command = (String) lines[i];
							
							if(command.startsWith("execute ")) {
								command = command.replaceFirst("execute ", "execute "+the_statement);
							}
							else {
								command = "execute "+the_statement+" run "+command;
							}
							
							lines[i]=command;
						}
					}
				}
				contentlist.addAll(Arrays.asList(lines));
			}
		}
		
		else if(keyword.equals("for")) {
			
			if(statement==null)throwError("Missing statement for for block", content.getLine(), obj.getFile());
			if(action == null)throwError("Missing action for for block", content.getLine(), obj.getFile());
			
			String[] statementparts = statement.split(",");
			
			if(statementparts.length!=2) throwError("Needing two parts in the for statement", content.getLine(), obj.getFile());
			
			int from = Integer.valueOf(statementparts[0]);
			int to = Integer.valueOf(statementparts[1]);
			
			for(int i=from; i<=to; i++) {
				
				HashMap<String, Object> filter = new HashMap<String, Object>();
				filter.putAll(consts);
				filter.put("i", i+"");
				
				for(Content a : action.getContent()) {
				
					Object[] slist = generate(a,filter).toArray();
						
					for(Object sub : slist) {
							
						if(sub instanceof String) {
							String s = (String)sub;
							if(s!=null&&!s.equals("")) {
								contentlist.add(ConstUseParser.filter(s, filter));
							}
						}
							
						else if(sub instanceof FileEdit) {
							
							FileEdit tfe = (FileEdit)sub;
							String filtered = ConstUseParser.filter(tfe.getFile(), filter);
							tfe.setFile(filtered);
							contentlist.add(tfe);
							
						}
						else {
							contentlist.add(sub);
						}
							
					}
				}
			}
		}
		
		if(keyword.equals("while")) {
			if(statement==null)throwError("Missing statement for while block", content.getLine(), obj.getFile());
			if(action == null)throwError("Missing action for while block", content.getLine(), obj.getFile());
			
			List<String> statements = generateStatement(statement);
			
			for(String s : statements)
				contentlist.add("execute if entity @e[tag=mcscriptTags,tag=!mcsStopWhile"+whileid+"] "+s+"run function "+Main.getActualDataFolder()+":mcscript/while"+whileid);
			
			
			contentlist.add(new FileEdit("mcscript/while"+whileid));
			
			for(Content c : action.getContent()) {
				
				if(c.getType().equals("keyword")) {
					Object[] x = (Object[]) c.getContent();
					String kw = (String) x[0];
					
					if(kw.equals("stop")) {
						contentlist.add("tag @e[tag=mcscriptTags] add mcsStopWhile"+whileid+"");
					}
					else if(kw.equals("continue")) {
						contentlist.add("function "+Main.getActualDataFolder()+":mcscript/while"+whileid);					
					}
					else {
						contentlist.addAll(generate(c));
					}
				}
				else contentlist.addAll(generate(c));
			}
			
			for(String s : statements) {
				contentlist.add("execute if entity @e[tag=mcscriptTags,tag=!mcsStopWhile"+whileid+"] "+s+"run function "+Main.getActualDataFolder()+":mcscript/while"+whileid);
			}
			whileid++;
			
		}

		if(fe != null)contentlist.add(fe);
		return contentlist;
	}

	public List<String> generateStatement(String statement) {
		
		StatementParser parser = new StatementParser(statement, content.getLine(), obj.getFile());
		List<String> parsed = parser.parse();
		
		return parsed;
	}
}
