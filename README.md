LDProxy
=======
keep you LD URI's resolvable

```sh
mvn package
mkdir cached
rm *.txt && java -jar target/ldproxy.jar
```

![FF proxy settings](assets/Preferences.png)

http://purl.org/dc/terms/abstract will return the stored [DC terms RDF XML](src/resources/dc.xml)


----
Based on https://github.com/stefano-lupo/Java-Proxy-Server