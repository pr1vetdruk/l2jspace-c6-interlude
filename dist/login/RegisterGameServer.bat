@echo off
title Register Game Server
color 17
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/* ru.privetdruk.l2j.tools.gsregistering.BaseGameServerRegister -c
pause