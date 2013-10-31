Due by 11:59:59 PM MT on 11/18/2013 
 
Total Points for this Programming Assignment: 100 
 
The goal of your program is to build your own simplified version of SSL,
called mySSL. 
You can use one of C, C++, or Java for this assignment. Use client
server sockets to 
create a TCP connection. Your client server programs must do the
following: 
 
Handshake Phase (use the SSL handshake shown in your text book) (45
points) 
• The client and the server authenticate each other using certificates.
You need to create 
the certificates and include them in the mySSL messages. 
• The client also informs the server what data encryption and integrity
protection 
scheme to use (there is no negotiation). Pick your favorite integrity
protection and 
encryption algorithms. 
• The client and server also send encrypted nonces to each other. These
nonces are then 
xored to create a master secret. 
• Compute a hash of all messages exchanged at both the client and server
and exchange 
these hashes. Use keyed SHA-1 for computing the hash. The client appends
the string 
CLIENT for computing its keyed hash and the server appends the string
SERVER for 
computing its keyed hash. Verify the keyed hashes at the client and the
server. 
• Generate four keys (two each for encryption, authentication, in each
direction of the 
communication between the client and the server) using this master
secret. Pick your 
own key generation function (should be a function of the master secret). 
 
Data Phase (45 points) 
• Use the SSL record format and securely transfer a file, at least 50
Kbytes long, from 
the server to client. 
• Decrypt the file at the client and do a diff of the original and the
decrypted file to 
ensure that the secure file transfer was successful. 
 
(5 points for comments in the code, 5 points for error handling in the
code) 
 
Use opnessl or any other security library of your choice in any form
convenient to you to 
generate certificates and to extract public keys from certificates and
also for keyed hash 
computation, encryption, and data integrity protection. 
 
Include print commands in your code to show 
1. a failed verification of keyed hashes (possibly due to corruption or
changes in one of 
the handshake messages, and 
2. a successful client-server mutual authentication, key establishment,
and secure data 
transfer. 
 
Using the handin utility (cs5958 students should also use cs6490),
electronically turn in 
your code along with the output files, and a readme file. The readme
file should briefly explain how the code is organized. Your code should
be well commented. Your code 
must run on CADE lab machines. 
 
Note: Please use the broad guidelines stated above for completing your
assignment. 
Specific implementation choices are left to you. 
