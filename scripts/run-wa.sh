[ -z $1 ] && exit

stamp=`date +%y-%m-%d.%H%M%S`.`whoami`

src=$1.zh
trg=$1.en

plain2snt $src $trg 1>$stamp.plain2snt.stdout 2>$stamp.plain2snt.stderr


if [ -z $2 ]; then 
	snt2cooc ${src}_${trg}.cooc ${src}.vcb ${trg}.vcb ${src}_${trg}.snt 1>$stamp.snt2cooc.stdout 2>$stamp.snt2cooc.stderr
else
	snt2cooc ${src}.vcb ${trg}.vcb ${src}_${trg}.snt 1>${src}_${trg}.cooc 2>$stamp.snt2cooc.stderr
fi


mkcls -c80 -n5 -p$src.vcb -V$src.vcb.classes 1>$stamp.mkcls.stdout 2>$stamp.mkcls.stderr
mkcls -c80 -n5 -p$trg.vcb -V$trg.vcb.classes 1>>$stamp.mkcls.stdout 2>>$stamp.mkcls.stderr

mgiza -s ${src}.vcb -t ${trg}.vcb -c ${src}_${trg}.snt -coocurrence ${src}_${trg}.cooc 1>$stamp.mgiza.stdout 2>$stamp.mgiza.stderr

