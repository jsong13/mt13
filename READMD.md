----------
files in data folder

train50k.en, train50k.zh (both in utf8)
corpus, words are separated by space.

*.vcb (in utf8)
vocabulary files 
each row is in the format:
index word frequency
index is starting from 2. 
index 0 is reserved for NULL.
index 1 is reseverd as separator of records in encoded files.

train50k.zh_train50k.en.snt (in ascii) 
encoded corpus.
every three rows contains a pair of sentences:
a line contains only "1" is a separtor.
the first row after "1" is the sentence in source language (zh),
the second row after "1" is the sentence in target language (en).

*a_b.t3 (in ascii)
translation table from a to b
every line is the format of:  a b P(b|a)
a and b are indices of source and target word. P(b|a) is the probablity translating a to b.

*.wa
word alignment results
every 5 line is a for a sentence pair
1st line:  line number 
2nd line: source sentence encoded in integer
3rd line: target sentence encoded in integer
4th line: word alignment, from source to target
5th line: word alignment, from target to source
position of a word in a sentence is counted from 1, where 0 is reserved of NULL word.
Word alignments might not cover all the way to the end of the sentences, e. g. the number of fields in line 4 migth be smaller than line 2. This is because word alignment implementation cuts off sentences if their lenths > 100.

----------
Compile and run the MTTester using "ant mt"
