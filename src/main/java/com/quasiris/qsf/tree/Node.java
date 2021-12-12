package com.quasiris.qsf.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Node<T> {


    private T data = null;

    private List<Node<T>> children = new ArrayList<>();


    private Set<Node> parents =  new HashSet<>();

    public Node() {
    }

    public Node(T data) {
        this.data = data;
    }


    public void addChild(Node child) {
        child.addParent(this);
        this.children.add(child);
    }

    public void addChild(T data) {
        Node<T> newChild = new Node<>(data);
        this.addChild(newChild);
    }

    public Node<T> addChildIfNotExists(T data) {
        Node<T> newChild = new Node<>(data);

        for(Node<T> child : children) {
            if(child.equals(newChild)) {
                return child;
            }
        }
        this.addChild(newChild);
        return newChild;
    }

    public void addChildren(List<Node<T>> children) {
        for(Node t : children) {
            t.addParent(this);
        }
        this.children.addAll(children);
    }

    public void addParent(Node parent) {
        this.parents.add(parent);
    }

    public List<Node<T>> getChildren() {
        return children;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


    public void traverse(Node<T> node) {
        if (node != null) {
            System.out.println(node.getData());
            for(Node<T> child: node.getChildren()) {
                child.traverse(child);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node<?> node = (Node<?>) o;
        return data.equals(node.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return "Node{" +
                "data=" + data +
                ", children=" + children +
                '}';
    }
}
