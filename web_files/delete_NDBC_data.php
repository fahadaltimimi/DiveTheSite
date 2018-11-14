

<?php
 
/*
* Following will delete NDBC files no longer needed (already processed) so that updated ones can be downloaded
*/

// set up basic connection
$ftp_server = "divethesite.com";
$conn_id = ftp_connect($ftp_server);

// login with username and password
$ftp_user_name = "divethesitecrons";
$ftp_user_pass = "88iNuzKJ";
$login_result = ftp_login($conn_id, $ftp_user_name, $ftp_user_pass);

// check connection
if ((!$conn_id) || (!$login_result)) {
    die("FTP connection has failed !");
}

// browse to directory
$logs_dir = "www.ndbc.noaa.gov/data/realtime2";
ftp_chdir($conn_id, $logs_dir);

// delete all files in directory
$files = ftp_nlist($conn_id, ".");
foreach ($files as $file)
{
	ftp_delete($conn_id, $file);
}

// close the connection
ftp_close($conn_id);    

?>
        