package Backups;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import sek2016.Posicao;

public class Astar_Backup {

	Celula_Backup[][] mapa;
	ArrayList<Celula_Backup> listaAberta = new ArrayList<Celula_Backup>();
	ArrayList<Celula_Backup> listaFechada = new ArrayList<Celula_Backup>();

	Astar_Backup(Celula_Backup[][] mapa) {
		this.mapa = mapa;
		listaAberta = new ArrayList<Celula_Backup>();
		listaFechada = new ArrayList<Celula_Backup>();
	}

	/**
	 * M�todo A* para busca de caminho mais pr�ximo
	 * 
	 * @param inicio
	 *            posi��o inicial
	 * @param fim
	 *            posi��o final
	 * @return lista de posi��es para caminho mais pr�ximo
	 * @throws PathNotFoundException
	 *             caminho n�o encontrado
	 */
	
	public List<Celula_Backup> search(Posicao inicio, Posicao fim) throws Exception {
		// Apagam os valores calculados do mapa e as listas
		apagarMap();
		listaAberta.clear();
		listaFechada.clear();
		// ------------------------------------------------

		listaAberta.add(mapa[inicio.x][inicio.y]);

		while (listaAberta.size() > 0) {
			
			// Pega o menor f(x) para ir para o proximo--------------------------
			int indiceMenor = 0;
			for (int i = 0; i < listaAberta.size(); i++) {
				if (listaAberta.get(i).f < listaAberta.get(indiceMenor).f) {
					indiceMenor = i;
				}
			//--------------------------------------------------------------------
				
			}
			
			Celula_Backup noAtual = listaAberta.get(indiceMenor);
			// Fim -- o resultado foi encontrado, retorne o caminho encontrado
			
			if (noAtual == mapa[fim.x][fim.y]) {
				Celula_Backup atual = noAtual;
				Stack<Celula_Backup> ret = new Stack<Celula_Backup>();
				while (atual.parent != null) {
					ret.push(atual);
					atual = atual.parent;
				}
				System.out.println("FIM");
				return reverse(ret);
			}

			// Caso normal -- mude o CurrentNode da lista aberta para a fechada
			// para
			// cada um dos vizinhos
			listaAberta.remove(noAtual);
			listaFechada.add(noAtual);
			List<Celula_Backup> vizinhos = vizinhos(noAtual);

			for (int i = 0; i < vizinhos.size(); i++) {
				Celula_Backup vizinho = vizinhos.get(i);
				if (vizinho.isOccupied() || listaFechada.contains(vizinho)) {
					// n�o � um n� v�lido, pule para o proximo vizinho
					continue;
				}

				// g � a menor distancia do come�o at� o n� atual,
				// precisamos checar se o caminho que chegamos at� esse vizinho
				// � o menor
				// que vimos at� agora
				int pontoG = noAtual.g + 1; // 1 � a distancia do n� at� o seu
											// vizinho
				boolean pontoGmelhor = false;

				if (!listaAberta.contains(vizinho)) {
					// Essa � a primeira vez que chegamos a esse n�, ele deve
					// ser o melhor
					// Tamb�m devemos somar com h (heuristica) j� que n�o
					// fizemos isso ainda
					pontoGmelhor = true;
					vizinho.h = heuristica(vizinho.getPosicao(), fim);
					listaAberta.add(vizinho);
				} else if (pontoG < vizinho.g) {
					// J� vimos esse n�, mas da ultima vez tinha um pior g
					pontoGmelhor = true;
				}

				if (pontoGmelhor) {
					// Encontrou o caminho at� agora para esse n�. Guarda a
					// informa��o
					// de como chegou aqui e o qu�o bom �
					vizinho.parent = noAtual;
					vizinho.g = pontoG;
					vizinho.f = vizinho.g + vizinho.h;
				}
			}
		}
		throw new Exception();
	}

