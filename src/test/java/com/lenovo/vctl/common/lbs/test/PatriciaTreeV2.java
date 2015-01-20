package com.lenovo.vctl.common.lbs.test;

abstract class ElementoArvore {
	ElementoArvore oPai = null;
	
	public abstract ElementoArvore getDireita();
	public abstract ElementoArvore getEsquerda();
	public abstract String toString();
	public ElementoArvore getPai() {
		return oPai;
	}
}


class FolhaV2 extends ElementoArvore {
	String chave = null;

	public FolhaV2( String s ) {
		chave = s;
	}
	
	public ElementoArvore getDireita() { return null; }
	public ElementoArvore getEsquerda() { return null; }
	public String toString() { return chave; }
}



enum FILHOS {
	ESQ, DIR;
}

class TroncoV2 extends ElementoArvore {
	ElementoArvore filhos[] = { null, null }; // filhos da direita ou esquerda
	int oIndice = 0; // claro, aqui eh o indice da diferenca...
	char oChar = 0; // o caracter do nodo

	public TroncoV2(int idx, char umC, FolhaV2 umFilho) {
		oIndice = idx;
		oChar = umC;
		if (IndexUtil.verificaMenorChar(umFilho.chave, umC, idx))
			filhos[FILHOS.ESQ.ordinal()] = umFilho;
		else
			filhos[FILHOS.DIR.ordinal()] = umFilho;
		
		umFilho.oPai = this;
	}

	public FolhaV2 getOnlyChild() throws Exception {
		if( ( filhos[0] != null && filhos[1] != null ) || 
				( filhos[0] == null && filhos[1] == null ) )
			throw new Exception( "tentativa de pegar apenas um filho, mas não deu certo!");

		ElementoArvore elem = ( filhos[0] != null ) ? filhos[0] : filhos[1];
		if( !( elem instanceof FolhaV2 ) )
			throw new Exception( "tentativa de pegar um filho tronco, não dará certooo!!" );
		
		return ( FolhaV2 )elem;
	}

	
	public void adicionaFilho(ElementoArvore elemento) throws Exception {
		// salva o pai do elemento
		ElementoArvore umPai = elemento.oPai;

		// faz o elemento ser filho dele
		filhos[filhoIndex(null)] = elemento;
		// faz este tronco ser pai do elemento
		elemento.oPai = this;

		// faz o pai deste ter o pai do outro
		this.oPai = umPai;

		// faz o pai atualizar a referencia do outro filho para este de agora
		if( umPai != null && umPai instanceof TroncoV2 ) {
			TroncoV2 umT = ( TroncoV2 )umPai;
			umT.trocaFilho( elemento, this );
		}
	}

	public int filhoIndex(Object o) throws Exception {
		int idxF = IndexUtil.indexOf(filhos, o);
		if (idxF == -1)
			throw new Exception(
					"Foi pedido um indice de filho, mas não existe!");
		return idxF;
	}

	public void trocaFilho(ElementoArvore filhoAntigo, TroncoV2 novoFilho)
			throws Exception {
		filhos[filhoIndex(filhoAntigo)] = novoFilho;
	}

	public ElementoArvore getDireita() {
		return filhos[FILHOS.DIR.ordinal()];
	}

	public ElementoArvore getEsquerda() {
		return filhos[FILHOS.ESQ.ordinal()];
	}

	public String toString() {
		String str;
		char outroC = (oChar == 0) ? '_' : oChar;
		str = "" + oIndice + "/" + outroC;
		return str;
	}
}

public class PatriciaTreeV2 {

	public ElementoArvore raiz = null;

	public void treina(String[] umaLista) throws Exception {
		for (String vlr : umaLista) {
			add(vlr);
		}
	}

	public FolhaV2 buscaSemelhante(ElementoArvore umElem, String compara) {
		if (umElem == null)
			return null;

		if (umElem instanceof FolhaV2) {
			return (FolhaV2) umElem;
		}

		TroncoV2 umTronc = (TroncoV2) umElem;

		if (IndexUtil.verificaMenorChar(compara, umTronc.oChar, umTronc.oIndice))
			return buscaSemelhante(umElem.getEsquerda(), compara);
		else
			return buscaSemelhante(umElem.getDireita(), compara);
	}

	public void posicionaElemento( ElementoArvore umEl, TroncoV2 novoT ) throws Exception {
		novoT.adicionaFilho( umEl );
		if ( umEl == raiz ) {
			raiz = novoT;
		}
	}


	// funcao recursivaaa...
	public void adicionaTronco(ElementoArvore umElem, TroncoV2 novoTronco)
			throws Exception {

		if (umElem instanceof FolhaV2) {
			posicionaElemento( umElem, novoTronco );
			return;
		}

		if ( !(umElem instanceof TroncoV2) )
			throw new Exception( "existem tipos demais por aqui! Melhor VERIFICAR!!");

		TroncoV2 umT = (TroncoV2) umElem;
		int i1 = novoTronco.oIndice;
		char c1 = novoTronco.oChar;

		int i2 = umT.oIndice;
		char c2 = umT.oChar;

		char charFilho = IndexUtil.getChar( novoTronco.getOnlyChild().toString(), umT.oIndice );
				
		// eh AQUIIII OOO EEERRRROOOOO!!!!!
		// TODO: arrumarrrrr

		// pelo jeito aqui tem q verificar se o indice dentro de umT eh igual ao do novoTronco
		if ((i1 < i2) || (i1 == i2 && c1 < c2) ) {
			posicionaElemento( umElem, novoTronco );
			return;
		}

		if ( ( i1 == i2 && c1 == c2 ) || ( charFilho <= c2 ) ) {
			adicionaTronco(umT.getEsquerda(), novoTronco);
			return;
		}

		// TODO: compara o indice do tronco atual da arvore com o indice do filho...
		
		adicionaTronco(umT.getDireita(), novoTronco);
	}

	public ElementoArvore search( String umaChave ) {

		FolhaV2 umaF = buscaSemelhante(raiz, umaChave );
/*		int idx = IndexUtil.indiceDaDiferenca(umaF.chave, novaFolha.chave);
		char umC;
		if (IndexUtil.verificaMenorChar(novaFolha.chave, umaF.chave, idx))
			umC = IndexUtil.getChar(novaFolha.chave, idx);
		else
			umC = IndexUtil.getChar(umaF.chave, idx);
	*/	
		return umaF;
	}
	
	public void add(String s) throws Exception {

		if (raiz == null) {
			raiz = new FolhaV2(s);
			return;
		}

		FolhaV2 novaFolha = new FolhaV2(s);
		FolhaV2 umaF = buscaSemelhante(raiz, s);
		int idx = IndexUtil.indiceDaDiferenca(umaF.chave, novaFolha.chave);
		char umC;
		if (IndexUtil.verificaMenorChar(novaFolha.chave, umaF.chave, idx))
			umC = IndexUtil.getChar(novaFolha.chave, idx);
		else
			umC = IndexUtil.getChar(umaF.chave, idx);

		TroncoV2 novoTronco = new TroncoV2(idx, umC, novaFolha);
		adicionaTronco(raiz, novoTronco);
	}
	
	public static void main(String[] args) throws Exception {
		PatriciaTreeV2 tree = new PatriciaTreeV2();
		tree.add("aaaaabbbbcbcbcbdba");
		tree.add("aaaaabbbbcefffbcbdba");
		System.out.println(tree.search("aaaaabbbbce"));
	}
}

