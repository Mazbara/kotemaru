package org.kotemaru.apthelper;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.kotemaru.apthelper.annotation.ProcessorGenerate;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.TypeDeclaration;

public class AptHelperProcessor extends ApBase {
	private static final String APT_PATH = "../apt";
	private static final String AP_SUFFIX = "Ap";
	private static final String FACTORY_NAME = "ApFactory";
	private static final String PROCESSOR_VM = "processor.vm";
	private static final String FACTORY_VM = "factory.vm";


	public AptHelperProcessor(AnnotationProcessorEnvironment env) {
		super(env);
	}


	@Override
	public void process() {
		List<TypeDeclaration> list = new ArrayList<TypeDeclaration>();
		for (TypeDeclaration classDecl : environment.getTypeDeclarations())  {
			try {
				boolean isProcess = processClass(classDecl);
				if (isProcess) list.add(classDecl);
			} catch (Exception e)  {
				e.printStackTrace();
			}
		}

		try {
			generateFactory(list);
		} catch (Exception e)  {
			e.printStackTrace();
		}

	}
	protected boolean processClass(TypeDeclaration classDecl) throws Exception {
		ProcessorGenerate pgAnno = classDecl.getAnnotation(ProcessorGenerate.class);
		if(pgAnno == null) return false;

		VelocityContext context = initVelocity();
		context.put("masterClassDecl", classDecl);
		context.put("annotation", pgAnno);
		context.put("helper", new AptUtil(classDecl));

		String pkgName = getPackageName(classDecl, APT_PATH);
		String clsName = classDecl.getSimpleName() + AP_SUFFIX;
		String templ = getResourceName(PROCESSOR_VM);

		applyTemplate(context, pkgName, clsName, templ);
		return true;
	}

	protected boolean generateFactory(List<TypeDeclaration> list) throws Exception {
		if(list.size() == 0) return false;
		TypeDeclaration classDecl = list.get(0);

		VelocityContext context = initVelocity();
		context.put("masterClassDecls", list);
		String pkgName = AptUtil.getPackageName(classDecl, APT_PATH);
		String templ = getResourceName(FACTORY_VM);

		applyTemplate(context,pkgName, FACTORY_NAME, templ);
		return true;
	}

	protected String getResourceName(String name) {
		if (name.startsWith("/")) return name;
		String pkg = this.getClass().getPackage().getName().replace('.', '/');
		return pkg +'/'+name;
	}

}