package test;

import pl.pabilo8.modworks.annotations.sound.ModSound;
import pl.pabilo8.modworks.annotations.MCModInfo;

/**
 * @author Pabilo8
 * @since 01.08.2023
 */
public class HelloWorld
{
	@ModSound(name = "test", subtitle = "*")
	public static int device1;

	@ModSound(name = "test3", subtitle = "*")
	public static int device2;
	public static void main(String[] args)
	{
		System.out.println("Hello World!");
	}

	@MCModInfo()
	public static class Mod
	{

	}
}