<?php

$raw = file_get_contents("php://input");
$json = json_decode($raw);

$deviceId      = $json->{'deviceId'};
$docNum        = $json->{'docNum'};
$docDate       = $json->{'docDate'};
$docDateString = DateTime::createFromFormat('YmdHis', $docDate)->format('Y-m-d H:i:s');
$status        = $json->{'status'};

$host     = '192.168.54.4';
$user     = 'sa';
$password = 'ubgjnbyepf';
$dbname   = 'local2009';

$link = mssql_connect($host, $user, $password);
if (!$link) {
	die('Cannot connect to database server!');
}

mssql_select_db($dbname, $link);	

$isRowNum = true;
$currRowNum = 0;
$currRowQuant = 0;
foreach($json->{'row_quants'} as $item){
	
	if($isRowNum == true) {
		$currRowNum = $item;
	} else {
		
		$currRowQuant = $item;
		
		$queryText = "
		UPDATE _Document10048_VT10063
		SET _Fld10066 = $currRowQuant
		FROM _Document10048 AS doc
		LEFT JOIN _Document10048_VT10063 AS vt
		ON doc._IDRRef = vt._Document10048_IDRRef
		WHERE doc._Date_Time = '".$docDateString."' AND doc._Number = '".$docNum."' AND _LineNo10064 = $currRowNum;";
		
		mssql_query($queryText)or die ("Cannot perform query : ".mssql_get_last_message());
		
		$currRowNum = 0;
		$currRowQuant = 0;
	}
	$isRowNum = !$isRowNum;
}

# Установка статуса.
$currDate = date("Y-m-d H:i:s", time());
$queryText = "
UPDATE _Document10048
SET
	_Fld10052 = $status,
	_Fld10088 = '".$currDate."'
WHERE _Date_Time = '".$docDateString."' AND _Number = '".$docNum."';";
mssql_query($queryText)or die ("Cannot perform query: ".mssql_get_last_message());

# Получение статуса.
$queryText = "
SELECT TOP 1
	RTRIM(docs._Number) AS DocNum,
	CONVERT(nvarchar(30), docs._Date_Time, 120) AS DocDateTime,
	docs._Fld10052 AS Status
FROM _Document10048 AS docs
WHERE _Date_Time = '".$docDateString."' AND _Number = '".$docNum."';";
$result = mssql_query($queryText)or die ("Cannot perform query: ".mssql_get_last_message());

$newStatus = 0;
if($data = mssql_fetch_array($result)) {
	$newStatus = $data['Status'];
}
$json_data = array ('date'=>date("Y-m-d H:i:s", time()), 'deviceId'=>$deviceId, 'docNum'=>$docNum, 'docDate'=>$docDateString, 'status'=>$newStatus);

echo json_encode($json_data, JSON_UNESCAPED_UNICODE);

?>