#include <stdio.h>
#include <math.h>
#include <sys/alt_alarm.h>

#include "system.h"
#include "drivers/simple_graphics.h"
#include "drivers/alt_ring_ttc_proc.h"

#define WIDTH 800
#define HEIGHT 600
#define MAX_ITER 255

alt_video_display* display;
unsigned int framebuffer[WIDTH * HEIGHT];

void draw_mandelbrot()
{
	  int i,j;

	  for (i=0;i<15000;i++) //upper limit: HEIGHT*WIDTH / 16
	  {
		  for (j=0;j<16;j++)
		  {
			  int iter = i*16+j;
			  int ii = iter % WIDTH;
			  int jj = iter / WIDTH;
			  int x0 = ((-2) << 28) + ((((3 << 20) * ii) / WIDTH) << 8);
			  int y0 = ((-1) << 28) + ((((2 << 20) * jj) / HEIGHT) << 8);
			  request(x0, y0, MAX_ITER, j);  // send request to TTC
		  }
		  for (j=0;j<16;j++)
		  {
			  result ret = response();
			  int iter = i*16 + ret.ttc_id;
			  int ii = iter % WIDTH;
			  int jj = iter / WIDTH;
			  int intensity = round(0xFF * (255 - ret.iter) / 255);
			  vid_set_pixel(ii, jj, ((intensity << 16) | (intensity << 8) | intensity), display);
			  vid_set_pixel(ii, HEIGHT - jj - 1, ((intensity << 16) | (intensity << 8) | intensity), display);
		  }
	  }
}

void alt_lcd_init()
{
	printf("Initialising LCD display controller\n");
	display = alt_video_display_init(
			CONTROLLER_0_DMA_NAME, 					// Name of the LCD-DMA controller
			WIDTH, 									// Width of display
			HEIGHT, 								// Height of display
			32, 									// Colour depth
			(int) framebuffer, 						// Base address (dynamic) of the frame buffer(s)
			CONTROLLER_0_DMA_DESCRIPTOR_MEM_BASE, 	// Base address (static) of the DMA descriptor
			1										// The number of frame buffers provided (for fancy graphics tricks)
	);
	if (display)
		printf(" - LCD initialisation OK\n");
	else
		printf(" - LCD initialisation FAILED (%d)\n", (int) display);
}

void alt_ring_proc_init()
{
	printf("Initialising TTC ring processor\n");
	int err = alt_ring_ttc_proc_init(
			PROC_0_FIFO_REQUEST_IN_BASE,			// Base address (static) of the request FIFO
			PROC_0_FIFO_REQUEST_IN_CSR_BASE,		// Base address (static) of the request FIFO control status register
			PROC_0_FIFO_RESPONSE_OUT_BASE,
			PROC_0_FIFO_RESPONSE_OUT_CSR_BASE
	);

	if (err)
		printf(" - Ring processor FIFO initialisation FAILED (%d)\n", err);
	else
		printf(" - Ring processor FIFO initialisation OK\n");
}

int main()
{
	alt_lcd_init();
	alt_ring_proc_init();
	unsigned int before = alt_nticks();
	draw_mandelbrot();
	unsigned int after = alt_nticks();
	printf("Finished in %i ms\n", ((after - before) * 1000) / alt_ticks_per_second());
	return 0;
}
