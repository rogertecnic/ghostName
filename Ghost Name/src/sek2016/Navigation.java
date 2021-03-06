package sek2016;

import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.utility.Delay;
import testes_variados.MainTeste;
import testes_variados.Testa_PID;

/**
 * Controle de movimento do robo
 */
public class Navigation implements Runnable {
	// ---------------------DECLARACAO DOS MOTORES---------------------------
	static EV3LargeRegulatedMotor rodaE;
	static EV3LargeRegulatedMotor rodaD;
	static EV3MediumRegulatedMotor motorG;

	// ---------------------CONSTANTES DE DESCRICAO--------------------------
	public final static float VELO_INI = 0.19f; // em m/s, velocidade linear do
												// robo andar
	public final static float VELO_CURVA = 0.08f; // em m/s, velocidade linear
													// do robo fazer o turn
	public final static float aceleration = 0.50f; // m/s^2 PARA A RODA (0.26f
													// <=> 548 graus/s^2),
													// (0.37978f <=> 800
													// graus/s^2), default:
													// 2.84837 m/s^2 <=> 6000
													// m/s^2)
	public final static float DISTANCIA_ENTRE_RODAS = 0.1378f;// metros, ja
																// conferido
																// (0.14f)
	public final static float RAIO = 0.0272f; // metros, ja conferido (se
												// alterar tem que alterar o de
												// cima)

	// ------------------- CONSTANTES DE ORIENTACAO------------------------
	/*
	 * Constantes de orienta��o do robo
	 */
	final static int BACK = 0, LEFT = 1, FRONT = 2, RIGTH = 3;

	/**
	 * Orienta��o real do robo
	 */
	static int orientation = FRONT;

	// ---------------------VARIAVEIS DE PROCESSO----------------------------

	public static boolean avoidedError = false;

	public static boolean garraFechada = false, // a garra esta fechada?
			andandoRe = false; // o robo esta andando de RE? para inverter o pid
								// no metodo calculaPID se caso ele for andar de
								// re

	private static boolean tachometer = false;

	public static Posicao robotPosition; // posi��o de
											// entrada
	public static float globalTacho = 0;
	// ========================Constantes de processo=====================

	private static final float PI = (float) Math.PI;
	private static final float CELL_SIZE = Celula.commonSize;// tamanho da
																// c�lula

	// --------------------METODOS------------------------------------------

	/**
	 * M�todo Respos�vel pela Thread
	 */
	@Override
	public void run() {

		while (AlienRescue.alienRescueON) {

			if (tachometer) {

				cellExchanger();

			}
		}

	}

	/**
	 * Faz a checagem constante da tacometria e identifica quando o robo andou o
	 * suficiente para que seja feito o calculo da uma nova posi��o
	 */
	private static void cellExchanger() {
		float tacho = (Navigation.getTacho("B") / 360) + (globalTacho / 360);
		float distancia = (2 * PI * Navigation.RAIO) * tacho;
		if (distancia >= 10 && distancia <= 11) {

			AlienRescue.cellAlreadyRead = false;

		}
		if (distancia >= CELL_SIZE) {
			AlienRescue.cellExchanged = true;
			AlienRescue.cellAlreadyRead = false;
			newPosition();
			globalTacho = 0;
			Navigation.resetTacho();

		}

	}

	/**
	 * Quando acionado faz o calculo da nova posi��o do robo levando em conta
	 * sua orienta��o<br>
	 * S� deve ser acionado quando for identificado que de fato o robo mudou de
	 * celula
	 */
	private static void newPosition() {
		if (Navigation.orientation == Navigation.FRONT) {
			robotPosition.setLinha(robotPosition.x + 1);
			System.out.println(robotPosition.x + "\t" + robotPosition.y);
		}

		else if (Navigation.orientation == Navigation.BACK) {
			robotPosition.setLinha(robotPosition.x - 1);
			System.out.println(robotPosition.x + "\t" + robotPosition.y);

		}

		else if (Navigation.orientation == Navigation.LEFT) {
			robotPosition.setColuna(robotPosition.y + 1);
			System.out.println(robotPosition.x + "\t" + robotPosition.y);

		}

		else if (Navigation.orientation == Navigation.RIGTH) {
			robotPosition.setColuna(robotPosition.y - 1);
			System.out.println(robotPosition.x + "\t" + robotPosition.y);

		}

	}

