package sek2016;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.utility.Delay;
import plano_B.Const;
import plano_B.Plano_B;
import sek2016.Celula.Status;

/**
 * 
 * @author Equipe Sek 2016:<br>
 * Diego Costa - Eng. da Computacao<br>
 * Karinny Gol�alves<br>
 * Lucas Sim�es - Eng. da Computacao<br>
 * Mariana Moreno - Eng. Mec�nica<br>
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


	// =========================CONSTANTES DE PROCESSO=========================
	private static final float PI = (float)Math.PI;
	private static final float CELL_SIZE = Celula.commonSize;// tamanho da
	// celula em
	// Metros
	private static final int COL_AMT = 9; // quantidade de colunas na matriz
	private static final int LIN_AMT = 9;// quantidade de linhas na matriz


	// =======================VARIAVEIS DO MAPA==============================
	private final static Celula[][] CENTRAL_MAP = new Celula[LIN_AMT][COL_AMT];
	private final static Celula[][] CAVE_MAP = new Celula[LIN_AMT][COL_AMT];
	private final static Celula[][] OBSTACLE_MAP = new Celula[LIN_AMT][COL_AMT];

	private static Posicao inputCell = new Posicao(0, 4);
	private static Posicao caveEntrance;
	private static Posicao caveExit;
	private static Posicao obstacleEntrace;
	private static Posicao obstacleExit;


	// ====================VARIAVEIS DE PROCESSO===============================
	private static Posicao robotPosition = inputCell; // posi��o de entrada


	// ========================================================================
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

			//victorySong();
			Navigation.openGarra();
			//float dist = -0.3f;
			//int ang = 45;
			//Navigation.andar(Const.LADO_ARENA_CENTRAL+0.4f);
			//Navigation.turn(360*2);
			Plano_B.partiu();
			//Plano_B.debugBusca();
			
/*			while(Button.ENTER.isUp()){
				Sensors.verificaDistObstaculo();
		//System.out.println(Sensors.verificaDistObstaculo());
			}
			
			
			
/*
			boolean captured = false;
			boolean temp = true;// s� tempor�ria, at� resolver uns bugs a�
			while(temp){
				cellExchanger();

				if (allowedReading() && !captured){
					if (Sensors.verificaObstaculo()){
						Navigation.stop();
						Navigation.closeGarra();
						/*
						 * codigo de checagem de cor
						 

						alienRescueON = false;
						captured = true;


					}else{

					}

				}

			}
*/
			//======FINAL DO CODIGO=============================================================
			alienRescueON = false;
		}
		catch(ThreadDeath e){// quando o menu � chamado, essa thread � desligada e lan�a essa exception
			e.getStackTrace();
		}
	}

	/**
	 * Reproduz o alegre som de sambar na cara das inimigas
	 */
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

	//======================Logica de captura, mapeamento e retorno============

	private static void cellExchanger() {
		float tacho = Navigation.getTacho("B");
		float dist = (2 * PI * Navigation.RAIO) * tacho;
		if (dist >= CELL_SIZE) {
			newPosition();
			Navigation.resetTacho();
		}
	}

	private static void newPosition() {
		if (Navigation.orientation == Navigation.FRONT) {
			robotPosition.setLinha(robotPosition.x + 1);
		}

		else if (Navigation.orientation == Navigation.BACK) {
			robotPosition.setLinha(robotPosition.x - 1);
		}

		else if (Navigation.orientation == Navigation.LEFT) {
			robotPosition.setColuna(robotPosition.y + 1);
		}

		else if (Navigation.orientation == Navigation.RIGTH) {
			robotPosition.setColuna(robotPosition.y - 1);
		}

	}

	private static boolean allowedReading() {

		if (robotPosition.x == (LIN_AMT - 1) && (Navigation.orientation == Navigation.FRONT
				|| Navigation.orientation == Navigation.LEFT || Navigation.orientation == Navigation.RIGTH)) {

			return false;

		} else if (robotPosition.x == 0 && (Navigation.orientation == Navigation.BACK
				|| Navigation.orientation == Navigation.LEFT || Navigation.orientation == Navigation.RIGTH)) {

			return false;

		} else if (robotPosition.y == (COL_AMT - 1) && (Navigation.orientation == Navigation.LEFT
				|| Navigation.orientation == Navigation.BACK || Navigation.orientation == Navigation.FRONT)) {

			return false;

		} else if (robotPosition.y == 0 && (Navigation.orientation == Navigation.RIGTH
				|| Navigation.orientation == Navigation.BACK || Navigation.orientation == Navigation.FRONT)) {

			return false;

		}

		else {

			return true;

		}
	}
	
	private static void frontRobotCell(){
		
	}

	public static void iniciaPerifericoFrenteDireita(int cave) {
		/*
		 * Chama o launcher dos mapas
		 */
		mapLauncher();
		
		switch (cave){
		
		case Const.CAV_DIR:

			caveEntrance = new Posicao(4, 0);
			obstacleEntrace = new Posicao(8, 4);
			
			caveExit = new Posicao(0, 4);
			obstacleExit = new Posicao(0, 4);
			break;
			
		case Const.CAV_CIMA:
			caveEntrance = new Posicao(8, 4);
			obstacleEntrace = new Posicao(4, 0);
			
			caveExit = new Posicao(0, 4);
			obstacleExit = new Posicao(0, 4);
			break;
		}
		
	}
	
	public static void iniciaPerifericoFrenteEsquerda(int cave){
		/*
		 * chama o launcher dos mapas
		 */
		mapLauncher();
		
		switch (cave){
		
		case Const.CAV_CIMA:
			caveEntrance = new Posicao(8, 4);
			obstacleEntrace = new Posicao(4, 8);
			
			caveExit = new Posicao(0, 4);
			obstacleExit = new Posicao(0, 4);
			
			break;
		
		case Const.CAV_ESQ:
			caveEntrance = new Posicao(4, 8);
			obstacleEntrace = new Posicao(8, 4);
			
			caveExit = new Posicao(0, 4);
			obstacleExit = new Posicao(0, 4);
			
			break;
		}
		
	}
	
	public static void iniciaPerifericoLateral(int cave){
		/*
		 * chama o launcher dos mapas
		 */
		mapLauncher();
		
		switch (cave){
		
		case Const.CAV_ESQ:
			caveEntrance = new Posicao(4, 8);
			obstacleEntrace = new Posicao(4, 0);
			
			caveExit = new Posicao(0, 4);
			obstacleExit = new Posicao(0, 4);
			
			break;
			
		case Const.CAV_DIR:
			caveEntrance = new Posicao(4, 0);
			obstacleEntrace = new Posicao(4, 8);
			
			caveExit = new Posicao(0, 4);
			obstacleExit = new Posicao(0, 4);
			
			break;
		}
	}
	
	private static void mapLauncher(){
		/*
		 * Inicia o modulo central e perifericos com todas as celulas como n�o checadas
		 */
		for(int i = 0; i < LIN_AMT; i++){
			for(int j = 0; j < COL_AMT; j++){
				CENTRAL_MAP[i][j] = new Celula(new Posicao(i, j), Status.unchecked);
				CAVE_MAP[i][j] = new Celula(new Posicao(i, j), Status.unchecked);
				OBSTACLE_MAP[i][j] = new Celula(new Posicao(i, j), Status.unchecked);

			}
		}
	}
	
}