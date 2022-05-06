JAVAC_CMD=${JAVA_HOME}/bin/javac
# javac -target 1.5 -source 1.5

all: AddressBook.class

clean:
	rm -f *.class
	rm -rf results/
	rm -f runparesults

AddressBook.class: AddressBook.java
	$(JAVAC_CMD) AddressBook.java

submission: submit/alias.dtl submit/problem1.txt submit/alias_refined.dtl submit/pacs_field_insensitive.dtl submit/problem3.txt submit/problem4.txt 
	tar czvf submission.tar.gz $^


