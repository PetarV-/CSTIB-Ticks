# ECAD & Architecture Practical Class - Lab 2 - Simulation and Architecture
#
# Petar Velickovic
# Trinity College
# pv273
# 25 October 2013

# mandelbrot_point - Fetches three numbers (X, Y and maxIter) from the input stream for the TTC
#		   - Returns the amount of iterations needed for point (X, Y) to be excluded from the Mandelbrot set (up to maxIter iterations are made)
#     		   - The result is put on the output stream

# variable and label assignments to registers:
# r0 - Stores the PC when jumping.
# r1 - the routine that loads X.
# r2 - the routine that loads Y.
# r3 - the routine that loads maxIter.
# r4 - the routine that represents while(true).
# r5 - the routine that outputs the result.
# r6 - stores X.
# r7 - stores Y.
# r8 - stores maxIter.
# r9 - stores the helper variable x.
# r10 - stores the helper variable y.
# r11 - stores the helper variable iter.
# r12 - stores the helper variable x_sq.
# r13 - stores the helper variable y_sq.
# r14 - stores the helper variable xy.
# r15 - stores the helper variable two_xy.
# r16 - stores the helper variable mag_sq.
# r17 - stores the constant value 1. (1/2^28 represented as 4.28 fixed point)
# r18 - stores the constant value (4 << 28). 
# r19 - stores the result of max_iter - iter.
# r20 - stores the first break point within the loop.
# r21 - stores the second break point within the loop.

lc r17 1			# Assign constants
lc r18 1
and r18 r18 r18 rot1
and r18 r18 r18 rot1

lc r1 loadX			# Assign labels
lc r2 loadY
lc r3 loadMaxIter
lc r4 whileTrue
lc r5 output
lc r20 breakPoint1
lc r21 breakPoint2

loadX:
    ldin r6 sin     		# Get input parameters (repeat until input stream is ready)
    jmp r0 r1
loadY:
    ldin r7 sin
    jmp r0 r2
loadMaxIter:
    ldin r8 sin
    jmp r0 r3  

lc r9 0		    		# Assign helper variables. 
lc r10 0
lc r11 0

whileTrue:
    fpmul r12 r9 r9		# x_sq = (x*x).
    fpmul r13 r10 r10		# y_sq = (y*y).
    fpmul r14 r9 r10		# xy = (x*y).

    add r15 r14 r14		# two_xy = xy + xy.
    add r16 r12 r13		# mag_sq = x_sq + y_sq.

    sub r16 r18 r16 sltz     	# mag_sq = (4 << 28) - mag_sq. If mag_sq < 0, then jump to output. otherwise continue loop by jumping to breakPoint1.
    jmp r0 r20
    jmp r0 r5

breakPoint1:
    sub r19 r8 r11 sez       	# r19 = max_iter - iter. If r19 = 0, then jump to output. otherwise continue loop by jumping to breakPoint2.
    jmp r0 r21
    jmp r0 r5

breakPoint2:
    sub r9 r12 r13		# x = x_sq - y_sq + X.
    add r9 r9 r6		
    add r10 r15 r7		# y = two_xy + Y.

    add r11 r11 r17		# iter++.

    jmp r0 r4 			# back to whileTrue.

output:    
    stout r11        		# Output result
    
jmp r0 r1           		# Restart program
