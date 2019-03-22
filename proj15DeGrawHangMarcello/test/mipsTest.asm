# file:  MIPS-0.asm
# author: Dale Skrien
# date: Feb. 1, 2019
# This program reads in integers until a negative integer is entered.
# It then outputs the sum of the positive integers.

    .text       # the code segment of the program
    .globl main # the starting point of the program

main:
    li $t0, 0   # initialize the sum to 0
loop:
    li $v0 10
    syscall     # read next int into $v0
    bltz $v0 endloop  # exit the loop if the int entered (in $v0) is negative
    add $t0, $t0, $v0 # add the next int to the sum
    b loop

endloop:
    move $a0, $t0
    li $v0, 1
    syscall     # output the value in $a0
    li $v0, 10
    syscall     # exit