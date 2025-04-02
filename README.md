# CSCI 455 Project 2
- I used JetBrains IntelliJ to program this project
- Any editor should work the same as long as the UDPClient file can run multiple instances
- To run the program:
    - Start the server
    - Start as many clients as necessary
- Some extra functionality was added to experiment with the concepts learned in class
  (e.g., a continue option for the client).
- If you have any feedback on the implementations, please let me know.

## Difference From the TCP Application
- The biggest difference is that more logic is in the UDPServer file
- Every message is sent from the server to the client
  - The TCP application had the TCPClient file printing certain messages to the client, 
  but this is no longer the case