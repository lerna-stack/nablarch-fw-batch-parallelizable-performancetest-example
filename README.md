# nablarch-fw-batch-parallelizable-performancetest-example

*nablarch-fw-batch-parallelizable* を使用したバッチアプリケーションの性能検証サンプルです。  
本サンプルは以下の記事中で使用したバッチアプリケーションです。

https://fintan.jp/?p=7061

※本アプリケーションは並列実行時のスループット差異を計測しやすくするために、ダミーウエイトを挿入しています。そのため、通常より、処理に時間がかかります。詳細は上記ブログ記事をご確認ください。

※*nablarch-fw-batch-parallelizable* は、*nablarch-fw-batch* でバッチ並列実行を実現するカスタムハンドラを提供するライブラリです。
[*nablarch-fw-batch-parallelizable*](https://github.com/lerna-stack/nablarch-fw-batch-parallelizable/) や [*nablarch-fw-batch*](https://nablarch.github.io/docs/LATEST/doc/application_framework/application_framework/batch/index.html) の 利用方法については、
それぞれの 公式ドキュメント をご確認ください。

## Requirements
次のソフトウェアがインストールされている必要があります。

* *JDK 8*
* *Maven 3.0.5+*


## Target versions
動作確認済みの
[*nablarch-bom*](https://github.com/nablarch/nablarch-profiles) および
*nablarch-batch-parallelizable* は次のバージョンです。

* *nablarch-bom 5u19*
* *nablarch-fw-batch-paralellizable 1.2.0*

## Getting started

### リソースを生成する

データベースリソース(*H2DB*のデータベースファイル)とDBにアクセスするためのコードを生成します。
生成するには次のコマンドを実行してください。

```shell
mvn clean generate-resources
```

生成に成功すると、次のようなログがコンソールに出力されます。

```shell
(...truncated)
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
(...truncated)
```

生成されるリソースは次のようなファイルです。
- `h2/db/nablarch_example.mv.db`
- `target/classes/*.dicon`
- `target/ddl/*.sql`
- `target/generated-sources/**/*.java`


### アプリケーションをビルドする

アプリケーションをビルドします。
ビルドするには次のコマンドを実行してください。

*注意* アプリケーションをビルドするためには、リソースの生成が完了している必要があります。
リソースを生成する手順は、[リソースを生成する](#リソースを生成する)をご覧ください。

```shell
mvn package
```
    
ビルドに成功すると、次のようなログがコンソールに出力さます。

```shell
(...truncated)
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
(...truncated)
```

ビルドされるファイルは次のようなファイルです。
- `target/application-1.2.0.zip`
- `target/nablarch-fw-batch-parallelizable-performancetest-example-1.2.0.jar`
- etc...


### アプリケーションを実行する

サンプルとして、*データバインドを使用した住所登録バッチ* が実装されています。
*データバインドを使用した住所登録バッチ* では、
指定フォルダ内(デフォルトでは[work/input](./work/input))の住所情報CSVファイルを、データバインドを使用して読み込み、DBに登録します。
指定フォルダは、[import-zip-code-file.properties](src/main/resources/batch-config/import-zip-code-file.properties) で変更できます。

*データバインドを使用した住所登録バッチ* を実行するには、次のコマンドを実行してください。  
※このコマンドはダミーウエイトにより時間がかかります。`COMMIT COUNT=[20000]`になると終了します。

```shell
mvn exec:java -Dexec.mainClass=nablarch.fw.launcher.Main -Dexec.args="'-requestPath' 'ImportZipCodeFileAction/ImportZipCodeFile' '-diConfig' 'classpath:import-zip-code-file.xml' '-userId' '105'"
```


`maven-assembly-plugin` を使用して実行可能 jar の生成を行っているため、
*データバインドを使用した住所登録バッチ* は次の手順でも実行できます。

1. `target/application-1.2.0.zip` をtarget ディレクトリに解凍します。  
   ```shell
   unzip -o target/application-1.2.0.zip -d target/
   ```
1. `jar` からバッチを起動します。
    ```shell
    java -Dexec.mainClass=nablarch.fw.launcher.Main -jar target/nablarch-fw-batch-parallelizable-performancetest-example-1.2.0.jar '-requestPath' 'ImportZipCodeFileAction/ImportZipCodeFile' '-diConfig' 'classpath:import-zip-code-file.xml' '-userId' '105'
    ```


### データベースを確認する

バッチを実行すると、*H2DB*のDBファイル [nablarch_example.mv.db](h2/db/nablarch_example.mv.db) にデータが書き込まれます。
*データバインドを使用した住所登録バッチ* では、`PUBLIC` データベースの `ZIP_CODE_DATA` テーブル に住所情報を登録します。

*H2DB* に接続し、次の SQL 文を実行すると登録された情報を確認できます。
```sql
SELECT * FROM PUBLIC.ZIP_CODE_DATA;
```

#### H2DB に接続する方法

H2DB をインストールし、ブラウザのH2コンソールからデータベースに接続できます。
これ以外にも、好みのIDEでデータベース接続プラグインを使う方法があります。
例えば、*IntelliJ IDEA* では
[データベース接続](https://pleiades.io/help/idea/connecting-to-a-database.html#connect-to-couchbase-database)
に接続方法が記載されています。

1. [H2 Database Engine Cheat Sheet](http://www.h2database.com/html/cheatSheet.html) から*H2DB*をインストールしてください。
1. `{インストールフォルダ}/bin/h2.bat` (Windowsの場合)を実行してください。
1. ブラウザから [http://localhost:8082](http://localhost:8082) を開き、次に示す情報でH2コンソールにログインしてください。  
  `JDBC URLの {dbファイルのパス}` には、`nablarch_example.mv.db` ファイルの[格納ディレクトリ](h2/db)までの絶対パスを指定してください。  
  H2コンソールからデータベースに接続している間はバッチアプリケーションからDBにアクセスすることができないため、バッチを実行できないことに注意してください。

| 設定項目   | 設定値                                    |
| -------- | ---------------------------------------- |
| JDBC URL | `jdbc:h2:{dbファイルのパス}/nablarch_example` |
| ユーザ名   | `NABLARCH_EXAMPLE`                         |
| パスワード | `NABLARCH_EXAMPLE`                         |

## License

`nablarch-fw-batch-parallelizable-performancetest-example` is released under the terms of the [Apache License Version 2.0](LICENSE.txt).

© 2021 TIS Inc.
