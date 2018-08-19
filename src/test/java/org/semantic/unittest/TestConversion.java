package org.semantic.unittest;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.hl7rdf.HL7Converter;
import org.junit.Test;

import ca.uhn.hl7v2.HL7Exception;

public class TestConversion {

	@Test
	public void test() {
		try {
			String message;

			org.apache.jena.rdf.model.Model model = ModelFactory.createDefaultModel();
			HL7Converter hl7Segments = new HL7Converter(model);
	
			message = "MSH|^~\\&|ADT1|MCM|LABADT|MCM|198808181126|SECURITY|ADT^A01|MSG00001-|P|2.5.1\r\n"
					+ "EVN|A01|198808181123\r\n"
					+ "PID|||PATID1231^5^M11||JONES^WILLIAM^A^III||19610615|M-||C|1200 N ELM STREET^^GREENSBORO^NC^27401-1020|GL|(91-9)379-1212|(919)271-3434||S||PATID12345001^2^M10|123456789|9-87654^NC\r\n"
					+ "NK1|1|JONES^BARBARA^K|WIFE||||||NK\r\n"
					+ "PV1|1|I|2000^2012^01||||004777^LEBAUER^SIDNEY^J.|||SUR||-||ADM|A0-\r\n"
					+ "AL1|||^Cat dander|Respiratory distress\r\n"
					+ "ORC|NW|987654321^EPC|123456789^EPC||||||20161003000000|||SMITH\r\n"
					+ "OBR|1|341856649^HNAM_ORDERID|000000000000000000|648088^Basic Metabolic Panel|||20150101000000|||||||||1620^Johnson^Corey^A||||||20150101000000|||F|||||||||||20150101000000|\r\n"
					+ "OBX|1|NM|GLU^Glucose Lvl|59|mg/dL|65-99^65^99|L|||F|||20150102000000|\r\n"
					+ "DG1|1||78900^ABDMNAL PAIN UNSPCF SITE^I9CDX|||W\r\n"
					+ "DG1|3||1488000^Postoperative nausea and vomiting^SCT|||W\r\n";

			//+ "OBX|4|CE|57131-5^Newborn conditions with positive markers [Identifier] in Dried blood spot^LN|1|LA12509-8^MCAD^LN^128596003^Medium-chain acyl-coenzyme A dehydrogenase deficiency^SCT|||A|||F\r\n";
			hl7Segments.ConvertToRDF(message);

		
			
			message = "MSH|^~\\&|REGADT|GOOD HEALTH HOSPITAL|GHH LAB||200712311501||ADT^A04^ADT_A01|000001|P|2.6|||\r\n"
					+ "EVN|A04|200701101500|200701101400|01||200701101410\r\n"
					+ "PID|||191919^^^GOOD HEALTH HOSPITAL^MR^GOOD HEALTH HOSPITAL^^^USSSA^SS|253763|EVERYMAN^ADAM^A||19560129|M|||2222 HOME STREET^^ISHPEMING^MI^49849^\"\"^||555-555-2004|555-555- 2004||S|CHR|10199925^^^GOOD HEALTH HOSPITAL^AN|371-66-9256||\r\n"
					+ "NK1|1|NUCLEAR^NELDA|SPO|6666 HOME STREET^^ISHPEMING^MI^49849^\"\"^|555-555-5001|555-555-5001~555-555-5001|C^FIRST EMERGENCY CONTACT\r\n"
					+ "NK1|2|MUM^MARTHA|MTH|4444 HOME STREET^^ISHPEMING^MI^49849^\"\"^|555-555 2006|555-555-2006~555-555-2006|C^SECOND EMERGENCY CONTACT\r\n"
					+ "NK1|3\r\n"
					+ "NK1|4|||6666 WORKER LOOP^^ISHPEMING^MI^49849^\"\"^||(900)545-1200|E^EMPLOYER|19940605||PROGRAMMER|||WORK IS FUN, INC.\r\n"
					+ "PV1||O|O/R||||0148^ATTEND^AARON^A|0148^ATTEND^AARON^A|0148^ATTEND^AARON^A|MED|||||||0148^ATTEND^AARON^A|S|1400|A||||||||||||||||||||||||199501101410|\r\n"
					+ "PV2||||||||200701101400||||||||||||||||||||||||||N\r\n"
					+ "OBX||ST|1010.1^BODY WEIGHT||62|kg|||||F\r\n" + "OBX||ST|1010.1^HEIGHT||190|cm|||||F\r\n"
					+ "DG1|1|19||BIOPSY||A|\r\n"
					+ "GT1|1||EVERYMAN^ADAM^A||2222 HOME STREET^^ISHPEMING^MI^49849^\"\"^|444-33 3333|555-555-2004||||SEL^SELF|444-33 3333||||AUTO CLINIC|2222 HOME STREET^^ISHPEMING^MI^49849^\"\"|555-555-2004|\r\n"
					+ "IN1|0|0|UA1|UARE INSURED, INC.|8888 INSURERS CIRCLE^^ISHPEMING^M149849^\"\"^||555-555-3015|90||||||50 OK|\r\n"
					+ "DG1|1||236084000^Chemotherapy-induced nausea and vomiting^SCT|||W\r\n";

			hl7Segments.ConvertToRDF( message);
		
			ResultSet results = applyQuery(model);
		    assertEquals(3, results.getRowNumber());

			
		} catch (HL7Exception | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	 static ResultSet applyQuery(Model model) {
		String queryString = "PREFIX hl7: <http://www.HL7.org/segment#> "
				+ "SELECT DISTINCT ?patient ?GivenName ?FamilyName ?diagnosis " 
				+ "WHERE {"
				+ "?PatientName hl7:GivenName ?GivenName ." 
				+ "?PatientName hl7:FamilyName ?FamilyName ."
				+ "?PID hl7:PatientName ?PatientName . " 
				+ "?patient hl7:PID ?PID ." + "?patient hl7:DG1 ?DG1 . "
				+ "?DG1 hl7:DiagnosisCodeDG1  ?Identifier1." 
				+ "?Identifier1 hl7:Text ?diagnosis " + "      }";

		// + "?AL1 hl7:AllergySeverity \"Respiratory distress\" "
		// + "?AL1 hl7:AllergyReaction \"Produces hives\" "

		Query query = QueryFactory.create(queryString);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();

		// Output query results
		ResultSetFormatter.out(System.out, results, query);
		return results;
		}


}
