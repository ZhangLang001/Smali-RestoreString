package Smali;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Smali.SmaliClass.SmaliType;
import Smali.SmaliModifier.ModifierAttribute;
import Smali.SmaliModifier.ModifierPremission;
import Smali.SmaliModifier.ModifierType;

public class SmaliClass extends SmaliObject{
	private final String ClassName;
	private final ArrayList<SmaliField> fields;
	private final ArrayList<SmaliMethod> methods;
	private File classFile;
	private BufferedReader reader;
	private BufferedWriter writer;
	private ArrayList<String> codeLines;
	
	private static final ArrayList<SmaliClass> ClassTable = new ArrayList<SmaliClass>();
	
	public static class SmaliType extends SmaliClass{
		private boolean isArr;
		private int arrNum;
		

		public SmaliType(ModifierPremission premission,ArrayList<ModifierAttribute> attributes,ArrayList<SmaliField> fields,ArrayList<SmaliMethod> methods,String name,boolean isArr,int arrNum){
			super(premission,attributes,fields,methods,name,null);
			this.isArr = isArr;
			this.arrNum = arrNum;
		}

		public boolean isArr() {
			return isArr;
		}

		public void setArr(boolean isArr) {
			this.isArr = isArr;
		}
		
		public int getArrNum() {
			return arrNum;
		}

		public void setArrNum(int arrNum) {
			this.arrNum = arrNum;
		}


		public static final SmaliType V = new SmaliType(ModifierPremission.PREMISSION_PUBLIC,null,null,null,"V",false,0);
		public static final SmaliType Z = new SmaliType(ModifierPremission.PREMISSION_PUBLIC,null,null,null,"Z",false,0);
		public static final SmaliType I = new SmaliType(ModifierPremission.PREMISSION_PUBLIC,null,null,null,"I",false,0);
		public static final SmaliType B = new SmaliType(ModifierPremission.PREMISSION_PUBLIC,null,null,null,"B",false,0);
		public static final SmaliType S = new SmaliType(ModifierPremission.PREMISSION_PUBLIC,null,null,null,"S",false,0);
		public static final SmaliType C = new SmaliType(ModifierPremission.PREMISSION_PUBLIC,null,null,null,"C",false,0);
		public static final SmaliType J = new SmaliType(ModifierPremission.PREMISSION_PUBLIC,null,null,null,"J",false,0);
		public static final SmaliType F = new SmaliType(ModifierPremission.PREMISSION_PUBLIC,null,null,null,"F",false,0);
		public static final SmaliType D = new SmaliType(ModifierPremission.PREMISSION_PUBLIC,null,null,null,"D",false,0);
		
		public char getChar(){
			return toString().charAt(0);
		}
		
		public static SmaliType GetBaseType(char c){
			if(c == V.getChar()){
				return V;
			}
			else if(c == Z.getChar()){
				return Z;
			}
			else if(c == I.getChar()){
				return I;
			}
			else if(c == B.getChar()){
				return B;
			}
			else if(c == S.getChar()){
				return S;
			}
			else if(c == C.getChar()){
				return C;
			}
			else if(c == J.getChar()){
				return J;
			}
			else if(c == F.getChar()){
				return F;
			}
			else if(c == D.getChar()){
				return D;
			}
			else{
				return null;
			}
		}
	}

	private SmaliClass(ModifierPremission premission,ArrayList<ModifierAttribute> attributes,ArrayList<SmaliField> fields,ArrayList<SmaliMethod> methods,String name,File classFile){
		super(premission,attributes,0);
		this.ClassName = name;
		this.fields = fields;
		this.methods = methods;
		this.classFile = classFile;
	}
	
