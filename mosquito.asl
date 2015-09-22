// Agent mosquito in project testJason.mas2j

/* Initial beliefs and rules */

/* Initial goals */
!start.

/* Plans */
+!start <-
	.print("hello world. I am mosquito.");
.
/* Lay Eggs */
+pos(X,Y) : gender(female) & pregnant & cell(X,Y,waterspot) <-
    lay_eggs;
    .print("Lay eggs.");
	-pregnant
.
/* Male Mosquito */
+pos(X,Y) : gender(male) & cell(NX,NY,female) <-
	mate
.
/* Female Mosquito */
+pos(X,Y) : gender(female) & cell(NX,NY,male) <-
	mate;
	.print("Mate.");
	+pregnant
.
/* Sting */
+pos(X,Y) : gender(female) & cell(NX,NY,person) <-
	sting(NX,NY)
.
/* Keep moving */
+pos(X,Y) <-
	move(random)
.