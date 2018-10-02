#rem -toascii -toutf8 -folder /XXX/YYY -regex (x+?)
#rem -toascii OR -toutf8 

#java -cp ../dist/QSystem.jar ru.apertum.qsystem.utils.Utf8AndAscii -toascii -folder .\es_ES\ru\apertum\qsystem
java -cp ../dist/QSystem.jar ru.apertum.qsystem.utils.Utf8AndAscii -toascii -folder ./es_ES/ru/apertum/qsystem/
cd es_ES
zip -r0 es_ES.zip *
#zip -r0 es_ES.zip . -i ./es_ES/
#pause
mv es_ES.zip ..
cd ..

