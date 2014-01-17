/*
 Petar Velickovic (pv273)
 Trinity College
 C/C++ Assessed Exercise
 summary.c
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

char *getIPAddress(uint32_t addr)
{
    char *ret = (char*)malloc(16 * sizeof(char));
    int val1 = (addr >> 24) & 0xFF;
    int val2 = (addr >> 16) & 0xFF;
    int val3 = (addr >> 8) & 0xFF;
    int val4 = (addr & 0xFF);
    sprintf(ret, "%d.%d.%d.%d", val1, val2, val3, val4);
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
    FILE *f;
    char *source;
    char *destination;
    int first_ihl;
    int first_len;
    int first_offset;
    int total_ip_packets = 0;
    
    char buff[BUFSIZE];

    if (argc != 2)
    {
        printf("Usage: summary <file>\n");
        return 1;
    }

    if ((f = fopen(argv[1], "rb")) == 0)
    {
        printf("Unable to open the log file to summarize\n");
        return 2;
    }
    
    while (!feof(f))
    {
        IP *currIP = (IP*)malloc(sizeof(IP));
        TCP *currTCP = (TCP*)malloc(sizeof(TCP));
        
        char *cptr = buff;
        
        fread(buff, sizeof(char), BUFSIZE, f);
        if (feof(f)) break;
        readIPHeader(cptr, currIP);
        
        int ihl = IP_HLEN(currIP -> hlenver);
        int totlen = (currIP -> len);
        
        if (!total_ip_packets)
        {
            /* 
             The client has to initiate the connection by sending a packet to the server, 
             hence the source of the first packet is actually the destination, and vice-versa.
            */
            source = getIPAddress(currIP -> dst);
            destination = getIPAddress(currIP -> src);
            first_ihl = ihl;
            first_len = totlen;
        }
        
        int rem = (4*ihl - BUFSIZE); /* remaining bytes for the header */
        fseek(f, rem, SEEK_CUR); /* skip the remainder of the header */
        
        fread(buff, sizeof(char), BUFSIZE, f);
        cptr = buff;
        readTCPHeader(cptr, currTCP);
        
        int offset = TCP_OFF(currTCP -> off);
        
        if (!total_ip_packets)
        {
            first_offset = offset;
        }
        
        int rem2 = totlen - (2*BUFSIZE + rem);
        fseek(f, rem2, SEEK_CUR);
        
        total_ip_packets++;
    }
    
    printf("%s %s %d %d %d %d\n", source, destination, first_ihl, first_len, first_offset, total_ip_packets);
    
    fclose(f);

    return 0;
}
