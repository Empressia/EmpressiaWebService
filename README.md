# Empressia WebService

* [概要](#概要)
* [使い方](#使い方)
* [設定方法](#設定方法)
* [制限事項](#制限事項)
* [ライセンス](#ライセンス)

## 概要

JAX-RS（Jakarta WebService）用のライブラリです。  

現在は、JSONをWeb APIのパラメーターにバインドするための、  
JSONParamアノテーションだけです。  

ほら、ちょっとしたJSONの型を定義するのって面倒だから、  
PathParamとかQueryParamみたいにできたらいいでしょ？  

## 使い方

例えば、以下のようにWeb APIを設定します。  

```java
	@Path("register")
	@Consumes("application/json")
	public void register(@JSONParam("Issuer") String issuer, @JSONParam("Subject") String subject) {
		System.out.println(issuer + "," + subject);
	}
```

リクエストのBodyに、以下のようなJSONを設定して呼び出すと、  

```json
{"Issuer":"test1","Subject":"test2"}
```

標準出力に、以下のように表示されます。  

> test1 test2

## 設定方法

Gradleであれば、例えば、以下のように設定します。  

* JSON-Pベースで使用する場合
	```groovy
		// use Empressia WebService.
		implementation(group:"jp.empressia", name:"jp.empressia.enterprise.ws.jersey.jsonp", version:"1.1.1");
	```

* Jacksonベースで使用する場合
	```groovy
		// use Empressia WebService.
		implementation(group:"jp.empressia", name:"jp.empressia.enterprise.ws.jersey.jackson", version:"1.1.1");
	```

各サーバーに含まれるライブラリがある場合は、適宜、provided指定を追加してください。  
例えば、以下のようにprovided指定を追加してください。  
指定しない場合は、runtimeとして自動で追加されます。  

* JSON-Pベースで使用する場合
	```groovy
		// use MicroProfile Config API.
		providedCompile(group:"org.glassfish.jersey.core", name:"jersey-server", version:"2.29.1");
		// use JSON-P.
		providedRuntime(group:"javax.json", name:"javax.json-api", version:"1.1.4");
	```

* Jacksonベースで使用する場合
	```groovy
		// use MicroProfile Config API.
		providedCompile(group:"org.glassfish.jersey.core", name:"jersey-server", version:"2.29.1");
		// use Jackson.
		// providedRuntime(group:"com.fasterxml.jackson.core", name:"jackson-databind", version:"2.10.0");
	```

コメントアウトしてる部分は、通常そのままで平気だと思います。  

## 制限事項

* Jerseyに依存しています。
* JSON-PかJacksonが必要です。  
* 値までのパスに配列が含まれるケースはサポートされていません。
* Web APIに指定できる型は以下の通りです。
	|#|型|メモ|
	|-|-|-|
	|1|String||
	|2|int||
	|3|Integer||
	|4|long||
	|5|Long||
	|6|boolean||
	|7|Boolean||
	|8|double||
	|9|Double||
	|10|java.time.LocalDateTime||
	|11|java.time.LocalDate||
	|12|javax.json.JsonValue|（JSON-Pを使用する場合）|
	|13|com.fasterxml.jackson.databind.JsonNode|（Jacksonを使用する場合）|
	|14|Enum||

## ライセンス

いつも通りのライセンスです。  
zlibライセンス、MITライセンスでも利用できます。  

ただし、チーム（複数人）で使用する場合は、MITライセンスとしてください。  
