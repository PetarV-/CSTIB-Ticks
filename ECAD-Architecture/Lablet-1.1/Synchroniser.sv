/***
 *** ECAD & Architecture Practical Class - Lab 1 - Synthesis, Architecture and Software
 ***
 *** Petar Velickovic
 *** Trinity College
 *** pv273
 *** 25 October 2013
 ***/

module Synchroniser (
   input clock,
	input wire asyncinput,
	output reg syncoutput
);
     
    /*** Instantiate sub-modules ***/
     
	 /*** Declare state (registers) ***/
    reg interm;
     
    /*** Define state machine ('always' blocks) ***/
    
    always_ff @(posedge clock) begin
	   interm <= asyncinput;
		syncoutput <= interm;
	 end 
	 
 
endmodule
