

<?php
 
/*
 * Following code will list all the dive sites
 */
include('../variables/variables.php'); 

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

// To protect MySQL injection (more detail about MySQL injection)
$USER_ID = stripslashes($USER_ID);
$USER_ID = mysql_real_escape_string($USER_ID);

$USER_NAME = stripslashes($USER_NAME);
$USER_NAME = mysql_real_escape_string($USER_NAME);

$EMAIL = stripslashes($EMAIL);
$EMAIL = mysql_real_escape_string($EMAIL);

$sql = "";

if ($USER_ID != "") {
	$sql = "SELECT * from tbusers WHERE USER_ID = '$USER_ID'";
} else if ($USER_NAME != "") {
	$sql = "SELECT * from tbusers WHERE USERNAME = '$USER_NAME'"; 	
} else if ($EMAIL != "") {
	$sql = "SELECT * from tbusers WHERE EMAIL = '$EMAIL'"; 	
}

if ($sql != "") {
	$result_diver = mysql_query($sql, $con) or die(mysql_error());
	 
	// check for empty result
	if (mysql_num_rows($result_diver) > 0) {
	    $response["DIVERS"] = array();
		
		$row = mysql_fetch_array($result_diver); 

		$divers = array();
	    
		$divers["USER_ID"] = $row["USER_ID"];
	    
		// Get picture
		$picture_file = $row["PICTURE"];
		if ($picture_file == '') {
	        $picture_file = "diveFlag.jpg";
	    }
	    $picture_file = "../profileImages/" . $picture_file;
		
		// Save picture file as string
		$type = pathinfo($picture_file, PATHINFO_EXTENSION);
		$data = file_get_contents($picture_file);
		$base64 = 'data:image/' . $type . ';base64,' . base64_encode($data);
		
		$divers["PICTURE"] = $base64;
		
		// check for empty result
		// Set response results
		$response["SUCCESS"] = 1;
		array_push($response["DIVERS"], $divers);
	} else {
	    // no user found
	    $response["MESSAGE"] = "Diver not found";
	}
} else {
	// no user found
    $response["MESSAGE"] = "Diver not found";
}

echo json_encode($response);

mysql_close($con);

?>
        