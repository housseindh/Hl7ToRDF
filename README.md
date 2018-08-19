# Hl7ToRDF-Healthcare data converter.

Hl7ToRDF Data Converter: This library converts a set of hl7 v2.x message to RDF format.
Here is an example of a SPARQL query that can be applied in converted data.

HL7 message 1
```
MSH|^~\&|ADT1|MCM|LABADT|MCM|198808181126|SECURITY|ADT^A01|MSG00001-|P|2.5.1
EVN|A01|198808181123
PID|||PATID1231^5^M11||JONES^WILLIAM^A^III||19610615|M-||C|1200 N ELM STREET^^GREENSBORO^NC^27401-1020|GL|(91-9)379-1212|(919)271-3434||S||PATID12345001^2^M10|123456789|9-87654^NC
PV1|1|I|2000^2012^01||||004777^LEBAUER^SIDNEY^J.|||SUR||-||ADM|A0-
AL1|||^Cat dander|Respiratory distress
OBX|1|NM|GLU^Glucose Lvl|59|mg/dL|65-99^65^99|L|||F|||20150102000000|
DG1|1||78900^ABDMNAL PAIN UNSPCF SITE^I9CDX|||W
DG1|3||1488000^Postoperative nausea and vomiting^SCT|||W

```
HL7 message 2
```
MSH|^~\&|REGADT|GOOD HEALTH HOSPITAL|GHH LAB||200712311501||ADT^A04^ADT_A01|000001|P|2.6|||
EVN|A04|200701101500|200701101400|01||200701101410
PID|||191919^^^GOOD HEALTH HOSPITAL^MR^GOOD HEALTH HOSPITAL^^^USSSA^SS|253763|EVERYMAN^ADAM^A||19560129|M|||2222 HOME STREET^^ISHPEMING^MI^49849^\"\"^||555-555-2004|555-555- 2004||S|CHR|10199925^^^GOOD HEALTH HOSPITAL^AN|371-66-9256||
OBX||ST|1010.1^BODY WEIGHT||62|kg|||||F\r\n" + "OBX||ST|1010.1^HEIGHT||190|cm|||||F
DG1|1||236084000^Chemotherapy-induced nausea and vomiting^SCT|||W
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


---------------------------------------------------------------------------------------
| patient       | GivenName | FamilyName | diagnosis                                  |
=======================================================================================
| hl7:PATID1231 | "WILLIAM" | "JONES"    | "ABDMNAL PAIN UNSPCF SITE"                 |
| hl7:PATID1231 | "WILLIAM" | "JONES"    | "Postoperative nausea and vomiting"        |
| hl7:          | "ADAM"    | "EVERYMAN" | "Chemotherapy-induced nausea and vomiting" |
---------------------------------------------------------------------------------------

