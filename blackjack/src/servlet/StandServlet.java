package servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import service.CardAction;
import util.BlackJackUtil;

/**
 * スタンドボタン押下された時に呼ばれるサーブレット
 * @author カルヤン
 */
@WebServlet("/stand")
public class StandServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// ロガーを呼ぶ
	private static Logger logger = LoggerFactory.getLogger(StandServlet.class);

	/**
	 * スタンドボタン押下時の処理
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("スタンドボタン押下時の処理開始。");

		// ======================= １．データを取得 =======================

		// １．１画面からデータを習得する。
		BufferedReader buffer = new BufferedReader(request.getReader());
		String reqJson = buffer.readLine();

		// ======================= ２．データのバリデーション =======================

		// ２．１ディーラーのカード合計チェック
		try {
			// JSON⇒MAP変換
			ObjectMapper mapper = new ObjectMapper();
			HashMap<String, Object> jsonData =
					mapper.readValue(reqJson, new TypeReference<HashMap<String, Object>>() {});

			// カードを取得
			String[] card = jsonData.get("deck").toString().split(",");
			ArrayList<String> cardList = new ArrayList<String>(Arrays.asList(card));

			// ディーラーのカード取得
			String[] dealerCard = jsonData.get("dealerCardValue").toString().split(",");
			ArrayList<String> dealerCardList = new ArrayList<String>(Arrays.asList(dealerCard));

			// プレイヤーのカード取得
			String[] playerCard = jsonData.get("playerCardValue").toString().split(",");
			ArrayList<String> playerCardList = new ArrayList<String>(Arrays.asList(playerCard));

			// カードロジッククラスのインスタンス生成
			CardAction cardAction = new CardAction();

			// プレイヤーの合計
			int playerCardTotal = cardAction.calculateCardsValue(playerCardList);
			logger.info("プレイヤー合計：　" + playerCardTotal);

			// ディーラーの合計
			int dealerCardTotal = Integer.parseInt(jsonData.get("dealerTotalValue").toString());
			logger.info("プレイヤー合計：　" + dealerCardTotal);

			// ディーラーの合計が17以上の場合
			if (16 < dealerCardTotal) {
				int result = BlackJackUtil.getResult(playerCardTotal, dealerCardTotal);
				// "0"の場合
				if (0 == result) {
					// プレイヤーの勝ち
					jsonData.put("winner", "PLAYER");
				// "1"の場合
				} else if (1 == result) {
					// ディーラーの勝ち
					jsonData.put("winner", "DEALER");
				} else if (2 == result) {
					// 同じ合計の場合
					jsonData.put("winner", "EQUAL");
				}
				// ディーラのカード更新
				jsonData.put("dealerCardValue", dealerCardList);

				// ディーラーの合計更新
				jsonData.put("dealerTotalValue", dealerCardTotal);
			}
			// ======================= ３．データの加工 =======================

			// ディーラーのカード合計チェック
			else if (dealerCardTotal < 17) {
				// 最大ヒット回数５回までループ
				for (int i = 0; i < 5; i++) {
					// ヒットする
					dealerCardList.add(cardList.get(0));

					// ヒットされたカードを削除
					cardList.remove(0);

					// ディーラーの合計チェック
					dealerCardTotal = cardAction.calculateCardsValue(dealerCardList);

					// 17以上の場合
					if (16 < dealerCardTotal) {
						// バストチェック
						boolean isBlast = BlackJackUtil.BlastCheck(dealerCardTotal);
						if (isBlast) {
							jsonData.put("blast", "BLAST");
							jsonData.put("winner", "PLAYER");
						}
						// ディーラーの合計更新
						jsonData.put("dealerTotalValue", dealerCardTotal);

						break;
					}

				}

				// ディーラーのカード更新
				jsonData.put("dealerCardValue", dealerCardList);

				// 勝負判定
				int result = BlackJackUtil.getResult(playerCardTotal, dealerCardTotal);

				// "0"の場合
				if (0 == result) {
					// プレイヤーの勝ち
					jsonData.put("winner", "PLAYER");
				// "1"の場合
				} else if (1 == result) {
					// ディーラーの勝ち
					jsonData.put("winner", "DEALER");
				} else if (2 == result) {
					// 同じ合計の場合
					jsonData.put("winner", "EQUAL");
				}

			}

			// ======================= ４．勝負結果をセットし画面へ返却 =======================

			// カードを格納
			jsonData.put("deck", cardList);

			// 画面へ渡す為データをJSONに変換
			String resData = mapper.writeValueAsString(jsonData);

			// 日本語が表示できるようにする
			response.setContentType("application/json;charset=UTF-8");

			// JSONを返す
			PrintWriter out = response.getWriter();
			out.print(resData);
			out.close();

		} catch(Exception e) {
			logger.error("スタンド時の処理でエラーが発生しました。");
		}

		logger.info("スタンド時の処理正常完了");

	}

}
