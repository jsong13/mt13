WORD ALIGNMENT files in data-* folder

train50k.en, train50k.zh (both in utf8)
corpus, words are separated by space.

*.vcb 
vocabulary file in utf8 encoding, 
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

*.t3.final (in ascii)
translational table
every line is the format of:  si ti P(ti|si)
si and ti are indices of source and target word. P(ti|si) is the probablity translating si to ti.

*.t3.final.sorted (in utf8)
translation table in string format. Each row is 
s t P(t|s)
where rows are sorted from largest P(t|s) to smallest when s is given.



Compile and run the MTTester using "ant mt"
