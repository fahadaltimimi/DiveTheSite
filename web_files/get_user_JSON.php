<?php
 
/*
 * Following code will retreive a single user's data
 */
include('../variables/variables.php'); 

ob_start();

// array for JSON response
$response = array();
$response["SUCCESS"] = 0;

// Get inputted user name
$USER_ID = "";
$USER_NAME = "";
$EMAIL = "";

if (isset($_GET["USER_ID"])) {
	$USER_ID = $_GET["USER_ID"];
}

if (isset($_GET["USER_NAME"])) {
	$USER_NAME = $_GET["USER_NAME"];
}

if (isset($_GET["EMAIL"])) {
	$EMAIL = $_GET["EMAIL"];
}

// Connect to DB and run sql
$con = mysql_connect($DBHOST,$DBUSER,$DBPASS,$NEWLINK);
mysql_select_db($DBDIVE, $con);
mysql_query('SET CHARACTER SET utf8');

// To protect MySQL injection (more detail about MySQL injection)
$USER_ID = stripslashes($USER_ID);
$USER_ID = mysql_real_escape_string($USER_ID);

$USER_NAME = stripslashes($USER_NAME);
$USER_NAME = mysql_real_escape_string($USER_NAME);

$EMAIL = stripslashes($EMAIL);
$EMAIL = mysql_real_escape_string($EMAIL);

$sql = "SELECT USER_ID, FIRST_NAME, LAST_NAME, EMAIL, CITY, PROVINCE, COUNTRY, USERNAME, APPROVED, BIO, PICTURE, IS_MOD, CREATED, LAST_MODIFIED, 
  			   (SELECT COUNT(logs.LOG_ID) FROM tbdivelog logs WHERE logs.USER_ID = users.USER_ID) as LOG_COUNT,
  			   (SELECT COUNT(sites.SITE_ID) FROM tbdivesites sites WHERE sites.USER_ID = users.USER_ID) as DIVE_SITE_SUBMITTED_COUNT 
		FROM tbusers users";

if ($USER_ID != "") {
	$sql = $sql . " WHERE USER_ID = '$USER_ID'";
} else if ($USER_NAME != "") {
	$sql = $sql . " WHERE USERNAME = '$USER_NAME'"; 	
} else if ($EMAIL != "") {
	$sql = $sql . " WHERE EMAIL = '$EMAIL'"; 	
}

if ($sql != "") {
	$result_diver = mysql_query($sql, $con) or die(mysql_error());
	 
	// check for empty result
	if (mysql_num_rows($result_diver) > 0) {
	    $response["DIVERS"] = array();
		
		$row = mysql_fetch_array($result_diver); 

		$divers = array();
		
		$USER_ID = $row["USER_ID"];
			    
		$divers["USER_ID"] = $USER_ID;
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
		
		$response["DIVER_CERTIFICATIONS_DIVER_".$USER_ID] = array();
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
		
		// Set response results
		$response["SUCCESS"] = 1;
	} else {
	    // no user found
	    $response["MESSAGE"] = "Diver not found";
	}
} else {
	// no user found
    $response["MESSAGE"] = "Diver not found";
}

ob_end_clean();

echo json_encode($response);

mysql_close($con);

?>
        