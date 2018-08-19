# Hl7ToRDF-Healthcare data converter.

Hl7ToRDF Data Converter: This library converts a set of hl7 v2.x message to RDF format.
Here is an example of a SPARQL query that can be applied in converted data.

HL7 message
```
MSH|^~\\&|ADT1|MCM|LABADT|MCM|198808181126|SECURITY|ADT^A01|MSG00001-|P|2.5.1
EVN|A01|198808181123
PID|||PATID1231^5^M11||JONES^WILLIAM^A^III||19610615|M-||C|1200 N ELM STREET^^GREENSBORO^NC^27401-1020|GL|(91-9)379-1212|(919)271-3434||S||PATID12345001^2^M10|123456789|9-87654^NC
PV1|1|I|2000^2012^01||||004777^LEBAUER^SIDNEY^J.|||SUR||-||ADM|A0-
AL1|||^Cat dander|Respiratory distress
OBX|1|NM|GLU^Glucose Lvl|59|mg/dL|65-99^65^99|L|||F|||20150102000000|
DG1|1||78900^ABDMNAL PAIN UNSPCF SITE^I9CDX|||W
DG1|3||1488000^Postoperative nausea and vomiting^SCT|||W

```

SPARQL query
```
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
```

Result

```
--------------------------------------------------------------------------------
| patient       | GivenName | FamilyName | diagnosis                           |
================================================================================
| hl7:PATID1231 | "WILLIAM" | "JONES"    | "ABDMNAL PAIN UNSPCF SITE"          |
| hl7:PATID1231 | "WILLIAM" | "JONES"    | "Postoperative nausea and vomiting" |
--------------------------------------------------------------------------------
```
