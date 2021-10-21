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
 * ヒットボタン押下された時に呼ばれるサーブレット<br>
 * Servlet implementation class HitServlet
 * @author カルヤン
 */
@WebServlet("/hit")
public class HitServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	// ロガーを呼ぶ
	private static Logger logger = LoggerFactory.getLogger(HitServlet.class);

	/**
	 * ヒットボタン押下時の処理
	 * @throws IOException
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("ヒット時の処理開始");

		// ========================== １．データ取得 ==========================

		// 送信されたJSONの取得
		BufferedReader buffer = new BufferedReader(request.getReader());
		String reqJson = buffer.readLine();

		try {
			// JSON⇒HASHMAP変換
			ObjectMapper mapper = new ObjectMapper();
			HashMap<String, Object> jsonData =
					mapper.readValue(reqJson, new TypeReference<HashMap<String, Object>>() {});

			// ２．========================== データのバリデーション ==========================

			// ２．１ヒット残回数のチェック
			int availableHitTimes = Integer.parseInt(jsonData.get("availableHitTimes").toString());
			logger.info("ヒット残回数：　" + availableHitTimes);
			// ヒット回数が０の場合
			if (availableHitTimes < 1) {
				jsonData.put("hitTimeOver", "HITTIMEOVER");
			} else {
				// ヒット回数デクリメント
				availableHitTimes--;
				// ヒット回数更新
				jsonData.put("availableHitTimes", availableHitTimes);
			}
			// ３．========================== データの加工 ==========================

			// プレイヤーのカードを取得する。
			String[] playerCard = jsonData.get("playerCardValue").toString().split(",");
			ArrayList<String> playerCardList = new ArrayList<String>(Arrays.asList(playerCard));

			// カードを取得
			String[] deck = jsonData.get("deck").toString().split(",");
			ArrayList<String> card = new ArrayList<String>(Arrays.asList(deck));

			// カードをヒットする。
			CardAction cardAction = new CardAction();

			// ヒットする
			playerCardList.add(card.get(0));

			// ヒットしたカードを削除
			card.remove(0);

			// カードの更新
			jsonData.put("deck", card);

			// プレイヤーの合計計算
			int playerCardTotal = cardAction.calculateCardsValue(playerCardList);

			// バーストチェック
			boolean isBlast = BlackJackUtil.BlastCheck(playerCardTotal);

			// バーストの場合
			if (isBlast) {
				// バーストをセット
				jsonData.put("blast", "BLAST");
			}
			// プレイヤー合計更新
			jsonData.put("totalPlayerValue", playerCardTotal);

			// プレイヤーのカード更新
			jsonData.put("playerCardValue", playerCardList);

			// ========================== ４．加工データをセットし画面へ返却 ==========================

			// 画面へ渡す為データをJSONに変換
			String resData = mapper.writeValueAsString(jsonData);

			// 日本語が表示できるようにする
			response.setContentType("application/json;charset=UTF-8");

			// JSONを返す
			PrintWriter out = response.getWriter();
			out.print(resData);
			out.close();

		} catch(Exception e) {
			logger.error("ヒット時の処理でエラーが発生しました。");
		}

	}

}
