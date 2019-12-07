grammar Galileo;

faulttree: top';' ((gate|basicevent)';')* EOF;
top: TOPLEVEL name=NAME;
gate: name=NAME operation (inputs+=NAME)*;
basicevent: name=NAME property*; /*TODO: forbid setting the same property more than once*/
property : (lambda|phase|probability|dormancy|repair|numFailureStates);
lambda : LAMBDA EQ val=NUMBER;
phase: PH EQ val=rateMatrix;
rateMatrix: '[' ( matrixRow ';')* matrixRow ']';
matrixRow: (NUMBER',')* NUMBER;
numFailureStates : FAILURE_STATES EQ val=INT;
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
PH: 'ph';
PROBABILITY : 'prob';
DORMANCY : 'dorm';
REPAIR : 'repair';
FAILURE_STATES : 'failurestates';
fragment DIGIT : [0-9];
NUMBER : DIGIT+ | DIGIT*'.'DIGIT+;
NAME: '"'[a-zA-Z]IDENTIFIER*'"'; /*TODO: some better solution for the greediness*/

IDENTIFIER : [a-zA-Z_][a-zA-Z_0-9]*?;

COMMENT : '/*' .*? '*/' -> skip;
WS : [ \t\n\r] -> skip;