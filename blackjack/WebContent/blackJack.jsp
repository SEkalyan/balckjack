<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
<link rel="stylesheet" href="style.css"></link>
<title>ブラックジャック</title>
</head>
<body>
	<div class="game">
		<div id="header">
			<input type="button" id="startBtn" value="スタート" ></input>
			<input type="button" id="hitBtn" value="ヒット"></input>
			<input type="button" id="standBtn" value="スタンド"></input>
		</div>
		<div id="result"></div>
		<div id="gameZone">
			<div class="playerZone">
				<h2>プレイヤー</h2>
				<div id="totalValue"></div>
				<div id="availableHitTimes"></div>
				<input type="hidden" name="totalPlayerValue" value=""></input>
				<input type="hidden" name="playerCardValue" value=""></input>
				<input type="hidden" name="availableHitTimes" value=""></input>
			</div>
			<div id="totalValue"></div>
			<div class="dealerZone">
				<h2>ディーラー</h2>
				<div id="totalValue"></div>
				<input type="hidden" name="totalDealerValue" value=""></input>
				<input type="hidden" name="dealerCardValue" value=""></input>
			</div>
			<div class="cardZone"></div>
		</div>
		<div id="deck">
			<input type="hidden" name="deck" value=""></input>
		</div>
	</div>
