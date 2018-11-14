

<?php
 
/*
 * Following code get a single dive sites
 */
include('../variables/variables.php'); 
  
// array for JSON response
$response = array();
$response["SUCCESS"] = 0;

// Get inputted params
$LAST_MODIFIED = 1;
if (isset($_GET["LAST_MODIFIED"]))
   $LAST_MODIFIED = $_GET["LAST_MODIFIED"];
   
$SITE_ID = -1;
if (isset($_GET["DIVE_SITE_ID"]))
   $SITE_ID = $_GET["DIVE_SITE_ID"];
            
// Connect to DB and run sql
$con = mysql_connect($DBHOST,$DBUSER,$DBPASS,$NEWLINK);
mysql_select_db($DBDIVE, $con);
mysql_query('SET CHARACTER SET utf8');

// To protect MySQL injection (more detail about MySQL injection)
$LAST_MODIFIED = stripslashes($LAST_MODIFIED);
$LAST_MODIFIED = mysql_real_escape_string($LAST_MODIFIED);

$SITE_ID = stripslashes($SITE_ID);
$SITE_ID = mysql_real_escape_string($SITE_ID);
 
date_default_timezone_set('UTC');
$LAST_MODIFIED_SEC = $LAST_MODIFIED / 1000;
$LAST_MODIFIED_DATE = date("Y-m-d H:i:s", $LAST_MODIFIED_SEC);

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
		WHERE sites.SITE_ID = '$SITE_ID' AND sites.LAST_MODIFIED >= '$LAST_MODIFIED_DATE'";
		
$result = mysql_query($sql, $con) or die(mysql_error());
 
