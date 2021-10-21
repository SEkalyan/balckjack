package service;

import java.util.ArrayList;

public class CardAction {
	/**
	 * カードの値をセットし合計を計算する。
	 * @param cards 計算するカード
	 * @return totalValue カード合計
	 */
	public int calculateCardsValue(ArrayList<String> cards) {
		// カード合計
		int totalValue = 0;
		// ACEの存在チェック
		int aces = 0;
		// カードで繰り返し
		for (String singleCard : cards) {
			String[] val = singleCard.split("_");
			String cardValue = val[1];
			switch (cardValue) {
			case "2" : totalValue += 2; break;
			case "3" : totalValue += 3; break;
			case "4" : totalValue += 4; break;
			case "5" : totalValue += 5; break;
			case "6" : totalValue += 6; break;
			case "7" : totalValue += 7; break;
			case "8" : totalValue += 8; break;
			case "9" : totalValue += 9; break;
			case "10" : totalValue += 10; break;
			case "JACK" : totalValue += 10; break;
			case "QUEEN" : totalValue += 10; break;
			case "KING" : totalValue += 10; break;
			case "ACE" : aces += 1; break;
			}
		}
		// ACEの値を決める
		for (int j = 0; j < aces; j++) {
			// ACE以外の合計が１０より大きいい場合
			if (10 < totalValue) {
				totalValue += 1;
			// ACE以外の合計が１０以下の場合
			} else {
				totalValue += 11;
			}
		}
		return totalValue;
	}

}
