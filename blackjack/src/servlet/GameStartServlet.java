package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import service.CardAction;


/**
 * スタートボタン押下した際に呼ばれるサーブレット<br>
 * Servlet implementation class MainServlet
 * @author カルヤン
 */
@WebServlet("/gameStart")
public class GameStartServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// ロガーを呼ぶ
	private static Logger logger = LoggerFactory.getLogger(GameStartServlet.class);

	/**
	 * スタートボタン押下時の処理
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("ゲームスタート処理開始");

		// ========================= １．カードが配られる =========================

		// （２~1０）までの数字と英字（J, Q, K, A）
		String[] suit = {"スペード", "ダイヤ", "クラブ", "ハート"};
		// カードの種類
		String[] value = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "JACK", "QUEEN", "KING", "ACE"};

		// カードを用意する
		ArrayList<String> card = new ArrayList<>();

		// カードの種類分繰り返し
		for(String cardSuit : suit) {
			// 値がある分繰り返し
			for(String cardValue : value) {
				// カードをリストに格納
				card.add(cardSuit + "_" + cardValue);
			}
		}

		// カードをシャフル
		Collections.shuffle(card);

		// プレイヤーに配られるカード格納用
		ArrayList<String> playerCard = new ArrayList<>();

		// ディーラに配られるカード格納用
		ArrayList<String> dealerCard = new ArrayList<>();

		// 最初プレイヤーに配られるカード（二枚）
		playerCard.add(card.get(0));
		playerCard.add(card.get(2));

		// 最初ディーラーに配られるカード（二枚）
		dealerCard.add(card.get(1));
		dealerCard.add(card.get(3));

		// 配られたカード削除 (上から4枚)
		for (int i = 0; i < 4; i++) {
			card.remove(0);
		}

		// ========================= ２．カードの合計チェック =========================

		// カードの値を決めて合計を計算
		CardAction cardAction = new CardAction();

		// プレイヤーの合計
		int totalPlayerValue = cardAction.calculateCardsValue(playerCard);
		logger.info("プレイヤー合計　" + totalPlayerValue);

		// ディーラーの合計
		int totalDealerValue = cardAction.calculateCardsValue(dealerCard);
		logger.info("ディーラー合計　" + totalDealerValue );

		// データを返却用
		Map<String, Object> data = new HashMap<>();

		// ブラックジャックチェック
		if (21 == totalPlayerValue) {
			// ブラックジャックを設定
			data.put("blackJack", "BLACKJACK");
		}

		// ========================= ３．データをセットし画面へ返却 =========================

		// カード
		data.put("deck", card);
		// プレイヤーのカード
		data.put("playerCard", playerCard);
		// ディーラーのカード
		data.put("dealerCard", dealerCard);
		// プレイヤーの合計
		data.put("playerTotalValue", totalPlayerValue);
		// ディーラーの合計
		data.put("dealerTotalValue", totalDealerValue);
		// ヒット残回数　※最大5回
		int availableHitTimes = 5;
		data.put("availableHitTimes", availableHitTimes);

		try {
			// Map⇒JSON変換
			ObjectMapper mapper = new ObjectMapper();
			// 画面に渡す為JSONに変換
			String resData = mapper.writeValueAsString(data);

			// 日本語が表示できるようにする
			response.setContentType("application/json;charset=UTF-8");

			PrintWriter out = response.getWriter();
			// JSONを返す
			out.print(resData);
			out.close();
		} catch (Exception e) {
			logger.error("ゲームスタート処理でエラーが発生しました。");

		}

		logger.info("ゲームスタート処理正常完了");
	}

}