	/**
	 * Gira o robo no proprio eixo, n�o usa o giroscopio, usa o tacometro das
	 * rodas, o m�todo segura o programa dentro dele
	 * 
	 * @param graus
	 *            inteiro positivo (anti-hor�rio) inteiro negativo (hor�rio)
	 */
	public static void turn(float graus) {
		PID.pidRunning = false; // pausa o pid para o pid nao recalcular a
								// velocidade durante o turn
		while (!PID.PIDparado) { // aguarda o pid realmente parar
		}
		PID.zeraPID(); // apos o pid parado ele eh zerado
		setVelocidade(VELO_CURVA, VELO_CURVA); // seta a velocidade da curva

		if (graus > 0) {
			int giro = (int) (graus / 90);
			for (int x = 0; x < giro; x++) {
				alterOrientation(-1);
			}
		} else if (graus < 0) {
			int giro = (int) (graus / -90);
			for (int x = 0; x < giro; x++) {
				alterOrientation(1);
			}
		}

		float theta = (graus * DISTANCIA_ENTRE_RODAS) / (2 * RAIO); // angulo
																	// que a
																	// roda
																	// precisa
																	// girar
																	// para o
																	// robo
																	// girar os
																	// graus
																	// passados
		float positioninicialE = rodaE.getTachoCount(); // posicao inicial em
														// graus da roda e
		float positioninicialD = rodaD.getTachoCount(); // posicao inicial em
														// graus da roda d
		float wRoda = VELO_CURVA / RAIO * (float) (180 / Math.PI); // velo
																	// angular
																	// das rodas
																	// em
																	// graus/s
		float acc = (aceleration) / RAIO * (float) (180 / Math.PI); // aceleracao
																	// das rodas
																	// em
																	// graus/s^2
		float t = wRoda / (acc); // tempo que o robo demora a parar depois que
									// ele chama o metodo stop devido a
									// desaceleracao normal do lejos
		float ang_defasado = wRoda * t - (acc / 2) * t * t; // robo deve chamar
															// o metodo stop
															// antes do local de
															// parar, esse
															// ang_defasado �
															// essa distancia em
															// graus da roda

		if (graus > 0) { // turn anti horario
			rodaD.forward();
			rodaE.backward();
			while (rodaE.getTachoCount() > (positioninicialE - theta + ang_defasado) && // espera
																						// o
																						// robo
																						// girar
																						// as
																						// rodas
																						// ate
																						// a
																						// posicao
																						// de
																						// chamar
																						// o
																						// metodo
																						// stop
					rodaD.getTachoCount() < (positioninicialD + theta - ang_defasado)) { // no
																							// momento
																							// certo
																							// antes
																							// da
																							// posical
																							// final
																							// do
																							// giro,
																							// sai
																							// do
																							// while
																							// e
																							// vai
																							// direto
																							// para
																							// o
																							// metodo
																							// stop
			}
		}
		if (graus < 0) { // turn horario
			rodaE.forward();
			rodaD.backward();
			while (rodaD.getTachoCount() > (positioninicialD + theta + ang_defasado) && // mesma
																						// ideia
																						// do
																						// de
																						// cima
					rodaE.getTachoCount() < (positioninicialE - theta - ang_defasado)) { //
			}
		}
		Navigation.stop(); // metodo chamado no momento de desaceleracao do robo
		// para ele parar onde ele deve
		Navigation.resetTacho();

	}

	/**
	 * Flag de controle de tacometria
	 * 
	 * @param tachometer
	 *            Boolean para mudar a flag de tacometria
	 */
	public static void setTachometer(boolean tachometer) {

		Navigation.tachometer = tachometer;

	}

	/**
	 * Retorna o estado atual da flag de controle da tacometria
	 * 
	 * @return Flag de controle da tacometria
	 */
	public static boolean getTachometer() {
		return Navigation.tachometer;
	}

