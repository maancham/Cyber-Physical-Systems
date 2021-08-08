package com.example.airhockey.models;

import java.io.Serializable;

public class Pair<T,V> implements Serializable {

    public T first;
    public V second;

    public Pair(T first, V second){
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "Pair{first=" + first +
                ", second=" + second + '}';
    }
}
