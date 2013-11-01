#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include "system.h"
#include "altera_avalon_pio_regs.h"

int hex_to_7seg[16] =
{
		0x40, 0x79, 0x24, 0x30, 0x19, 0x12, 0x02, 0x78, 0x00, 0x10, // 0-9
		0x08, 0x03, 0x46, 0x21, 0x06, 0x0E	                    	// a-f
};

void print_bin(int val)
{
	IOWR_ALTERA_AVALON_PIO_DATA(PIO_0_BASE, val);
}

void print_hex(int val)
{
	int firstHexDigit = val & 0xF;
	int secondHexDigit = (val >> 4) & 0xF;
	int segValue = (hex_to_7seg[secondHexDigit] << 7) | hex_to_7seg[firstHexDigit];
	IOWR_ALTERA_AVALON_PIO_DATA(PIO_2_BASE, segValue);
}

void print_dec(int val)
{
	int firstDecDigit = val % 10;
	int secondDecDigit = (val / 10) % 10;
	int segValue = (hex_to_7seg[secondDecDigit] << 7) | hex_to_7seg[firstDecDigit];
	IOWR_ALTERA_AVALON_PIO_DATA(PIO_3_BASE, segValue);
}

void print_oct(int val)
{
	int firstOctDigit = val & 7;
	int secondOctDigit = (val >> 3) & 7;
	int thirdOctDigit = (val >> 6) & 7;
	int fourthOctDigit = (val >> 9) & 7;
	int segValue = (hex_to_7seg[fourthOctDigit] << 21) | (hex_to_7seg[thirdOctDigit] << 14) | (hex_to_7seg[secondOctDigit] << 7) | hex_to_7seg[firstOctDigit];
	IOWR_ALTERA_AVALON_PIO_DATA(PIO_4_BASE, segValue);
}

int main()
{
	int num, dir;
	char buff[6];

	// Keep asking for more numbers
	while (1)
	{
		printf("Enter a number: ");
		// Read 3 characters from the terminal
		while(fgets(buff,sizeof(buff),stdin) != buff);
		printf("You entered: %s\n",buff);

		// Start by incrementing from 0 to the number (if valid)
		dir = 1, num = atoi(buff);
		if ((num == 0) | (num > 99))
		{
			printf("Number out of range [1..100]\n");
			continue;
		}
		int number = 0;

		while(dir != 0)
		{
			print_bin(number);
			print_hex(number);
			print_dec(number);
			print_oct(number);

			int buttons = IORD_ALTERA_AVALON_PIO_DATA(PIO_1_BASE);
			buttons = ~buttons;
			if (buttons & 1) break;
			if ((buttons >> 1) & 1) dir = 1;
			if ((buttons >> 2) & 1) dir = 2;

			if (dir == 1 && number < num) number++;
			if (dir == 2 && number > 0) number--;

			usleep(500000);
		}
	}
}