	public static SmaliClass PraseClass(File file){
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			System.out.println("[*PraseClassError]FileNotFoundException: " + e.toString());
			return null;
		}
		String line;
		String className = "";
		int lineInFile = 0;
		ModifierPremission premission = ModifierPremission.PREMISSION_DEFAULT;
		ArrayList<ModifierAttribute> attributes = new ArrayList<ModifierAttribute>();
		ArrayList<SmaliField> fields = new ArrayList<SmaliField>();
		ArrayList<SmaliMethod> methods = new ArrayList<SmaliMethod>();
		try {
			while((line = reader.readLine()) != null){
				line = line.trim();
				if(line.startsWith(ModifierType.TYPE_CLASS.toString())){
					String[] sp = line.split(" ");  //sp[] = {".class",Premission,{Attributes},ClassName}
					premission = ModifierPremission.Get(sp[1]);
					for(int i = 2;i < sp.length - 1;i++){ 
						attributes.add(ModifierAttribute.Get(sp[i]));
					}
					className = sp[sp.length-1];
				}
				/*
				if(line.startsWith(SmaliModifier.ModifierType.TYPE_FIELD.getModifierText())){
					String[] sp = line.split(" ");
					SmaliModifier.ModifierPremission fieldPremission = SmaliModifier.ModifierPremission.Get(sp[0]);
					ArrayList<SmaliModifier.ModifierAttribute> fieldAttributes = new ArrayList<SmaliModifier.ModifierAttribute>();
					for(int i = 2;i < sp.length - 1 ;i++){
						fieldAttributes.add(SmaliModifier.ModifierAttribute.Get(sp[i]));
					}
					String sp1[] = sp[sp.length-1].split(":");
					SmaliField field = new SmaliField();
				}*/
				
				if(line.startsWith(ModifierType.TYPE_METHOD.toString())){
					String[] sp = line.split(" ");	//sp[] = {".method",Premission,{Attributes},MethodName+MethodSig}
					ModifierPremission methodPremission= ModifierPremission.Get(sp[1]);
					ArrayList<ModifierAttribute> methodAttributes = new ArrayList<ModifierAttribute>();
					for(int i = 2;i < sp.length - 1 ;i++){
						methodAttributes.add(ModifierAttribute.Get(sp[i]));
					}
					String[] sp1 = sp[sp.length-1].split("\\(");
					String methodName = sp1[0];
					ArrayList<String> args = SmaliMethod.PraseMethodArgs(sp1[1].split("\\)")[0]);
					String returnType  = sp1[1].split("\\)")[1];
					SmaliMethod method = new SmaliMethod(methodPremission,methodAttributes,args,returnType,className,methodName,lineInFile);
					methods.add(method);
				}
				lineInFile++;
			}
			reader.close();
			SmaliClass cls = new SmaliClass(premission,attributes,fields,methods,className,file);
			SmaliClass.ClassTable.add(cls);
			return cls;
		} catch (IOException e) {
			System.out.println("[*PraseClassError]IOException: " + e.toString());
			return null;
		}
	}
	
	public String toString(){
		return ClassName;
	}

	public boolean equals(SmaliClass cls){
		if(cls == null){
			return false;
		}
		return cls.toString() == this.toString();
	}

	public ArrayList<SmaliField> getFields(){
		return fields;
	}

	public BufferedWriter getWriter(){
		if(writer == null){
			if(classFile == null){
				return null;
			}
			try {
				writer = new BufferedWriter(new FileWriter(classFile));
			} catch (IOException e) {
				System.out.println("[*E] FileNotFound:" + e.toString());
			}
		}
		return writer;
	}

	public ArrayList<String> getCodes(){
		if(codeLines == null){
			if(reader == null ){
				if(classFile == null){
					return null;
				}
				try {
					reader = new BufferedReader(new FileReader(classFile));
				} catch (FileNotFoundException e) {
					System.out.println("[*GetCodesError] FileNotFound: " + e.toString());
					return null;
				}
			}
			ArrayList<String> codes = new ArrayList<String>();
			String tmp;
			try {
				while((tmp = reader.readLine()) != null){
					codes.add(tmp);
				}
				reader.close();
			} catch (IOException e) {
				System.out.println("[*GetCodesError] IOExceotion: " + e.toString());
				return null;
			}
			
			codeLines = codes;
		}
		return codeLines;
	}

	public ArrayList<SmaliMethod> getMethods(){
		return methods;
	}

	public File getClassFile(){
		return classFile;
	}

	public ArrayList<SmaliMethod> getInvokeMethodList(){
		ArrayList<SmaliMethod> list = new ArrayList<SmaliMethod>();
		for(String line : getCodes()){
			line = line.trim();
			String[] sp = line.split(" ");
			if(sp[0].indexOf("invoke") != -1){
				Pattern pattern = Pattern.compile("([a-zA-Z0-9/_$]{0,128});->([0-9a-zA-Z_$]{0,128})\\(([a-zA-Z0-9/_$;]{0,512})\\)([a-zA-Z0-9/_$;]{0,128})");
				Matcher matcher = pattern.matcher(line);
				if(matcher.find()){
					String superClass = matcher.group(1);
					String returnType;
					SmaliMethod method;
					ArrayList<String> args = SmaliMethod.PraseMethodArgs(matcher.group(3));
					returnType = matcher.group(4);
					method = new SmaliMethod(ModifierPremission.PREMISSION_DEFAULT,null,args,returnType,superClass,matcher.group(2),-1);
					list.add(method);
				}
			}
		}
		return list;
	}

	public int addMethod(SmaliMethod method,String smaliCode){
		if(method == null){
			return -1;
		}
		method.setLineInFile(getCodes().size()+2);
		StringBuilder builder = new StringBuilder();
		ArrayList<String> codeLines = getCodes();
		codeLines.add("");
		builder.append(SmaliModifier.ModifierType.TYPE_METHOD+" ");
		builder.append(method.getPremission().toString()+" ");
		if(method.getAttributes() != null && method.getAttributes().size() != 0){
			for(SmaliModifier modifier : method.getAttributes()){
				builder.append(modifier + " ");
			}
		}
		builder.append(method.getMethodName()+"(");
		if(method.getArgs() != null){
			for(String type : method.getArgs()){
				builder.append(type.toString());
			}
		}
		builder.append(")");
		builder.append(method.getReturnTypeName());
		codeLines.add(builder.toString());
		String[] smaliLines = smaliCode.split("\r\n");
		for(String line : smaliLines){
			codeLines.add(line);
		}
		codeLines.add(".end method");
		this.codeLines = codeLines;
		method.setSuperClass(this.toString());
		methods.add(method);
		
		return 0;
		
	}

	public int saveChange(){
		try {
			if(writer != null){
				writer.close();
				writer = null;
			}
			if(this.codeLines == null){
				return 0;
			}
			writer = getWriter();
			for(String tmp : getCodes()){
				writer.write(tmp+"\r\n");
			}
			writer.flush();
			cleanMem();
			return 0;
		} catch (IOException e) {
			System.out.println("[*SaveChangeError: ]" +e.toString());
			return -1;
		}
	}

	public void cleanMem(){
		if(reader != null){
			try {
				reader.close();
			} catch (IOException e) {
			}
		}
		if(writer != null){
			try {
				writer.close();
			} catch (IOException e) {
			}
			
		}
		codeLines = null;
		reader = null;
		writer = null;
	}

	public SmaliMethod findMethod(SmaliMethod method){
		for(SmaliMethod m : methods){
			if(m.equals(method)){
				return m;
			}
		}
		return null;
	}

	public static SmaliClass FindClass(String cls){
		if(cls == null || cls.isEmpty()){
			return null;
		}
		if(cls.length() ==  1 ){
			return SmaliType.GetBaseType(cls.charAt(0));
		}
		for(SmaliClass sc : ClassTable){
			if(sc.equals(cls)){
				return sc;
			}
		}
		return null;
	}

	public boolean isInterface(){
		if(getAttributes().indexOf(ModifierAttribute.ATTRIBUTE_INTERFACE) != -1){
			return true;
		}
		return false;
	}
	
	public boolean isAbstract(){
		if(getAttributes().indexOf(ModifierAttribute.ATTRIBUTE_ABSTRACT) != -1){
			return true;
		}
		return false;
	}

	public boolean equals(Object cls){
		return cls.toString().equals(this.toString());
	}
}
