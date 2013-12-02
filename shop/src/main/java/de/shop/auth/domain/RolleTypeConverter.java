package de.shop.auth.domain;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;


/**
 * @author <a href="mailto:Juergen.Zimmermann@HS-Karlsruhe.de">J&uuml;rgen Zimmermann</a>
 */
@Converter(autoApply = true)
@FacesConverter(forClass = RolleType.class)
public class RolleTypeConverter implements AttributeConverter<RolleType, String>, javax.faces.convert.Converter {
	@Override
	public String convertToDatabaseColumn(RolleType rolleType) {
		if (rolleType == null) {
			return null;
		}
		return rolleType.getInternal();
	}

	@Override
	public RolleType convertToEntityAttribute(String internal) {
		return RolleType.build(internal);
	}

	@Override
	public String getAsString(FacesContext ctx, UIComponent comp, Object obj) {
		if (obj == null)
			return "";
		
		return RolleType.class
				        .cast(obj)
				        .getInternal();
	}
	
	@Override
	public Object getAsObject(FacesContext ctx, UIComponent comp, String str) {
		return RolleType.build(str);
	}
}
