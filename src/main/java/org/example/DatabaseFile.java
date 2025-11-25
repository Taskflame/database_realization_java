package org.example;

import java.io.Serializable;
import java.sql.Array;
import java.util.*;

public class DatabaseFile implements Serializable {
    private static final long serialVersionUID = 1L;
    // основная таблица - ключ/запись
    private TreeMap<Integer, Record> table = new TreeMap<>();

    private Map<String, List<Integer>> indexByName = new HashMap<>();
    private Map<Integer, List<Integer>> indexByAge = new HashMap<>();

    private void updateIndexForRecord(Record record) {
        String name = record.getName();
        int age = record.getAge();
        int id = record.getId();
        // обновление индкса по имени
        indexByName.computeIfAbsent(name, k -> new ArrayList<>()).add(id);
        // обновление индекса по возрасту
        indexByAge.computeIfAbsent(age, k -> new ArrayList<>()).add(id);
    }

    // удаляем id
    private <K> void removeFromIndex(Map<K, List<Integer>> index, K key, int id) {
        List<Integer> ids = index.get(key);
        if (ids != null) {
            ids.remove((Integer) id);
        }
        if (ids.isEmpty()) { index.remove(key); }
    }

    // добавление записи
    public boolean addRecord(Record record) {
        int id = record.getId();
        if (table.containsKey(id)) {
            return false; // запись с таким id уже существует
        }
        table.put(id, record); // O(log n) — кладём запись в TreeMap
        updateIndexForRecord(record);
        return true;
    }

    // удаление записи по ключу
    public Record removeRecordById(int id) {
        Record removed = table.remove(id);

        if (removed != null) {
            removeFromIndex(indexByName, removed.getName(), id);
            removeFromIndex(indexByAge, removed.getAge(), id);
        }

        return removed;
    }

    // удаление записи не по ключевым полям
    public List<Record> removeRecordsByName(String name) {
        List<Integer> ids = indexByName.getOrDefault(name, Collections.emptyList());
        List<Record> removedRecords = new ArrayList<>(); // создаём пустой список, чтобы запомнить, какие записи были удалены
        for (int id : ids) { // проходим по всем id, у которых имя совпадает с заданным.
            Record removed = table.remove(id); // Удаляем запись из основной таблицы (TreeMap<Integer, Record> table) по id
            if (removed != null) { //  Если запись удалилась -> добавляем её в список удалённых.
                removedRecords.add(removed);
                removeFromIndex(indexByName, name, id);
                removeFromIndex(indexByAge, removed.getId(), id);
            }
        }
        return removedRecords;
    }

    public List<Record> removeRecordsByAge(int age) {
        List<Integer> ids = indexByAge.getOrDefault(age, Collections.emptyList());
        List<Record> removedRecords = new ArrayList<>();
        for (int id : ids) {
            Record removed = table.remove(id);
            if (removed != null) {
                removedRecords.add(removed);
                removeFromIndex(indexByName, removed.getName(), id);
                removeFromIndex(indexByAge, age, id);
            }
        }
        return removedRecords;
    }

    // редактирование записи
    public boolean updateRecord(Record updatedRecord) {
        int id = updatedRecord.getId(); // получаем id из переданной новой записи
        Record oldRecord = table.get(id); // обращаемся к основной структуре данных (TreeMap<Integer, Record> table) и пытаемся найти запись с таким ID.
        if (oldRecord == null) {
            return false; // Запись не найдена
        }
        // удаление страных индексов из соответсвующих хеш-таблиц
        removeFromIndex(indexByName, oldRecord.getName(), id);
        removeFromIndex(indexByAge, oldRecord.getAge(), id);
        // обнова записи
        table.put(id, updatedRecord);
        // добавление новых индексов
        updateIndexForRecord(updatedRecord);
        return true;
    }

    // получение записи по ключу
    public Record getRecordById(int id) {
        return table.get(id);
    }

    // поиск всех записей по имени
    public List<Record> findByName(String name) {
        List<Integer> ids = indexByName.getOrDefault(name, Collections.emptyList()); // поиск ключа в карте
        List<Record> result = new ArrayList<>(); // список для складывания найденных данных

        for (int id : ids) {
            Record r = table.get(id);
            if (r != null) result.add(r);
        }
        return result;
    }

    // поиск всех записей по возрасту
    public List<Record> findByAge(int age) {
        List<Integer> ids = indexByAge.getOrDefault(age, Collections.emptyList());
        List<Record> result = new ArrayList<>();

        for(int id : ids) {
            Record r = table.get(id);
            if ( r!= null) result.add(r);
        }
        return result;
    }

    // отчитска всей таблицы
    public void clear() {
        table.clear();
        indexByName.clear();
        indexByAge.clear();
    }

    // доступ к таблице для GUI
    public TreeMap<Integer, Record> getTable() {
        return table;
    }
}

// своя реализация remove()