@echo off

rem �����⨬ ���� ��᪮���� 横�

echo ���� �ࢥ� ��।�
rem java -cp dist/QSystem.jar;D:/Apertum/QSkySenderPlugin/dist/QSkySenderPlugin.jar ru.apertum.qsystem.server.QServer debug
java -cp dist/QSystem.jar ru.apertum.qsystem.server.QServer -http 8081 -debug
 
echo ��ࢥ� ��।� �४�⨫ ࠡ���

pause

