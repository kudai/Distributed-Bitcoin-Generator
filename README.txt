Distributed Operating Systems : Project 1

Name:Kushagra Udai
Email: kudai@ufl.edu
UFID: 0937-7483

This project is to be run solely as two projects on eclipse for Scala. 
And N = 1000000, K = 10, np = 4 by default.
The results were obtained on a Windows 8.1, i7 - 4510U: 1 processor of 2 cores
The BCClient.scala file need not be modified and the BCServer.scala file needs to be modified to take the IP of the client in the configuration where hostname is specified. This is line 29 of the file.

The default work unit of the code is 1000 messages for 4 actors which all use strings of length 10. he code can be modified to generate larger (or smaller) strings at line 101 in BCClient.scala
The program has been run with 2 machines as a distributed system.

The largest bitcoins that were found were for 5 leading zeros. Two such coins were found.
kudai1nu3oDyqJR : 000002e1c3d9569eaa51ac3e241f6f14b4686663fff1324adafddf770b07eb3e
kudaiQkWWXQzTiD : 0000013ea5b3f0bf3f732e7cea1409cb66f35dce2423be3c27fe482b708b6c8e

The output for the program with 4 leading zeros for the bitcoin is separately attached as 4.txt

References:
http://doc.akka.io/docs/akka/2.0/intro/getting-started-first-scala.html
http://doc.akka.io/docs/akka/snapshot/scala/remoting.html
