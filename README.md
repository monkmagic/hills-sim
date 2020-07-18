# hills-sim
Simple trading simulator in Java 8
Overview
Hills-Sim is a simple program, that allows users to back-test trading strategies. 
This project was written in early 2018, using Java 8 and Postgresql.

This is done by:

Writing technical indicators
Writing strategies, via a combination of technical indicators
Finding the optimal set of parameters for a strategy, given a range of parameters
In this code, there are simple trading related codes for:

Managing market, stop, and limit orders (open/close)
Sample technical indicators (working with time-series data points)
Using concurrency for retrieving data-points and processing them

Why write the code?
-------------------
In Aug 17, this idea was like an itch in my mind, that can't be quiet down. 
So, I set forth to hit the library, and open the software engineering books that I have long left behind. 
Within 4 months of full-time work, I nailed it, I felt great.

Little did I know back then, the time spent on Hills-Sim will embarrass me, until today. 
Friends and colleagues thought I was crazy to pursue it full-time. 
Prospective employers thought I was not committed to my career as an accountant. 
I thought of myself as a fool.

Today, something changed.

I watched a Youtube show by Honeypot, on Vue JS and Evan You's story. 
That was really inspiring! So much so, that I have risen to my attic, and brought down the crusty code. 
This code may have bad smell. 
It may have poor design choices, and doubtful implementations. 

But, it is my first project :)
