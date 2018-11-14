

<?php
 
/*
 * Following code will list all the dive sites
 */
include('../variables/variables.php'); 
  
// array for JSON response
$response = array();
$response["SUCCESS"] = 0;

$MIN_LATITUDE = -90;
$MAX_LATITUDE = 90;
$MIN_LONGITUDE = -180;
$MAX_LONGITUDE = 180;
$STATION_UPDATED = 1;
$MIN_LAST_UPDATE_TIMESTAMP = 0;

if (isset($_GET["MIN_LATITUDE"]) && $_GET["MIN_LATITUDE"] != "") {
	$MIN_LATITUDE = $_GET["MIN_LATITUDE"];
}

if (isset($_GET["MAX_LATITUDE"]) && $_GET["MAX_LATITUDE"] != "") {
	$MAX_LATITUDE = $_GET["MAX_LATITUDE"];
}

if (isset($_GET["MIN_LONGITUDE"]) && $_GET["MIN_LONGITUDE"] != "") {
	$MIN_LONGITUDE = $_GET["MIN_LONGITUDE"];
}

if (isset($_GET["MAX_LONGITUDE"]) && $_GET["MAX_LONGITUDE"] != "") {
	$MAX_LONGITUDE = $_GET["MAX_LONGITUDE"];
}

if (isset($_GET["STATION_UPDATED"])) {
	$STATION_UPDATED = $_GET["STATION_UPDATED"];
}

if (isset($_GET["MIN_LAST_UPDATE_TIMESTAMP"])) {
	$MIN_LAST_UPDATE_TIMESTAMP = $_GET["MIN_LAST_UPDATE_TIMESTAMP"];
}

$CURRENT_LATITUDE = "";
if (isset($_GET["CURRENT_LATITUDE"])) {
	$CURRENT_LATITUDE = $_GET["CURRENT_LATITUDE"];
}

$CURRENT_LONGITUDE = "";
if (isset($_GET["CURRENT_LONGITUDE"])) {
	$CURRENT_LONGITUDE = $_GET["CURRENT_LONGITUDE"];
}

$DISTANCE_FROM_CURRRNT = "";
if (isset($_GET["DISTANCE_FROM_CURRRNT"])) {
	$DISTANCE_FROM_CURRRNT = $_GET["DISTANCE_FROM_CURRRNT"];
}

$START_INDEX_LOAD = "";
if (isset($_GET["START_INDEX_LOAD"])) {
	$START_INDEX_LOAD = $_GET["START_INDEX_LOAD"];
}

$COUNT_LOAD = "";
if (isset($_GET["COUNT_LOAD"])) {
	$COUNT_LOAD = $_GET["COUNT_LOAD"];
}

// Connect to DB and run sql
$con = mysql_connect($DBHOST,$DBUSER,$DBPASS,$NEWLINK);
mysql_select_db($DBDIVE, $con);

// To protect MySQL injection (more detail about MySQL injection)
$MIN_LATITUDE = stripslashes($MIN_LATITUDE);
$MIN_LATITUDE = mysql_real_escape_string($MIN_LATITUDE);

$MAX_LATITUDE = stripslashes($MAX_LATITUDE);
$MAX_LATITUDE = mysql_real_escape_string($MAX_LATITUDE);

$MIN_LONGITUDE = stripslashes($MIN_LONGITUDE);
$MIN_LONGITUDE = mysql_real_escape_string($MIN_LONGITUDE);

$MAX_LONGITUDE = stripslashes($MAX_LONGITUDE);
$MAX_LONGITUDE = mysql_real_escape_string($MAX_LONGITUDE);

$STATION_UPDATED = stripslashes($STATION_UPDATED);
$STATION_UPDATED = mysql_real_escape_string($STATION_UPDATED);

$MIN_LAST_UPDATE_TIMESTAMP = stripslashes($MIN_LAST_UPDATE_TIMESTAMP);
$MIN_LAST_UPDATE_TIMESTAMP = mysql_real_escape_string($MIN_LAST_UPDATE_TIMESTAMP);

