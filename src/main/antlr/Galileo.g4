grammar Galileo;

faulttree: top (gate|basicevent)* EOF;
top: TOPLEVEL name=NAME;
gate: name=NAME op=(OR|AND) NAME*;
basicevent: name=NAME property*; /*TODO: forbid same kind of properties*/
property : (lambda|probability|dormancy);
lambda : LAMBDA EQ val=NUMBER;
probability : PROBABILITY EQ val=NUMBER;
dormancy : DORMANCY EQ val=NUMBER;

EQ : '=';
OR : 'or';
AND : 'and';
TOPLEVEL : 'toplevel';
LAMBDA : 'lambda';
PROBABILITY : 'prob';
DORMANCY : 'dorm';
fragment DIGIT : [0-9];
NUMBER : DIGIT+ | DIGIT*'.'DIGIT+;
NAME: [a-zA-Z]IDENTIFIER;

IDENTIFIER : [a-zA-Z_0-9]+;

COMMENT : '/*' .*? '*/' -> skip;
WS : [ \t\n\r] -> skip;