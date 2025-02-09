LOC 7           ;BEGIN AT LOCATION 7
Data 15         ;PUT 15 AT LOCATION 7
Data 3          ;PUT 3 AT LOCATION 7
Data End        ;PUT 1524 AT LOCATION 7
Data 0
Data 12
Data 9
Data 17
Data 12
LDX 2,7         ;X2 GETS 3
LDR 3,0,15      ;R3 GETS 12
LDR 2,2,15      ;R2 GETS 12
LDR 1,2,15,1    ;R1 GETS 18
LDA 0,0,0       ;R0 GETS 0 to set CONDITION CODE
LDX 1,8         ;X1 GETS 1524
JZ 0,1,0        ;JUMP TO End IF R0 = 0
LOC 1524
End: HLT        ;STOP
