

<?php
 
/*
 * Following code will list all the dive logs
 */
include('../variables/variables.php'); 
  
// array for JSON response
$response = array();
$response["SUCCESS"] = 0;

$DATE_CREATED = 0;
$USER_ID = -1;
$SITE_ID = -1;

// Get inputted last modified date
if (isset($_GET["DATE_CREATED"])) {
   $DATE_CREATED = $_GET["DATE_CREATED"];
}

if (isset($_GET["USER_ID"])) {
   $USER_ID = $_GET["USER_ID"];
}

if (isset($_GET["SITE_ID"])) {
   $SITE_ID = $_GET["SITE_ID"];
}

$CURRENT_LATITUDE = "";
if (isset($_GET["CURRENT_LATITUDE"])) {
	$CURRENT_LATITUDE = $_GET["CURRENT_LATITUDE"];
}

$CURRENT_LONGITUDE = "";
if (isset($_GET["CURRENT_LONGITUDE"])) {
	$CURRENT_LONGITUDE = $_GET["CURRENT_LONGITUDE"];
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
mysql_query('SET CHARACTER SET utf8');

// To protect MySQL injection (more detail about MySQL injection)
$DATE_CREATED = stripslashes($DATE_CREATED);
$DATE_CREATED = mysql_real_escape_string($DATE_CREATED);

$USER_ID = stripslashes($USER_ID);
$USER_ID = mysql_real_escape_string($USER_ID);

$SITE_ID = stripslashes($SITE_ID);
$SITE_ID = mysql_real_escape_string($SITE_ID);

$CURRENT_LATITUDE = stripslashes($CURRENT_LATITUDE);
$CURRENT_LATITUDE = mysql_real_escape_string($CURRENT_LATITUDE);

$CURRENT_LONGITUDE = stripslashes($CURRENT_LONGITUDE);
$CURRENT_LONGITUDE = mysql_real_escape_string($CURRENT_LONGITUDE);

$START_INDEX_LOAD = stripslashes($START_INDEX_LOAD);
$START_INDEX_LOAD = mysql_real_escape_string($START_INDEX_LOAD);

$COUNT_LOAD = stripslashes($COUNT_LOAD);
$COUNT_LOAD = mysql_real_escape_string($COUNT_LOAD);

date_default_timezone_set('UTC');
$DATE_CREATED_SEC = $DATE_CREATED / 1000;
$DATE_CREATED_DATE = date("Y-m-d H:i:s", $DATE_CREATED_SEC);

$sqlLimit = "";
if ($START_INDEX_LOAD != "" && $COUNT_LOAD != "") {
	$sqlLimit = " LIMIT $START_INDEX_LOAD, $COUNT_LOAD ";
}

// Retrieve and save count of items, send back with each item result 
$sqlCounter = "SELECT COUNT(logs.LOG_ID) as LOG_COUNT
               FROM tbdivelog logs 
			   WHERE logs.DATE_ADDED >= '$DATE_CREATED_DATE'";
			   
$sqlTotalMinutes = "SELECT SUM(logs.DIVE_TIME) as TOTAL_MINUTES
	                FROM tbdivelog logs 
				    WHERE logs.DATE_ADDED >= '$DATE_CREATED_DATE'";

$sql = "SELECT logs.LOG_ID,
			   logs.SITE_ID,
			   logs.TIMESTAMP,
			   logs.AIR_TYPE,
			   logs.START_PRESSURE,
			   logs.END_PRESSURE,
			   logs.START_AIR_VALUE,
			   logs.START_AIR_UNITS,
			   logs.END_AIR_VALUE,
			   logs.END_AIR_UNITS,
			   logs.DIVE_TIME,
			   logs.MAX_DEPTH_VALUE,
			   logs.MAX_DEPTH_UNITS,
			   logs.AVERAGE_DEPTH_VALUE,
			   logs.AVERAGE_DEPTH_UNITS,
			   logs.SURFACE_TEMPERATURE_VALUE,
			   logs.SURFACE_TEMPERATURE_UNITS,
			   logs.WATER_TEMPERATURE_VALUE,
			   logs.WATER_TEMPERATURE_UNITS,
			   logs.VISIBILITY_VALUE,
			   logs.VISIBILITY_UNITS,
			   logs.WEIGHTS_REQUIRED_VALUE,
			   logs.WEIGHTS_REQUIRED_UNITS,
			   logs.SURFACE_TIME_VALUE,
			   logs.SURFACE_TIME_UNITS,
			   logs.RATING,
			   logs.COMMENTS,
			   logs.IS_COURSE,
			   logs.IS_PHOTO_VIDEO,
			   logs.IS_ICE,
			   logs.IS_DEEP,
			   logs.IS_INSTRUCTING,
			   logs.IS_NIGHT,
			   logs.LAST_MODIFIED,
			   users.USER_ID,
			   users.USERNAME
	 	FROM tbdivelog logs LEFT JOIN tbusers users ON logs.USER_ID = users.USER_ID 
		WHERE logs.DATE_ADDED >= '$DATE_CREATED_DATE'";
		
$condition = "";
if ($USER_ID != -1) {
	$condition = $condition . " AND logs.USER_ID = '$USER_ID'";
}

if ($SITE_ID != -1) {
	$condition = $condition . " AND logs.SITE_ID = '$SITE_ID'";
}

$sql = $sql . $condition;
$sqlCounter = $sqlCounter . $condition;
$sqlTotalMinutes = $sqlTotalMinutes . $condition;

$sql = $sql . " ORDER BY logs.TIMESTAMP DESC $sqlLimit";

// Get counter result and total minutes first
$LOG_COUNT = 0;
$resultCounter = mysql_query($sqlCounter, $con) or die(mysql_error());
if (mysql_num_rows($resultCounter) > 0) {
	$row = mysql_fetch_array($resultCounter);
	$LOG_COUNT = $row["LOG_COUNT"];
}

$TOTAL_MINUTES = 0;
$resultTotalMinutes = mysql_query($sqlTotalMinutes, $con) or die(mysql_error());
if (mysql_num_rows($resultTotalMinutes) > 0) {
	$row = mysql_fetch_array($resultTotalMinutes);
	$TOTAL_MINUTES = $row["TOTAL_MINUTES"];
}


$result = mysql_query($sql, $con) or die(mysql_error());
 
// check for empty result
if (mysql_num_rows($result) > 0) {
    // looping through all divelogs
 
    while ($row = mysql_fetch_array($result)) {
        // temp user array
    	$response["DIVELOGS"] = array();
				
        $divelogs = array();
		$divelogs["LOG_COUNT"] = $LOG_COUNT;
		$divelogs["TOTAL_MINUTES"] = $TOTAL_MINUTES;
        $divelogs["LOG_ID"] = $row["LOG_ID"];
		$divelogs["SITE_ID"] = $row["SITE_ID"];
		$divelogs["USER_ID"] = $row["USER_ID"];
		$divelogs["USERNAME"] = $row["USERNAME"];
		$divelogs["TIMESTAMP"] = $row["TIMESTAMP"];
		$divelogs["AIR_TYPE"] = $row["AIR_TYPE"];
		$divelogs["START_PRESSURE"] = $row["START_PRESSURE"];
		$divelogs["END_PRESSURE"] = $row["END_PRESSURE"];
		$divelogs["START_AIR_VALUE"] = $row["START_AIR_VALUE"];
		$divelogs["START_AIR_UNITS"] = $row["START_AIR_UNITS"];
		$divelogs["END_AIR_VALUE"] = $row["END_AIR_VALUE"];
		$divelogs["END_AIR_UNITS"] = $row["END_AIR_UNITS"];
		$divelogs["DIVE_TIME"] = $row["DIVE_TIME"];
		$divelogs["MAX_DEPTH_VALUE"] = $row["MAX_DEPTH_VALUE"];
		$divelogs["MAX_DEPTH_UNITS"] = $row["MAX_DEPTH_UNITS"];
		$divelogs["AVERAGE_DEPTH_VALUE"] = $row["AVERAGE_DEPTH_VALUE"];
		$divelogs["AVERAGE_DEPTH_UNITS"] = $row["AVERAGE_DEPTH_UNITS"];
		$divelogs["SURFACE_TEMPERATURE_VALUE"] = $row["SURFACE_TEMPERATURE_VALUE"];
		$divelogs["SURFACE_TEMPERATURE_UNITS"] = $row["SURFACE_TEMPERATURE_UNITS"];
		$divelogs["WATER_TEMPERATURE_VALUE"] = $row["WATER_TEMPERATURE_VALUE"];
		$divelogs["WATER_TEMPERATURE_UNITS"] = $row["WATER_TEMPERATURE_UNITS"];
		$divelogs["VISIBILITY_VALUE"] = $row["VISIBILITY_VALUE"];
		$divelogs["VISIBILITY_UNITS"] = $row["VISIBILITY_UNITS"];
		$divelogs["WEIGHTS_REQUIRED_VALUE"] = $row["WEIGHTS_REQUIRED_VALUE"];
		$divelogs["WEIGHTS_REQUIRED_UNITS"] = $row["WEIGHTS_REQUIRED_UNITS"];
		$divelogs["SURFACE_TIME_VALUE"] = $row["SURFACE_TIME_VALUE"];
		$divelogs["SURFACE_TIME_UNITS"] = $row["SURFACE_TIME_UNITS"];
		$divelogs["RATING"] = $row["RATING"];
		$divelogs["COMMENTS"] = $row["COMMENTS"];
		$divelogs["IS_COURSE"] = $row["IS_COURSE"];
		$divelogs["IS_PHOTO_VIDEO"] = $row["IS_PHOTO_VIDEO"];
		$divelogs["IS_ICE"] = $row["IS_ICE"];
		$divelogs["IS_DEEP"] = $row["IS_DEEP"];
		$divelogs["IS_INSTRUCTING"] = $row["IS_INSTRUCTING"];
		$divelogs["IS_NIGHT"] = $row["IS_NIGHT"];
		$divelogs["LAST_MODIFIED_ONLINE"] = $row["LAST_MODIFIED"] . " UTC";
		
        array_push($response["DIVELOGS"], $divelogs);
		
		// Now get dive log's buddies and sites
		$diveLogID = $divelogs["LOG_ID"];
		
		$sql = "SELECT LOG_BUDDY_ID,
					   LOG_ID,
					   USER_ID,
					   USERNAME
			    FROM tbdivelogbuddies WHERE LOG_ID = '$diveLogID'";
				
		$resultBuddy = mysql_query($sql, $con) or die(mysql_error());
 
		// check for empty result
		if (mysql_num_rows($resultBuddy) > 0) {
		    // looping through all buddies
		    $response["BUDDY_DIVELOG_".$diveLogID] = array();
			
			while ($rowBuddy = mysql_fetch_array($resultBuddy)) {
		        $diveLogBuddy = array();
			    $diveLogBuddy["LOG_BUDDY_ID"] = $rowBuddy["LOG_BUDDY_ID"];
				$diveLogBuddy["LOG_BUDDY_LOG_ID"] = $rowBuddy["LOG_ID"];
				$diveLogBuddy["LOG_BUDDY_DIVER_ID"] = $rowBuddy["USER_ID"];
				$diveLogBuddy["LOG_BUDDY_DIVER_USERNAME"] = $rowBuddy["USERNAME"];
				
				array_push($response["BUDDY_DIVELOG_".$diveLogID], $diveLogBuddy);
			}
		}
		
		$sql = "SELECT STOP_ID,
					   LOG_ID,
					   TIME,
					   DEPTH_VALUE,
					   DEPTH_UNITS				
			    FROM tbdivelogstops WHERE LOG_ID = '$diveLogID'";
				
		$resultStop = mysql_query($sql, $con) or die(mysql_error());
 
		// check for empty result
		if (mysql_num_rows($resultStop) > 0) {
		    // looping through all sites
		    $response["STOP_DIVELOG_".$diveLogID] = array();
			
			while ($rowSite = mysql_fetch_array($resultStop)) {
		        $diveLogStop = array();
			    $diveLogStop["LOG_STOP_ID"] = $rowSite["STOP_ID"];
				$diveLogStop["LOG_STOP_LOG_ID"] = $rowSite["LOG_ID"];
				$diveLogStop["LOG_STOP_TIME"] = $rowSite["TIME"];
				$diveLogStop["LOG_STOP_DEPTH_VALUE"] = $rowSite["DEPTH_VALUE"];
				$diveLogStop["LOG_STOP_DEPTH_UNITS"] = $rowSite["DEPTH_UNITS"];
				
				array_push($response["STOP_DIVELOG_".$diveLogID], $diveLogStop);
			}
		}
		
		// Now get dive log's site and pictures
		$DIVELOG_SITEID = $divelogs["SITE_ID"];
		$sql = "SELECT sites.SITE_ID,
			   sites.NAME,
			   sites.TOTAL_RATE,
			   sites.NUM_RATES,
			   sites.CITY,
			   sites.PROVINCE,
			   sites.COUNTRY,
			   sites.DIFFICULTY,
			   sites.ISSALT,
			   sites.ISFRESH,
			   sites.ISSHORE,
			   sites.ISBOAT,
			   sites.ISWRECK,
			   sites.HISTORY,
			   sites.DESCRIPTION,
			   sites.DIRECTIONS,
			   sites.SOURCE,
			   sites.NOTES,
			   sites.LATITUDE,
			   sites.LONGITUDE,
			   sites.ALTITUDE,
			   sites.APPROVED,
			   sites.USER_ID,
			   sites.DATE_ADDED,
			   sites.LAST_MODIFIED,
			   users.USER_ID,
			   users.USERNAME
	 	FROM tbdivesites sites LEFT JOIN tbusers users ON sites.USER_ID = users.USER_ID 
		WHERE sites.SITE_ID = '$DIVELOG_SITEID'";
				
		$resultSite = mysql_query($sql, $con) or die(mysql_error());
 
		// check for empty result
		if (mysql_num_rows($resultSite) > 0) {
		    // looping through all sites
		    $response["DIVESITES_DIVELOG_".$diveLogID] = array();
			
			while ($rowSite = mysql_fetch_array($resultSite)) {
		        $diveLogSite = array();
		        $diveLogSite["SITE_ID"] = $rowSite["SITE_ID"];
		        $diveLogSite["NAME"] = $rowSite["NAME"];
		        $diveLogSite["TOTAL_RATE"] = $rowSite["TOTAL_RATE"];
		        $diveLogSite["NUM_RATES"] = $rowSite["NUM_RATES"];
		        $diveLogSite["CITY"] = $rowSite["CITY"];
				$diveLogSite["PROVINCE"] = $rowSite["PROVINCE"];
				$diveLogSite["COUNTRY"] = $rowSite["COUNTRY"];
				$diveLogSite["DIFFICULTY"] = $rowSite["DIFFICULTY"];
				$diveLogSite["ISSALT"] = $rowSite["ISSALT"];
				$diveLogSite["ISFRESH"] = $rowSite["ISFRESH"];
				$diveLogSite["ISSHORE"] = $rowSite["ISSHORE"];
				$diveLogSite["ISBOAT"] = $rowSite["ISBOAT"];
				$diveLogSite["ISWRECK"] = $rowSite["ISWRECK"];
				$diveLogSite["HISTORY"] = $rowSite["HISTORY"];
				$diveLogSite["DESCRIPTION"] = $rowSite["DESCRIPTION"];
				$diveLogSite["DIRECTIONS"] = $rowSite["DIRECTIONS"];
				$diveLogSite["SOURCE"] = $rowSite["SOURCE"];
				$diveLogSite["NOTES"] = $rowSite["NOTES"];
				$diveLogSite["LATITUDE"] = $rowSite["LATITUDE"];
				$diveLogSite["LONGITUDE"] = $rowSite["LONGITUDE"];
				$diveLogSite["ALTITUDE"] = $rowSite["ALTITUDE"];
				$diveLogSite["APPROVED"] = $rowSite["APPROVED"];
				$diveLogSite["USER_ID"] = $rowSite["USER_ID"];
				$diveLogSite["DATE_ADDED"] = strtotime($rowSite["DATE_ADDED"]) * 1000;
				$diveLogSite["LAST_MODIFIED_ONLINE"] = $rowSite["LAST_MODIFIED"] . " UTC";
				$diveLogSite["USERNAME"] = $rowSite["USERNAME"];
				
				array_push($response["DIVESITES_DIVELOG_".$diveLogID], $diveLogSite);
				
				// Get Pictures for Dive Site		
				$sql = "SELECT PIC_ID, SITE_ID, PIC_DESC, FILE_NAME FROM tbsitepictures
						WHERE SITE_ID = '$DIVELOG_SITEID' ORDER BY PIC_ID";
						
				$resultPictures = mysql_query($sql, $con) or die(mysql_error());
				
				// check for empty result
				if (mysql_num_rows($resultPictures) > 0) {		 
					// temp user array
					$response["PICTURE_DIVESITE_" . $DIVELOG_SITEID] = array();
				    while ($rowPictures = mysql_fetch_array($resultPictures)) {
						$divesitePictures = array();
				        $divesitePictures["PIC_ID"] = $rowPictures["PIC_ID"];
				        $divesitePictures["SITE_ID"] = $rowPictures["SITE_ID"];
				        $divesitePictures["PIC_DESC"] = $rowPictures["PIC_DESC"];	
						
						if (trim($rowPictures["FILE_NAME"]) == "") {
							$divesitePictures["PIC_URL"] = "";
						} else {
							$divesitePictures["PIC_URL"] = "https://www.divethesite.com/siteImages/".$rowPictures["FILE_NAME"];	
						}
								
				        array_push($response["PICTURE_DIVESITE_" . $DIVELOG_SITEID], $divesitePictures);
					}
				}
			}
		} 
		
		// success
	    $response["SUCCESS"] = 1;
	 
	    // echoing JSON response  
	    echo json_encode($response);
		echo "\r\n";	
    }
} else {
    // no divelogs found but no error
	$response["DIVELOGS"] = array();
    $response["SUCCESS"] = 1;
 
    // echo no logs JSON
    echo json_encode($response);
}

mysql_close($con);