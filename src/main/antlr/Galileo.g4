grammar Galileo;

faulttree: top';' ((gate|basicevent)';')* EOF;
top: TOPLEVEL name=NAME;
gate: name=NAME operation (inputs+=NAME)*;
basicevent: name=NAME property*; /*TODO: forbid setting the same property more than once*/
property : (lambda|probability|dormancy|repair);
lambda : LAMBDA EQ val=NUMBER;
probability : PROBABILITY EQ val=NUMBER;
dormancy : DORMANCY EQ val=NUMBER;
repair: REPAIR EQ val=NUMBER;

operation : (or|and|of);
or : OR;
and: AND;
of: (k=INT)(OF)(n=INT);

INT: DIGIT+;
EQ : '=';
OR : 'or';
AND : 'and';
OF: 'of';
TOPLEVEL : 'toplevel';
LAMBDA : 'lambda';
PROBABILITY : 'prob';
DORMANCY : 'dorm';
REPAIR : 'repair';
fragment DIGIT : [0-9];
NUMBER : DIGIT+ | DIGIT*'.'DIGIT+;
NAME: '"'[a-zA-Z]IDENTIFIER*'"'; /*TODO: some better solution for the greediness*/

IDENTIFIER : [a-zA-Z_][a-zA-Z_0-9]*?;

COMMENT : '/*' .*? '*/' -> skip;
WS : [ \t\n\r] -> skip;