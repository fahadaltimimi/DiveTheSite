<?php
 
/*
 * Following code will list all the divers
 */
include('../variables/variables.php'); 

// array for JSON response
$response = array();
$response["SUCCESS"] = 0;

// Get inputted fields
$LAST_MODIFIED = 1;
if (isset($_GET["LAST_MODIFIED"]))
   $LAST_MODIFIED = $_GET["LAST_MODIFIED"];
  
$NAME = "";   
if (isset($_GET["NAME"]))
   $NAME = $_GET["NAME"];
   
$COUNTRY = "";   
if (isset($_GET["COUNTRY"]))
   $COUNTRY = $_GET["COUNTRY"];
   
$PROVINCE = "";   
if (isset($_GET["PROVINCE"]))
   $PROVINCE = $_GET["PROVINCE"];
   
$CITY = "";   
if (isset($_GET["CITY"]))
   $CITY = $_GET["CITY"];
   
$START_INDEX_LOAD = "";
if (isset($_GET["START_INDEX_LOAD"])) {
	$START_INDEX_LOAD = $_GET["START_INDEX_LOAD"];
}

$COUNT_LOAD = "";
if (isset($_GET["COUNT_LOAD"])) {
	$COUNT_LOAD = $_GET["COUNT_LOAD"];
}  
  
date_default_timezone_set('UTC');
$LAST_MODIFIED_SEC = $LAST_MODIFIED / 1000;
$LAST_MODIFIED_DATE = date("Y-m-d H:i:s", $LAST_MODIFIED_SEC);

// Connect to DB and run sql
$con = mysql_connect($DBHOST,$DBUSER,$DBPASS,$NEWLINK);
mysql_select_db($DBDIVE, $con);
mysql_query('SET CHARACTER SET utf8');

// To protect MySQL injection (more detail about MySQL injection)
$LAST_MODIFIED = stripslashes($LAST_MODIFIED);
$LAST_MODIFIED = mysql_real_escape_string($LAST_MODIFIED);

$NAME = stripslashes($NAME);
$NAME = mysql_real_escape_string($NAME);

$COUNTRY = stripslashes($COUNTRY);
$COUNTRY = mysql_real_escape_string($COUNTRY);

$PROVINCE = stripslashes($PROVINCE);
$PROVINCE = mysql_real_escape_string($PROVINCE);

$CITY = stripslashes($CITY);
$CITY = mysql_real_escape_string($CITY);

$START_INDEX_LOAD = stripslashes($START_INDEX_LOAD);
$START_INDEX_LOAD = mysql_real_escape_string($START_INDEX_LOAD);

$COUNT_LOAD = stripslashes($COUNT_LOAD);
$COUNT_LOAD = mysql_real_escape_string($COUNT_LOAD);

// Retrieve and save count of items, send back with each item result 
$sqlLimit = "";
if ($START_INDEX_LOAD != "" && $COUNT_LOAD != "") {
	$sqlLimit = " LIMIT $START_INDEX_LOAD, $COUNT_LOAD ";
}

$sqlCounter = "SELECT COUNT(USER_ID) as DIVER_COUNT
               FROM tbusers users 
			   WHERE LAST_MODIFIED >= '$LAST_MODIFIED_DATE'";

$sql = "SELECT USER_ID, FIRST_NAME, LAST_NAME, EMAIL, CITY, PROVINCE, COUNTRY, USERNAME, APPROVED, BIO, IS_MOD, PICTURE, CREATED, LAST_MODIFIED, 
  			   (SELECT COUNT(logs.LOG_ID) FROM tbdivelog logs WHERE logs.USER_ID = users.USER_ID) as LOG_COUNT,
  			   (SELECT COUNT(sites.SITE_ID) FROM tbdivesites sites WHERE sites.USER_ID = users.USER_ID) as DIVE_SITE_SUBMITTED_COUNT 
		FROM tbusers users WHERE LAST_MODIFIED >= '$LAST_MODIFIED_DATE'"; 
		
$condition = "";		
if ($NAME != "") {
	$condition = $condition + " AND (users.FIRST_NAME LIKE '%$NAME%' OR users.LAST_NAME LIKE '%$NAME%' OR users.USERNAME LIKE '%$NAME%')";	
}

if ($COUNTRY != "") {
	$condition = $condition + " AND users.COUNTRY LIKE '%$COUNTRY%'";
}

