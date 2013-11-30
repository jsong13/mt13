#!/usr/bin/python3

import sys, re

def clean(inputfn, outputfn=None):
    if outputfn is None:
        outputfn = inputfn.split('.')[0]+'.pnct'
    fin = open(inputfn, 'r', encoding='utf8')
    fout = open(outputfn, 'w', encoding='utf8')

    punctuations = []

    # English punctuations http://en.wikipedia.org/wiki/Basic_Latin_(Unicode_block)    
    punctuations += [chr(x) for x in range(0x20, 0x30)]
    punctuations += [chr(x) for x in range(0x3a, 0x41)]
    punctuations += [chr(x) for x in range(0x5b, 0x61)]
    punctuations += [chr(x) for x in range(0x7b, 0x7f)]

    # dollar, currency, copyright, middle point, ...
    # http://en.wikipedia.org/wiki/Latin-1_Supplement_(Unicode_block)
    punctuations += [chr(x) for x in range(0xa0, 0xc0)]


    # http://en.wikipedia.org/wiki/Latin_script_in_Unicode
    punctuations += [chr(x) for x in [0xd7, 0xf7]]
    punctuations += [chr(x) for x in range(0x2490, 0x24f0)]
    # full width punctuations
    punctuations += [chr(x) for x in range(0xff00, 0xff10)]
    punctuations += [chr(x) for x in range(0xff1a, 0xff21)]
    punctuations += [chr(x) for x in range(0xff3b, 0xff41)]
    punctuations += [chr(x) for x in range(0xff5b, 0xffef)]

    # CJK punctuations and symbols
    punctuations += [chr(x) for x in range(0x3000, 0x3040)]
    # CJK months and letters, such as item list symbol u3280
    punctuations += [chr(x) for x in range(0x3200, 0x3300)]


    for line in fin.readlines():
        line = line.strip()
        line1 = line.strip()
        for c in punctuations:
            line1 = line1.replace(c, ' '+c+' ')

        line1 = re.sub(r'\s{2,}', ' ', line1)
        line1 = line1.strip()

        fout.write(line1)
        fout.write('\n')
        
    fin.close()
    fout.close()


if __name__ == "__main__":
    clean(sys.argv[1])
