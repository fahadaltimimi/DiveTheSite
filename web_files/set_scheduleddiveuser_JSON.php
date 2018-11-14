<?php
 
/*
 * Following code will set the attendance of the given user to the given scheduled dive
 */
include('../variables/variables.php'); 

ob_start();
    
// array for JSON response
$response = array();
$response["SUCCESS"] = 0;

// Get parameters
$ScheduledDiveUserID = -1;
$ScheduledDiveUserLocalID = $_POST['SCHEDULED_DIVE_USER_LOCAL_ID'];
$ScheduledDiveID = $_POST['SCHEDULED_DIVE_ID'];
$ScheduledDiveLocalID = $_POST['SCHEDULED_DIVE_LOCAL_ID'];
$VotedScheduledDiveSiteID = $_POST['VOTED_SCHEDULED_DIVE_SITE_ID'];
$VotedScheduledDiveSiteLocalID = $_POST['VOTED_SCHEDULED_DIVE_SITE_LOCAL_ID'];
$UserID = $_POST['USER_ID'];
$AttendState = $_POST['ATTEND_STATE'];

// Connect to DB and run sql
$con = mysql_connect($DBHOST,$DBUSER,$DBPASS,$NEWLINK);
mysql_select_db($DBDIVE, $con);

// To protect MySQL injection (more detail about MySQL injection)
$ScheduledDiveUserLocalID = stripslashes($ScheduledDiveUserLocalID);
$ScheduledDiveUserLocalID = mysql_real_escape_string($ScheduledDiveUserLocalID);

$ScheduledDiveID = stripslashes($ScheduledDiveID);
$ScheduledDiveID = mysql_real_escape_string($ScheduledDiveID);

$ScheduledDiveLocalID = stripslashes($ScheduledDiveLocalID);
$ScheduledDiveLocalID = mysql_real_escape_string($ScheduledDiveLocalID);

$VotedScheduledDiveSiteID = stripslashes($VotedScheduledDiveSiteID);
$VotedScheduledDiveSiteID = mysql_real_escape_string($VotedScheduledDiveSiteID);

$VotedScheduledDiveSiteLocalID = stripslashes($VotedScheduledDiveSiteLocalID);
$VotedScheduledDiveSiteLocalID = mysql_real_escape_string($VotedScheduledDiveSiteLocalID);

$UserID = stripslashes($UserID);
$UserID = mysql_real_escape_string($UserID);

$AttendState = stripslashes($AttendState);
$AttendState = mysql_real_escape_string($AttendState);

// Make sure scheduled dive exists before updating
if ($ScheduledDiveID != -1) {
	$sql = "SELECT SCHEDULED_DIVE_ID FROM tbscheduleddives WHERE SCHEDULED_DIVE_ID ='$ScheduledDiveID'";
	$result = mysql_query($sql, $con) or die(mysql_error());
	
	if (mysql_num_rows($result) == 0) {
		$ScheduledDiveID = -1;
	}
}

if ($ScheduledDiveID != -1) {
	// Get Scheduled Dive User ID, if one exists, for the user and sceduled dive
	$sql = "SELECT SCHEDULED_DIVE_USER_ID FROM tbscheduleddivesusers 
			WHERE USER_ID ='$UserID' AND SCHEDULED_DIVE_ID = '$ScheduledDiveID'";

	$resultScheduledDiveUser = mysql_query($sql, $con) or die(mysql_error());
	if (mysql_num_rows($resultScheduledDiveUser) > 0) {
		$rowScheduledUser = mysql_fetch_array($resultScheduledDiveUser);
	    $ScheduledDiveUserID = $rowScheduledUser["SCHEDULED_DIVE_USER_ID"];
	}

	if ($ScheduledDiveUserID == -1) {
		// New Scheduled Dive User, insert new one		
		$sql = "INSERT INTO tbscheduleddivesusers (SCHEDULED_DIVE_ID, SCHEDULED_DIVE_SITE_ID, USER_ID, ATTEND_STATE)
				VALUES (\"$ScheduledDiveID\", \"$VotedScheduledDiveSiteID\", \"$UserID\", \"$AttendState\")";
									   
		mysql_query($sql, $con) or die(mysql_error());
		$ScheduledDiveUserID = mysql_insert_id(); 
	} else {
		// ID already exists, update existing Scheduled Dive User
		$sql = "UPDATE tbscheduleddivesusers
				SET SCHEDULED_DIVE_ID = \"$ScheduledDiveID\",
					SCHEDULED_DIVE_SITE_ID = \"$VotedScheduledDiveSiteID\",
					USER_ID = \"$UserID\",
					ATTEND_STATE = \"$AttendState\"
				WHERE SCHEDULED_DIVE_USER_ID = '$ScheduledDiveUserID'";
				
		mysql_query($sql, $con) or die(mysql_error()); 
	}

	if ($ScheduledDiveUserID != -1) {
		// Update last modified for schedule dive so it can be updated
		$sql = "UPDATE tbscheduleddives
				SET LAST_MODIFIED = UTC_TIMESTAMP()
				WHERE SCHEDULED_DIVE_ID = '$ScheduledDiveID'";
		mysql_query($sql, $con) or die(mysql_error()); 	 	

		// Now save Scheduled Dive User
		$response["SCHEDULEDDIVEUSER_SCHEDULEDDIVE_".$ScheduledDiveID] = array();
		$scheduleddivesUser = array();
	    $scheduleddivesUser["SCHEDULED_DIVE_USER_ID"] = $ScheduledDiveUserID;
		$scheduleddivesUser["SCHEDULED_DIVE_USER_LOCAL_ID"] = $ScheduledDiveUserLocalID;
		$scheduleddivesUser["SCHEDULED_DIVE_ID"] = $ScheduledDiveID;
		$scheduleddivesUser["SCHEDULED_DIVE_LOCAL_ID"] = $ScheduledDiveLocalID;
		$scheduleddivesUser["VOTED_SCHEDULED_DIVE_SITE_ID"] = $VotedScheduledDiveSiteID;
		$scheduleddivesUser["VOTED_SCHEDULED_DIVE_SITE_LOCAL_ID"] = $VotedScheduledDiveSiteLocalID;
		$scheduleddivesUser["USER_ID"] = $UserID;
		$scheduleddivesUser["ATTEND_STATE"] = $AttendState;
		
		array_push($response["SCHEDULEDDIVEUSER_SCHEDULEDDIVE_".$ScheduledDiveID], $scheduleddivesUser);
		// success
    	$response["SUCCESS"] = 1;	
	} else {
	    // no schedule dive user found, error must have occured with saving
	    $response["SUCCESS"] = 0;  
	}
} else {
    // no schedule dive found wit given id, error occured
    $response["SUCCESS"] = 0;  
}

ob_end_clean();

echo json_encode($response);

mysql_close($con);

?>