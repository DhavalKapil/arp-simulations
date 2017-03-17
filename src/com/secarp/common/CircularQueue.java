package com.secarp.common;

/**
 * This implements a circular queue using a fixed size array
 */
public class CircularQueue<T> {
    // The internal array
    private T[] queue;

    // The capacity of the queue
    private int capacity;

    // The head and tail pointers
    private int head, tail;

    /**
     * Constructor function
     *
     * @param capacity The maximum capacity of the queue
     */
    public CircularQueue(int capacity) {
        this.queue = (T[])new Object[capacity];
        this.capacity = capacity;
        this.head = -1;
        this.tail = -1;
    }

    /**
     * Returns the length of the queue
     *
     * @return The number of elements in the queue
     */
    public int length() {
        if (this.head == -1) {
            return 0;
        }
        if (this.head <= this.tail) {
            return this.tail - this.head + 1;
        }
        return this.capacity - (this.head - this.tail - 1);
    }

    /**
     * Add element to the queue
     *
     * @param el The item to be added
     */
    public void enqueue(T el) {
        if (this.head == -1) {
            this.head = this.tail = 0;
        } else {
            this.tail = (this.tail + 1) % this.capacity;
        }
        this.queue[this.tail] = el;
    }

    /**
     * Removes the first element from the queue and returns it
     * If the queue was empty, does nothing and returns NULL
     *
     * @return T The first item
     */
    public T dequeue() {
        if (this.head == -1) {
            return null;
        }
        T first = this.queue[this.head];
        this.head = (this.head + 1) % this.capacity;
        return first;
    }

    /**
     * Returns the first element of the queue without deleting it
     *
     * @return T The first item
     */
    public T top() {
        if (this.head == -1) {
            return null;
        }
        return this.queue[this.head];
    }
}
