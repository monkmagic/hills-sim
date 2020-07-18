@echo off

cls
:: java -cp "sim.jar;libs/*" com.hills.sim.SimEngine --settings settings_teststrategy.html
java -cp "sim.jar;libs/*" com.hills.sim.SimEngine --settings settings_prototype.html
