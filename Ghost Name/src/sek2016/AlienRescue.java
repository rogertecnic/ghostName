package sek2016;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.utility.Delay;

/**
 * 
 * @author Equipe Sek 2016:<br>
 * Diego Costa<br>
 * Karinny Gol�alves<br>
 * Lucas *n�o sei o sobrenome*<br>
 * Mariana *nao sei o sobrenome*<br>
 * Rog�rio Pereira Batista - Eng. El�trica<br>
 */
public class AlienRescue implements Runnable{
	/**
	 * Variavel global que indica se a Thread do programa
	 * est� executando (ON) ou fechada (OFF)
	 */
	public static boolean alienRescueON;
	
	/**
	 * Thread que comanda a execu��o do PID
	 */
	public static Thread threadPID;

	/**
	 * Metodo que rege todo o codigo do robo
	 */
	@Override
	public void run() {
		try{ // o codigo deve ficar dentro desse try gigante
//======INICIO DO CODIGO=============================================================
			threadPID = new Thread(new PID());
			threadPID.setDaemon(true);
			threadPID.setName("threadPID");
			PID.pidRunning = true;
			threadPID.start();
			Navigation.setAcceleration(500, 500);
			
			victorySong();
			boolean flag = true; // utilidade de testes
			Navigation.openGarra();
			Navigation.forward(0.3f);
			Navigation.setVelocidade(360,360);
			//Navigation.turn(360*5);
			//Navigation.forward();
			
			while (flag){
				if(Sensors.verificaObstaculo()==true){
					Navigation.stop();
					Navigation.closeGarra();
					flag = false;
				}
				else{
				}
			}
			Navigation.turn(360*5);
			
//======FINAL DO CODIGO=============================================================
			alienRescueON = false;
		}
		catch(ThreadDeath e){// quando o menu � chamado, essa thread � desligada e lan�a essa exception
			e.getStackTrace();
		}
	}
	
	private static void victorySong(){
		Sound.setVolume(50);
		Sound.playTone(3000, 100);
		Sound.playTone(4000, 100);
		Sound.playTone(4500, 100);
		Sound.playTone(5000, 100);
		Delay.msDelay(80);
		Sound.playTone(3000, 200);
		Sound.playTone(5000, 500);
	}
}
