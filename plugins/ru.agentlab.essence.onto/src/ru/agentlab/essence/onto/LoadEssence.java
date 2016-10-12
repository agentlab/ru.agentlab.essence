package ru.agentlab.essence.onto;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import org.powerloom.service.PowerLoomService;

import edu.isi.powerloom.PLI;
import edu.isi.powerloom.PlIterator;
import edu.isi.powerloom.logic.LogicObject;
import edu.isi.stella.Cons;
import edu.isi.stella.InputStream;
import edu.isi.stella.InputStringStream;
import edu.isi.stella.Module;
import edu.isi.stella.SExpressionIterator;
import edu.isi.stella.Stella;
import edu.isi.stella.Stella_Object;

public class LoadEssence {
	public static final String CONCEPT_NAME = "Concept";

	public static void main(String[] args) {
		PowerLoomService pls = PowerLoomService.getInstance();

		try {
			pls.loadPlm("Essence-Lang.plm");
			pls.loadPlm("System-Essence-Kernel.plm");
			pls.loadPlm("Representation.plm");
			pls.loadPlm("Essence-Representation.plm");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Module systemEssenceKernelModule = PLI.getModule("SYSTEM-ESSENCE-KERNEL", null);
		Cons query = parseQuery("all ?y (and (AreaOfConcern ?x) (areaOfConcernContainsElement ?x ?y))");
		execAndPrint(query, systemEssenceKernelModule);

		Module essenceRepresentationModule = PLI.getModule("ESSENCE-REPRESENTATION", null);
		Cons query2 = parseQuery("all (?c ?url) (and (Image ?i) (imageHasUrl ?i ?url) (classHasImage ?c ?i))");
		execAndPrint(query2, essenceRepresentationModule);
		Cons query4 = parseQuery("all ?r (isAContainmentRelation ?r)");
		execAndPrint(query4, essenceRepresentationModule);

		Module plKernelModule = PLI.getModule("PL-KERNEL", null);
		Cons query3 = parseQuery("all ?x (Concept ?x)");
		execAndPrint(query3, plKernelModule);

	    System.out.println("\nAll the instances of " + CONCEPT_NAME);

	    LogicObject concept = PLI.getConcept(CONCEPT_NAME, systemEssenceKernelModule, null);
		PlIterator iter = PLI.getConceptInstances(concept, systemEssenceKernelModule, null);
		while (iter.nextP()) {
			String s = PLI.getNthString(iter, 0, null, null);
			int index = s.lastIndexOf("/");
			String trim = s.substring(index + 1);
			if (trim.contains("|")) {
				trim = trim.replaceAll("\\|", "");
			}
			LogicObject sd = PLI.getConcept(trim, systemEssenceKernelModule, null);

			System.out.println(trim + " " + systemEssenceKernelModule + " " + sd);
//			if (PLI.getHomeModule(sd).equals(module)){
//				l.add(trim);
//				editorPane.paletteViewer.addElement(trim);
//			}
		}
		System.out.println("\nLoadEssence terminated succesfully!!!");
	}

	private static void execAndPrint(Cons query, Module module) {
		PlIterator answer = PLI.retrieve(query, module, null);
	    System.out.println("\nAnswers to query `" + query + "'");
	    while (answer.nextP()) {
	      System.out.println(answer.value/* + " " + ((Cons)answer.value).rest.value*/);
	    }
	}

	public static Cons parseQuery(String query) {
		Cons queryform = Stella.NIL;
		Stella_Object sexp = null;
		Cons cons = null;
		SExpressionIterator iter = InputStream.sExpressions(InputStringStream.newInputStringStream(query));

		while (iter.nextP()) {
			sexp = iter.value;
			if (cons == null) {
				cons = Cons.cons(sexp, Stella.NIL);
				if (queryform == Stella.NIL) {
					queryform = cons;
				} else {
					Cons.addConsToEndOfConsList(queryform, cons);
				}
			} else {
				cons.rest = Cons.cons(sexp, Stella.NIL);
				cons = cons.rest;
			}
		}
		return queryform;
	}
}
