package uk.ac.cam.pv273.fjava.tick1;

public class HelloWorld 
{
	
	public static void main(String[] args) 
	{
		String obj = "world";
		if (args.length == 1) obj = args[0];
		System.out.println("Hello, " + obj);
	}

}