if ($PROVINCE != "") {
	$condition = $condition + " AND users.PROVINCE LIKE '%$PROVINCE%'";
}

if ($CITY != "") {
	$condition = $condition + " AND users.CITY LIKE '%$CITY%'";
}

$sql = $sql . $condition;
$sqlCounter = $sqlCounter . $condition;

$sql = $sql . " ORDER BY USERNAME $sqlLimit";

// Get counter result first
$DIVER_COUNT = 0;
$resultCounter = mysql_query($sqlCounter, $con) or die(mysql_error());
if (mysql_num_rows($resultCounter) > 0) {
	$row = mysql_fetch_array($resultCounter);
	$DIVER_COUNT = $row["DIVER_COUNT"];
}

$result_diver = mysql_query($sql, $con) or die(mysql_error());
 
// check for empty result
if (mysql_num_rows($result_diver) > 0) {
    while ($row = mysql_fetch_array($result_diver)) {     
	    $response["DIVERS"] = array(); 	
		
		$divers = array();
	    
		
		$USER_ID = $row["USER_ID"];
		$USER_NAME = $row["USERNAME"];
		
		$divers["DIVER_COUNT"] = $DIVER_COUNT;
		$divers["USER_ID"] = $row["USER_ID"];
	    $divers["FIRST_NAME"] = $row["FIRST_NAME"];
		$divers["LAST_NAME"] = $row["LAST_NAME"];
	    $divers["EMAIL"] = $row["EMAIL"];
		$divers["CITY"] = $row["CITY"];
		$divers["PROVINCE"] = $row["PROVINCE"];
		$divers["COUNTRY"] = $row["COUNTRY"];
		$divers["USERNAME"] = $row["USERNAME"];
		$divers["APPROVED"] = $row["APPROVED"];
		$divers["BIO"] = $row["BIO"];
		$divers["IS_MOD"] = $row["IS_MOD"];
		$divers["CREATED"] = $row["CREATED"];
		$divers["LAST_MODIFIED"] = $row["LAST_MODIFIED"];
		$divers["PICTURE"] = "";
		if (trim($row["PICTURE"]) == "") {
			$divers["PICTURE_URL"] = "";
		} else {
			$divers["PICTURE_URL"] = "https://www.divethesite.com/profileImages/".$row["PICTURE"];	
		}		
		$divers["LOG_COUNT"] = $row["LOG_COUNT"];
		$divers["DIVE_SITE_SUBMITTED_COUNT"] = $row["DIVE_SITE_SUBMITTED_COUNT"];
		
		// Now get user's certifications
		$sql = "SELECT * from tbusercerts WHERE USER_ID=$USER_ID";
		$result_certs = mysql_query($sql, $con) or die(mysql_error());
		 
		// check for empty result
		// Set response results
		$response["SUCCESS"] = 1;
		array_push($response["DIVERS"], $divers);
		
		$response["DIVER_CERTIFICATIONS_DIVER_".$divers["USER_ID"]] = array();
		if (mysql_num_rows($result_certs) > 0) {
		    while ($row = mysql_fetch_array($result_certs)) {		 		     
			    $diverCertifications = array();
			    $diverCertifications["CERTIF_ID"] = $row["CERTIF_ID"];
				$diverCertifications["USER_ID"] = $row["USER_ID"];
				$diverCertifications["CERTIF_NAME"] = $row["CERTIF_NAME"];
				$diverCertifications["CERTIF_DATE"] = $row["CERTIF_DATE"];
				$diverCertifications["CERTIF_NO"] = $row["CERTIF_NO"];
				$diverCertifications["INSTR_NO"] = $row["INSTR_NO"];
				$diverCertifications["INSTR_NAME"] = $row["INSTR_NAME"];
				$diverCertifications["LOCATION"] = $row["LOCATION"];
				$diverCertifications["IS_PRIMARY"] = $row["IS_PRIMARY"];
				
				array_push($response["DIVER_CERTIFICATIONS_DIVER_".$diverCertifications["USER_ID"]], $diverCertifications);
			}
		}
		
		// success
	    $response["SUCCESS"] = 1;
	 
	    // echoing JSON response  
	    echo json_encode($response);
		echo "\r\n";	
	}
} else {
    // no user found, still success
	$response["DIVERS"] = array();
    $response["SUCCESS"] = 1;
	
    // echo no users JSON
    echo json_encode($response);
}

mysql_close($con);

?>
        