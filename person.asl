// Agent person in project testJason.mas2j

/* Initial beliefs and rules */
/* Initial goals */
!start.
/* Plans */
+!start <- .print("hello world. I am a person.").

+sting <-
    .print("I hate mosquitoes!");
	+sick
.
/* Send awareness message */
+pos(X,Y) : aware & cell(NX,NY,person) <- awareness(NX,NY).

/* Waterspot to clean */
+pos(X,Y) : aware & cell(X,Y,waterspot) <-
    .print("Clear waterspot");
	clear_waterspot
.
/* Waterspot nearby */
+pos(X,Y) : aware & cell(X+1,Y,waterspot) <- move(right).
+pos(X,Y) : aware & cell(X-1,Y,waterspot) <- move(left).
+pos(X,Y) : aware & cell(X,Y-1,waterspot) <- move(up).
+pos(X,Y) : aware & cell(X,Y+1,waterspot) <- move(down).

/* Keep moving */
+pos(X,Y) <-
	move(random)
.
