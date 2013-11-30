# range inclusive
trainstart=1
trainend=49997
trainsize=`expr $trainend - $trainstart + 1`

devstart=49998
devend=60005
devsize=`expr $devend - $devstart + 1`

teststart=60006
testend=74067
testsize=`expr $testend - $teststart + 1`

cat 131127_EN.pnct | head -n $trainend | tail -n $trainsize > train50k.en
cat 131127_EN.pnct | head -n $devend | tail -n $devsize > dev10k.en
cat 131127_EN.pnct | head -n $testend | tail -n $testsize > test14k.en

cat 131127_ZH.pnct | head -n $trainend | tail -n $trainsize > train50k.zh
cat 131127_ZH.pnct | head -n $devend | tail -n $devsize > dev10k.zh
cat 131127_ZH.pnct | head -n $testend | tail -n $testsize > test14k.zh
