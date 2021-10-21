package util;

/**
 * Utilクラス <br>
 * @author カルヤン
 */
public class BlackJackUtil {

	/**
	 * バストをチェックします。
	 * @param totalValue カード合計
	 * @return true⇒バスト false⇒セーフ
	 */
	public static boolean BlastCheck(int totalValue) {
		// 21以上の場合
		if (21 < totalValue) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 値が大きいほうを返す
	 * @param playerTotalScore プレイヤーの合計
	 * @param dealerTotalScore ディーラーの合計
	 * @return 0 ⇒ プレイヤーの勝ち、１⇒ ディーラーの勝ち
	 */
	public static int getResult(int playerTotalScore, int dealerTotalScore) {
		// 合計のチェック⇒合計大きい方を返す
		if (dealerTotalScore < playerTotalScore) {
			return 0;
		// 合計が等しい場合
		} else if (playerTotalScore == dealerTotalScore) {
			return 2;
		}
		return 1;
	}

}
