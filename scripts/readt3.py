#!/usr/bin/python3

import os, sys, re

def readvcb(vcbfn):
    vcb = {}
    with open(vcbfn, 'r', encoding='utf8') as f:
        for line in f.readlines():
            line=line.strip()
            words = re.split(r'\s+', line)
            ind = int(words[0])
            w = words[1]
            freq = int(words[2])

            vcb[ind] = w
    return vcb

def readt3_stf(t3fn):
    td = {}
    with open(t3fn, 'r') as f:
        for line in f.readlines():
            line = line.strip()
            words = re.split(r'\s+', line)
            s = int(words[0])
            t = int(words[1])
            f = float(words[2])
            
            if td.get(s) is None: td[s] = {}
            td[s][t] = f
    return td

def readt3_tsf(t3fn):
    td = {}
    with open(t3fn, 'r') as f:
        for line in f.readlines():
            line = line.strip()
            words = re.split(r'\s+', line)
            s = int(words[0])
            t = int(words[1])
            f = float(words[2])
            
            if td.get(t) is None: td[t] = {}
            td[t][s] = f
    return td



def sorted_keys(dt):
    k = list(dt.keys())
    return sorted(k, key=lambda x: -dt[x])


if __name__=='__main__':
    srcvcb = readvcb(sys.argv[1])
    tgtvcb = readvcb(sys.argv[2])
    stscore = readt3_stf(sys.argv[3])
    tsscore = readt3_tsf(sys.argv[3])

    foutfn = sys.argv[3]+".sorted"
    fout = open(foutfn, 'w', encoding='utf8')
    for s in stscore.keys():
        dt = stscore[s]
        sk = sorted_keys(dt)
        for t in sk:
            line = "%-20s %-20s %g\n" % (
                    srcvcb.get(s, "MissingIndex"+str(s)), 
                    tgtvcb.get(t, "MissingIndex"+str(t)), 
                    dt[t]);
            fout.write(line);
    fout.close()