<script>

	/*----------------------------------------------------------------------------------------------------------------*
	 * オンロード時処理
	 *----------------------------------------------------------------------------------------------------------------*/
	$(document).ready(function() {
		// カードを表示
		 $('.cardZone').css("display", "none");
	});

	/*----------------------------------------------------------------------------------------------------------------*
	 * スタートボタン押下時の処理
	 *----------------------------------------------------------------------------------------------------------------*/
	$(function() {
		$('#startBtn').click(function() {
			$('#result').html("");
			$('.dealerZone #totalValue').html("");
			$.ajax({
				url : "/blackjack/gameStart"
			}).done(function(data) {
				// ゲームスタート時の処理
				startGame(data);

				// 値をhiddenで持つ
				updateValue(data["deck"], data["playerTotalValue"], data["dealerTotalValue"], data["availableHitTimes"]);

			}).fail(function() {
				alert('ajax通信に失敗しました。');
			});

		});
	});

	/*----------------------------------------------------------------------------------------------------------------*
	 * ヒットボタン押下時の処理
	 *----------------------------------------------------------------------------------------------------------------*/
	$(function() {
		$('#hitBtn').click(function() {
			// ヒット回数チェック
			if ($('input[name="availableHitTimes"]').val() < 1) {
				alert('ヒット回数終わりました。スタンドしてください');
				return;
			}
				var data = {
				deck : $('input[name="deck"]').val(), // カード
				playerTotalValue : $('input[name="totalPlayerValue"]').val(), // プレイヤーの合計
				dealerTotalValue : $('input[name="totalDealerValue"]').val(), // ディーラーの合計
				availableHitTimes : $('input[name="availableHitTimes"]').val(), // ヒット残数
				playerCardValue : $('input[name="playerCardValue"]').val(), // プレイヤーのカード
				dealerCardValue : $('input[name="dealerCardValue"]').val() // ディーラーのカード
			}
			$.ajax({
				url : "/blackjack/hit",
				type : "POST",
				data : JSON.stringify(data)
			}).done(function(data) {

				playerCard(data["playerCardValue"]);

				// プレイヤーの合計
				$('#totalValue').html('<h4>合計 ：' + data["totalPlayerValue"] + '</h4>');

				// ヒット回数
				$('#availableHitTimes').html('<h4>ヒット残数： ' + data["availableHitTimes"] + '</h4>');

				// カード表示
 				var deckSize = data["deck"].length;
 				$('.cardZone').html('<h1>' + deckSize + '</h1>');

				var blast = data["blast"];
				// ブラストの場合
				if (blast) {
					alert ('バストです。残念でした！');
					endGameDOM();
					$("#result").html('<h3>ディーラーの勝ちです。</>');
					return;
				}

				// 値をhiddenで持つ
				updateValue(data["deck"], data["playerTotalValue"], data["dealerTotalValue"], data["availableHitTimes"]);

			}).fail(function() {
				alert('ajax通信に失敗しました。');
			});
		});
	});

	/*----------------------------------------------------------------------------------------------------------------*
	 * スタンドボタン押下時処理
	 *----------------------------------------------------------------------------------------------------------------*/
	$(function() {
		$('#standBtn').click(function() {

			var data = {
					deck : $('input[name="deck"]').val(), // カード
					playerTotalValue : $('input[name="totalPlayerValue"]').val(), // プレイヤー合計
					dealerTotalValue : $('input[name="totalDealerValue"]').val(), // ディーラー合計
					availableHitTimes : $('input[name="availableHitTimes"]').val(), // ヒット残数
					dealerCardValue : $('input[name="dealerCardValue"]').val(), // ディーラーのカード
					playerCardValue : $('input[name="playerCardValue"]').val() // プレイヤーカード
				}

			$.ajax({
				url : '/blackjack/stand',
				type : 'POST',
				data : JSON.stringify(data)
			}).done(function(data) {
				// ディーラーのカード表示
				dealerCard(data["dealerCardValue"]);

				// ディーラーの合計
				$('.dealerZone #totalValue').html('<h4>合計 ：' + data["dealerTotalValue"] + '</h4>');

				// カード表示
 				var deckSize = data["deck"].length;
 				$('.cardZone').html('<h1>' + deckSize + '</h1>');

				var blast = data["blast"];

				// ブラストの場合
				if (blast) {
					alert('ディーラーバストです。プレイヤーの勝ちです。');
					$("#result").html('<h3>プレイヤーの勝ちです。</>');

					//ゲーム終了時DOM
					endGameDOM();

					return;
				}

				// 勝ち負けを表示
				var winner = data["winner"];

				if ("EQUAL" == winner) {
					// 同じ合計の場合
					$("#result").html('<h3>勝ち負けないです。</h3>')
				} else {
					// 勝者がいる場合
					$("#result").html('<h3>' + winner + ' の勝ちです。</h3>');
				}

 				// ゲーム終了時DOM
				endGameDOM();

			}).fail(function() {
				alert('ajax通信に失敗しました。');
			});
		});
	});

	/*----------------------------------------------------------------------------------------------------------------*
	 * スタートボタン押下時呼ばれるfunction
	 * @param data サーブレットから返されたデータ
	 *----------------------------------------------------------------------------------------------------------------*/
	function startGame(data) {

		// プレイヤーのゲーム領域表示
		$('.playerZone').css("display", "inline");

		// ディーラーのゲーム領域表示
		$('.dealerZone').css("display", "inline");

		// プレイヤーカード表示
		playerCard(data["playerCard"]);

		// ディーラーのカードを表示
		dealerCard(data["dealerCard"]);

		// カードを表示
		 $('.cardZone').css("display", "inline");

		// カードのサイズ
		var deckSize = data["deck"].length;
		$('.cardZone').html('<h1>' + deckSize + '</h1>');

		var blackJack = data["blackJack"];

		// ディーラーの一枚カード非表示
		if (!data["blackJack"]) {
			$('.dealerZone > h3:first').next().html('<h4>非表示</h4>');
		}

		// プレイヤーの合計
		$('.playerZone #totalValue').html('<h4>合計 ：' + data["playerTotalValue"] + '</h4>');

		// ヒット回数
		$('#availableHitTimes').html('<h4>ヒット残数： ' + data["availableHitTimes"] + '</h4>');

		// カード表示
		$('.cardZone').html('<h1>' + deckSize + '</h1>');

		// ブラックジャックチェック
		if (blackJack) {
			alert('プレイヤーのブラックジャックです。\n 合計：' + data["playerTotalValue"] );
			$("#result").html('<h3>プレイヤーの勝ちです。</>');
			// ゲーム終了時DOM
			endGameDOM();

			return;
		}

		// スタートボタン非表示
		$('#startBtn').css("display", "none");

		// 「ヒットボタン」表示
		$('#hitBtn').css("display", "inline");

		// 「スタンドボタン」表示
		$('#standBtn').css("display", "inline");

	}

	/*----------------------------------------------------------------------------------------------------------------*
	 * プレイヤーのカードの処理
	 * @param cardOfPlayer プレイヤーのカード
	 *----------------------------------------------------------------------------------------------------------------*/
	function playerCard(cardOfPlayer) {
		// プレイヤーのカードを表示
		if (cardOfPlayer) {
			// 既存データを削除
			var beforeHtml = $('.playerZone').find('h3');
			if (beforeHtml) {
				beforeHtml.remove();
			}
			var playerCardValue = "";
			var playerCard = "";
			// 画面にカードを反映させる
			for (var i = 0; i < cardOfPlayer.length; i++) {
				playerCard = cardOfPlayer[i];
				playerCardValue += playerCard;
				if (i != cardOfPlayer.length - 1) {
					playerCardValue += ",";
				}
				$('.playerZone').append('<h3>' + playerCard + '</h3>');
			}
			// プレイヤーのカードをhiddenで持つ
			$('input[name="playerCardValue"]').val(playerCardValue);
		}
	}

	/*----------------------------------------------------------------------------------------------------------------*
	 * ディーラーのカードの処理
	 * @param cardOfDealer ディーラーのカード
	 *----------------------------------------------------------------------------------------------------------------*/
	function dealerCard(cardOfDealer) {
		if (cardOfDealer) {
			// 既存データ削除
			var beforeHtml = $('.dealerZone').find('h3');
			if (beforeHtml) {
				beforeHtml.remove();
			}
			// ディーラーのカードを表示
			var dealerCardValue = "";
			var dealerCard = "";
			// 画面にカードを反映させる
			for (var i = 0; i < cardOfDealer.length; i++) {
				dealerCard = cardOfDealer[i];
				dealerCardValue += dealerCard;
				if (i != cardOfDealer.length - 1) {
					dealerCardValue += ",";
				}
				$('.dealerZone').append('<h3>' + dealerCard + '</h3>');
			}
			// ディーラーのカードをhiddenで持つ
			$('input[name="dealerCardValue"]').val(dealerCardValue);

		}
	}

	/*----------------------------------------------------------------------------------------------------------------*
	 * ゲーム終了時DOM
	 *----------------------------------------------------------------------------------------------------------------*/
	function endGameDOM() {
		// スタートボタン表示
		$('#startBtn').css("display", "inline");
		$('#startBtn').val("再スタート");
		// 「ヒットボタン」非表示
		$('#hitBtn').css("display", "none");
		// 「スタンドボタン」非表示
		$('#standBtn').css("display", "none");
		// 		// ディーラーの一枚カード非表示
	}

	/*----------------------------------------------------------------------------------------------------------------*
	 * 値を更新
	 * @param card カード
	 * @param totalPlayerValue プレイヤーのカード
	 * @param totalDealerValue ディーラーのカード
	 * @param availableHitTimes ヒット残回数
	 *----------------------------------------------------------------------------------------------------------------*/
	function updateValue(card, totalPlayerValue, totalDealerValue, availableHitTimes) {
		// カード
		var card = card;
		var deck = "";
		// カードを","区切りで持つ
		for (var i = 0; i < card.length; i++) {
			deck += card[i];
			if (i != card.length - 1) {
				deck += ',';
			}
		}
		$('input[name="deck"]').val(deck);
		// プレイヤーの合計
		$('input[name="totalPlayerValue"]').val(totalPlayerValue);
		// ディーラーの合計
		$('input[name="totalDealerValue"]').val(totalDealerValue);
		// ヒット残数
		$('input[name="availableHitTimes"]').val(availableHitTimes);
	}

</script>
</body>
</html>