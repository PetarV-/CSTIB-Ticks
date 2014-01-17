% Petar Velickovic (pv273)
% Trinity College
% Prolog Tick

% Exercise 2.1 - Piece generation
% To check whether or not a piece is valid,
% we should verify that the length of the string
% encoding the labels is 2, and that the list consists
% of four lists of six binary digits.

% First define a predicate is_binary_list(+L) that checks
% whether or not a list only has binary digits as its elements.

is_binary_list([]).
is_binary_list([1|T]) :- !, is_binary_list(T).
is_binary_list([0|T]) :- is_binary_list(T).

% Now, define a separate predicate, is_sides_list(+L) that checks
% whether or not a list contains valid side representations.

is_sides_list([]).
is_sides_list([H|T]) :- length(H, 6), is_binary_list(H), is_sides_list(T).

% Finally, define piece(?P) as described.

piece([S, L]) :- string_length(S, 2), length(L, 4), is_sides_list(L).

% Exercise 2.2 - Rotating lists
% In order to check whether list B is the list A rotated left by N elements,
% we should divide the list A into two parts, i.e. A = (L :: R),
% such that X contains N elements, and then check whether B = (R :: L).

% Start with a predicate take_first(+A, +N, ?L, ?R),
% that will split a list into the parts as required.

take_first(A, 0, [], A) :- !.
take_first([H|T], N, [H|R], L) :- N1 is N-1, take_first(T, N1, R, L).

% Next, define a predicate concat(A, B, C),
% that concatenates lists A and B to create list C.

concat([], R, R).
concat([H|T], R, [H|B]) :- concat(T, R, B).

% Finally, define rotate(+A, +N, ?B) as described.

rotate(A, N, B) :- take_first(A, N, L, R), concat(R, L, B).

% Exercise 2.3 - Reversing lists
% To check whether list B is a reverse of list A, we can use an
% accumulating argument to reverse list A, then compare that to B.

reverse([], B, B) :- !.
reverse([H|T], B, Acc) :- reverse(T, B, [H|Acc]).

% Use this to make a version without an accumulator, reverse(?A, ?B):

reverse(A, B) :- reverse(A, B, []).

% Exercise 2.4 - Exclusive-OR
% Straightforward direct implementation of the xor(?A, ?B) predicate.

xor(0, 1).
xor(1, 0).

% Exercise 2.5 - Exclusive-OR List
% Extending the previous predicate to handle lists.

xorlist([], []).
xorlist([X|T1], [Y|T2]) :- xor(X, Y), xorlist(T1, T2).

% Exercise 2.6 - Number ranges

range(Min, Max, Min) :- Min is Max-1, !.
range(Min, _, Min).
range(Min, Max, Val) :- Min < Max, Min1 is Min+1, range(Min1, Max, Val).

% Exercise 3 - Piece orientation
% In order to implement the flipped(+P, ?FP) predicate, 
% we need to check whether the labels are the same, 
% and also whether the appropriate list pairs are reversed. 

flipped([S, [A, B, C, D]], [S, [A1, B1, C1, D1]]) :- reverse(A, A1), reverse(B, D1), reverse(C, C1), reverse(D, B1).

% To implement the orientation(+P, ?O, -OP) predicate,
% we first check whether Or is in the range [0, 3] (anticlockwise rotation) 
% or [-1, -4] (flip + anticlockwise rotation). Afterwards,
% we check if the labels match and whether the sides are properly rotated.
% Some redundant checks have been added to aid clarity.

orientation([S, L], Or, [S, L1]) :- range(0, 4, Or), rotate(L, Or, L1).
orientation([S, L], Or, [S, L1]) :- range(1, 5, Or1), Or is -Or1, flipped([S, L], [S, Fl]), rotate(Fl, Or1, L1).

% Exercise 4 - Piece compatibility

% First implement a predicate test_edge(+L1, +L2),
% that checks whether two edges fit together.
% This reduces to a simple call to xorlist.

test_edge([_, L1, L2, L3, L4, _], [_, R1, R2, R3, R4, _]) :- xorlist([L1, L2, L3, L4], [R1, R2, R3, R4]).

% The compatible(+P1, +Side1, +P2, +Side2) predicate consists of
% simply getting to the correct side lists for each piece
% and calling test_edge on them.

