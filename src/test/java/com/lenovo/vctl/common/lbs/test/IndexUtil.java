package com.lenovo.vctl.common.lbs.test;

public class IndexUtil {

	public static boolean verificaMenorChar( String s1, char c2, int idx ) {
		char c1 = IndexUtil.getChar( s1, idx );
		if( c1 <= c2 )
			return true;
		else
			return false;
	}
	
	public static boolean verificaMenorChar( String s1, String s2, int idx ) {
		char c2 = IndexUtil.getChar( s2, idx );
		return verificaMenorChar( s1, c2, idx );
	}
	
	// irah retornar [0 ... len()], ou -1 se nao encontrar nenhuma diferenca...
	public static int indiceDaDiferenca(String s1, String s2) throws Exception {

		if ( s1 == null || s2 == null || s1 == s2 || s1.equals( s2 ) ) {
			throw new Exception(
					"Vc tah tentanto comparar chaves nulas??? ou as mesmas chaves??");
		}
		
		int i;
		// pega o menor valor
		int minLen = (s1.length() < s2.length()) ? s1.length() : s2.length();
		
		int maxLen = (s1.length() >= s2.length()) ? s1.length() : s2.length();
		for (i = 0; i < minLen; ++i) {
			if ( s1.charAt(i) != s2.charAt(i) )
				break;
		}

		if( i >= maxLen ) {
			throw new Exception( "A diferenca aqui tah com algum problemaaaa!" );
		}

		return i;
	}

	public static char getChar( String str, int idx ) {
		int len = str.length();
		char umChar = ( len > idx ) ? str.charAt( idx ) : 0;
		return umChar;
	}
	
	// from: org.apache.commons.lang.ArrayUtils
	public static int indexOf( Object array[], Object objectToFind ) {
		return indexOf( array, objectToFind, 0 );
	}

	// from: org.apache.commons.lang.ArrayUtils
	public static int indexOf(Object[] array, Object objectToFind, int startIndex) {
        if (array == null) {
            return -1;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (objectToFind == null) {
            for (int i = startIndex; i < array.length; i++) {
                if (array[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = startIndex; i < array.length; i++) {
                if (objectToFind.equals(array[i])) {
                    return i;
                }
            }
        }
        return -1;
    }
}