	public List<Celula_Backup> searchReversePath(Posicao inicio, Posicao fim) throws Exception {

		// Apagam os valores calculados do mapa e as listas
		apagarMap();
		listaAberta.clear();
		listaFechada.clear();
		// ------------------------------------------------

		listaAberta.add(mapa[inicio.x][inicio.y]);
		while (listaAberta.size() > 0) {

			// Pega o menor f(x) para ir para o proximo
			int indiceMenor = 0;
			for (int i = 0; i < listaAberta.size(); i++) {
				if (listaAberta.get(i).f < listaAberta.get(indiceMenor).f) {
					indiceMenor = i;
				}
			}
			Celula_Backup noAtual = listaAberta.get(indiceMenor);

			// Fim -- o resultado foi encontrado, retorne o caminho encontrado
			if (noAtual == mapa[fim.x][fim.y]) {
				Celula_Backup atual = noAtual;
				Stack<Celula_Backup> ret = new Stack<Celula_Backup>();
				while (atual.parent != null) {

					ret.push(atual);
					atual = atual.parent;
				}
				System.out.println("FIM");
				return reverse(ret);
			}

			// Caso normal -- mude o CurrentNode da lista aberta para a fechada
			// para
			// cada um dos vizinhos
			listaAberta.remove(noAtual);
			listaFechada.add(noAtual);
			List<Celula_Backup> vizinhos = vizinhos(noAtual);

			for (int i = 0; i < vizinhos.size(); i++) {
				Celula_Backup vizinho = vizinhos.get(i);
				if (vizinho.isOccupied() || listaFechada.contains(vizinho) || !vizinho.isChecked()) {
					// n�o � um n� v�lido, pule para o proximo vizinho
					continue;
				}

				// g � a menor distancia do come�o at� o n� atual,
				// precisamos checar se o caminho que chegamos at� esse vizinho
				// � o menor
				// que vimos at� agora
				int pontoG = noAtual.g + 1; // 1 � a distancia do n� at� o seu
											// vizinho
				boolean pontoGmelhor = false;

				if (!listaAberta.contains(vizinho)) {
					// Essa � a primeira vez que chegamos a esse n�, ele deve
					// ser o melhor
					// Tamb�m devemos somar com h (heuristica) j� que n�o
					// fizemos isso ainda
					pontoGmelhor = true;
					vizinho.h = heuristica(vizinho.getPosicao(), fim);
					listaAberta.add(vizinho);
				} else if (pontoG < vizinho.g) {
					// J� vimos esse n�, mas da ultima vez tinha um pior g
					pontoGmelhor = true;
				}

				if (pontoGmelhor) {
					// Encontrou o caminho at� agora para esse n�. Guarda a
					// informa��o
					// de como chegou aqui e o qu�o bom �
					vizinho.parent = noAtual;
					vizinho.g = pontoG;
					vizinho.f = vizinho.g + vizinho.h;
				}
			}
		}
		throw new Exception();
	}

	/**
	 * @param stack
	 *            pilha
	 * @return retorna a pilha em ordem reversa na forma de lista
	 */
	private List<Celula_Backup> reverse(Stack<Celula_Backup> stack) {
		ArrayList<Celula_Backup> listaReversa = new ArrayList<Celula_Backup>();
		while (!stack.empty()) {
			listaReversa.add(stack.pop());
		}
		return listaReversa;
	}

	/**
	 * 
	 * @param node
	 *            celula
	 * @return retorna uma lista de c�lulas vizinhas a celula passada como
	 *         parametro
	 */
	private List<Celula_Backup> vizinhos(Celula_Backup no) {
		ArrayList<Celula_Backup> ret = new ArrayList<Celula_Backup>();
		Posicao posicao = no.getPosicao();
		if (posicao.x > 0) {
			ret.add(mapa[posicao.x - 1][posicao.y]);
		}
		if (posicao.x < mapa.length - 1) {
			ret.add(mapa[posicao.x + 1][posicao.y]);
		}
		if (posicao.y > 0) {
			ret.add(mapa[posicao.x][posicao.y - 1]);

		}
		if (posicao.y < mapa.length - 1) {
			ret.add(mapa[posicao.x][posicao.y + 1]);
		}
		return ret;
	}

	/**
	 * @param p0
	 * @param p1
	 * @return retorna a distancia(Manhattan) do ponto p0 ao ponto p1
	 */
	private int heuristica(Posicao p0, Posicao p1) {
		int d1 = Math.abs(p1.x - p0.x);
		int d2 = Math.abs(p1.y - p0.y);
		return d1 + d2;
	}

	/**
	 * apaga os valores calculados do mapa
	 */
	private void apagarMap() {
		for (int i = 0; i < mapa.length; i++) {
			for (int j = 0; j < mapa.length; j++) {
				mapa[i][j].apagar();
			}
		}
	}

}