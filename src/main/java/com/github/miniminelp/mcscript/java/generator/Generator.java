/**
 * 
 */

package com.github.miniminelp.mcscript.java.generator;

/**
 * @author Minimine
 * @since 0.0.1
 * @version 0.0.4
 *
 */

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import com.github.miniminelp.mcscript.java.parser.ConstUseParser;
import com.github.miniminelp.mcscript.java.parser.Content;
import com.github.miniminelp.mcscript.java.parser.ParsedObject;
import com.github.miniminelp.mcscript.java.filemanager.FileContext;
import com.github.miniminelp.mcscript.java.filemanager.Files;
import com.github.miniminelp.mcscript.java.generator.rules.*;
import com.github.miniminelp.mcscript.java.generator.rules.FileSet.FileEdit;
import com.github.miniminelp.mcscript.java.util.MCScriptObject;

public class Generator implements MCScriptObject, GeneratorFunctions {
	
	private ParsedObject obj;
	private HashMap<String, Object> consts = new HashMap<String,Object>();
	private HashMap<String, Object> globalconsts = new HashMap<String, Object>();
	private HashMap<String,GeneratorRule> rules = new HashMap<String,GeneratorRule>();
	
	public Generator(ParsedObject obj) {
		this.obj = obj;

		this.rules.put("command", new Command());
		this.rules.put("keyword", new Keyword());
		this.rules.put("switch", new Switch());
		this.rules.put("if", new If());
		this.rules.put("functioncall", new FunctionCall());
		this.rules.put("comment", new Comment());
		this.rules.put("vardeclaration", new VarDeclaration());
		this.rules.put("fileset", new FileSet());
		this.rules.put("constdeclaration", new ConstDeclaration());
		this.rules.put("booleandeclaration", new BooleanDeclaration());
		this.rules.put("varedit", new VarEdit());
		this.rules.put("raycast", new Raycast());
		this.rules.put("dowhile", new DoWhile());
		this.rules.put("foreach", new ForEach());
		this.rules.put("debug", new Debug());
	}
	
	public Generator(ParsedObject obj, HashMap<String, Object> consts) {
		this(obj);
		globalconsts = consts;
	}
	
	public void generate() {
		
		// add local methods
		for(Content c : obj.getMethods()) {
			String name = (String)((Object[])c.getContent())[0];
			consts.put(name,c);
		}
		
		consts.putAll(globalconsts);
		
		while(obj.hasSpace(0)){
			generateContent(obj.actual());
			obj.skip();
		}
	}
	
	public void generateContent(Content c) {
		for(Entry<String, GeneratorRule> entry : rules.entrySet()) {
			
			String key = entry.getKey();
			GeneratorRule rule = entry.getValue();
			
			if(c.getType().equals(key)) {
				
				rule.setContent(c);
				rule.setObj(obj);
				rule.setConstants(consts);
				rule.setGenerator(this);
				
				List<Object> l = rule.generate();
				c=rule.getContent();
				this.obj=rule.getObj();
				this.consts=rule.getConstants();
				
				FileContext file = Files.open(obj.getFile());
				for(Object o : l) {
					if(o instanceof FileEdit) {
						
						FileEdit fe = (FileEdit)o;
						String f = fe.getFile();
						
						f = ConstUseParser.filter(f, consts);
						fe.setFile(f);
						
						obj.setFile(f);
						file = Files.open(f);
						
					} else {
						
						if(!c.getType().equals("comment")) {
							String s = o.toString();
							s=s.replaceAll("\r", "").replaceAll("\n", "");
							while(s.startsWith("\t"))s=s.replaceFirst("\t", "");
							while(s.startsWith(" "))s=s.replaceFirst(" ", "");
							file.write(ConstUseParser.filter(s, consts));
						}
						else {
							String s = o.toString();
							s=s.replaceAll("\r", "").replaceAll("\n", "");
							while(s.startsWith("\t"))s=s.replaceFirst("\t", "");
							while(s.startsWith(" "))s=s.replaceFirst(" ", "");
							file.write(s);
						}
					}
				}
			}
		}
	}
	
	public List<Object> generate(Content c) {
		return generate(c, consts);
	}
	public List<Object> generate(Content c, HashMap<String,Object> filter) {
		
		List<Object> ret = new LinkedList<Object>();
		
		for(Entry<String, GeneratorRule> entry : rules.entrySet()) {
			
			String key = entry.getKey();
			GeneratorRule rule = entry.getValue();
			
			if(c.getType().equals(key)) {
				
				rule.setContent(c);
				rule.setObj(obj);
				rule.setConstants(filter);
				rule.setGenerator(this);
				
				List<Object> zw = rule.generate();
				
				for(Object o : zw) {
					if(o instanceof String) {
						String s = (String)o;
						s = ConstUseParser.filter(s, filter);
						ret.add(s);
					}
					else if(o instanceof FileEdit) {
						FileEdit fe = (FileEdit)o;
						String s = fe.getFile();
						s = ConstUseParser.filter(s, filter);
						fe.setFile(s);
						ret.add(fe);
						
					}
					else {
						ret.add(o);
					}
				}
			}
		}
		return ret;
	}
}
