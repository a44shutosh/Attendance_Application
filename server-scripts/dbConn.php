<?php
	 define('HOST','localhost');
	 define('USER','root');
	 define('PASS','password');
	 define('DB','NFC');
	 
	 
	// Create connection
	$conn = new mysqli(HOST, USER, PASS, DB);
	// Check connection
	if ($conn->connect_error) {
		die("Connection failed: " . $conn->connect_error);
	}
?>
