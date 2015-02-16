<?php
header ("Content-Type:text/html;charsret=utf-8");
if (isset($_GET['deviceId'])){

    $deviceId = $_GET['deviceId'];

    $host = '192.168.54.4';
    $user = 'sa';
    $password = 'ubgjnbyepf';

    $dbname = 'local2009';

    $link = mssql_connect($host, $user, $password);
    if (!$link) {
		die('Cannot connect to database server!');
    }

    mssql_select_db($dbname, $link);

    if(isset($_GET['command'])) {

		$command       = $_GET['command'];
		
		if(strcmp($command, 'setstatus') == 0) {
	
			if(isset($_GET['docNum']) & isset($_GET['docDate']) & isset($_GET['status'])) {
		
				$docNum        = $_GET['docNum'];
				$docDate       = $_GET['docDate'];
				$docDateString = DateTime::createFromFormat('YmdHis', $docDate)->format('Y-m-d H:i:s');
				$status        = $_GET['status'];
				$currDate      = date("Y-m-d H:i:s", time());
			
				mssql_query("
				UPDATE _Document10048
				SET
					_Fld10052 = $status,
					_Fld10088 = '".$currDate."'
				WHERE _Date_Time = '".$docDateString."' AND _Number = ".$docNum
				)or die ("Cannot perform query : ".mssql_get_last_message());

				$result = mssql_query("
				SELECT TOP 1
					RTRIM(docs._Number) AS DocNum,
					CONVERT(nvarchar(30), docs._Date_Time, 120) AS DocDateTime,
					docs._Fld10052 AS Status
				FROM _Document10048 AS docs
				WHERE _Date_Time = '".$docDateString."' AND _Number = ".$docNum
				)or die ("Cannot perform query: ".mssql_get_last_message());

				$dom = new domDocument("1.0", "utf-8");
				
				$root = $dom->createElement("exp_rev");
				$root->setAttribute("date", date("Y-m-d H:i:s", time()));
				$root->setAttribute("deviceId", $deviceId);
				
				while ($data = mssql_fetch_array($result)){
					$rootDoc = $dom->createElement("doc");
					$rootDoc->setAttribute("docNum",    $data['DocNum']);
					$rootDoc->setAttribute("docDate",   $data['DocDateTime']);
					$rootDoc->setAttribute("status", $data['Status']);
					$root->appendChild($rootDoc);
				}
				
				$dom->appendChild($root);
			
			} else {
				die('Invalid parameters set!');
			}
		} else {
			die('Unknown command!');
		}
		
    } else {

	$result = mssql_query("
	SELECT
	    RTRIM(docs._Number) AS DocNum,
	    CONVERT(nvarchar(30), docs._Date_Time, 120) AS DocDateTime,
	    docs._Fld10052 AS DocStatus,
	    docs._Fld10054 AS DocComment,
	    RowsCounter.Rows AS Rows,
	    RTRIM(stores._Code) AS StoreCode,
	    stores._Description AS StoreDescription
	FROM _Document10048 as docs
	LEFT JOIN _Reference80 as stores
	ON docs._Fld10055RRef = stores._IDRRef
	LEFT JOIN(
	    SELECT
		_Document10048_IDRRef,
		COUNT(_LineNo10064) AS Rows
	    FROM _Document10048_VT10063
	    GROUP BY _Document10048_IDRRef
	    ) AS RowsCounter
	ON docs._IDRRef = RowsCounter._Document10048_IDRRef
	WHERE docs._Fld10052 = 1
	ORDER BY stores._Code")
	or die ("Cannot perform query: ".mssql_get_last_message());
	
	$dom = new domDocument("1.0", "utf-8");
	
	$root = $dom->createElement("exp_rev");
	$root->setAttribute("date", date("Y-m-d H:i:s", time()));
	$root->setAttribute("deviceId", $deviceId);
	
	while ($data = mssql_fetch_array($result)){
		
		$rootDoc = $dom->createElement("doc");
		$rootDoc->setAttribute("docNum",  $data['DocNum']);
		$rootDoc->setAttribute("docDate", $data['DocDateTime']);
		$rootDoc->setAttribute("rows", $data['Rows']);
			
		$rootStore = $dom->createElement("store");
		$rootStore->setAttribute("code", $data['StoreCode']);
		
		$rootStoreDescr = $dom->createElement("descr");
		$rootStoreDescr->appendChild($dom->createTextNode($data['StoreDescription']));
		
		$rootStore->appendChild($rootStoreDescr);
		
		$rootDoc->appendChild($rootStore);
		
		$rootComment = $dom->createElement("comm");
		$rootComment->appendChild($dom->createTextNode($data['DocComment']));
		
		$rootDoc->appendChild($rootComment);
		
		$root->appendChild($rootDoc);
	}

	$dom->appendChild($root);

	}

	echo $dom->saveXML();

	mssql_free_result($result);
	mssql_close($link);

} else {
	die('Unknown device!');
}
?>