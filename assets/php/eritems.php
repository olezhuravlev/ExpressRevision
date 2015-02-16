<?php
header ("Content-Type:text/html;charsret=utf-8");
if(isset($_GET['deviceId'])) { 
	
	$deviceId = $_GET['deviceId'];
	
	#$filepath = $_SERVER['DOCUMENT_ROOT'].'/'.'eritems_log.txt';
	#$handle = fopen($filepath, "a+");
	#@flock($handle, LOCK_EX);
	#fwrite($handle, "deviceId=".$deviceId."\n");
	#@flock($handle, LOCK_UN);
	#fclose($handle);
	
	if(isset($_GET['docNum']) & isset($_GET['docDate'])) {

		$docNum        = $_GET['docNum'];
		$docDate       = $_GET['docDate']; 

		if(isset($_GET['firstrow'])) {
		    $firstRow = $_GET['firstrow'];
		} else {
		    $firstRow = 1;
		}

		if(isset($_GET['lastrow'])) {
		    $lastRow = $_GET['lastrow'];
		} else {
		    $lastRow = 2147483647;
		}

		$docDateString = DateTime::createFromFormat('YmdHis', $docDate)->format('Y-m-d H:i:s');

		#$filepath = $_SERVER['DOCUMENT_ROOT'].'/'.'eritems_log.txt';
		#$handle = fopen($filepath, "a+");
		#@flock($handle, LOCK_EX);
		#fwrite($handle, "docNum=".$docNum."\n");
		#fwrite($handle, "docDate=".$docDate."\n");
		#fwrite($handle, "docDateString=".$docDateString."\n");
		#@flock($handle, LOCK_UN);
		#fclose($handle);

		$host     = '192.168.54.4';
		$user     = 'sa';
		$password = 'ubgjnbyepf';

		$dbname = 'local2009';
		
		ini_set('mssql.datetimeconvert', 0);
		
		$link = mssql_connect($host, $user, $password);
		if (!$link) {
			die('Cannot connect to database server!');
		}
		
		mssql_select_db($dbname, $link);

		$queryText = "
		SELECT
			RTRIM(doc._Number) AS DocNum,
			doc._Date_Time AS DocDateTime,
			vt._LineNo10064 AS DocRowNum,
			RTRIM(items._Code) AS ItemCode,
			items._Description AS ItemDescription,
			items._Fld802 AS ItemDescriptionFull,
			CASE items._Fld819
				WHEN 0 THEN 0
				ELSE 1
				END AS ItemUseSpecif,
			RTRIM(specif._Code) AS SpecifCode,
			specif._Description AS SpecifDescription,
			measur._Description AS MeasurDescription,
			vt._Fld10073 AS Price,
			vt._Fld10068 AS QuantityAcc,
			vt._Fld10066 AS Quantity
		FROM _Document10048 AS doc
		LEFT JOIN _Document10048_VT10063 AS vt
		ON doc._IDRRef = vt._Document10048_IDRRef
		LEFT JOIN _Reference51 AS items
		ON vt._Fld10070RRef = items._IDRRef
		LEFT JOIN _Reference101 AS specif
		ON vt._Fld10074RRef = specif._IDRRef
		LEFT JOIN _Reference36 AS measur
		ON vt._Fld10065RRef = measur._IDRRef
		WHERE
		    vt._LineNo10064 IS NOT NULL AND
		    vt._LineNo10064 BETWEEN $firstRow AND $lastRow AND
		    doc._Date_Time = '".$docDateString."' AND
		    doc._Number = ".$docNum."
		ORDER BY vt._LineNo10064";
				 
		#$filepath = $_SERVER['DOCUMENT_ROOT'].'/'.'eritems_log.txt';
		#$handle = fopen($filepath, "a+");
		#@flock($handle, LOCK_EX);
		#fwrite($handle, "".$queryText."\n");
		#@flock($handle, LOCK_UN);
		#fclose($handle);
		
		$result = mssql_query($queryText)
		or die ("Cannot perform query: ".mssql_get_last_message());
		
		$dom = new domDocument("1.0", "utf-8");
		
		$root = $dom->createElement("exp_rev");
		$root->setAttribute("date", date("Y-m-d H:i:s", time()));
		$root->setAttribute("deviceId", $deviceId);
		
		$rootDoc = $dom->createElement("doc");
		
		while ($data = mssql_fetch_array($result)){
		
			$rootDoc->setAttribute("docNum", $data['DocNum']);
			$rootDoc->setAttribute("docDate", $data['DocDateTime']);
			$rootDoc->setAttribute("firstrow", $firstRow);
			$rootDoc->setAttribute("lastrow", $lastRow);
			
			$rootDocRow = $dom->createElement("doc_row");
			$rootDocRow->setAttribute("num", $data['DocRowNum']);
			
			$rootItem = $dom->createElement("item");
			$rootItem->setAttribute("code", $data['ItemCode']);
			
			$rootItemDescr = $dom->createElement("descr");
			$rootItemDescr->appendChild($dom->createTextNode($data['ItemDescription']));			
			$rootItem->appendChild($rootItemDescr);
			
			$rootItemDescrFull = $dom->createElement("descr_full");
			$rootItemDescrFull->appendChild($dom->createTextNode($data['ItemDescriptionFull']));	
			$rootItem->appendChild($rootItemDescrFull);
			
			$rootItemUseSpecif = $dom->createElement("use_specif");
			$rootItemUseSpecif->appendChild($dom->createTextNode($data['ItemUseSpecif']));	
			$rootItem->appendChild($rootItemUseSpecif);
			
			$rootDocRow->appendChild($rootItem);
				
			$rootSpecif = $dom->createElement("specif");
			$rootSpecif->setAttribute("code", $data['SpecifCode']);
				
			$rootSpecifDescr = $dom->createElement("descr");
			$rootSpecifDescr->appendChild($dom->createTextNode($data['SpecifDescription']));	
				
			$rootSpecif->appendChild($rootSpecifDescr);
				
			$rootDocRow->appendChild($rootSpecif);
				
			$rootMeasur = $dom->createElement("measur");
				
			$rootMeasurDescr = $dom->createElement("descr");
			$rootMeasurDescr->appendChild($dom->createTextNode($data['MeasurDescription']));	
				
			$rootMeasur->appendChild($rootMeasurDescr);
			
			$rootDocRow->appendChild($rootMeasur);
				
			$rootPrice = $dom->createElement("price");
			$rootPrice->appendChild($dom->createTextNode($data['Price']));	
			
			$rootDocRow->appendChild($rootPrice);
				
			$rootQuantityAcc = $dom->createElement("quant_acc");
			$rootQuantityAcc->appendChild($dom->createTextNode($data['QuantityAcc']));	
			
			$rootDocRow->appendChild($rootQuantityAcc);	
				
			$rootQuantity = $dom->createElement("quant");
			$rootQuantity->appendChild($dom->createTextNode($data['Quantity']));	
			
			$rootDocRow->appendChild($rootQuantity);	
				
			$rootDoc->appendChild($rootDocRow);
		}

		$root->appendChild($rootDoc);
		$dom->appendChild($root);
		
		echo $dom->saveXML();
		
		mssql_free_result($result);
		mssql_close($link);
		
	} else{
		die('No params!');
	}
} else {
	die('Unknown device!');
}
?>