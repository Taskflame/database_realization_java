package org.example;

import java.io.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DbManager {
    private DatabaseFile db; // тек. бд в памяти
    private File currentFile; // фаил, откуда она загружена и куда сохарняется. Условно, путь к файлу на диске.

    public DbManager() {
        this.db = new DatabaseFile(); // конструктор DbManager(). создаётся пустая бд в памяти.
    }

    public DatabaseFile getDb() {
        return db;
    }

    // создание новой бд
    public void createNew() {
        db = new DatabaseFile();
        currentFile = null;
    }

    // открыть базу данных из файла
    public boolean open(File file) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) { // new FileInputStream(file) - открываем байтовый поток для чтения из файла. new ObjectInputStream(...) — оборачиваем его в объектный поток, который умеет читать объекты, записанные через writeObject.
            db = (DatabaseFile) ois.readObject(); // ois.readObject() читает один объект из файла.
            currentFile = file;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // save this bd
    public boolean save() {
        if (currentFile == null) return false;
        return saveAs(currentFile);
    }

    // сохранить как
    public boolean saveAs(File file) {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(db);
            currentFile = file;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // работа с файлами импорта из excel
    public boolean loadFromExcel(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) { // создаём объект excel книги из потока

            Sheet sheet = workbook.getSheetAt(0); // читаем первую вкладку Excel
            DatabaseFile newDb = new DatabaseFile(); // создаём временную новую БД, чтобы не ломать текущую

            // цикл по строкам
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue; // пустые строки пропускаем

                // читаем ячейки
                int id = (int) row.getCell(0).getNumericCellValue();
                String name = row.getCell(1).getStringCellValue();
                int age = (int) row.getCell(2).getNumericCellValue();
                double height = row.getCell(3).getNumericCellValue();

                // создаём запись и добавляем в новую БД
                newDb.addRecord(new Record(id, name, age, height));
            }

            // теперь заменяем старую базу новой
            this.db = newDb;

            // так как Excel — НЕ сериализованная БД, файл сбрасываем
            this.currentFile = null;

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // отчистка базы
    public void clear() {
        db.clear();
    }

    // геттер для GUI
    public File getCurrentFile(){
        return currentFile;
    }

}
