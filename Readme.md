Java version: Java 1.8.0_101

Created by Hassaan Ali. October 18th 2016
Created in Eclipse

Compilation:
$ javac Dictionary.java Spellcheck.java CheckerThread.java Word.java

Run:
java SpellCheck

Overview:

The goal of this project is to create a spellchecker application that allows users to enter their own dictionary in and then parse text based on whether or not it is spelled correctly based on the dictionary. They can enter words into the dictionary via file or through console. Similarly they can do the same for entering text through the console or through a file. After text is parsed users can modify the mispelled words and save their text into a file. 

A HashSet is used for the dictionary as it allows consant time additions as well as lookups, and it does not allow for duplicates.  

The program is multithreaded, as it checks the spelling of each word in its own thread. 

Comments:

- Spell checker does not currently remove punctuation such as periods and brackets when considering spelling.
- The inclusion of multi threadding while spellchecking adds much more complexity. Threads are started in order of word appearance but can finish at different times which means that words must be resorted in order of appearance after threads are joined.
