package db.annotationprocessing;


import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.tools.Diagnostic.*;
import javax.tools.*;
import java.io.*;
import java.util.*;


@SupportedAnnotationTypes({"javax.persistence.Entity", "to.etc.annotations.GenerateProperties"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
/**
 * Generates QField classes for every Entity annotated class in the project where this processor is selected.
 * Leave the default .apt_generated folder as is.
 *
 * @author <a href="mailto:dennis.bekkering@itris.nl">Dennis Bekkering</a>
 * Created on Feb 3, 2013
 */
public class EntityAnnotationProcessor extends AbstractProcessor {
	static private final String VERSION = "1.0";

	private static final String PRE_FIX = "P";
	public EntityAnnotationProcessor() {
		super();
	}

	@Override
	public boolean process(Set< ? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if(roundEnv.processingOver()) {
			return false;
		}
		final Messager messager = processingEnv.getMessager();
		Set<Element> done = new HashSet<>();
		for(TypeElement ann : annotations) {
			Set< ? extends Element> rootElements = roundEnv.getElementsAnnotatedWith(ann);
			for(Element classElement : rootElements) {
				if(! done.add(classElement))
					continue;

				String pack = processingEnv.getElementUtils().getPackageOf(classElement).getQualifiedName().toString();
				//String cname = classElement.getSimpleName().toString();

				String cname = classElement.asType().toString();
				try {
					generateMainClass(pack, cname, classElement, ann);
					generateRootClass(pack, cname, classElement, ann);
				} catch(Exception e1) {
					e1.printStackTrace();
					messager.printMessage(Kind.ERROR, e1.toString() + " in " + getClass(), classElement);
				}
//				//stupid trick, eclipse will not compile ;(
//				File f = new File(jf.toUri());
//				File dest = new File(f.getAbsolutePath().replace('\\', '/').replace("/.apt_generated/", "/src/"));
//				dest.getParentFile().mkdirs();
//				if(!f.renameTo(dest)) {
//					messager.printMessage(Kind.ERROR, "cannot move file " + f + " to src folder in " + getClass(), classElement);
//				}
//
//				f = new File(jf2.toUri());
//				dest = new File(f.getAbsolutePath().replace('\\', '/').replace("/.apt_generated/", "/src/"));
//				dest.getParentFile().mkdirs();
//				if(!f.renameTo(dest)) {
//					messager.printMessage(Kind.ERROR, "cannot move file " + f + " to src folder in " + getClass(), classElement);
//				}
			}
		}
		return false;
	}

	private void generateRootClass(String pack, String cname, Element classElement, TypeElement ann) throws Exception {
		//ROOT CLASS
		FileObject jf2 = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, pack, PRE_FIX + classElement.getSimpleName() + "Root.java", ann);
		String qname = PRE_FIX + cname;

		try(Writer w = jf2.openWriter()) {
			w.append("package ").append(pack).append(";\n");
			w.append("\n");
			w.append("import to.etc.webapp.query.*;\n");
			w.append("import javax.annotation.*;\n");
			w.append("\n");
			w.append("@Generated(value = { \"Generated by etc.to PropertyAnnotationProcessor " + VERSION + "\" })\n");
			w.append("public final class ");
			w.append(qname);
			w.append("Root extends ");
			w.append(qname);
			w.append("<");
			w.append(qname);
			w.append("Root> {\n\n\t");
			w.append(qname);
			w.append("Root() {\n\t\tsuper(null, null, null);\n\t}\n\n\t");

			w.append("@Nonnull public QCriteria<");
			w.append(cname);
			w.append("> getCriteria() throws Exception {\n\t\t");
			w.append("validateGetCriteria();\n\t\t");
			w.append("return (QCriteria<");
			w.append(cname);
			w.append(">) criteria();\n\t}");

			w.append("\n}");
		}
	}


	private void generateMainClass(String pack, String cname, Element classElement, TypeElement ann) throws Exception {
		FileObject jf = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, pack, PRE_FIX + classElement.getSimpleName() + ".java", ann);

		String qname = PRE_FIX + cname;
		try(Writer w = jf.openWriter()) {
			w.append("package ").append(pack).append(";\n");
			w.append("\n");
			w.append("import to.etc.webapp.query.*;\n");
			w.append("import javax.annotation.*;\n");
			w.append("\n");
			w.append("@Generated(value = { \"Generated by etc.to PropertyAnnotationProcessor " + VERSION + ".\" })\n");
			w.append("public class ");
			w.append(qname);
			w.append("<R extends QField<R, ? >> extends QField<R,");
			w.append(cname);
			w.append("> {\n\n\tpublic ");
			w.append(qname);
			w.append("(@Nullable R root, @Nullable QField<R, ? > parent, @Nullable String name) {");
			w.append("\n\t\tsuper(root, parent, name);\n\t}");

			Element ce = classElement;

			while(ce != null && !ce.toString().equals("java.lang.Object")) {
				final Messager messager = processingEnv.getMessager();

				ElementScanner6 v = new Visitor(w, messager, qname);
				ce.accept(v, null);
				TypeElement asType = (TypeElement) ce;
				DeclaredType sup = (DeclaredType) asType.getSuperclass();
				ce = sup == null ? null : sup.asElement();
			}

			w.append("\n\n\t@Nonnull\n\tpublic static final ");
			w.append(qname);
			w.append("Root get() {\n\t\treturn new ");
			w.append(qname);
			w.append("Root();\n\t}");
			w.append("\n}");
		}
	}


	private final class Visitor extends ElementScanner6 {
		private final Writer m_w;

		private final Messager m_messager;

		private String m_qname;

		private Visitor(Writer w, Messager messager, String qname) {
			m_w = w;
			m_messager = messager;
			m_qname = qname;
		}

		@Override
		public Object visitExecutable(ExecutableElement m, Object p) {
			try {
				List< ? extends AnnotationMirror> annotationMirrors = m.getAnnotationMirrors();
				for(AnnotationMirror a : annotationMirrors) {
					Name annName = a.getAnnotationType().asElement().getSimpleName();
					if(annName.toString().equals("Column")) {
						String mname = m.getSimpleName().toString();
						if(mname.startsWith("is")) {
							mname = Character.toLowerCase(mname.charAt(2)) + mname.substring(3);
						} else {
							mname = Character.toLowerCase(mname.charAt(3)) + mname.substring(4);
						}
						TypeMirror returnType = m.getReturnType();
						String mtypeName;
						if(returnType instanceof PrimitiveType) {
							String retStr = returnType.toString();
							if(retStr.equals("int") || retStr.equals("short")) {
								retStr = "long";
							}
							mtypeName = Character.toUpperCase(retStr.charAt(0)) + retStr.substring(1);
							String pname = mname;
							mname = replaceReserved(mname);
							m_w.append("\n\n\t@Nonnull\n\tpublic final QField");
							m_w.append(mtypeName);
							m_w.append("<R> ");
							m_w.append(mname);
							m_w.append("() {\n\t\treturn new QField");
							m_w.append(mtypeName);
							m_w.append("<R>(new QField<R, ");
							m_w.append(retStr);
							m_w.append("[]>(m_root, this, \"");
							m_w.append(pname);
							m_w.append("\"));\n\t}");

						} else {
							Element mtype = processingEnv.getTypeUtils().asElement(returnType);

							//mtypeName = mtype.getSimpleName().toString();
							mtypeName = m.getReturnType().toString();

							String pname = mname;
							mname = replaceReserved(mname);
							m_w.append("\n\n\t@Nonnull\n\tpublic final QField<R,");
							m_w.append(mtypeName);
							m_w.append("> ");
							m_w.append(mname);
							m_w.append("() {\n\t\treturn new QField<R,");
							m_w.append(mtypeName);
							m_w.append(">(m_root, this, \"");
							m_w.append(pname);
							m_w.append("\");\n\t}");

						}

					} else if(annName.toString().equals("ManyToOne")) {
						String mname = m.getSimpleName().toString();
						mname = Character.toLowerCase(mname.charAt(3)) + mname.substring(4);
						Element mtype = processingEnv.getTypeUtils().asElement(m.getReturnType());
						String qtype = packName(m.getReturnType().toString()) + "." + PRE_FIX + mtype.getSimpleName().toString();

						String pname = mname;
						mname = replaceReserved(mname);
						m_w.append("\n\n\t@Nonnull\n\tpublic final ");
						m_w.append(qtype);
						m_w.append("<R> ");
						m_w.append(mname);
						m_w.append("() {\n\t\treturn new ");
						m_w.append(qtype);
						m_w.append("<R>(m_root, this, \"");
						m_w.append(pname);
						m_w.append("\");\n\t}");
					} else if(annName.toString().equals("OneToMany")) {
						String mname = m.getSimpleName().toString();
						mname = Character.toLowerCase(mname.charAt(3)) + mname.substring(4);
						Element mtype = processingEnv.getTypeUtils().asElement(m.getReturnType());
						DeclaredType rtype = (DeclaredType) m.getReturnType();

						DeclaredType rtypeArg = (DeclaredType) rtype.getTypeArguments().iterator().next();
						String rtypeArgName = rtypeArg.asElement().getSimpleName().toString();
						String frtypeArgName = rtypeArg.toString();

						String pack = packName(frtypeArgName);

						String qrtypeArgName = pack + "." + PRE_FIX + rtypeArgName;

						String pname = mname;
						mname = replaceReserved(mname);
						m_w.append("\n\n\t@Nonnull\n\tpublic final QList<R,");
						m_w.append(qrtypeArgName);
						m_w.append("Root> ");
						m_w.append(mname);
						m_w.append("() throws Exception {\n\t\treturn new QList<R,");
						m_w.append(qrtypeArgName);
						m_w.append("Root>(");
						m_w.append(qrtypeArgName);
						m_w.append(".get(), this,\"");
						m_w.append(pname);
						m_w.append("\");\n\t}");
					}


					if(annName.toString().equals("ManyToOne") || annName.toString().equals("Column")) {
						String mname = m.getSimpleName().toString();
						if(mname.startsWith("is")) {
							mname = Character.toLowerCase(mname.charAt(2)) + mname.substring(3);
						} else {
							mname = Character.toLowerCase(mname.charAt(3)) + mname.substring(4);
						}
						TypeMirror returnType = m.getReturnType();

						String mtypeName;
						if(returnType instanceof PrimitiveType) {
							mtypeName = returnType.toString();
							if(mtypeName.equals("int") || mtypeName.equals("short")) {
								mtypeName = "long";
							}

						} else {
							Element mtype = processingEnv.getTypeUtils().asElement(returnType);
							//mtypeName = mtype.getSimpleName().toString();
							mtypeName = m.getReturnType().toString();
						}

						mname = replaceReserved(mname);

						m_w.append("\n\n\t/**\n\t");
						m_w.append(" * Shortcut eq\n\t");
						m_w.append(" * @param ");
						m_w.append(mname);
						m_w.append("\n\t");
						m_w.append(" * @return\n\t");
						m_w.append(" */\n\t@Nonnull\n\tpublic final R ");
						m_w.append(mname);
						m_w.append("(@Nonnull ");
						m_w.append(mtypeName);
						m_w.append("... ");
						m_w.append(mname);
						m_w.append(") {\n\t\treturn ");
						m_w.append(mname);
						m_w.append("().eq(");
						m_w.append(mname);
						m_w.append(");\n\t}");


					}


				}


			} catch(Exception e1) {
				e1.printStackTrace();
				try {
					m_w.append(e1.toString());
				} catch(IOException ioe) {
					e1.printStackTrace();
				}
				m_messager.printMessage(Kind.ERROR, e1.toString() + " in " + getClass(), m);

			}
			return super.visitExecutable(m, p);
		}

		private String packName(String frtypeArgName) {
			int dotIndex = frtypeArgName.lastIndexOf('.');
			String pack = "";
			if(dotIndex != -1) {
				pack = frtypeArgName.substring(0, dotIndex);
			}
			return pack;
		}
	}

	String replaceReserved(String s) {
		if(m_reserved.contains(s)) {
			return s + "_";
		}
		return s;
	}

	static final Set<String> m_reserved = new HashSet<String>();
	static {
		m_reserved.add("abstract");
		m_reserved.add("assert");
		m_reserved.add("boolean ");
		m_reserved.add("break ");
		m_reserved.add("byte");
		m_reserved.add("case");
		m_reserved.add("catch");
		m_reserved.add("char");
		m_reserved.add("class");
		m_reserved.add("const");
		m_reserved.add("continue");
		m_reserved.add("default");
		m_reserved.add("double");
		m_reserved.add("do");
		m_reserved.add("else");
		m_reserved.add("enum");
		m_reserved.add("extends");
		m_reserved.add("false");
		m_reserved.add("final");
		m_reserved.add("finally");
		m_reserved.add("float");
		m_reserved.add("for");
		m_reserved.add("goto");
		m_reserved.add("if");
		m_reserved.add("implements");
		m_reserved.add("import");
		m_reserved.add("instanceof");
		m_reserved.add("int");
		m_reserved.add("interface");
		m_reserved.add("long");
		m_reserved.add("native");
		m_reserved.add("new");
		m_reserved.add("null");
		m_reserved.add("package");
		m_reserved.add("private");
		m_reserved.add("protected");
		m_reserved.add("public");
		m_reserved.add("return");
		m_reserved.add("short");
		m_reserved.add("static");
		m_reserved.add("strictfp");
		m_reserved.add("super");
		m_reserved.add("switch");
		m_reserved.add("synchronized");
		m_reserved.add("this");
		m_reserved.add("throw");
		m_reserved.add("throws");
		m_reserved.add("transient");
		m_reserved.add("true");
		m_reserved.add("try");
		m_reserved.add("void");
		m_reserved.add("volatile");
		m_reserved.add("while");

	}
}
