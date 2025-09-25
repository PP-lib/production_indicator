# 製造実績管理プロトタイプ (production_indicator)

現場オペレータが「誰が・どのアイテムを・何本・いつ作業したか」を高速かつオフラインでも記録し、日次 / 作業者別 / 時間帯別の出来高を可視化するための PoC リポジトリです。

## ゴール（PoC段階）
1. Android (Kotlin + Jetpack Compose) で NFC / バーコード / 数量入力フローをエミュレート（実機ハード連携は後段）
2. 端末ローカル(Room)へオフライン記録し、サーバ同期スタブを保持
3. FastAPI + SQLite で最小 API: 実績登録 / 日別集計 / 作業者別 / 時間帯別
4. 集計結果を簡易閲覧（FastAPI内エンドポイント JSON 返却、将来は Streamlit / BI）

## 現場フロー（エミュレーション方針）
| 実際の操作 | プロトでの代替 | 備考 |
|-------------|----------------|------|
| IDカード NFC タッチ | ボタンで擬似ユーザ選択 | 将来: NFCタグ UID → 従業員マスタ照合 |
| 帳票バーコードスキャン | テキスト入力 or ボタンでダミーJAN | 将来: 端末スキャナSDK連携 (Zebra/Honeywell等) |
| 本数テンキー入力 | Composeカスタムテンキー | 手袋考慮で大きめUI |
| 端末 + サーバ時刻記録 | 端末時刻 + サーバ受信時刻 | 改ざん検出: 乖離閾値検討 |
| オフライン一時保存 | Room + unsynced フラグ | WorkManager で再送キュー |
| 出来高確認 | 集計API + (後日)簡易表示 | MVPはJSONのみ |

## ディレクトリ構成（予定）
```
production_indicator/
	README.md
	backend/
		requirements.txt
		app/
			main.py
			database.py
			models.py
			schemas.py
			crud.py
			routers/
				records.py
			__init__.py
	mobile/
		ProductionIndicator/  (Android Studio プロジェクトルート)
			app/
				build.gradle.kts
				src/main/
					AndroidManifest.xml
					java/.../MainActivity.kt
					java/.../data/
					java/.../ui/
					java/.../work/
			build.gradle.kts
			settings.gradle.kts
			gradle.properties
```

## FastAPI 最小仕様
| メソッド | パス | 用途 | ボディ例 / パラメータ |
|----------|------|------|------------------------|
| POST | /records | 実績登録 | {"operator_id":"op1","item_code":"ITEM123","quantity":12,"terminal_time":"2025-09-25T10:12:30Z"} |
| GET | /records/daily?date=2025-09-25 | 日別合計 | - |
| GET | /records/operators/op1?date=2025-09-25 | 作業者別 | - |
| GET | /records/hourly?date=2025-09-25 | 時間帯別集計(00-23) | - |

### モデル（サーバ）
ProductionRecord:
- id (int PK)
- operator_id (str)
- item_code (str)
- quantity (int)
- terminal_time (端末送信時刻 UTC)
- server_time (サーバ受信時刻: 自動)
- created_at / updated_at

監査要件を見据え `server_time` と `terminal_time` の乖離記録を将来追加可能。

## Android クライアント（エミュレーション段階）
### 主要コンポーネント
- Entity: ProductionRecordEntity(id, operatorId, itemCode, quantity, terminalTime, serverId?, syncedFlag, createdAt)
- DAO: insert / getUnsynced / markSynced / dailySummary(Local)
- Repository: ローカル記録 + 同期ワーカー呼び出し
- ViewModel: UI状態管理 (operator, itemCode, quantity, message)
- WorkManager: 未同期データをAPIへPOST（失敗時 backoff）

### 画面遷移（簡易）
1. OperatorSelectScreen (擬似NFC)
2. ItemInputScreen (バーコード入力欄 + ダミーボタン)
3. QuantityInputScreen (テンキー) → 登録
4. SummaryScreen (直近レコード + ローカル集計)

## 立ち上げ手順
### 0. 前提
- Python 3.11+
- Android Studio (Giraffe 以降) + JDK 17

### 1. バックエンド起動
```
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

### 2. API 簡易テスト
```
curl -X POST http://127.0.0.1:8000/records \
	-H 'Content-Type: application/json' \
	-d '{"operator_id":"op1","item_code":"ITEM123","quantity":5,"terminal_time":"2025-09-25T10:10:00Z"}'
curl 'http://127.0.0.1:8000/records/daily?date=2025-09-25'
```

### 3. Android プロジェクトを開く
Android Studio で `mobile/ProductionIndicator` を Open。Sync 後、`app` を実行（エミュレータ可）。

### 4. エミュレート操作例
1. オペレーター選択 → op1
2. アイテムコード入力 → ITEM123
3. 数量 12 入力 → 登録
4. Summary 画面でローカル記録表示

### 5. 同期（将来）
- ネットワーク復帰時 WorkManager が `unsyncedFlag = false` に更新
- サーバから server_time を応答で受け取りローカル更新

## ロードマップ（抜粋）
| フェーズ | 内容 |
|----------|------|
| PoC v0 | 上記最小記録 & 集計 API & ローカル保存 |
| v1 | 実機SDK連携 (Zebra DataWedge / Honeywell) + NFC 実装 |
| v2 | 認証 / 従業員・品目マスタ同期 / 乖離検知 |
| v3 | ダッシュボード強化 (Streamlit→Power BI) |
| v4 | MDM / Kiosk / セキュリティ / 監査ログ強化 |

## ライセンス
TBD

## 開発メモ
初期は SQLite → 運用段階で Postgres へ移行しマイグレーション追加予定。

---
このREADMEは PoC 構築途中です。次ステップ: バックエンド/API スケルトンと Android プロジェクト雛形の追加。