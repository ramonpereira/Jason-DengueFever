// Agent inspector in project testJason.mas2j

/* Initial beliefs and rules */

/* Initial goals */
!start.
/* Plans */
+!start <- .print("hello world. I am a inspector.").

/* Waterspot to clean */
+pos(X,Y) : cell(X,Y,waterspot) <-
    .print("Clear waterspot");
	clear_waterspot
.
/* Waterspot is nearby */
+pos(X,Y) : cell(X+1,Y,waterspot) <- move(right).
+pos(X,Y) : cell(X+2,Y,waterspot) <- move(right).
+pos(X,Y) : cell(X-1,Y,waterspot) <- move(left).
+pos(X,Y) : cell(X-2,Y,waterspot) <- move(left).
+pos(X,Y) : cell(X,Y-1,waterspot) <- move(up).
+pos(X,Y) : cell(X,Y-2,waterspot) <- move(up).
+pos(X,Y) : cell(X,Y+1,waterspot) <- move(down).
+pos(X,Y) : cell(X,Y+2,waterspot) <- move(down).

/* Send awareness message */
+pos(X,Y) : cell(X-1,Y-1,person) <- awareness(X-1,Y-1).
+pos(X,Y) : cell(X,Y-1,person) <- awareness(X,Y-1).
+pos(X,Y) : cell(X+1,Y-1,person) <- awareness(X+1,Y-1).
+pos(X,Y) : cell(X-1,Y,person) <- awareness(X-1,Y).
+pos(X,Y) : cell(X+1,Y,person) <- awareness(X+1,Y).
+pos(X,Y) : cell(X-1,Y+1,person) <- awareness(X-1,Y+1).
+pos(X,Y) : cell(X,Y+1,person) <- awareness(X,Y+1).
+pos(X,Y) : cell(X+1,Y+1,person) <- awareness(X+1,Y+1).

/* Keep moving */
+pos(X,Y) <-
	move(random)
.
