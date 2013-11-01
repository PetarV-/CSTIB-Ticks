/***
 *** ECAD & Architecture Practical Class - Lab 2 - Simulation and Architecture
 ***
 *** Petar Velickovic
 *** Trinity College
 *** pv273
 *** 25 October 2013
 ***/

module toplevel (
	// clock from external oscillator
	input CLOCK_50,
	// LEDs and buttons
	input [3:0] KEY,
	output logic [6:0] HEX0,
	output logic [6:0] HEX1,
	output logic [6:0] HEX2,
	output logic [6:0] HEX3,
	output logic [6:0] HEX4,
	output logic [6:0] HEX5,
	output logic [6:0] HEX6,
	output logic [6:0] HEX7,
	output logic [7:0] LEDG,
	output logic [17:0] LEDR,
	// SDRAM interface
	output [12:0] DRAM_ADDR,
	output [1:0] DRAM_BA,
	output DRAM_CAS_N,
	output DRAM_CKE,
	output DRAM_CLK,
	output DRAM_CS_N,
	inout [31:0] DRAM_DQ,
	output [3:0] DRAM_DQM,
	output DRAM_RAS_N,
	output DRAM_WE_N,
	// LCD interface
	output [5:0] HC_R,
	output [5:0] HC_G,
	output [5:0] HC_B,
	output HC_DEN,
	output HC_NCLK
);	

   wire clk;
	reg button;
	
	pll altpll(.inclk0(CLOCK_50), .c0(clk), .c1(DRAM_CLK), .c2(HC_NCLK));
	Synchroniser sync(clk, KEY[3], button);
	
	logic [7:0] r, g, b;
	always_comb begin
		HC_R <= r[7:2];
		HC_G <= g[7:2];
		HC_B <= b[7:2];
	end
	
    mandelbrot u0 (
        .clk_clk                     (clk),                     						  //                  clk.clk
        .reset_reset_n               (button),               							  //                reset.reset_n
        .sdram_0_wire_addr           (DRAM_ADDR),           							  //         sdram_0_wire.addr
        .sdram_0_wire_ba             (DRAM_BA),             							  //                     .ba
        .sdram_0_wire_cas_n          (DRAM_CAS_N),          							  //                     .cas_n
        .sdram_0_wire_cke            (DRAM_CKE),            							  //                     .cke
        .sdram_0_wire_cs_n           (DRAM_CS_N),           							  //                     .cs_n
        .sdram_0_wire_dq             (DRAM_DQ),             							  //                     .dq
        .sdram_0_wire_dqm            (DRAM_DQM),            							  //                     .dqm
        .sdram_0_wire_ras_n          (DRAM_RAS_N),          							  //                     .ras_n
        .sdram_0_wire_we_n           (DRAM_WE_N),           							  //                     .we_n
        .controller_0_to_lcd_RGB_OUT ({r, g, b}), 											  //  controller_0_to_lcd.RGB_OUT
        .controller_0_to_lcd_HD      (),      												  //                     .HD
        .controller_0_to_lcd_VD      (),      												  //                     .VD
        .controller_0_to_lcd_DEN     (HC_DEN),     										  //                     .DEN
        .controller_0_clk_lcd_clk    (HC_NCLK),											     // controller_0_clk_lcd.clk
        .proc_0_clk_ttc_clk          (CLOCK_50)									           //       proc_0_clk_ttc.clk
    );

	
endmodule
