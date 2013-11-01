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

   wire clk;
	reg [3:0] sync_key;
	
   pll altpll(.inclk0(CLOCK_50), .c0(clk), .c1(DRAM_CLK));
	
	Synchroniser sync0(clk, KEY[0], sync_key[0]);
	Synchroniser sync1(clk, KEY[1], sync_key[1]);
	Synchroniser sync2(clk, KEY[2], sync_key[2]);
	Synchroniser sync3(clk, KEY[3], sync_key[3]);
	
	wire rst_n = sync_key[0];	
	
	
    hello_world u0 (
        .clk_clk                          (clk),						                           //                       clk.clk
        .reset_reset_n                    (rst_n),                 								   //                     reset.reset_n
        .sdram_0_wire_addr                (DRAM_ADDR),							                  //              sdram_0_wire.addr
        .sdram_0_wire_ba                  (DRAM_BA),								                  //                          .ba
        .sdram_0_wire_cas_n               (DRAM_CAS_N),								               //                          .cas_n
        .sdram_0_wire_cke                 (DRAM_CKE),								                  //                          .cke
        .sdram_0_wire_cs_n                (DRAM_CS_N),							                  //                          .cs_n
        .sdram_0_wire_dq                  (DRAM_DQ),								                  //                          .dq
        .sdram_0_wire_dqm                 (DRAM_DQM),								                  //                          .dqm
        .sdram_0_wire_ras_n               (DRAM_RAS_N),								               //                          .ras_n
        .sdram_0_wire_we_n                (DRAM_WE_N),							                  //                          .we_n
        .pio_0_external_connection_export (LEDR[7:0]),												   // pio_0_external_connection.export
        .pio_1_external_connection_export (sync_key[3:1]),											   // pio_1_external_connection.export
        .pio_2_external_connection_export ({HEX7, HEX6}),											   // pio_2_external_connection.export
        .pio_3_external_connection_export ({HEX5, HEX4}),											   // pio_3_external_connection.export
        .pio_4_external_connection_export ({HEX3, HEX2, HEX1, HEX0})  								// pio_4_external_connection.export
    );
	
endmodule
