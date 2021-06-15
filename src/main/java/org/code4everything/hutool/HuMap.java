package org.code4everything.hutool;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author pantao
 * @since 2021/6/11
 */
public class HuMap<K, V> implements Map<K, V> {

    private final Map<K, V> map;

    public HuMap() {
        this(new HashMap<>(16));
    }

    public HuMap(Map<K, V> map) {
        this.map = map;
    }

    public static <K, V> HuMap<K, V> of() {
        return new HuMap<>();
    }

    public static <K, V> HuMap<K, V> of(K key, V value) {
        return new HuMap<K, V>().push(key, value);
    }

    public static <K, V> HuMap<K, V> of(Map<K, V> map) {
        return new HuMap<>(map);
    }

    public static HuMap<String, Object> ofStrObj(String key, Object value) {
        return new HuMap<String, Object>().push(key, value);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        return map.put(key, value);
    }

    public HuMap<K, V> push(K key, V value) {
        put(key, value);
        return this;
    }

    @Override
    public V remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }
}
