

<?php
 
/*
 * Following code will list all the divers
 */
include('../variables/variables.php'); 

// array for JSON response
$response = array();
$response["SUCCESS"] = 0;

// Get inputted user name
$USER_NAME = "";

if (isset($_GET["USER_NAME"])) {
	$USER_NAME = $_GET["USER_NAME"];
}

// Connect to DB and run sql
$con = mysql_connect($DBHOST,$DBUSER,$DBPASS,$NEWLINK);
mysql_select_db($DBDIVE, $con);
mysql_query('SET CHARACTER SET utf8');

// To protect MySQL injection (more detail about MySQL injection)
$USER_NAME = stripslashes($USER_NAME);
$USER_NAME = mysql_real_escape_string($USER_NAME);

$sql = "SELECT USER_ID, FIRST_NAME, LAST_NAME, EMAIL, CITY, PROVINCE, COUNTRY, USERNAME, APPROVED, BIO, IS_MOD, CREATED, LAST_MODIFIED, 
  			   (SELECT COUNT(logs.LOG_ID) FROM tbdivelog logs WHERE logs.USER_ID = users.USER_ID) as LOG_COUNT,
  			   (SELECT COUNT(sites.SITE_ID) FROM tbdivesites sites WHERE sites.USER_ID = users.USER_ID) as DIVE_SITE_SUBMITTED_COUNT 
		FROM tbusers users WHERE WHERE USERNAME='$USER_NAME'"; 

$result_diver = mysql_query($sql, $con) or die(mysql_error());
 
// check for empty result
if (mysql_num_rows($result_diver) > 0) {
    $response["DIVERS"] = array();
	
	$row = mysql_fetch_array($result_diver); 

	$divers = array();
    
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
	$divers["LOG_COUNT"] = $row["LOG_COUNT"];
	$divers["DIVE_SITE_SUBMITTED_COUNT"] = $row["DIVE_SITE_SUBMITTED_COUNT"];

	// check for empty result
	// Set response results
	$response["SUCCESS"] = 1;
	array_push($response["DIVERS"], $divers);

		
    // echoing JSON response  
    echo json_encode($response);
} else {
    // no user found
    $response["MESSAGE"] = "Diver not found";
 
    // echo no users JSON
    echo json_encode($response);
}

mysql_close($con);

?>
        