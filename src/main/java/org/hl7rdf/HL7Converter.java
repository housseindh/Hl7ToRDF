package org.hl7rdf;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Composite;
import ca.uhn.hl7v2.model.Group;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.Visitable;
import ca.uhn.hl7v2.model.v251.segment.PID;
import ca.uhn.hl7v2.parser.DefaultEscaping;
import ca.uhn.hl7v2.parser.EncodingCharacters;
import ca.uhn.hl7v2.parser.Escaping;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.ValidationContext;
import ca.uhn.hl7v2.validation.impl.ValidationContextFactory;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class HL7Converter {


    public HL7Converter(Model rdfModel) {
		super();
		this.model = rdfModel;
		hapiContext = new DefaultHapiContext();
		hapiContext.setValidationContext((ValidationContext) ValidationContextFactory.noValidation());

	}

	private static final Escaping HL7_ESCAPING = new DefaultEscaping();
    private static final String HL7_URI = "http://www.HL7.org/segment#";
    private static final Pattern p = Pattern.compile("^(cm_msg|[a-z][a-z][a-z]?)([0-9]+)_(\\w+)$");
    private  HapiContext hapiContext;
    
    private  Model model;
    private  Resource mainRourseresouce;
    public  void ConvertToRDF(final String hl7Text) throws HL7Exception, IOException {
    	
		final PipeParser parser = hapiContext.getPipeParser();
		Message group;
		group = parser.parse(hl7Text);
		String uuid = findPID(group);
		mainRourseresouce = model.getResource(HL7_URI + uuid);
		
		createSegments(group);
       // hapiContext.close();
    }

    private  String findPID(final Group group) throws HL7Exception {
    	try {
    		Segment pid =(Segment) group.getAll("PID")[0];
    		Map<String,Type> segmentPid= createFields(pid);
    		Map<String, Type> componentId = createComponents(segmentPid.get("PatientIdentifierList"));
    		
    		
			return componentId.get("IDNumber").toString();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
    	
        if (!isEmpty(group)) {
            for (final String name : group.getNames()) {
                for (final Structure structure : group.getAll(name)) {
                    if (group.isGroup(name) && structure instanceof Group) {
                    	String id = findPID((Group)structure);
                    	if (! id.equals(""))
                    		return id;
                    
                }
               
            }
         }
    }
        return "";
    } 
    private  void createSegments(final Group group) throws HL7Exception {
        if (!isEmpty(group)) {
            for (final String name : group.getNames()) {
                for (final Structure structure : group.getAll(name)) {
                    if (group.isGroup(name) && structure instanceof Group) {
                    	createSegments((Group) structure);
                    } else if (structure instanceof Segment) {
                        createSegment((Segment) structure);
                    }
                }
               
            }
        }
    }

    private  void createSegment(final Segment segment) throws HL7Exception {
        if (!isEmpty(segment)) {
            final String segmentName = segment.getName();
            final StringBuilder sb = new StringBuilder().append(segmentName);
            final String segmentKey = sb.toString();
            convertHL7ToRDF(model, segmentKey, segment);
        }
    }

    	 
    private  Property createProperty(Model model, String s) {
		Property property = model.createProperty(s);
		return property;
	}
	
    private  void convertHL7ToRDF(Model model, String segmentKey, Segment segment) throws HL7Exception {
        	
        
               Resource segRourseresouce = model.createResource();
				mainRourseresouce.addProperty(createProperty(model, HL7_URI + segmentKey), segRourseresouce);

                //System.out.println("segmentKey:" + segmentKey );//+ " | segment:" + segment);
                
                final Map<String, Type> fields = createFields(segment);                
                for (final Map.Entry<String, Type> fieldEntry : fields.entrySet()) {
                    final String fieldKey = fieldEntry.getKey();
                    final Type field = fieldEntry.getValue();
                    Map<String,Type> mapComponents =  createComponents(field);

                    if (mapComponents.size()== 0){  
                    		//System.out.println("fieldKey:" + fieldKey + " | field:" + field);
							segRourseresouce.addProperty(createProperty(model, HL7_URI + fieldKey), field.toString());

                    }else{
                    	//System.out.println("fieldKey:" + fieldKey);
                    	Resource componentResource = model.createResource();
                    

                        for (final Map.Entry<String, Type> componentEntry : mapComponents.entrySet()) {
                            final String componentKey = componentEntry.getKey();
                            final Type component = componentEntry.getValue();
							
                            final String componentValue = HL7_ESCAPING.unescape(component.encode(), EncodingCharacters.defaultInstance());

							segRourseresouce.addProperty(createProperty(model, HL7_URI + fieldKey), componentResource
									.addProperty(createProperty(model, HL7_URI + componentKey), componentValue));
							
                            //System.out.println("componentKey:" + componentKey + " | component:" + componentValue);
                            
                            
                        }
                   }

                }
            
        
		//model.write(System.out, "N-TRIPLES");

    }


    private  Map<String, Type> createFields(final Segment segment) throws HL7Exception {
        final Map<String, Type> fields = new TreeMap<>();
        final String[] segmentNames = segment.getNames();
        for (int i = 1; i <= segment.numFields(); i++) {
            final Type field = segment.getField(i, 0);
            if (!isEmpty(field)) {
                final String fieldName;
                fieldName = WordUtils.capitalize(segmentNames[i-1]).replaceAll("\\W+", "");
               
                fields.put(fieldName, field);
            }
        }
        return fields;
    }

    private  Map<String, Type> createComponents(final Type field) throws HL7Exception {
        final Map<String, Type> components = new TreeMap<>();
        if (!isEmpty(field) && (field instanceof Composite)) {
           
                try {
                    final java.beans.PropertyDescriptor[] properties = PropertyUtils.getPropertyDescriptors(field);
                    for (final java.beans.PropertyDescriptor property : properties) {
                        final String name = property.getName();
                        final Matcher matcher = p.matcher(name);
                        if (matcher.find()) {
                            final Type type = (Type) PropertyUtils.getProperty(field, name);
                            if (!isEmpty(type)) {
                                final String componentName = matcher.group(3);
                               
                                components.put(componentName, type);
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            
        }
        return components;
    }

    private  boolean isEmpty(final Visitable vis) throws HL7Exception {
        return (vis == null || vis.isEmpty());
    }


}
