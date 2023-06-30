TEST_IMAGES:=			ct.crw
TARGET_FILE:=			file.list

LDFLAGS:=			-shared -llcms

JAVAC:=				javac
#JFLAGS:=			-source 1.5 -Xlint:unchecked

CFLAGS:=			-g

CPPFLAGS:=			-I/home/qrux/lcms/include
CPPFLAGS+=			-I/usr/lib/java/include
CPPFLAGS+=			-I/usr/lib/java/include/linux
CPPFLAGS+=			-I/home/qrux/dlroom/include

BOOTCLASSPATH:=			/usr/lib/SunJava2-1.4.2/jre/lib/rt.jar:/usr/lib/SunJava2-1.4.2/jre/lib/sunrsasign.jar:/usr/lib/SunJava2-1.4.2/jre/lib/jsse.jar:/usr/lib/SunJava2-1.4.2/jre/lib/jce.jar:/usr/lib/SunJava2-1.4.2/jre/lib/charsets.jar


.PHONY:			all
all:			clean link java


.PHONY:			old-all
old-all:		clean link java jni libnativecme.so


.PHONY:			link
link:
	@if [ ! -e dlroom ] ; then ln -s . dlroom ; fi


.PHONY:			java
java:
	$(JAVAC) $(JFLAGS) -g @$(TARGET_FILE)
#	jikes -source 1.4 -bootclasspath $(BOOTCLASSPATH) -g @$(TARGET_FILE)


.PHONY:			jni
jni:
	javah -verbose -d include -jni dlroom.cms.CMM



libnativecme.so:		cms/nativecme.o
	g++ $(LDFLAGS) -o $@ $<



.PHONY:			run
run:			all
	java -Xmx1500m com.crinqle.dlroom.app.Main $(TEST_IMAGES) > /dev/null


.PHONY:			clean
clean:
	find . -name "*.class" -exec rm -f {} \;
	rm -f lib*.so
	rm -f include/*.h
