package uk.ac.cam.pv273.fjava.tick2star;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FurtherJavaPreamble 
{
	enum Ticker {A, B, C, D};
	String author();
	String date();
	String crsid();
	String summary();
	Ticker ticker();
}
