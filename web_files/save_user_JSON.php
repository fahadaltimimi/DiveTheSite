

<?php
 
/*
 * Following code will save diver data
 */
include('../variables/variables.php'); 
include('../includes/DiveSiteAuth.php');
  
// array for JSON response
$response = array();
$response["SUCCESS"] = 0;

// Get parameters
$Username = $_POST['USERNAME'];
$Password = $_POST['PASSWORD'];
$Email = $_POST['EMAIL'];
$FirstName = $_POST['FIRST_NAME'];
$LastName = $_POST['LAST_NAME'];
$Country = $_POST['COUNTRY'];
$Province = $_POST['PROVINCE'];
$City = $_POST['CITY'];
$Bio = $_POST['BIO'];
$UserID = $_POST['USER_ID'];
$CertifCount = $_POST['CERTIF_COUNT'];
$NewProfileImageEncoded = $_POST['NEW_PICTURE'];

// Connect to DB and run sql
$con = mysql_connect($DBHOST,$DBUSER,$DBPASS,$NEWLINK);
mysql_select_db($DBDIVE, $con);
mysql_query('SET CHARACTER SET utf8');

// To protect MySQL injection (more detail about MySQL injection)
$Username = stripslashes($Username);
$Username = mysql_real_escape_string($Username);

$Password = stripslashes($Password);
$Password = mysql_real_escape_string($Password);

$Email = stripslashes($Email);
$Email = mysql_real_escape_string($Email);

$FirstName = stripslashes($FirstName);
$FirstName = mysql_real_escape_string($FirstName);

$LastName = stripslashes($LastName);
$LastName = mysql_real_escape_string($LastName);

$Country = stripslashes($Country);
$Country = mysql_real_escape_string($Country);

$Province = stripslashes($Province);
$Province = mysql_real_escape_string($Province);

$City = stripslashes($City);
$City = mysql_real_escape_string($City);

$Bio = stripslashes($Bio);
$Bio = mysql_real_escape_string($Bio);

$UserID = stripslashes($UserID);
$UserID = mysql_real_escape_string($UserID);

// Get user's existing info to check password and if email, username changed, and to save
$sql = "SELECT *, 
  			   (SELECT COUNT(logs.LOG_ID) FROM tbdivelog logs WHERE logs.USER_ID = users.USER_ID) as LOG_COUNT,
  			   (SELECT COUNT(sites.SITE_ID) FROM tbdivesites sites WHERE sites.USER_ID = users.USER_ID) as DIVE_SITE_SUBMITTED_COUNT 
		FROM tbusers users WHERE USER_ID='$UserID'";
$result = mysql_query($sql, $con) or die(mysql_error());

$UsernameCurrent = "";
$EmailCurrent = "";
$PasswordCurrent = "";
$SecurityTypeCurrent = 1;
$isMod = 0;
$PictureFileName = "";
$Created = "";
$LastModified = "";
$LogCount = "0";
$DiveSiteSubmitted = "0";

if (mysql_num_rows($result) > 0) {
	$row = mysql_fetch_array($result);
	
	$UsernameCurrent = $row["USERNAME"];
	$EmailCurrent = $row["EMAIL"];
	$PasswordCurrent = $row["PASSWORD"];
	$SecurityTypeCurrent = $row["SECURITY_TYPE"];
	$isMod = $row["IS_MOD"];
	$PictureFileName = $row["PICTURE"];
	$Created = $row["CREATED"];
	$LastModified = $row["LAST_MODIFIED"];
	$LogCount = $row["LOG_COUNT"];
	$DiveSiteSubmitted  = $row["DIVE_SITE_SUBMITTED_COUNT"];
}

// Check that user's password is good
$PW_Validation = false;
if($SecurityTypeCurrent == 0){
	$PW_Validation = md5($Password) == $PasswordCurrent;
}
else { 			
	$PW_Validation = validate_password($Password, $PasswordCurrent);
}