	/**
	 * robo anda pra frente(positivo) ou para traz (negativo) em uma determinada
	 * distancia.<br>
	 * velocidade definida pelo PID, metodo segura a thread dentro dele ate o
	 * robo completar a distancia
	 * 
	 * @param dist
	 *            ditancia em metros que o robo vai andar
	 */
	public static void andar(float dist) {
		PID.pidRunning = false; // pausa o pid para reinicia-lo
		while (!PID.PIDparado) { // espera o pid realmente parar
		}
		PID.zeraPID(); // zera o pid
		PID.pidRunning = true; // inicia o pid
		while (!PID.PIDparado) { // espera o pid ter a primeira iteracao para ja
									// ter alterado a velocidade, se nao, o
									// metodo continuaria e o robo andaria antes
									// do pid setar as velocidades pois sao
									// threads diferentes
		}

		float theta = (dist / RAIO) * (float) (180 / Math.PI); // graus que a
																// roda deve
																// girar para o
																// robo andar a
																// distancia
																// determinada
		float positionE = rodaE.getTachoCount(); // posicao inicial em graus da
													// roda e
		float positionD = rodaD.getTachoCount(); // posicao inicial em graus da
													// roda d

		/*
		 * velocidade angular em graus/s das rodas de modo geral, nao eh a
		 * velocidade que o pid regula, essa velocidade seria a velocidade que o
		 * pid mantem se o erro fosse 0 e seria igual para as 2 rodas
		 */
		float wRoda = VELO_INI / RAIO * (float) (180 / Math.PI);
		float acc = aceleration / RAIO * (float) (180 / Math.PI); // aceleracao
																	// angular
																	// em
																	// graus/s^2
																	// das rodas
		float t = wRoda / (acc); // tempo que o robo demora a parar depois que
									// ele chama o metodo stop devido a
									// desaceleracao normal do lejos
		float ang_defasado = wRoda * t - (acc / 2) * t * t; // robo deve chamar
															// o metodo stop
															// antes do local de
															// parar, esse
															// ang_defasado �
															// essa distancia em
															// graus da roda

		Delay.msDelay(30); /*
							 * tem que ter esse delay, o motivo nao sabemos ao
							 * certo o por que, verificamos a velocidade do pid
							 * nesse instante e ela continua certinha, se nao o
							 * robo exporadicamente vai girar a roda direita
							 * para traz por um curto periodo de tempo com
							 * velocidade maxima quando o robo for se movimentar
							 */
		if (dist > 0) { // a ideia aqui eh a mesma do turn
			andandoRe = false;
			rodaE.forward();
			rodaD.forward();
			while (rodaE.getTachoCount() < (positionE + theta - ang_defasado)
					&& rodaD.getTachoCount() < (positionD + theta - ang_defasado)) {
			}
		} else {
			andandoRe = true;
			rodaE.backward();
			rodaD.backward();

			while (rodaE.getTachoCount() > (positionE + theta + ang_defasado)
					&& rodaD.getTachoCount() > (positionD + theta + ang_defasado)) {
			}
		}
		Navigation.stop();
		andandoRe = false;
	}

	/**
	 * Frente, velocidade definida pelo PID
	 */
	public static void forward() {

		avoidingError();

		Delay.msDelay(50);
		rodaE.forward();
		rodaD.forward();
	}

	private static void avoidingError() {
		if (avoidedError == false) {

			PID.pidRunning = false; // pausa o pid para reinicia-lo
			while (!PID.PIDparado) { // espera o pid realmente parar
			}
			PID.zeraPID(); // zera o pid
			PID.pidRunning = true; // inicia o pid
			/*
			 * espera o pid ter a primeira iteracao para ja ter alterado a
			 * velocidade, se nao, o metodo continuaria e o robo andaria antes
			 * do pid setar as velocidades pois sao threads diferentes
			 */
			while (!PID.PIDparado) {

			}
			avoidedError = true;
		}
	}

	/**
	 * Traz. velocidade definida pelo PID
	 */
	public static void backward() {
		rodaE.backward();
		rodaD.backward();
	}

