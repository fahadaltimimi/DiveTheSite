

<?php
 
/*
 * Following code will create a user account
 */
include('../variables/variables.php'); 
include('../includes/DiveSiteAuth.php');
  
// array for JSON response
$response = array();
$response["SUCCESS"] = 0;

// Get parameters
$UsernameEntered = $_POST['USERNAME'];
$Password = $_POST['PASSWORD'];
$Email = $_POST['EMAIL'];
$FirstName = $_POST['FIRST_NAME'];
$LastName = $_POST['LAST_NAME'];
$Country = $_POST['COUNTRY'];
$Province = $_POST['PROVINCE'];
$City = $_POST['CITY'];
$PictureURL = $_POST['PICTURE_URL'];

// If approved is blank, generate a key and email the user to confirm their registration
$EmailUser = false;
$Approved = $_POST['APPROVED'];
if ($Approved == "") {
	$Approved = md5(rand());
	$EmailUser = true;
}

// Connect to DB and run sql
$con = mysql_connect($DBHOST,$DBUSER,$DBPASS,$NEWLINK);
mysql_select_db($DBDIVE, $con);
mysql_query('SET CHARACTER SET utf8');

// To protect MySQL injection (more detail about MySQL injection)
$UsernameEntered = stripslashes($UsernameEntered);
$UsernameEntered = mysql_real_escape_string($UsernameEntered);

$Passwoard = stripslashes($Password);
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

$PictureURL = stripslashes($PictureURL);
$PictureURL = mysql_real_escape_string($PictureURL);

// If username is empty need to set a unique one
if (trim($UsernameEntered) == "") {
	$count = 1;
	$UsernameExists = true;	
	while ($UsernameExists) {
		// Check if username set is unique
		$UsernameEntered = "Diver ".$count;
		$sql = "SELECT USER_ID from tbusers WHERE username='$UsernameEntered'";
		$result = mysql_query($sql, $con) or die(mysql_error());

		if (mysql_num_rows($result) == 0) {
			$UsernameExists = false;
		} else {
			$count = $count + 1;
		}
	}	
}

// Verify if entered username or email exist
$UsernameExists = false;
$EmailExists = false;

$sql = "SELECT USER_ID from tbusers WHERE username='$UsernameEntered'";
$result = mysql_query($sql, $con) or die(mysql_error());

if (mysql_num_rows($result) > 0) {
	$UsernameExists = true;
}

$sql = "SELECT USER_ID from tbusers WHERE email='$Email'";
$result = mysql_query($sql, $con) or die(mysql_error());

if (mysql_num_rows($result) > 0) {
	$EmailExists = true;
}

if ($UsernameExists == false && $EmailExists == false) {
	// Create password
	$Password  = create_hash($Password);
	
	// Copy picture from given URL if not empty
	if ($PictureURL != "") {
		$dir = '../profileImages/';
		$PictureNewFileName = generate_file_name($PictureURL);
		$PictureNewUrl = $dir . $PictureNewFileName;
	
		$content = file_get_contents($PictureURL);
		$fp = fopen($PictureNewUrl, "w");
		fwrite($fp, $content);
		fclose($fp);
	}
	
	// Not a duplicate username, proceed with adding user
	$sql = "INSERT INTO tbusers
            (USERNAME, PASSWORD, SECURITY_TYPE, EMAIL, FIRST_NAME, LAST_NAME, CITY, PROVINCE, COUNTRY, BIO, PICTURE, APPROVED, CREATED, LAST_MODIFIED)
            VALUES
            (\"$UsernameEntered\",\"$Password\",\"1\",\"$Email\",\"$FirstName\",\"$LastName\",
            \"$City\",\"$Province\",\"$Country\",\"\",\"$PictureNewFileName\",\"$Approved\",NOW(),NOW())";
			
	mysql_query($sql, $con) or die(mysql_error());
	
	$response["DIVERS"] = array();
	
	$divers = array();
	
	$User_ID = mysql_insert_id();
	
    $divers["USER_ID"] = $User_ID;
    $divers["FIRST_NAME"] = $FirstName;
	$divers["LAST_NAME"] = $LastName;
    $divers["EMAIL"] = $Email;
	$divers["CITY"] = $City;
	$divers["PROVINCE"] = $Province;
	$divers["COUNTRY"] = $Country;
	$divers["USERNAME"] = $UsernameEntered;
	$divers["APPROVED"] = $Approved;
	$divers["BIO"] = "";
	$divers["IS_MOD"] = "0";
	$divers["LOG_COUNT"] = "0";
	$divers["DIVE_SITE_SUBMITTED_COUNT"] = "0";
	
	$response["SUCCESS"] = 1;
	array_push($response["DIVERS"], $divers);
	
	// Check if user needs to be emailed and do so if set
	if ($EmailUser == true) {
		$subject = "Welcome to DiveTheSite!";

        $message = "
          <html>
          <head>
          <title>Welcome to DiveTheSite!</title>
          </head>
          <body>
          <p><span style=\"font-weight:bold\">Welcome to DiveTheSite!</span></p>
          <p>Please click the following link to confirm your registration:</p>
          <p><a href='https://www.DiveTheSite.com/UserConfirmRegistration.php?USER_ID=$User_ID&KEY=$Approved'>Complete Registration!</a></p>
          </body>
          </html>
          ";
      
        $headers  = 'MIME-Version: 1.0' . "\r\n"; 
        $headers .= 'Content-type: text/html; charset=iso-8859-1' . "\r\n";
        $headers .= 'From: DiveTheSite <noreply@divethesite.com>' . "\r\n";
      
        $mail = mail($Email, $subject, $message, $headers);	
	}
	
} else {
	if ($UsernameExists == true && $EmailExists == true) {
		$response["MESSAGE"] = "Duplicate username and email found. Contact info@divethesite.com if you are unable to access your account.";	
	} else if ($UsernameExists == true) {
		$response["MESSAGE"] = "Duplicate username found. Contact info@divethesite.com if you are unable to access your account.";	
	} else if ($EmailExists == true) {
		$response["MESSAGE"] = "Duplicate email found. Contact info@divethesite.com if you are unable to access your account.";	
	}
}

echo json_encode($response);

mysql_close($con);


function generate_file_name($copyurl) {
    $ext = pathinfo($copyurl, PATHINFO_EXTENSION);
    $newName = substr(md5(rand()), 0, 10) . '.' . $ext;
    return $newName;
}

?>

		