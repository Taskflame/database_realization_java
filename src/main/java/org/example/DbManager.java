package org.example;

import java.io.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DbManager {
    private DatabaseFile db; // тек. бд в памяти
    private File currentFile; // фаил, откуда она загружена и куда сохарняется. Условно, путь к файлу на диске.
    private boolean loadedFromExcel = false;


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
            loadedFromExcel = false;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // save this bd
    public boolean save() {
        if (currentFile == null) return false;

        if (loadedFromExcel) {
            return saveToExcel(currentFile);
        } else {
            return saveAs(currentFile);
        }
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

    // сохранить в эксель
    public boolean saveToExcel(File file) {
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Database");

            // Заголовки
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Age");
            header.createCell(3).setCellValue("Height");

            int rowIndex = 1;

            for (Record r : db.getTable().values()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(r.getId());
                row.createCell(1).setCellValue(r.getName());
                row.createCell(2).setCellValue(r.getAge());
                row.createCell(3).setCellValue(r.getHeight());
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    // работа с файлами импорта из excel
    public boolean loadFromExcel(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            DatabaseFile newDb = new DatabaseFile();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                int id = (int) row.getCell(0).getNumericCellValue();
                String name = row.getCell(1).getStringCellValue();
                int age = (int) row.getCell(2).getNumericCellValue();
                double height = row.getCell(3).getNumericCellValue();

                newDb.addRecord(new Record(id, name, age, height));
            }

            this.db = newDb;
            this.currentFile = file;         // теперь можно сохранять обратно!
            this.loadedFromExcel = true;     // !!! добавили флаг

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
