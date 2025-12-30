# Parksys Standalone Security APK

## 概要
QRコード不要・インストールするだけで動作するスタンドアロンセキュリティアプリ

## 特徴
- **QRコード不要**: インストール→起動→権限許可の3ステップで完了
- **内蔵ポリシー**: セキュリティ設定がアプリに組み込み済み
- **自動起動**: 端末再起動時もセキュリティサービスが自動開始
- **常駐保護**: バックグラウンドで継続的にセキュリティを適用

## ビルド方法

### GitHub Actionsでビルド（推奨）
1. このフォルダをGitHubリポジトリにプッシュ
2. Actionsタブで「Build Standalone Security APK」ワークフローを実行
3. Artifactsから生成されたAPKをダウンロード

### ローカルでビルド（Android Studio）
1. Android Studioでこのフォルダを開く
2. `File > Sync Project with Gradle Files`
3. `Build > Build Bundle(s) / APK(s) > Build APK(s)`
4. `app/build/outputs/apk/`からAPKを取得

### コマンドラインでビルド
```bash
./gradlew assembleRelease
```

## インストール手順
1. APKファイルをAndroid端末に転送
2. インストールしてアプリを起動
3. 「デバイス管理者権限」を許可
4. セキュリティ保護が自動で有効化

## 内蔵セキュリティポリシー
- カメラ: 無効
- マイク: 無効  
- 画面キャプチャ: 無効

## 動作要件
- Android 7.0 (API 24) 以上
- Android 14 (API 34) まで対応

---
Parksys Co., Ltd.