compatible([_, [L1|_]], 0, [_, [L2|_]], 0) :- !, reverse(L2, R2), test_edge(L1, R2).
compatible(P1, 0, [_, [_|T2]], Side2) :- !, Side2a is Side2-1, compatible(P1, 0, [_, T2], Side2a).
compatible([_, [_|T1]], Side1, P2, Side2) :- Side1a is Side1-1, compatible([_, T1], Side1a, P2, Side2).

% compatible_corner(+P1, +Side1, +P2, +Side2, +P3, +Side3) is implemented by
% getting to the correct side lists, and then checking the first elements
% as required.

compatible_corner([_, [[A|_]|_]], 0, [_, [[B|_]|_]], 0, [_, [[C|_]|_]], 0) :- A = 1, B = 0, C = 0, !.
compatible_corner([_, [[A|_]|_]], 0, [_, [[B|_]|_]], 0, [_, [[C|_]|_]], 0) :- A = 0, B = 1, C = 0, !.
compatible_corner([_, [[A|_]|_]], 0, [_, [[B|_]|_]], 0, [_, [[C|_]|_]], 0) :- !, A = 0, B = 0, C = 1.
compatible_corner(P1, 0, P2, 0, [_, [_|T3]], Side3) :- !, Side3a is Side3-1, compatible_corner(P1, 0, P2, 0, [_, T3], Side3a).
compatible_corner(P1, 0, [_, [_|T2]], Side2, P3, Side3) :- !, Side2a is Side2-1, compatible_corner(P1, 0, [_, T2], Side2a, P3, Side3).
compatible_corner([_, [_|T1]], Side1, P2, Side2, P3, Side3) :- Side1a is Side1-1, compatible_corner([_, T1], Side1a, P2, Side2, P3, Side3).

% Exercise 5 - Putting it all together
% First define a predicate permute_with_orientations(+Ps, ?Perm)
% to generate all possible permutations of pieces and orientations.

permute_with_orientations([], []).
permute_with_orientations(L, [[H, Or]|T]) :- concat(V, [H|U], L), concat(V, U, W), range(-4, 4, Or), permute_with_orientations(W, T).

% Finally, to define the puzzle(+Ps, ?S) predicate, we need to first
% constrain the first piece to be in the first slot and orientation zero
% if we are generating a solution, and permute all the other pieces.
% Afterwards we check all the necessary conditions and print the solution
% if it is satisfactory.

check([[P0, O0], [P1, O1], [P2, O2], [P3, O3], [P4, O4], [P5, O5]]) :-
    orientation(P0, O0, OP0),
    orientation(P1, O1, OP1),
    orientation(P2, O2, OP2),
    orientation(P3, O3, OP3),
    orientation(P4, O4, OP4),
    orientation(P5, O5, OP5),

    compatible(OP0, 2, OP1, 0),
    compatible(OP0, 3, OP2, 0),
    compatible(OP1, 3, OP2, 1),
    compatible(OP0, 1, OP3, 0),
    compatible(OP1, 1, OP3, 3),
    compatible(OP1, 2, OP4, 0),
    compatible(OP2, 2, OP4, 3),
    compatible(OP3, 2, OP4, 1),
    compatible(OP4, 2, OP5, 0),
    compatible(OP2, 3, OP5, 3),
    compatible(OP0, 0, OP5, 2),
    compatible(OP3, 1, OP5, 1),

    compatible_corner(OP0, 3, OP1, 0, OP2, 1),
    compatible_corner(OP0, 2, OP1, 1, OP3, 0),
    compatible_corner(OP2, 2, OP1, 3, OP4, 0),
    compatible_corner(OP3, 3, OP1, 2, OP4, 1),
    compatible_corner(OP5, 0, OP4, 3, OP2, 3),
    compatible_corner(OP5, 1, OP4, 2, OP3, 2),
    compatible_corner(OP5, 2, OP0, 1, OP3, 1),
    compatible_corner(OP5, 3, OP0, 0, OP2, 0),

    format('~w at ~w~n', [P0, O0]),
    format('~w at ~w~n', [P1, O1]),
    format('~w at ~w~n', [P2, O2]),
    format('~w at ~w~n', [P3, O3]),
    format('~w at ~w~n', [P4, O4]),
    format('~w at ~w~n', [P5, O5]).

puzzle([P0|Tp], [[P0, 0]|Ts]) :- !, permute_with_orientations(Tp, Ts), check([[P0, 0]|Ts]).
puzzle(Ps, S) :- permute_with_orientations(Ps, S), check(S).
