# Jadis

A reimplementation of the Redis in memory database using java.
This was created purely for fun as a good way to get a better understanding of this brilliant database.

## About the implementation

Unlike many Redis reimplementations Jadis does not use threads to handle concurrent requests. Just like Redis, Jadis is
single threaded.
It takes advantage of an event loop (implemented through the java NIO package) to handle concurrent requests without the
possibility of race conditions.
One of the most interesting parts of this project is the implementation of the Redis Serialization protocol which can be
found in the Protocol file.

This was also one of the first piece of Java code I had ever written, therefore I apologize if it's not very idiomatic.

## How to use

Step 1: Clone this repo

```bash
git clone
```

Step 2: Compile and run the code
Step 3: Your Jadis instance should be running on the standard Redis port (6379)
Step 4: To test Jadis you can use the redis-cli. Jadis is mostly compatible with it (see What is supported)

## What is supported

### Commands

- ping
- echo
- set
- get

### Data types

- Arrays
- Simple strings
- Simple errors
- Bulk strings
- Integers