// check for empty result
if (mysql_num_rows($result) > 0) {
    $row = mysql_fetch_array($result);
        
	// temp user array
	$response["DIVESITES"] = array();
    
	$divesites = array();
    $divesites["SITE_ID"] = $row["SITE_ID"];
    $divesites["NAME"] = $row["NAME"];
    $divesites["TOTAL_RATE"] = $row["TOTAL_RATE"];
    $divesites["NUM_RATES"] = $row["NUM_RATES"];
    $divesites["CITY"] = $row["CITY"];
	$divesites["PROVINCE"] = $row["PROVINCE"];
	$divesites["COUNTRY"] = $row["COUNTRY"];
	$divesites["DIFFICULTY"] = $row["DIFFICULTY"];
	$divesites["ISSALT"] = $row["ISSALT"];
	$divesites["ISFRESH"] = $row["ISFRESH"];
	$divesites["ISSHORE"] = $row["ISSHORE"];
	$divesites["ISBOAT"] = $row["ISBOAT"];
	$divesites["ISWRECK"] = $row["ISWRECK"];
	$divesites["HISTORY"] = $row["HISTORY"];
	$divesites["DESCRIPTION"] = $row["DESCRIPTION"];
	$divesites["DIRECTIONS"] = $row["DIRECTIONS"];
	$divesites["SOURCE"] = $row["SOURCE"];
	$divesites["NOTES"] = $row["NOTES"];
	$divesites["LATITUDE"] = $row["LATITUDE"];
	$divesites["LONGITUDE"] = $row["LONGITUDE"];
	$divesites["ALTITUDE"] = $row["ALTITUDE"];
	$divesites["APPROVED"] = $row["APPROVED"];
	$divesites["USER_ID"] = $row["USER_ID"];
	$divesites["DATE_ADDED"] = strtotime($row["DATE_ADDED"]) * 1000;
	$divesites["LAST_MODIFIED_ONLINE"] = $row["LAST_MODIFIED"] . " UTC";
	$divesites["USERNAME"] = $row["USERNAME"];
	
    array_push($response["DIVESITES"], $divesites);

	$SITE_ID = $divesites["SITE_ID"];
	
	// Get Pictures for Dive Site		
	$sql = "SELECT PIC_ID, SITE_ID, PIC_DESC, FILE_NAME FROM tbsitepictures
			WHERE SITE_ID = '$SITE_ID' ORDER BY PIC_ID";
			
	$resultPictures = mysql_query($sql, $con) or die(mysql_error());
	
	// check for empty result
	if (mysql_num_rows($resultPictures) > 0) {		 
		// temp user array
		$response["PICTURE_DIVESITE_" . $SITE_ID] = array();
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
					
			
	        array_push($response["PICTURE_DIVESITE_" . $SITE_ID], $divesitePictures);
		}
	}
	
	// Get Logs for Dive Site
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
	WHERE logs.SITE_ID = '$SITE_ID' ORDER BY LOG_ID";
	
	$resultsDiveLogs = mysql_query($sql, $con) or die(mysql_error());
	 
	// check for empty result
	if (mysql_num_rows($resultsDiveLogs) > 0) {
	    // looping through all divelogs
	    $response["DIVELOGS_DIVESITE_" . $SITE_ID] = array();
	 
	    while ($rowDiveLogs = mysql_fetch_array($resultsDiveLogs)) {
	        // temp user array
	        $divelogs = array();
	        $divelogs["LOG_ID"] = $rowDiveLogs["LOG_ID"];
			$divelogs["SITE_ID"] = $rowDiveLogs["SITE_ID"];
			$divelogs["USER_ID"] = $rowDiveLogs["USER_ID"];
			$divelogs["USERNAME"] = $rowDiveLogs["USERNAME"];
			$divelogs["TIMESTAMP"] = $rowDiveLogs["TIMESTAMP"];
			$divelogs["AIR_TYPE"] = $rowDiveLogs["AIR_TYPE"];
			$divelogs["START_PRESSURE"] = $rowDiveLogs["START_PRESSURE"];
			$divelogs["END_PRESSURE"] = $rowDiveLogs["END_PRESSURE"];
			$divelogs["START_AIR_VALUE"] = $rowDiveLogs["START_AIR_VALUE"];
			$divelogs["START_AIR_UNITS"] = $rowDiveLogs["START_AIR_UNITS"];
			$divelogs["END_AIR_VALUE"] = $rowDiveLogs["END_AIR_VALUE"];
			$divelogs["END_AIR_UNITS"] = $rowDiveLogs["END_AIR_UNITS"];
			$divelogs["DIVE_TIME"] = $rowDiveLogs["DIVE_TIME"];
			$divelogs["MAX_DEPTH_VALUE"] = $rowDiveLogs["MAX_DEPTH_VALUE"];
			$divelogs["MAX_DEPTH_UNITS"] = $rowDiveLogs["MAX_DEPTH_UNITS"];
			$divelogs["AVERAGE_DEPTH_VALUE"] = $rowDiveLogs["AVERAGE_DEPTH_VALUE"];
			$divelogs["AVERAGE_DEPTH_UNITS"] = $rowDiveLogs["AVERAGE_DEPTH_UNITS"];
			$divelogs["SURFACE_TEMPERATURE_VALUE"] = $rowDiveLogs["SURFACE_TEMPERATURE_VALUE"];
			$divelogs["SURFACE_TEMPERATURE_UNITS"] = $rowDiveLogs["SURFACE_TEMPERATURE_UNITS"];
			$divelogs["WATER_TEMPERATURE_VALUE"] = $rowDiveLogs["WATER_TEMPERATURE_VALUE"];
			$divelogs["WATER_TEMPERATURE_UNITS"] = $rowDiveLogs["WATER_TEMPERATURE_UNITS"];
			$divelogs["VISIBILITY_VALUE"] = $rowDiveLogs["VISIBILITY_VALUE"];
			$divelogs["VISIBILITY_UNITS"] = $rowDiveLogs["VISIBILITY_UNITS"];
			$divelogs["WEIGHTS_REQUIRED_VALUE"] = $rowDiveLogs["WEIGHTS_REQUIRED_VALUE"];
			$divelogs["WEIGHTS_REQUIRED_UNITS"] = $rowDiveLogs["WEIGHTS_REQUIRED_UNITS"];
			$divelogs["SURFACE_TIME_VALUE"] = $rowDiveLogs["SURFACE_TIME_VALUE"];
			$divelogs["SURFACE_TIME_UNITS"] = $rowDiveLogs["SURFACE_TIME_UNITS"];
			$divelogs["RATING"] = $rowDiveLogs["RATING"];
			$divelogs["COMMENTS"] = $rowDiveLogs["COMMENTS"];
			$divelogs["IS_COURSE"] = $rowDiveLogs["IS_COURSE"];
			$divelogs["IS_PHOTO_VIDEO"] = $rowDiveLogs["IS_PHOTO_VIDEO"];
			$divelogs["IS_ICE"] = $rowDiveLogs["IS_ICE"];
			$divelogs["IS_DEEP"] = $rowDiveLogs["IS_DEEP"];
			$divelogs["IS_INSTRUCTING"] = $rowDiveLogs["IS_INSTRUCTING"];
			$divelogs["IS_NIGHT"] = $rowDiveLogs["IS_NIGHT"];
			$divelogs["LAST_MODIFIED_ONLINE"] = $rowDiveLogs["LAST_MODIFIED"] . " UTC";
			
	        array_push($response["DIVELOGS_DIVESITE_" . $SITE_ID], $divelogs);
			
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
	    }
	}
	
	// success
    $response["SUCCESS"] = 1;
 
    // echoing JSON response  
    echo json_encode($response);
	echo "\r\n";	
} else {
    // no divesites found but no error
	$response["DIVESITES"] = array();
    $response["SUCCESS"] = 1;
 
    // echo no divesites JSON
    echo json_encode($response);
}

mysql_close($con);