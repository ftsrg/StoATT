grammar Galileo;

faulttree: top';' ((gate|basicevent)';')* EOF;
top: TOPLEVEL name=NAME;
gate: name=NAME operation (inputs+=NAME)*;
basicevent: name=NAME property*; /*TODO: forbid setting the same property more than once*/
property : (lambda|phase|probability|dormancy|repair|numFailureStates);
lambda : LAMBDA EQ val=(DOUBLE|INT);
phase: PH EQ val=rateMatrix;
rateMatrix: '[' ( matrixRow ';')* matrixRow ']';
matrixRow: ((vals+=(DOUBLE|INT))',')* vals+=(DOUBLE|INT);
numFailureStates : FAILURE_STATES EQ val=INT;
probability : PROBABILITY EQ val=(DOUBLE|INT);
dormancy : DORMANCY EQ val=(DOUBLE|INT);
repair: REPAIR EQ val=(DOUBLE|INT);

operation : (or|and|of|wsp);
or : OR;
and: AND;
of: (k=INT)(OF)(n=INT);
wsp: WSP;

INT: DIGIT+;
EQ : '=';
OR : 'or';
EXP: 'e' | 'E';
AND : 'and';
OF: 'of';
WSP: 'wsp';
TOPLEVEL : 'toplevel';
LAMBDA : 'lambda';
PH: 'ph';
PROBABILITY : 'prob';
DORMANCY : 'dorm';
REPAIR : 'repair';
FAILURE_STATES : 'failurestates';
fragment DIGIT : [0-9];
DOUBLE : DIGIT*'.'DIGIT+ | DIGIT+ EXP '-'? DIGIT+ | DIGIT*'.'DIGIT+ EXP '-'? DIGIT+;
NAME: '"'IDENTIFIER*'"'; /*TODO: some better solution for the greediness*/

IDENTIFIER : [a-zA-Z_][a-zA-Z_0-9]*?;

COMMENT : '/*' .*? '*/' -> skip;
WS : [ \t\n\r] -> skip;