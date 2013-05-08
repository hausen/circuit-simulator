PLUGINJAR=/usr/lib/jvm/java-6-sun-1.6.0.26/jre/lib/plugin.jar
#PLUGINJAR=/usr/share/icedtea-web/plugin.jar

all:
	javac -classpath $(PLUGINJAR):. *.java

jar: circuit.jar

src:
	cd .. && zip -r circuit-src.zip src/Makefile src/*.java src/*.txt src/circuits/

run: all
	java Circuit

circuit.jar: all
	jar cfm circuit.jar Manifest.txt *.class *.txt circuits/

clean:
	rm -f *.class circuit.jar
