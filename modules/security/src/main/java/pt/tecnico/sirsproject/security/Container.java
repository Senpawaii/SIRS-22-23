package pt.tecnico.sirsproject.security;

public class Container<T> {
    public T item;

    public Container() {}

    public Container(T item) {
        this.item = item;
    }
}
