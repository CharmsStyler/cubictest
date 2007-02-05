/*
 * This software is licensed under the terms of the GNU GENERAL PUBLIC LICENSE
 * Version 2, which can be found at http://www.gnu.org/copyleft/gpl.html
 */
package org.cubictest.exporters.htmlPrototype.delegates;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.cubictest.exporters.htmlPrototype.interfaces.IPageConverter;
import org.cubictest.exporters.htmlPrototype.interfaces.IPageElementConverter;
import org.cubictest.model.Page;
import org.cubictest.model.Transition;
import org.cubictest.model.UserInteractionsTransition;


public class PageConverter implements IPageConverter {
	private IPageElementConverter pageElementConverter;
	private String extension;
	private Map<String, HtmlPageCreator> pages = new HashMap<String, HtmlPageCreator>();
	
	public PageConverter(String extension, IPageElementConverter pageElementConverter) {
		this.pageElementConverter = pageElementConverter;
		this.extension = extension;
	}
	
	public static boolean isNewPageTransition(Transition transition) {
		if(transition instanceof UserInteractionsTransition) {
			return ((UserInteractionsTransition) transition).hasUserInteractions();
		}
		return false;
	}
		
	public HtmlPageCreator convert(Page page, HtmlPageCreator pageStart, Stack<HtmlPageCreator> breadcrumbs, File outFolder, String prefix) {
		if(pageStart == null || isNewPageTransition(page.getInTransition())) {
			File pageFile = new File(outFolder, HtmlPageCreator.filenameFor(page, extension, prefix));
			pageStart = new HtmlPageCreator(pageFile, pageElementConverter, extension, prefix, page.getName());
		}
		
		pageStart.convert(page, breadcrumbs);
		return pageStart;
	}
}