/***
 *** ECAD & Architecture Practical Class - Lab 1 - Synthesis, Architecture and Software
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
	/*** Declare state machine ***/
	logic [20:0] counter;
	reg button;
	reg button2;
	
	reg [17:0] leftPulse = 0;
	reg [17:0] rightPulse = 0;
	reg [17:0] aggregate = 0;
	
	Synchroniser sync1(CLOCK_50, KEY[3], button);
	Synchroniser sync2(CLOCK_50, KEY[2], button2);
	
	always_ff @(posedge CLOCK_50) begin
		  if (counter == 0) begin
		      if (!button) leftPulse <= ((leftPulse & aggregate) >> 1) + (1 << 17);
				else leftPulse <= (leftPulse & aggregate) >> 1;
				if (!button2) rightPulse <= ((rightPulse & aggregate) << 1) + 1;
				else rightPulse <= (rightPulse & aggregate) << 1;
				LEDR <= leftPulse ^ rightPulse;
		  end
		  else aggregate <= (leftPulse ^ rightPulse) & (~((leftPulse >> 1) & rightPulse)) & (~(leftPulse & (rightPulse << 1)));
		  counter <= counter + 1;
	end
endmodule
