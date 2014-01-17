/*
 Petar Velickovic (pv273)
 Trinity College
 C/C++ Assessed Exercise
 extract.c
*/

#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <arpa/inet.h>
#include <netinet/in.h>

#define BUFSIZE 20

#define IP_HLEN(lenver) (lenver & 0x0f)
#define IP_VER(lenver) (lenver >> 4)
#define TCP_OFF(offres) (offres >> 4)

typedef struct tcp_header
{
    uint16_t src; /* Source Port */
    uint16_t dst; /* Destination Port */
    uint32_t seqnum; /* Sequence Number */
    uint32_t acknum; /* Acknowledgment Number */
    uint8_t off; /* Data Offset + Reserved */
    uint8_t ctrl; /* Reserved + Control Bits */
    uint16_t win; /* Window */
    uint16_t chksum; /* Checksum */
    uint16_t urgptr; /* Urgent Pointer */
} TCP;

typedef struct ip_header
{
    uint8_t hlenver; /* Version + Internet Header Length */
    uint8_t tos; /* Type of Service */
    uint16_t len; /* Total Length */
    uint16_t id; /* Identification */
    uint16_t off; /* Flags + Fragment Offset */
    uint8_t ttl; /* Time to Live */
    uint8_t p; /* Protocol */
    uint16_t sum; /* Header Checksum */
    uint32_t src; /* Source Address */
    uint32_t dst; /* Destination Address */
} IP;

uint8_t readInt8(char **pos)
{
    uint8_t ret = (uint8_t)(**pos);
    (*pos)++;
    return ret;
}

uint16_t readInt16(char **pos)
{
    uint16_t ret = 0;
    ret = ((uint16_t)readInt8(pos)) << 8;
    ret |= (uint16_t)readInt8(pos);
    return ret;
}

uint32_t readInt32(char **pos)
{
    uint32_t ret = 0;
    ret = ((uint32_t)readInt8(pos)) << 24;
    ret |= ((uint32_t)readInt8(pos)) << 16;
    ret |= ((uint32_t)readInt8(pos)) << 8;
    ret |= ((uint32_t)readInt8(pos));
    return ret;
}

void readIPHeader(char *buf, IP *ip)
{
    ip -> hlenver = readInt8(&buf);
    ip -> tos = readInt8(&buf);
    ip -> len = readInt16(&buf);
    ip -> id = readInt16(&buf);
    ip -> off = readInt16(&buf);
    ip -> ttl = readInt8(&buf);
    ip -> p = readInt8(&buf);
    ip -> sum = readInt16(&buf);
    ip -> src = readInt32(&buf);
    ip -> dst = readInt32(&buf);
}

void readTCPHeader(char *buf, TCP *tcp)
{
    tcp -> src = readInt16(&buf);
    tcp -> dst = readInt16(&buf);
    tcp -> seqnum = readInt32(&buf);
    tcp -> acknum = readInt32(&buf);
    tcp -> off = readInt8(&buf);
    tcp -> ctrl = readInt8(&buf);
    tcp -> win = readInt16(&buf);
    tcp -> chksum = readInt16(&buf);
    tcp -> urgptr = readInt16(&buf);
}

int main(int argc, char **argv)
{
    FILE *fin, *fout;
    
    char buff[BUFSIZE];
    char *buffMain;
    int total_ip_packets = 0;
    uint32_t src, dst;
    
    if (argc != 3)
    {
        printf("Usage: extract <in> <out>\n");
        return 1;
    }
    
    if ((fin = fopen(argv[1], "rb")) == 0)
    {
        printf("Unable to open the log file to extract from\n");
        return 2;
    }
    
    fout = fopen(argv[2], "w");
    
    while (!feof(fin))
    {
        IP *currIP = (IP*)malloc(sizeof(IP));
        TCP *currTCP = (TCP*)malloc(sizeof(TCP));
        
        char *cptr = buff;
        
        fread(buff, sizeof(char), BUFSIZE, fin);
        if (feof(fin)) break;
        readIPHeader(cptr, currIP);
        
        int ihl = IP_HLEN(currIP -> hlenver);
        int totlen = (currIP -> len);
        
        if (!total_ip_packets)
        {
            /*
             The client has to initiate the connection by sending a packet to the server,
             hence the source of the first packet is actually the destination, and vice-versa.
            */
            src = currIP -> dst;
            dst = currIP -> src;
        }
        total_ip_packets++;
        
        int rem = (4*ihl - BUFSIZE); /* remaining bytes for the header */
        fseek(fin, rem, SEEK_CUR); /* skip the remainder of the header */
        
        fread(buff, sizeof(char), BUFSIZE, fin);
        cptr = buff;
        readTCPHeader(cptr, currTCP);
        
        int offset = TCP_OFF(currTCP -> off);
        
        int rem1 = (4*offset - BUFSIZE);
        fseek(fin, rem1, SEEK_CUR);
        int rem2 = totlen - (2*BUFSIZE + rem + rem1);
        
        buffMain = (char*)malloc(rem2 * sizeof(char));
        fread(buffMain, sizeof(char), rem2, fin);
        
        if (currIP -> src == src && currIP -> dst == dst) fwrite(buffMain, rem2, sizeof(char), fout);
    }
    
    fclose(fin);
    fclose(fout);
    
    return 0;
}