	/**
	 * seta aceleracao em graus/s^2 de cada roda, EVITAR USAR
	 * 
	 * @param accD
	 *            aceleracao em m/s^2 motor direito
	 * @param accE
	 *            aceleracao em m/s^2 motor esquerdo
	 */
	public static void setAcceleration(float accD, float accE) {
		Navigation.rodaD.setAcceleration((int) ((accD / RAIO) * (180 / Math.PI)));
		Navigation.rodaE.setAcceleration((int) ((accE / RAIO) * (180 / Math.PI)));
	}

	/**
	 * seta velocidade em graus/s de cada roda
	 * 
	 * @param veloD
	 *            float em m/seg da roda DIREITA
	 * @param veloE
	 *            float em m/seg^2 da roda ESQUERDA
	 */
	public static void setVelocidade(float veloD, float veloE) {
		Navigation.rodaD.setSpeed((int) ((veloD / RAIO) * (180 / Math.PI)));
		Navigation.rodaE.setSpeed((int) ((veloE / RAIO) * (180 / Math.PI)));
	}

	/**
	 * Parar o robo (trava o codigo dentro enquanto o robo nao parar)
	 */
	public static void stop() {
		rodaD.stop(true);
		rodaE.stop(true); // evita aquela jogadinha pro lado quando robo termina
							// um movimento
		while (rodaE.isMoving() || rodaD.isMoving()) {
		}
		avoidedError = false;
	}

	/**
	 * fechar garra
	 */
	public static void closeGarra() {
		if (garraFechada == false) {
			garraFechada = true;
			motorG.rotate(60);
		} else {
			LCD.clear();
			LCD.drawString("Garra fechada", 0, 0);
		}
	}

	/**
	 * Abrir garra
	 */
	public static void openGarra() {
		if (garraFechada == true) {
			garraFechada = false;
			motorG.rotate(-60);
		} else {
			LCD.clear();
			LCD.drawString("Garra aberta", 0, 0);
		}
	}

	/**
	 * instancia os motores
	 * 
	 * @param initMotors:
	 *            true ou false
	 */
	public static void init(boolean initMotors) {
		if (initMotors == true) {
			rodaE = new EV3LargeRegulatedMotor(MotorPort.A);
			rodaD = new EV3LargeRegulatedMotor(MotorPort.B);
			motorG = new EV3MediumRegulatedMotor(MotorPort.C);
			setAcceleration(aceleration, aceleration);
			setVelocidade(VELO_INI, VELO_INI);
		}
	}

	// ---------------------METODOS DA TACOMETRIA---------------------------
	/**
	 * Metodo que retorna a tacometria dos motores de acordo com os parametros
	 * especificados, caso o parametro seja desconhecido, retorna null.
	 * 
	 * @param L
	 *            (Left) para retornar a tacometria do motor esquerdo <br>
	 *            <b>R</b> (Right) para retornar a tacometria do motor direito
	 *            <br>
	 *            <b>B</b> (Both) para retornar a m�dia da tacometria dos dois
	 *            motores
	 * @return Retorna valores float
	 *         <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<QUE VALORES
	 *         FLOAT?
	 */
	public static float getTacho(String x) {
		if (x.toUpperCase() == "L") {
			return rodaE.getTachoCount();

		} else if (x.toUpperCase() == "R") {

			return rodaD.getTachoCount();

		} else if (x.toUpperCase() == "B") {
			float temp1 = rodaE.getTachoCount();
			float temp2 = rodaD.getTachoCount();

			return ((temp1 + temp2) / 2);

		} else {

			return (Float) null;
		}
	}

	/**
	 * Reseta a contagem dos tacometros de cada roda
	 */
	public static void resetTacho() {
		rodaE.resetTachoCount();
		rodaD.resetTachoCount();
	}

	/**
	 * Altera a orientacao do robo
	 * 
	 * @param ortt
	 */
	private static void alterOrientation(int ortt) {
		if (ortt > 1) {
			ortt = 1;
		} else if (ortt < -1) {
			ortt = -1;
		}

		switch (ortt) {
		case 1:
			orientation++;

			if (orientation > 3) {
				orientation = 0;
			}
			break;

		case -1:
			orientation--;

			if (orientation < 0) {
				orientation = 3;
			}
			break;
		}
	}

}
