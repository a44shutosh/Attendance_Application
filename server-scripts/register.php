<?php
	if($_SERVER['REQUEST_METHOD']=='POST'){
		$rollno = $_POST['rollNo'];
		$tagkey = $_POST['tagKey'];

		require_once('dbConn.php');
		
        if(!($stmt=$conn->prepare('UPDATE NFC.STUDENT SET TAGKEY=? WHERE STUDENT_ID=?;'))){
            echo 'Prepare failed.'.$conn->error;
        }
        
        if (!($stmt->bind_param("ss", $tagkey, $rollno))) {
            echo 'Binding parameters failed: (' . $stmt->errno . ') ' . $stmt->error;
        }
        
        if (!($stmt->execute())) {
            echo 'Execute failed: (' . $stmt->errno . ') ' . $stmt->error;
        }else{
            if($stmt->affected_rows == 1){
                echo '200';
            }else{
                if($result=$conn->query("SELECT * FROM NFC.STUDENT WHERE STUDENT_ID='$rollno'")){
                        if($result->num_rows ==1){
                            echo '300';
                        }else{
                            echo '301';
                        }
                }else{
                    echo '500';
                }
            }
        }
	}else{
		echo '501';
	}
    //to close the connection
    mysqli_close($conn);
?>
