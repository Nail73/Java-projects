package ru.promoz;

import java.util.*;

public class MyList<T> extends MySearch implements List<T>, AuthorHolder {

    private T[] item = (T[]) new Object[1];
    private int size;

    public MyList() {
    }

    public MyList(T[] list) {
        super();
    }

    @Override
    public void sort(Comparator<? super T> c) {
        if (c == null) {
            c = (Comparator<T>) (o1, o2) -> ((Comparable) o1).compareTo(o2);
        }
        QuickSort sort = new QuickSort(c);
        sort.quicksort((T[]) item);

    }

    private class QuickSort {
        private final Comparator<? super T> c;

        QuickSort(Comparator<? super T> comparator) {
            c = comparator;
        }

        public void quicksort(T[] a) {
            quicksort((T[]) item, 0, size - 1);
        }

        private void quicksort(T[] arr, int begin, int end) {
            if (begin < end) {
                int partitionIndex = partition(arr, begin, end);
                quicksort(arr, begin, partitionIndex - 1);
                quicksort(arr, partitionIndex + 1, end);
            }
        }

        private int partition(T arr[], int begin, int end) {
            T pivot = arr[end];
            int i = (begin - 1);
            for (int j = begin; j < end; j++) {
                if (c.compare(arr[j], pivot) <= 0) {
                    i++;
                    T swapTemp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = swapTemp;
                }
            }
            T swapTemp = arr[i + 1];
            arr[i + 1] = arr[end];
            arr[end] = swapTemp;

            return i + 1;
        }
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        for (int i = 0; i < size; i++) {
            if (item[i].equals(o)) return true;
        }
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return new ElementsIterator();
    }

    @Override
    public Object[] toArray() {
        T[] newItem = (T[]) new Object[this.size()];
        System.arraycopy(item, 0, newItem, 0, this.size());
        return newItem;
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        if (a.length < size) return (T1[]) Arrays.copyOf(item, size, a.getClass());
        System.arraycopy(item, 0, a, 0, size);
        if (a.length > size) a[size] = null;
        return a;
    }

    @Override
    public boolean add(T t) {
        if (item.length == size) {
            final T[] oldItem = item;
            item = (T[]) new Object[this.size() * 2];
            System.arraycopy(oldItem, 0, item, 0, oldItem.length);
        }
        item[size++] = t;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        for (int i = 0; i < size(); i++) {
            if (item[i].equals(o)) {
                this.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (final Object item : c) {
            if (!this.contains(item)) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        for (final T item : c) {
            add(item);
        }
        return true;
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        for (Object item : c) {
            remove(item);
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        for (T item : this) {
            if (!c.contains(item)) this.remove(item);
        }
        return true;
    }

    @Override
    public void clear() {
        item = (T[]) new Object[1];
        size = 0;
    }

    @Override
    public T remove(int index) {
        T element = item[index];
        if (index != this.size() - 1) System.arraycopy(item, index + 1, item, index, this.size() - index - 1);
        size--;
        return element;
    }

    @Override
    public List<T> subList(int start, int end) {
        if (start < 0 || end > size || start > end) throw new IndexOutOfBoundsException();
        List<T> list = new MyList<>();
        list.addAll(Arrays.asList(item).subList(start, end));
        return list;
    }

    @Override
    public ListIterator listIterator() {
        return new ElementsIterator(0);
    }

    @Override
    public ListIterator listIterator(int index) {
        return new ElementsIterator(index);
    }

    @Override
    public int lastIndexOf(Object target) {
        for (int i = size() - 1; i >= 0; i--) {
            if (item[i].equals(target)) return i;
        }
        return -1;
    }

    @Override
    public int indexOf(Object target) {
        for (int i = 0; i < size(); i++) {
            if (item[i].equals(target)) return i;
        }
        return -1;
    }

    @Override
    public void add(int index, T element) {
        if (index < 0 || index > size()) throw new IndexOutOfBoundsException();
        if (item.length == size) {
            final T[] oldM = item;
            item = (T[]) new Object[this.size() * 2];
            System.arraycopy(oldM, 0, item, 0, oldM.length);
        }
        if (index == size()) {
            item[size++] = element;
        } else {
            System.arraycopy(item, index, item, index + 1, size() - index);
            item[index] = element;
            size++;
        }
    }

    @Override
    public boolean addAll(int index, Collection elements) {
        int prevSize = size();
        if (index < 0 || index > size()) throw new IndexOutOfBoundsException();
        Iterator elementsIterator = elements.iterator();
        for (int i = index; i < index + elements.size(); i++) {
            add(i, (T) elementsIterator.next());
        }
        return size() > prevSize;
    }

    @Override
    public T set(int index, T element) {
        item[index] = element;
        return element;
    }

    @Override
    public T get(int index) {
        return item[index];
    }

    @Override
    public String toString() {
        return Arrays.toString(item);
    }


    private class ElementsIterator implements ListIterator<T> {
        private int index;
        private int last = -1;

        public ElementsIterator() {
            this(0);
        }

        public ElementsIterator(final int index) {
            this.index = index;
        }

        @Override
        public boolean hasNext() {
            return index < MyList.this.size();
        }

        @Override
        public T next() {
            if (!hasNext()) throw new NoSuchElementException();
            last = index;
            return MyList.this.item[index++];
        }

        @Override
        public void add(T element) {
            if (last == -1) throw new IllegalStateException();
            MyList.this.add(index, element);
            last = -1;
        }

        @Override
        public void set(T element) {
            if (last == -1) throw new IllegalStateException();
            MyList.this.set(last, element);
        }

        @Override
        public int previousIndex() {
            if (index == 0) return -1;
            return index - 1;
        }

        @Override
        public int nextIndex() {
            return index;
        }

        @Override
        public boolean hasPrevious() {
            return index > 0;
        }

        @Override
        public T previous() {
            if (!hasPrevious()) throw new NoSuchElementException();
            int prevIndex = previousIndex();
            last = prevIndex;
            index--;
            return MyList.this.item[prevIndex];
        }

        @Override
        public void remove() {
            if (last == -1) throw new IllegalStateException();
            MyList.this.remove(last);
            index--;
            last = -1;
        }
    }
}


