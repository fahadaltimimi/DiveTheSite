

<?php
 
/*
 * Following code will publish a single dive site
 */
include('../variables/variables.php'); 

ob_start();
    
// array for JSON response
$response = array();
$response["SUCCESS"] = 0;

// Get parameters
$DiveSiteID = $_POST['DIVE_SITE_ID'];
$DiveSiteName = $_POST['DIVE_SITE_NAME'];
$TotalRating = $_POST['TOTAL_RATING'];
$NumRates = $_POST['NUM_RATES'];
$Difficulty = $_POST['DIFFICULTY'];
$Country = $_POST['COUNTRY'];
$Province = $_POST['PROVINCE'];
$City = $_POST['CITY'];
$UserID = $_POST['USER_ID'];
$Username = $_POST['USERNAME'];
$IsSalt = $_POST['IS_SALT'];
$IsFresh = $_POST['IS_FRESH'];
$IsShore = $_POST['IS_SHORE'];
$IsBoat = $_POST['IS_BOAT'];
$IsWreck = $_POST['IS_WRECK'];
$History = $_POST['HISTORY'];
$Description = $_POST['DESCRIPTION'];
$Directions = $_POST['DIRECTIONS'];
$Source = $_POST['SOURCE'];
$Notes = $_POST['NOTES'];
$Latitude = $_POST['LATITUDE'];
$Longitude = $_POST['LONGITUDE'];
$DateAdded = $_POST['DATE_ADDED'];
$LastModified = $_POST['LAST_MODIFIED'];
$Approved = $_POST['APPROVED'];
$PictureCount = $_POST['PICTURE_COUNT'];

// Connect to DB and run sql
$con = mysql_connect($DBHOST,$DBUSER,$DBPASS,$NEWLINK);
mysql_select_db($DBDIVE, $con);
mysql_query('SET CHARACTER SET utf8');

// To protect MySQL injection (more detail about MySQL injection)
$DiveSiteID = stripslashes($DiveSiteID);
$DiveSiteID = mysql_real_escape_string($DiveSiteID);

$DiveSiteName  = stripslashes($DiveSiteName);
$DiveSiteName  = mysql_real_escape_string($DiveSiteName);

$TotalRating = stripslashes($TotalRating);
$TotalRating = mysql_real_escape_string($TotalRating);

$NumRates = stripslashes($NumRates);
$NumRates = mysql_real_escape_string($NumRates);

$Difficulty = stripslashes($Difficulty);
$Difficulty = mysql_real_escape_string($Difficulty);

$Country = stripslashes($Country);
$Country = mysql_real_escape_string($Country);

$Province = stripslashes($Province);
$Province = mysql_real_escape_string($Province);

$City = stripslashes($City);
$City = mysql_real_escape_string($City);

$UserID = stripslashes($UserID);
$UserID = mysql_real_escape_string($UserID);

$Username = stripslashes($Username);
$Username = mysql_real_escape_string($Username);

$IsSalt = stripslashes($IsSalt);
$IsSalt = mysql_real_escape_string($IsSalt);

$IsFresh = stripslashes($IsFresh);
$IsFresh = mysql_real_escape_string($IsFresh);

$IsShore = stripslashes($IsShore);
$IsShore = mysql_real_escape_string($IsShore);

$IsBoat = stripslashes($IsBoat);
$IsBoat = mysql_real_escape_string($IsBoat);

$IsWreck = stripslashes($IsWreck);
$IsWreck = mysql_real_escape_string($IsWreck);

$History = stripslashes($History);
$History = mysql_real_escape_string($History);

$Description = stripslashes($Description);
$Description = mysql_real_escape_string($Description);

$Directions = stripslashes($Directions);
$Directions = mysql_real_escape_string($Directions);

$Source = stripslashes($Source);
$Source = mysql_real_escape_string($Source);

$Notes = stripslashes($Notes);
$Notes = mysql_real_escape_string($Notes);

$Latitude = stripslashes($Latitude);
$Latitude = mysql_real_escape_string($Latitude);

$Longitude = stripslashes($Longitude);
$Longitude = mysql_real_escape_string($Longitude);

$DateAdded = stripslashes($DateAdded);
$DateAdded = mysql_real_escape_string($DateAdded);

$LastModified = stripslashes($LastModified);
$LastModified = mysql_real_escape_string($LastModified);

$Approved = stripslashes($Approved);
$Approved = mysql_real_escape_string($Approved);

$PictureCount = stripslashes($PictureCount);
$PictureCount = mysql_real_escape_string($PictureCount);

// If dive site id was given, make sure it still exists before trying to update
if ($DiveSiteID != -1) {
	$sql = "SELECT SITE_ID FROM tbdivesites WHERE SITE_ID ='$DiveSiteID'";
	$result = mysql_query($sql, $con) or die(mysql_error());
	
	if (mysql_num_rows($result) == 0) {
		$DiveSiteID = -1;
	}
}

