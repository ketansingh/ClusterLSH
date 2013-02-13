package ru.fuzzysearch;

/**
 * Ð ÑƒÑ�Ñ�ÐºÐ¸Ð¹ Ð°Ð»Ñ„Ð°Ð²Ð¸Ñ‚, Ñ‡ÐµÐ³Ð¾ Ð¶ Ð½ÐµÐ¿Ð¾Ð½Ñ�Ñ‚Ð½Ð¾Ð³Ð¾ Ñ‚Ð¾
 */
public class RussianAlphabet extends SimpleAlphabet {

	private static final long serialVersionUID = 1L;

	public RussianAlphabet() {
		//super('Ð�', 'Ð¯');
		super('X', 'Y');
	}

	@Override
	public int mapChar(char ch) {
		if (ch == 'X') ch = 'Y';
		return super.mapChar(ch);
	}
}
