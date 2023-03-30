package ru.promoz;

public interface AuthorHolder {

    default String getAuthor() {
        return "The author of this collection: Nail Fakhrtdinov";
    }
}
