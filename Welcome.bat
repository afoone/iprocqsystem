@echo off


echo Arrancando kiosco

javaw -cp dist/QSystem.jar ru.apertum.qsystem.client.forms.FWelcome -sport 3128 -cport 3129 -s localhost -wm touch +med +info

pause