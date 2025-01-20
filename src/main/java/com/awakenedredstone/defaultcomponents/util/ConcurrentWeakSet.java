package com.awakenedredstone.defaultcomponents.util;

import java.lang.ref.WeakReference;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentWeakSet<E> extends AbstractSet<E> implements Set<E> {
    private final ConcurrentHashMap<WeakReference<E>, Boolean> map;
    
    public ConcurrentWeakSet() {
        map = new ConcurrentHashMap<>();
    }

    public ConcurrentWeakSet(int capacity) {
        map = new ConcurrentHashMap<>(capacity);
    }
    
    public ConcurrentWeakSet(Collection<? extends E> c) {
        this();
        addAll(c);
    }
    
    @Override
    public boolean add(E element) {
        if (element == null) {
            throw new NullPointerException("Null elements are not allowed");
        }
        
        // Clean up any garbage collected references first
        removeStaleReferences();
        
        // Create a new WeakReference for the element
        WeakReference<E> weakRef = new WeakReference<>(element);
        return map.put(weakRef, Boolean.TRUE) == null;
    }
    
    @Override
    public boolean remove(Object object) {
        if (object == null) {
            return false;
        }
        
        // Clean up any garbage collected references
        removeStaleReferences();
        
        // Find and remove the matching reference
        for (WeakReference<E> ref : map.keySet()) {
            E element = ref.get();
            if (element != null && object.equals(element)) {
                return map.remove(ref) != null;
            }
        }
        return false;
    }
    
    @Override
    public boolean contains(Object object) {
        if (object == null) {
            return false;
        }
        
        // Clean up any garbage collected references
        removeStaleReferences();
        
        // Check if the object exists in any of the weak references
        for (WeakReference<E> ref : map.keySet()) {
            E element = ref.get();
            if (element != null && object.equals(element)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Iterator<E> iterator() {
        removeStaleReferences();
        
        return new Iterator<E>() {
            private final Iterator<WeakReference<E>> iterator = map.keySet().iterator();
            private E nextElement = null;
            private boolean hasNextCalled = false;
            
            @Override
            public boolean hasNext() {
                if (hasNextCalled) {
                    return nextElement != null;
                }
                
                // Find the next non-null reference
                while (iterator.hasNext()) {
                    WeakReference<E> ref = iterator.next();
                    nextElement = ref.get();
                    if (nextElement != null) {
                        hasNextCalled = true;
                        return true;
                    } else {
                        // Remove stale reference
                        iterator.remove();
                    }
                }
                hasNextCalled = true;
                return false;
            }
            
            @Override
            public E next() {
                if (!hasNextCalled) {
                    hasNext();
                }
                hasNextCalled = false;
                return nextElement;
            }
            
            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }
    
    @Override
    public int size() {
        removeStaleReferences();
        return map.size();
    }
    
    private void removeStaleReferences() {
        map.keySet().removeIf(ref -> ref.get() == null);
    }
}