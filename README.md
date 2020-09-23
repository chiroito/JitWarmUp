# JIT コンパイルのウォーミングアップ監視ツール

JIT コンパイルされたメソッド数を数えて、十分にコンパイルされるとMXBeanに`true`を設定する。

```
mvn package
java -javaagent:target\JitWarmUp-1.0-SNAPSHOT.jar XxxApplication
```

`-javaagent:target\JitWarmUp-1.0-SNAPSHOT.jar=xxx=value1,yyy=value2,zzz=value3`とすることでパラメータを設定できる。

パラメータとその名前およびデフォルト値は以下のとおり。

- カウントするコンパイルレベル(compileLevel=4)
- 十分にコンパイルされたと判断するメソッド数(threshold=2000)
- MXBeanの名前(mxbeanName=com.example:type=JitWarmUp)

カウントするコンパイルレベルは 0 から 4 までを指定します。十分にコンパイルされたと判断するメソッド数は 0 以上intの最大値以下を指定します。MXBeanの名前については任意の名前を付けられます。詳しくは MXBean のドキュメントを参照して下さい。

例：カウントするコンパイルレベルは 4 （サーバコンパイル）以上で、十分にコンパイルされたと判断するメソッド数が 5000 メソッド
```
-javaagent:target\JitWarmUp-1.0-SNAPSHOT.jar=compileLevel=4,threshold=5000
```