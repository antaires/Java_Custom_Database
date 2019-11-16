default: Item Record Table Database ReadWrite Display Input DbManager
%: %.java
	javac -Xlint $@.java
	java -ea $@
