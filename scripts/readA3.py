#!/usr/bin/python3


import re, sys, os

def dict2array(dct):
    ret = []
    for k in sorted(dct.keys()):
        ret.append(dct[k])
    return ret 

def parseTrgLine(line):
    # each unit goes like |word ({ 9 12 }) | or |word ({ }) |
    pat = re.compile(r"^([^\s]+) \(\{([\d ]+)\}\)\s*")
    wast = {}
    start = 0
    iword = 0
    words = []

    while start < len(line):
        m = pat.search(line[start:])

        if m is None:
            raise Exception("Wrong format in A3");

        word = m.group(1) 
        words.append(m.group(1))

        poses = []
        for ip in  re.split(r"\s+", m.group(2)):
            if len(ip)==0: continue
            i = int(ip)
            if wast.get(i) is not None:
                raise Exception("Wrong format a3")
            wast[i] = iword
        
        iword += 1
        start += m.end();

    wast1 = dict2array(wast)
    return ( wast1, " ".join(words[1:]) )


def loadA3part(fn):
    ret = []
    with open(fn, 'r', encoding='utf8') as f:
        lineno = None
        linesrc = None
        linetrg = None

        linesAfterComment = 0;
        for line in f.readlines():
            line = line.strip()

            if line[0] == '#':
                linesAfterComment=0
                m = re.search(r"^# Sentence pair \((\d+)\)", line)
                if m is None:
                    raise Exception("Wrong input format", line);
                lineno = int(m.group(1))

            if linesAfterComment==1:
                linesrc = line
            
            if linesAfterComment==2:
                wa, linetrg = parseTrgLine(line)
                wa_str = " ".join([str(a) for a in wa])
                
                ret.append([lineno, linesrc, linetrg, wa_str])

            linesAfterComment += 1
    return ret




if __name__=="__main__":
    if len(sys.argv) < 2:
        print("""
read *.A3.final.part* in a given directory. 
output to one or serveral merged files in the format of 4-line record
first line, line no: #12## 
second line, target sentence in string
third line, source sentence in string
last line, target->source alignment
""")
        exit(0)
    basepath = sys.argv[1]
    
    A3finals = {}

    for r, d, files in os.walk(basepath):
        for f in files:
            m = re.search(r"(.*\.A3\.final)\.part.$", f)
            if m is None: continue
            pr = os.path.join(r, m.group(1))
            if A3finals.get(pr) is None:
                A3finals[pr] = []
            A3finals[pr].append(os.path.join(r,f))

        break  # skip subfolders
    

    for k, parts in A3finals.items():
        records = []
        for p in parts:
            print(p + " ... ", end=" ")
            records += loadA3part(p)
            print(" done.")
            sys.stdout.flush()
        nr  = sorted(records, key = lambda x : int(x[0]))
        print("writing out to " + k, end=" " )
        with open(k, "w", encoding="utf8") as f:
            for nrr in nr:
                f.write("#" + str(nrr[0]) + "##" + "\n")
                f.write(nrr[1] + "\n")
                f.write(nrr[2] + "\n")
                f.write(nrr[3] + "\n")

        print("done" )