if ($PW_Validation == true) {

	$UsernameExists = false;
	$EmailExists = false;

	if ($Username != $UsernameCurrent) {
		// Check if new username exists
		$sql = "SELECT USER_ID from tbusers WHERE username='$UsernameEntered'";
		$result = mysql_query($sql, $con) or die(mysql_error());

		if (mysql_num_rows($result) > 0) {
			$UsernameExists = true;
		}	
	}

	if ($Email != $EmailCurrent) {
		// Check if new email exists
		$sql = "SELECT USER_ID from tbusers WHERE email='$Email'";
		$result = mysql_query($sql, $con) or die(mysql_error());

		if (mysql_num_rows($result) > 0) {
			$EmailExists = true;
		}	
	}

	if ($UsernameExists == false && $EmailExists == false) {
		// Save new profile image if one was given
		if ($NewProfileImageEncoded != "") {
			$NewProfileImage = base64_decode($NewProfileImageEncoded);
			$PictureFileName = $Username.".jpg";
			file_put_contents("../profileImages/".$PictureFileName, $NewProfileImage);
		}
		
		$response["DIVERS"] = array();
		$response["DIVER_CERTIFICATIONS_DIVER_".$UserID] = array();
		
		// Not a duplicate username, proceed with editing user
		$sql = "UPDATE tbusers
				SET USERNAME = \"$Username\",
					EMAIL = \"$Email\",
					FIRST_NAME = \"$FirstName\",
					LAST_NAME = \"$LastName\",
					CITY = \"$City\",
					PROVINCE = \"$Province\",
					COUNTRY = \"$Country\",
					BIO = \"$Bio\",
					PICTURE = \"$PictureFileName\",
					LAST_MODIFIED = NOW()
				WHERE USER_ID = $UserID;";
				
		mysql_query($sql, $con) or die(mysql_error());
	
		$divers = array();
		
	    $divers["USER_ID"] = $UserID;
	    $divers["FIRST_NAME"] = $FirstName;
		$divers["LAST_NAME"] = $LastName;
	    $divers["EMAIL"] = $Email;
		$divers["CITY"] = $City;
		$divers["PROVINCE"] = $Province;
		$divers["COUNTRY"] = $Country;
		$divers["USERNAME"] = $Username;
		$divers["BIO"] = "$Bio";
		$divers["IS_MOD"] = $isMod;
		$divers["PICTURE_URL"] = "https://www.divethesite.com/profileImages/".$PictureFileName;
		$divers["CREATED"] = $Created;
		$divers["LOG_COUNT"] = $LogCount;
		$divers["DIVE_SITE_SUBMITTED_COUNT"] = $DiveSiteSubmitted;
		
		$response["SUCCESS"] = 1;
		array_push($response["DIVERS"], $divers);
		
		// Get and update certifications
		for ($i = 0; $i < $CertifCount; $i++) {
			$CertifID = $_POST['CERTIF_ID_' . $i] ;
			$CertifUserID = $_POST['CERTIF_USER_ID_' . $i];
			$CertifTitle = $_POST['CERTIF_TITLE_' . $i];
			$CertifNumber = $_POST['CERTIF_NUMBER_' . $i];
			$CertifLocation = $_POST['CERTIF_LOCATION_' . $i];
			$CertifDate = $_POST['CERTIF_DATE_' . $i];
			$CertifPrimary = $_POST['CERTIF_PRIMARY_' . $i];
			
			$CertifID = stripslashes($CertifID);
			$CertifID = mysql_real_escape_string($CertifID);
			
			$CertifUserID = stripslashes($CertifUserID);
			$CertifUserID = mysql_real_escape_string($CertifUserID);
			
			$CertifTitle = stripslashes($CertifTitle);
			$CertifTitle = mysql_real_escape_string($CertifTitle);
			
			$CertifNumber = stripslashes($CertifNumber);
			$CertifNumber = mysql_real_escape_string($CertifNumber);
			
			$CertifLocation = stripslashes($CertifLocation);
			$CertifLocation = mysql_real_escape_string($CertifLocation);
			
			$CertifDate = stripslashes($CertifDate);
			$CertifDate = mysql_real_escape_string($CertifDate);
			
			$CertifPrimary = stripslashes($CertifPrimary);
			$CertifPrimary = mysql_real_escape_string($CertifPrimary);
			
			$sql = "";
		 	if ($CertifID <= 0) {
				// Inserting a new certification for the user
				$sql = "INSERT INTO tbusercerts 
							   (CERTIF_NAME, CERTIF_DATE, CERTIF_NO, INSTR_NO, INSTR_NAME, LOCATION, USER_ID, IS_PRIMARY)
				        VALUES (\"$CertifTitle\", \"$CertifDate\", \"$CertifNumber\", 
						        \"\", \"\", \"$CertifLocation\", \"$CertifUserID\", \"$CertifPrimary\");";
						
				mysql_query($sql, $con) or die(mysql_error());
				$CertifID = mysql_insert_id();
			} else {
				// Updating existing certification
				$sql = "UPDATE tbusercerts
				 		SET CERTIF_NAME = \"$CertifTitle\",
							CERTIF_DATE = \"$CertifDate\",
							CERTIF_NO = \"$CertifNumber\",
							INSTR_NO = \"\",
							INSTR_NAME = \"\",
							LOCATION = \"$CertifLocation\",
							USER_ID = \"$CertifUserID\",
							IS_PRIMARY = \"$CertifPrimary\"
						WHERE CERTIF_ID = $CertifID;";
			
				mysql_query($sql, $con) or die(mysql_error());	
			}
			
			$diverCertifications = array();
		    $diverCertifications["CERTIF_ID"] = $CertifID;
			$diverCertifications["USER_ID"] = $CertifUserID;
			$diverCertifications["CERTIF_NAME"] = $CertifTitle;
			$diverCertifications["CERTIF_DATE"] = $CertifDate;
			$diverCertifications["CERTIF_NO"] = $CertifNumber;
			$diverCertifications["INSTR_NO"] = "";
			$diverCertifications["INSTR_NAME"] = "";
			$diverCertifications["LOCATION"] = $CertifLocation;
			$diverCertifications["IS_PRIMARY"] = $CertifPrimary;
			
			array_push($response["DIVER_CERTIFICATIONS_DIVER_".$CertifUserID], $diverCertifications);
		}
		
	} else {
		$response["SUCCESS"] = 0;
	    
		if ($UsernameExists == true && $EmailExists == true) {
			$response["MESSAGE"] = "Username and email not available.";	
		} else if ($UsernameExists == true) {
			$response["MESSAGE"] = "Username not avaiable.";	
		} else if ($EmailExists == true) {
			$response["MESSAGE"] = "Email already exists. Contact info@divethesite.com if you are unable to access your account.";	
		}
	}
} else {
	$response["SUCCESS"] = 0;
	$response["MESSAGE"] = "Password Incorrect.";	
}

echo json_encode($response);

mysql_close($con);


function generate_file_name($copyurl) {
    $ext = pathinfo($copyurl, PATHINFO_EXTENSION);
    $newName = substr(md5(rand()), 0, 10) . '.' . $ext;
    return $newName;
}

?>

		