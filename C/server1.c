/*
 Petar Velickovic (pv273)
 Trinity College
 C/C++ Assessed Exercise
 server1.c
*/

#include <stdio.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <netinet/in.h>

#define MAXOPEN 5
#define BUFSIZE 1024

int main(int argc, char *argv[]) {

  int listenfd, connfd;
  FILE *fp;
  struct sockaddr_in server;

  if (argc != 3) { /* bug 2 fixed */
    puts("Usage: server <port> <file>");
    return 1;
  }

  if ((fp=fopen(argv[2],"rb")) == 0) {
    perror("Cannot find file to serve");
    return 2;
  }

  memset(&server,0,sizeof(server));
  server.sin_family = AF_INET;
  server.sin_addr.s_addr = htonl(INADDR_ANY);
  server.sin_port = htons(atoi(argv[1]));

  if ((listenfd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP)) < 0) {
    perror("Cannot create server socket");
    return 3;
  }

  if (bind(listenfd, (struct sockaddr *) &server, sizeof(server)) < 0) {
    perror("Cannot open the interface");
    return 4;
  }

  if (listen(listenfd,MAXOPEN) < 0) {
    perror("Cannot listen on the interface");
    return 5;
  }

  for(;;) {

    if ( (connfd = accept(listenfd, (struct sockaddr *) NULL, NULL)) < 0 ) {
      perror("Error accepting a client connection");
      return 6;
    }

    while(!feof(fp)) {
      char bytes[BUFSIZE];
      int r,w = 0; /* bug 3 fixed */

      r = fread(bytes,sizeof(char),BUFSIZE,fp);

      while(w<r) {
	int total = write(connfd,bytes,r);
	if (total < 0) {
	  perror("Error writing data to client");
	  return 7;
	}
	w+=total;
      }
    }
    fseek(fp,0,SEEK_SET);
    close(connfd);
    } /* bug 1 fixed */

    return 0;
}