$CURRENT_LATITUDE = stripslashes($CURRENT_LATITUDE);
$CURRENT_LATITUDE = mysql_real_escape_string($CURRENT_LATITUDE);

$CURRENT_LONGITUDE = stripslashes($CURRENT_LONGITUDE);
$CURRENT_LONGITUDE = mysql_real_escape_string($CURRENT_LONGITUDE);

$DISTANCE_FROM_CURRRNT = stripslashes($DISTANCE_FROM_CURRRNT);
$DISTANCE_FROM_CURRRNT = mysql_real_escape_string($DISTANCE_FROM_CURRRNT);

$START_INDEX_LOAD = stripslashes($START_INDEX_LOAD);
$START_INDEX_LOAD = mysql_real_escape_string($START_INDEX_LOAD);

$COUNT_LOAD = stripslashes($COUNT_LOAD);
$COUNT_LOAD = mysql_real_escape_string($COUNT_LOAD);

date_default_timezone_set('UTC');
$MIN_LAST_UPDATE_TIMESTAMP_SEC = $MIN_LAST_UPDATE_TIMESTAMP / 1000;
$MIN_LAST_UPDATE_TIMESTAMP_DATE = date("Y-m-d H:i:s", $MIN_LAST_UPDATE_TIMESTAMP_SEC);

$sqlDistanceResult = "";
$sqlDistanceSelect = "";
$sqlDistanceWhere = "";
$sqlDistanceOrder = "";
if ($CURRENT_LONGITUDE != "" && $CURRENT_LATITUDE != "") {
    $sqlDistanceResult = "(111.045 * vincenty($CURRENT_LATITUDE, $CURRENT_LONGITUDE, LATITUDE, LONGITUDE))";
	
	$sqlDistanceSelect = ", $sqlDistanceResult as DISTANCE";
	$sqlDistanceOrder = " ORDER BY DISTANCE";
	
	if ($DISTANCE_FROM_CURRRNT != "") {
		$sqlDistanceWhere = " AND $sqlDistanceResult <= $DISTANCE_FROM_CURRRNT";
	}
}

$sql = "SELECT STATION_ID,
			   NAME,
			   LATITUDE,
			   LONGITUDE,
			   TYPE,
			   LAST_UPDATE
			   $sqlDistanceSelect
		FROM tbNDBCStations 
		WHERE LATITUDE >= $MIN_LATITUDE AND LATITUDE <= $MAX_LATITUDE
			AND LONGITUDE >= $MIN_LONGITUDE AND LONGITUDE <= $MAX_LONGITUDE
			AND LAST_UPDATE >= '$MIN_LAST_UPDATE_TIMESTAMP_DATE' $sqlDistanceWhere";
		
if ($STATION_UPDATED != "0") {
	$sql = $sql . " AND LAST_UPDATE <> 0";
}

$sqlLimit = "";
if ($START_INDEX_LOAD != "" && $COUNT_LOAD != "") {
	$sqlLimit = " LIMIT $START_INDEX_LOAD, $COUNT_LOAD ";
}

$sql = $sql . $sqlDistanceOrder . $sqlLimit;

$result = mysql_query($sql, $con) or die(mysql_error());
 
// check for empty result
if (mysql_num_rows($result) > 0) {
    // looping through all stations
    while ($row = mysql_fetch_array($result)) {
    	$response["NDBC_STATIONS"] = array();
		
        $stations = array();
        $stations["STATION_ID"] = $row["STATION_ID"];
        $stations["NAME"] = $row["NAME"];
        $stations["LATITUDE"] = $row["LATITUDE"];
		$stations["LONGITUDE"] = $row["LONGITUDE"];
        $stations["LAST_UPDATE"] = $row["LAST_UPDATE"];
		
        // push single product into final response array
        array_push($response["NDBC_STATIONS"], $stations);
		
		// success
	    $response["SUCCESS"] = 1;
	    echo json_encode($response);
		echo "\r\n";
    }    
} else {
	$response["NDBC_STATIONS"] = array();
	
    // no stations found but no error
    $response["SUCCESS"] = 1;
 	echo json_encode($response);
}

mysql_close($con);

?>