if ($DiveSiteID == -1) {
	// New Dive Site, insert new site		
	$sql = "INSERT INTO tbdivesites (NAME, TOTAL_RATE, NUM_RATES, DIFFICULTY, COUNTRY, PROVINCE, CITY, USER_ID,
	                                 ISSALT, ISFRESH, ISSHORE, ISBOAT, ISWRECK, HISTORY, DESCRIPTION, DIRECTIONS, SOURCE,
									 NOTES, LATITUDE, LONGITUDE, DATE_ADDED, LAST_MODIFIED, APPROVED)
		    VALUES (\"$DiveSiteName\", \"$TotalRating\", \"$NumRates\", \"$Difficulty\", \"$Country\",
			        \"$Province\", \"$City\", \"$UserID\", \"$IsSalt\",
					\"$IsFresh\", \"$IsShore\", \"$IsBoat\", \"$IsWreck\", \"$History\", 
					\"$Description\", \"$Directions\", \"$Source\", \"$Notes\", \"$Latitude\",
					\"$Longitude\", UTC_TIMESTAMP(), UTC_TIMESTAMP(), \"$Approved\")";	
				
	mysql_query($sql, $con) or die(mysql_error());
	$DiveSiteID = mysql_insert_id();
} else {
	// ID already exists, update existing site
	$sql = "UPDATE tbdivesites
			SET NAME = \"$DiveSiteName\",
			    TOTAL_RATE = \"$TotalRating\",
				NUM_RATES = \"$NumRates\",
				DIFFICULTY = \"$Difficulty\",
				COUNTRY = \"$Country\",
				PROVINCE = \"$Province\",
				CITY = \"$City\",
				USER_ID = \"$UserID\",
				ISSALT = \"$IsSalt\",
				ISFRESH = \"$IsFresh\",
				ISSHORE = \"$IsShore\",
				ISBOAT = \"$IsBoat\",
				ISWRECK = \"$IsWreck\",
				HISTORY = \"$History\",
				DESCRIPTION = \"$Description\",
				DIRECTIONS = \"$Directions\",
				SOURCE = \"$Source\",
				NOTES = \"$Notes\",
				LATITUDE = \"$Latitude\",
				LONGITUDE = \"$Longitude\",
				LAST_MODIFIED = UTC_TIMESTAMP(),
				APPROVED = \"$Approved\"
			WHERE SITE_ID = '$DiveSiteID'";
	mysql_query($sql, $con) or die(mysql_error());
	
	// Delete existing pictures, will be readded
	$sql = "SELECT FILE_NAME FROM tbsitepictures WHERE SITE_ID = '$DiveSiteID'";
	$result = mysql_query($sql, $con) or die(mysql_error());
 
	while ($row = mysql_fetch_array($result)) {
		$FileName = $row["FILE_NAME"];	
		unlink("../siteImages/" . $FileName);
	}
	
	$sql = "DELETE FROM tbsitepictures WHERE SITE_ID = '$DiveSiteID'"; 
	mysql_query($sql, $con) or die(mysql_error());
}

// Get and insert pictures
$response["PICTURE_DIVESITE_" . $DiveSiteID] = array();
for ($i = 0; $i < $PictureCount; $i++) {
	$LocalID = $_POST['PIC_LOCAL_ID' .$i];
	$SiteID = $_POST['SITE_ID' . $i] ;
	$Description = $_POST['PIC_DESC' . $i] ;
	$URL = $_POST['PIC_URL' . $i] ;
	$FilePath = $_POST['FILE_PATH' . $i];
	$NewImageEncoded = $_POST['NEW_IMAGE' . $i] ;
	
	$LocalID = stripslashes($LocalID);
	$LocalID = mysql_real_escape_string($LocalID);
	
	$SiteID = stripslashes($SiteID);
	$SiteID = mysql_real_escape_string($SiteID);
	
	$Description = stripslashes($Description);
	$Description = mysql_real_escape_string($Description);
	
	$URL = stripslashes($URL);
	$URL = mysql_real_escape_string($URL);
	$FileName = basename($URL);
	
	$FilePath = stripslashes($FilePath);
	$FilePath = mysql_real_escape_string($FilePath);
	
	if ($NewImageEncoded != "") {
		$NewProfileImage = base64_decode($NewImageEncoded);
		
		// Need unique file name
		$FileName = uniqid('SITE_' . $DiveSiteID . '_', true);
		file_put_contents("../siteImages/" . $FileName, $NewProfileImage);
	}
	
	$sql = "INSERT INTO tbsitepictures (SITE_ID, PIC_DESC, FILE_NAME)
	        VALUES (\"$DiveSiteID\", \"$Description\", \"$FileName\")";
	mysql_query($sql, $con) or die(mysql_error());
	$PictureID = mysql_insert_id();
	
	// Save dive site pictures
	$divesitePictures = array();
	$divesitePictures["PIC_ID"] = $PictureID;
	$divesitePictures["PIC_LOCAL_ID"] = $LocalID;
    $divesitePictures["SITE_ID"] = $DiveSiteID;
    $divesitePictures["PIC_DESC"] = $Description;	
	$divesitePictures["PIC_URL"] = "https://www.divethesite.com/siteImages/".$FileName;
	$divesitePictures["FILE_PATH"] = $FilePath;
				
    array_push($response["PICTURE_DIVESITE_" . $DiveSiteID], $divesitePictures);
}

// Now retrieve the updated dive site to get the new modified date
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
		WHERE sites.SITE_ID = '$DiveSiteID'";

$result = mysql_query($sql, $con) or die(mysql_error());
 
// check for empty result
if (mysql_num_rows($result) > 0) {
    // Should return one dive site if successful
    $response["DIVESITES"] = array();
 
    $row = mysql_fetch_array($result);
	
    // temp user array
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
	
    // push single product into final response array
    array_push($response["DIVESITES"], $divesites);
	
	$SITE_ID = $divesites["SITE_ID"];
	
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
			$divelogs["LAST_MODIFIED_ONLINE"] = $rowDiveLogs["LAST_MODIFIED"] . "UTC";
			
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
} else {
    // no divesite found, error must have occured with saving
    $response["SUCCESS"] = 0;    
}

ob_end_clean();

echo json_encode($response);

mysql_close($con);

?>