jdb -classpath build/classes nlp.project.SyncGrammarTester  \
  -vcb_src data/train50k.zh.vcb \
  -vcb_trg data/train50k.en.vcb \
  -wa data/train50k.wa \
	-t3_s2t data/train50k.zh_en.t3 \
  -t3_t2s data/train50k.en_zh.t3 \
  -dev_src data/dev10k.zh \
  -dev_trg data/dev10k.en \
  -test_src data/test14k.zh \
  -test_trg data/test14k.en \
  -train_tree_src data/train50k.zh.0.mrg \


 
