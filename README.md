# Hl7ToRDF-Healthcare data converter.

Hl7ToRDF Data Converter: This library converts hl7 v2.x message to RDF format.
Here is an example of a SPARQL query that can be applied in converted data.

PREFIX hl7: <http://www.HL7.org/segment#> 
		SELECT DISTINCT ?patient ?GivenName ?FamilyName ?diagnosis 
		 WHERE {
			?PatientName hl7:GivenName ?GivenName.
			?PatientName hl7:FamilyName ?FamilyName.
			?PID hl7:PatientName ?PatientName. 
			?patient hl7:PID ?PID ." + "?patient hl7:DG1 ?DG1. 
			?DG1 hl7:DiagnosisCodeDG1  ?Identifier1.
			?Identifier1 hl7:Text ?diagnosis
      }
