SRW GC Menu Text Editor by Dashman
-----------------

As always, you need to have Java installed in your computer to run this.

Use this program to open and edit the file add02dat.bin. It only edits the text (there's other stuff in that file).

When you're done editing, save it to another file (I haven't put an option to save over the open file, just in case).

To reinsert the file into the game, you'll need the program Gamecube Rebuilder. Grab it here: http://www.romhacking.net/utilities/619/

The program will automatically convert your keystrokes to Shift-JIS (if you don't uncheck the option in the Edit menu). 
It's recommended to leave it like that *for now*. If the game receives a VWF *maybe* that option will be useful.

If you paste text on a field, it won't be converted to Shift-JIS though, don't try it.... unless you're pasting Shift-JIS code, in which case it's okay.

You can't overwrite highlighted text. That is, the program won't let you; if you try, you'll get what you wrote + the highlighted text.
UPDATE: Now you can. Sorry about that.

There are a couple of special characters that are displayed "wrong" on purpose: the roman numbers for 1, 2 and 3, as well as the inverted omega in Dragogameo 1's name. Don't modify these characters! The reason they're displayed like that is because they're stored as non-standard Shift-JIS characters, and if the program used those it would classify them as just "?", and their values would be lost.

If you write on top of any of them, you can just copy the originals from other entries or find them in this page: http://www.rikai.com/library/kanjitables/kanji_codes.sjis.shtml

The characters you'd be looking for would be:

I - 83 a7 (another I)
II - 84 50 (Pi)
III - 84 59 (looks like a III... but it's open at the top)
inverted omega - 84 5f (looks like an I-O symbol)

If you have a fairly recent computer and the program looks like it's made for ants... I'm terribly sorry. The computer I programmed this on is from... 2007? 
Anyway, it looked bigger in that one. I'll see if I can make something that doesn't hurt anybody's eyes, but no promises. Java is a bitch when it comes to modifying UIs.


* When reinserting with Gamecube Rebuilder, do the following:
1) Open your original image (image -> open)

2) Right-click on root -> export to a folder (preferably empty)

3) You'll have a root folder there with all the files. Replace add02dat.bin with your bigger version

4) Close the image (image -> close)

5) Select Root -> Open and browse the root folder you created

6) Select Root -> Save... and browse for a path + file where you want to save the new ISO (this does NOT create the ISO)

7) Select Root -> Rebuild

The extracted root folder doesn't get destroyed in the process, so you can start this method from point 5) the next time if you don't delete it.
