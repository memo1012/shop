package de.shop.util.web;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Model
public class SnoopModel {
	@Inject
	private HttpServletRequest request;
	
	private List<String> headerNames;
	private List<String> parameterNames;
	private List<String> attributeNames;
	private List<String> initParameterNames;

	@PostConstruct
	private void postConstruct() {
		Enumeration<String> e = request.getHeaderNames();
		headerNames = new ArrayList<>();
		if (e != null && e.hasMoreElements()) {
			while (e.hasMoreElements()) {
				headerNames.add(e.nextElement());
			}
		}
		
		e = request.getParameterNames();
		parameterNames = new ArrayList<>();
		if (e != null && e.hasMoreElements()) {
			while (e.hasMoreElements()) {
				parameterNames.add(e.nextElement());
			}
		}
		
		e = request.getAttributeNames();
		attributeNames = new ArrayList<>();
		if (e != null && e.hasMoreElements()) {
			while (e.hasMoreElements()) {
				attributeNames.add(e.nextElement());
			}
		}
		
		e = request.getServletContext().getInitParameterNames();
		initParameterNames = new ArrayList<>();
		if (e != null && e.hasMoreElements()) {
			while (e.hasMoreElements()) {
				initParameterNames.add(e.nextElement());
			}
		}
	}

	public List<String> getHeaderNames() {
		return headerNames;
	}

	public List<String> getParameterNames() {
		return parameterNames;
	}

	public List<String> getAttributeNames() {
		return attributeNames;
	}

	public List<String> getInitParameterNames() {
		return initParameterNames;
	}
}
