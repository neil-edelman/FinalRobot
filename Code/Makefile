# MacOSX gcc 4.2.1

TITLE := Swagbot
FILES := AlexDriver Robot Position Odometer Controller Colour Display Locobot Ping Swagbot
BDIR  := bin
SDIR  := src
OBJS  := $(patsubst %,$(BDIR)/%.class,$(FILES))
SRCS  := $(patsubst %,$(SDIR)/%.java,$(FILES))
BACK  := backup

CC   := nxjc
CF   := -g:none -O -verbose -d $(BDIR) $(SDIR)/*.java -Xlint:unchecked

default: $(OBJS)

$(BDIR)/%.class: $(SDIR)/%.java
	@mkdir -p $(BDIR)
	$(CC) $(CF) $?

.PHONY: clean backup

backup:
	@mkdir -p $(BACK)
	zip $(BACK)/$(TITLE)-`date +%Y-%m-%dT%H%M%S`.zip Makefile $(SRCS)

clean:
	-rm $(OBJS)
