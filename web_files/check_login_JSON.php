

<?php
 
/*
 * Following code will verify a user's username and password
 */
include('../variables/variables.php'); 
include('../includes/DiveSiteAuth.php');
  
// array for JSON response
$response = array();
$response["SUCCESS"] = 0;

// Get username and password
$UsernameEntered = $_POST['USERNAME'];
$PasswordEntered = $_POST['PASSWORD'];

// Connect to DB and run sql
$con = mysql_connect($DBHOST,$DBUSER,$DBPASS,$NEWLINK);
mysql_select_db($DBDIVE, $con);
mysql_query('SET CHARACTER SET utf8');

// To protect MySQL injection (more detail about MySQL injection)
$UsernameEntered = stripslashes($UsernameEntered);
$UsernameEntered = mysql_real_escape_string($UsernameEntered);

$sql = "SELECT *, 
  			   (SELECT COUNT(logs.LOG_ID) FROM tbdivelog logs WHERE logs.USER_ID = users.USER_ID) as LOG_COUNT,
  			   (SELECT COUNT(sites.SITE_ID) FROM tbdivesites sites WHERE sites.USER_ID = users.USER_ID) as DIVE_SITE_SUBMITTED_COUNT 
		FROM tbusers users WHERE USERNAME='$UsernameEntered'";

$result = mysql_query($sql, $con) or die(mysql_error());
 
// check for empty result
if (mysql_num_rows($result) > 0) {
    $response["DIVERS"] = array();
 
 	// Only looking for one user as usernames are unique
    $row = mysql_fetch_array($result);
        
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
	$divers["APPROVED"] = $row["APPROVED"];
	$divers["CREATED"] = $row["CREATED"];
	$divers["LAST_MODIFIED"] = $row["LAST_MODIFIED"];	
	$divers["LOG_COUNT"] = $row["LOG_COUNT"];
	$divers["DIVE_SITE_SUBMITTED_COUNT"] = $row["DIVE_SITE_SUBMITTED_COUNT"];
	
	// Check user's entered password
	$PW_Validation = false;
	if($row["SECURITY_TYPE"] == 0){
		$PW_Validation = md5($PasswordEntered) == $row["PASSWORD"];
	}
	else { 			
		$PW_Validation = validate_password($PasswordEntered, $row["PASSWORD"]);
	}
	
	if ($PW_Validation == true) {
		// Check if user has confirm their email or not
		if ($divers["APPROVED"] == "1") {
			$response["SUCCESS"] = 1;
			array_push($response["DIVERS"], $divers);
		} else {
			$response["MESSAGE"] = "Email account used not yet confirmed, check your email for a link to confirm your registration!";
		}
	} else {
		$response["MESSAGE"] = "Invalid username or password";
	}
	
    // echoing JSON response  
    echo json_encode($response);
} else {
    // no user found    
    $response["MESSAGE"] = "Invalid username or password";
 
    // echo no users JSON
    echo json_encode($response);
}

mysql_close($con);

?>
        