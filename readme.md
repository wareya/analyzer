## unnamed japanese text analyzer

generates a word frequency list from japanese utf-8 text  
depends on kuromoji-unidic-kanaaccent from maven  
invoke java -jar analyzer.jar mycorpus.txt > myfrequencylist.txt  
licensed under a public domain–like permissive license  
particles, auxiliary verbs, etc are blacklisted from output

use the companion program to combine lists made from different sources: https://github.com/wareya/normalizer
