package sek2016;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class Sensors {

	// ===================declaracao dos sensores ============================
	private static EV3GyroSensor gyro; // nem preciso falar
	private static EV3UltrasonicSensor ultrasonic; // nem esse
	private static EV3ColorSensor dollColor; // verificar cor do boneco no modo
	// RGB
	private static EV3ColorSensor floorColor; // verificar a cor do chao
	private static float[] angleSample, // vetor contendo um elemento do gyro,
	// angulo em graus
	distSample, // vetor contendo um elemento do ultrassom, a dist em
	// metros
	rgbSample, // vetor contendo 3 elementos de nivel de intensidade RGB
	// de 0 a 1 do doll color
	floorSample;

	// =====================constantes de processo=======================
	private static final float DIST_MIN = 0.05f, // distancia minima do
			// boneco
			DIST_MAX = 0.18f; // distancia maxima do boneco (0.18 para nao dar erro)
	/*
	 * cada cor do sensor RGB DollColor foi dividida em 3 intervalos que vao
	 * corresponder a cada cor de bonecos, esses intervalos serao definidos no
	 * metodo de calibragem, com o menor valor sendo 0 e o maior valor sendo 1 e
	 * os intervalos serao entre estes 2 extremos; exemplo: 0 <p< r1 < b < r2 <
	 * v < 1; REFERENTES A COR DO BONECO v: vermelho b: branco p: preto
	 */
	private static float r1, // red
	g1, g1floor, // green
	b1, b1floor; // blue
	private static final int BRANCO = 3, VERMELHO = 4, PRETO = 5;
	// ======================metodos======================
	/**
	 * Metodo que instancia todos os sensores.
	 * 
	 * @param DC
	 *            true ou false
	 * @param US
	 *            true ou false
	 * @param FC
	 *            true ou false
	 * @param GR
	 *            true ou false
	 */
	public static void init(boolean DC, boolean US, boolean FC, boolean GR) {
		if (GR == true) {
			gyro = new EV3GyroSensor(SensorPort.S2);
			angleSample = new float[1];
		}
		if (US == true) {
			ultrasonic = new EV3UltrasonicSensor(SensorPort.S3);
			distSample = new float[1];
		}
		if (FC == true) {
			floorColor = new EV3ColorSensor(SensorPort.S4);
			floorSample = new float[3];
		}
		if (DC == true) {
			dollColor = new EV3ColorSensor(SensorPort.S1);
			rgbSample = new float[3];
		}
	}

	/**
	 * Metodo que verifica se tem algo na frente do ultrassom entre DIST_MIN e
	 * DIST_MAX;
	 * 
	 * @return true se tiver;<br>
	 *         false se nao tiver;
	 */
	public static boolean verificaObstaculo() {
		ultrasonic.getDistanceMode().fetchSample(distSample, 0);
		if ((distSample[0] >= DIST_MIN) && (distSample[0] <= DIST_MAX))
			return true;
		else
			return false;
	}


	/**
	 * Metodo que verifica se tem algo na frente do ultrassom
	 * entre DIST_MIN e DIST_MAX;
	 * @return distancia lida;
	 */
	public static float verificaDistObstaculo(){
		float[] dist = new float[10];
		distSample[0] = 0f;
		for(int i = 0;i<10;i++){
			ultrasonic.getDistanceMode().fetchSample(dist, i);
			distSample[0] += dist[i];
		}
		distSample[0] = distSample[0]/10;
		if((distSample[0] >= DIST_MIN) && (distSample[0] <= DIST_MAX))
			return distSample[0];
		else 
			return 0f;
	}

	
	
	/**
	 * Metodo que verifica somente a posicao do gyro;
	 * 
	 * @return valor float com a diferenca em graus da posicao da ultima zerada
	 *         para a posicao atual
	 */
	public static float getAngle() {
		gyro.getAngleMode().fetchSample(angleSample, 0);
		return angleSample[0];
	}

	/**
	 * Modifica a posicao angular do gyro para 0 graus para recalibrar;<br>
	 * <h1>O ROBO DEVE ESTAR PARADO;
	 */
	public static void resetAngle() {
		if (Navigation.rodaD.isMoving() || Navigation.rodaE.isMoving()) {
			Navigation.stop();
		}
		gyro.reset();
	}

	/*
	 * public static int VerificaCorDoll() {
	 * dollColor.getRGBMode().fetchSample(rgbSample, 0); if (rgbSample[0] < vr2
	 * && rgbSample[0] > vr1) { // verificação vermelho if (rgbSample[1] < vg2
	 * && rgbSample[1] > vg1) { if (rgbSample[2] < vb2 && rgbSample[2] > vb1) {
	 * System.out.println("vermelho"); return 5;
	 * 
	 * } }
	 * 
	 * }
	 * 
	 * if (rgbSample[0] < br2 && rgbSample[0] > br1) { // verificação branco if
	 * (rgbSample[1] < bg2 && rgbSample[1] > bg1) { if (rgbSample[2] < bb2 &&
	 * rgbSample[2] > bb1) { System.out.println("branco"); return 3; } }
	 * 
	 * }
	 * 
	 * if (rgbSample[0] < r1 && rgbSample[0] > 0) { // verificação preto if
	 * (rgbSample[1] < g1 && rgbSample[1] > 0) { if (rgbSample[2] < b1 &&
	 * rgbSample[2] > 0) { System.out.println("preto"); return 4; } }
	 * 
	 * } System.out.println(); return 0; }
	 */

	/**
	 * metodo para ver a cor do boneco
	 * 
	 * @return 3 branco 4 vermelho 5 preto
	 * 
	 */
	public static int verificaCorDoll() {
		dollColor.getRGBMode().fetchSample(rgbSample, 0);
		if (rgbSample[0] > r1) { // verificação vermelho
			if (rgbSample[1] > g1 && rgbSample[2] > b1) {
				System.out.println("branco");
				return BRANCO;
			} else {
				System.out.println("vermelho");
				return VERMELHO;
			}
		} else {
			System.out.println("preto");
			return PRETO;
		}
	}

	/**
	 * Metodo que verifica a cor do chao, a leitura do sensor eh dada em rgb,
	 * obtemos empiricamente que para o r a variacao nao eh significante
	 * para o g o valor maior de reflexao eh o floor verde (obvio)
	 * para o b o valor maior de reflexao eh o floor azul
	 * o metodo esta usando somente o g para testar
	 * @return 5 se for preto e 3 se for branco;
	 */
	public static int verificaFloor(){
		floorColor.getRGBMode().fetchSample(floorSample, 0);
		if(floorSample[1] >=g1floor){ // solo verde, resgata boneco preto
			System.out.println("resg PRETO");
			return PRETO;
		}else{
			System.out.println("resg BRANCO");
			Delay.msDelay(1000);
			return BRANCO;
		}
	}
	
	/**
	 * Calibragem do sensor de dollColor
	 */
	public static void calibraCorDoll() {
		float[] red = new float[3], blue = new float[3], green = new float[3];
		float t;
		LCD.clear();
		LCD.drawString("CALIBRAGEM", 0, 0);
		LCD.drawString("Coloque o vermelho", 0, 1);
		LCD.drawString("aperte o botao central", 0, 2);
		Button.ENTER.waitForPressAndRelease();
		dollColor.getRGBMode().fetchSample(rgbSample, 0);
		red[0] = rgbSample[0];
		green[0] = rgbSample[1];
		blue[0] = rgbSample[2];
		LCD.clear();
		LCD.drawString("CALIBRAGEM", 0, 0);
		LCD.drawString("Coloque o branco", 0, 1);
		LCD.drawString("aperte o botao central", 0, 2);
		Button.ENTER.waitForPressAndRelease();
		dollColor.getRGBMode().fetchSample(rgbSample, 0);
		red[1] = rgbSample[0];
		green[1] = rgbSample[1];
		blue[1] = rgbSample[2];
		LCD.clear();
		LCD.drawString("CALIBRAGEM", 0, 0);
		LCD.drawString("Coloque o preto", 0, 1);
		LCD.drawString("aperte o botao central", 0, 2);
		Button.ENTER.waitForPressAndRelease();
		dollColor.getRGBMode().fetchSample(rgbSample, 0);
		red[2] = rgbSample[0];
		green[2] = rgbSample[1];
		blue[2] = rgbSample[2];
		t = 0;
		// organizando os intervalos do red
		for (int i = 0; i <= 2; i++) {
			for (int j = 2; j > i; j--) {
				if (red[j] <= red[i]) {
					t = red[i];
					red[i] = red[j];
					red[j] = t;
				}
			}
		}

		t = 0;
		// organizando os intervalos do green
		for (int i = 0; i <= 2; i++) {
			for (int j = 2; j > i; j--) {
				if (green[j] <= green[i]) {
					t = green[i];
					green[i] = green[j];
					green[j] = t;
				}
			}
		}

		t = 0;
		// organizando os intervalos do blue
		for (int i = 0; i <= 2; i++) {
			for (int j = 2; j > i; j--) {
				if (blue[j] <= blue[i]) {
					t = blue[i];
					blue[i] = blue[j];
					blue[j] = t;
				}
			}
		}

		LCD.clear();
		r1 = red[0] * 2;
		g1 = blue[0] * 2;
		b1 = green[0] * 2;

	}

	/**
	 * Calibragem do sensor de cor do chao
	 */
	public static void calibraCorChao() {
		float[] red = new float[2], blue = new float[2], green = new float[2];
		float t;
		LCD.clear();
		LCD.drawString("CALIBRAGEM ARENA", 0, 0);
		LCD.drawString("Coloque no verde", 0, 1);
		LCD.drawString("aperte o botao central", 0, 2);
		Button.ENTER.waitForPressAndRelease();
		floorColor.getRGBMode().fetchSample(floorSample, 0);
		red[0] = floorSample[0];
		green[0] = floorSample[1];
		blue[0] = floorSample[2];
		LCD.clear();
		LCD.drawString("CALIBRAGEM ARENA", 0, 0);
		LCD.drawString("Coloque no azul", 0, 1);
		LCD.drawString("aperte o botao central", 0, 2);
		Button.ENTER.waitForPressAndRelease();
		floorColor.getRGBMode().fetchSample(floorSample, 0);
		red[1] = floorSample[0];
		green[1] = floorSample[1];
		blue[1] = floorSample[2];

		t = 0;
		// organizando os intervalos do red
		for (int i = 0; i <= 1; i++) {
			for (int j = 1; j > i; j--) {
				if (red[j] <= red[i]) {
					t = red[i];
					red[i] = red[j];
					red[j] = t;
				}
			}
		}

		t = 0;
		// organizando os intervalos do green
		for (int i = 0; i <= 1; i++) {
			for (int j = 1; j > i; j--) {
				if (green[j] <= green[i]) {
					t = green[i];
					green[i] = green[j];
					green[j] = t;
				}
			}
		}
		b1floor = (blue[0] +blue[1])/2;
		g1floor = (green[0] + green[1])/2;

	}
}