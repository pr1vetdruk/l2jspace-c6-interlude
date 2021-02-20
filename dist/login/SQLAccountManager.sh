#!/bin/sh
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*: ru.privetdruk.l2j.tools.accountmanager.SQLAccountManager
