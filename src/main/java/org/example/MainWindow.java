package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class MainWindow extends JFrame { // класс становиться окном
    private final DbManager dbManager; // объект, через который GUI обращается к базе (создать/открыть/сохранить/очистить).
    private final JTable table; // модель данных для JTable, здесь хранятся строки.
    private final DefaultTableModel tableModel; // компонент Swing, который отображает модель в виде таблицы.

    // конструктор окна
    public MainWindow() {
        super("Файловая БД");

        dbManager = new DbManager();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout()); // разбиваем окно на зоны

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Age", "Height"}, 0
        );
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 4, 5, 5)); // FlowLayout() - все элементы слева напрво

        JButton createBtn = new JButton("Создать");
        JButton openBtn = new JButton("Открыть");
        JButton saveBtn = new JButton("Сохранить");
        JButton saveAsBtn = new JButton("Сохранить как");
        JButton addBtn = new JButton("Добавить запись");
        JButton deleteBtn = new JButton("Удалить запись");
        JButton findByNameBtn = new JButton("Найти по имени");
        JButton findByAgeBtn = new JButton("Найти по возрасту");
        JButton deleteByNameBtn = new JButton("Удалить по имени");
        JButton deleteByAgeBtn = new JButton("Удалить по возрасту");
        JButton editBtn = new JButton("Редактировать запись");
        JButton clearBtn = new JButton("Очистить БД");
        JButton excelBtn = new JButton("Импорт Excel");

        buttonPanel.add(createBtn);
        buttonPanel.add(openBtn);
        buttonPanel.add(saveBtn);
        buttonPanel.add(saveAsBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(findByNameBtn);
        buttonPanel.add(findByAgeBtn);
        buttonPanel.add(deleteByNameBtn);
        buttonPanel.add(deleteByAgeBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(excelBtn);


        add(buttonPanel, BorderLayout.NORTH); // кладём панель кнопок в верхнюю часть окна

        // обработчики кнопок //

        // Создать новую БД
        createBtn.addActionListener(e -> {
            dbManager.createNew();
            updateTable();
            showInfo("Создана новая пустая база данных.");
        });

        // Открыть БД
        openBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Открыть базу данных");

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (dbManager.open(file)) {
                    updateTable();
                    showInfo("База данных успешно открыта.");
                } else {
                    showError("Ошибка при открытии файла.");
                }
            }
        });

        //Сохранить БД
        saveBtn.addActionListener(e -> {
            if (dbManager.save()) {
                showInfo("База данных сохранена.");
            } else {
                showError("Нечего сохранять или файл не выбран.");
            }
        });

        //  Сохранить как
        saveAsBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Сохранить базу как…");

            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (dbManager.saveAs(file)) {
                    showInfo("База данных успешно сохранена.");
                } else {
                    showError("Ошибка сохранения.");
                }
            }
        });

        // Добавить запись
        addBtn.addActionListener(e -> {
            JTextField idField = new JTextField();
            JTextField nameField = new JTextField();
            JTextField ageField = new JTextField();
            JTextField heightField = new JTextField();

            Object[] fields = {
                    "ID:", idField,
                    "Имя:", nameField,
                    "Возраст:", ageField,
                    "Рост:", heightField
            };

            int option = JOptionPane.showConfirmDialog(
                    this, fields, "Добавление записи", JOptionPane.OK_CANCEL_OPTION
            );

            if (option == JOptionPane.OK_OPTION) {
                try {
                    int id = Integer.parseInt(idField.getText());
                    String name = nameField.getText();
                    int age = Integer.parseInt(ageField.getText());
                    double height = Double.parseDouble(heightField.getText());

                    Record r = new Record(id, name, age, height);

                    boolean added = dbManager.getDb().addRecord(r);
                    if (!added) {
                        showError("ID уже существует!");
                    }

                    updateTable();

                } catch (Exception ex) {
                    showError("Ошибка: проверьте правильность введённых данных.");
                }
            }
        });

        //  Удалить запись по ID
        deleteBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Введите ID для удаления:");

            if (input != null) {
                try {
                    int id = Integer.parseInt(input);

                    Record removed = dbManager.getDb().removeRecordById(id);

                    if (removed != null) {
                        updateTable();
                        showInfo("Запись удалена.");
                    } else {
                        showError("Запись с таким ID не найдена.");
                    }

                } catch (Exception ex) {
                    showError("Некорректный ID.");
                }
            }
        });

        //  Найти по имени
        findByNameBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Введите имя для поиска:"); // протокол для ввода текста
            if (name != null && !name.trim().isEmpty()) {
                List<Record> results = dbManager.getDb().findByName(name.trim()); // обращаемся к индексу
                if (results.isEmpty()) {
                    showInfo("Записи с именем \"" + name + "\" не найдены.");
                } else {
                    StringBuilder sb = new StringBuilder("Найдено " + results.size() + " записей:\n\n"); // класс для склеивания строк
                    for (Record r : results) { // для каждого объекта Record в списке results:
                        sb.append(r.toString()).append("\n"); //
                    }
                    JOptionPane.showMessageDialog(this, sb.toString(), "Результат поиска", JOptionPane.INFORMATION_MESSAGE); // sb.toString() — содержимое StringBuilder, JOptionPane.INFORMATION_MESSAGE — иконка "инфо"
                }
            }
        });

        //  Найти по возрасту
        findByAgeBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Введите возраст для поиска:");
            if (input != null) {
                try {
                    int age = Integer.parseInt(input.trim());
                    List<Record> results = dbManager.getDb().findByAge(age);
                    if (results.isEmpty()) {
                        showInfo("Записи с возрастом " + age + " не найдены.");
                    } else {
                        StringBuilder sb = new StringBuilder("Найдено " + results.size() + " записей:\n\n");
                        for (Record r : results) {
                            sb.append(r.toString()).append("\n");
                        }
                        JOptionPane.showMessageDialog(this, sb.toString(), "Результат поиска", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    showError("Пожалуйста, введите корректное число.");
                }
            }
        });

        //  Удалить по имени
        deleteByNameBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Введите имя для удаления ВСЕХ записей:");
            if (name != null && !name.trim().isEmpty()) {
                List<Record> removed = dbManager.getDb().removeRecordsByName(name.trim());
                if (removed.isEmpty()) {
                    showInfo("Записи с именем \"" + name + "\" не найдены.");
                } else {
                    updateTable(); // обновляем таблицу в GUI
                    showInfo("Удалено " + removed.size() + " записей с именем \"" + name + "\".");
                }
            }
        });

        // Удалить по возрасту
        deleteByAgeBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Введите возраст для удаления ВСЕХ записей:");
            if (input != null) {
                try {
                    int age = Integer.parseInt(input.trim());
                    List<Record> removed = dbManager.getDb().removeRecordsByAge(age);
                    if (removed.isEmpty()) {
                        showInfo("Записи с возрастом " + age + " не найдены.");
                    } else {
                        updateTable(); // обновляем таблицу в GUI
                        showInfo("Удалено " + removed.size() + " записей с возрастом " + age + ".");
                    }
                } catch (NumberFormatException ex) {
                    showError("Пожалуйста, введите корректное число.");
                }
            }
        });

        // Редактировать запись
        editBtn.addActionListener(e -> {
            String idInput = JOptionPane.showInputDialog(this, "Введите ID записи для редактирования:");
            if (idInput == null) return; // пользователь нажал Отмена

            try {
                int id = Integer.parseInt(idInput.trim());
                Record oldRecord = dbManager.getDb().getRecordById(id);

                if (oldRecord == null) {
                    showError("Запись с ID " + id + " не найдена.");
                    return;
                }

                // Создаём поля ввода с текущими значениями
                JTextField nameField = new JTextField(oldRecord.getName());
                JTextField ageField = new JTextField(String.valueOf(oldRecord.getAge()));
                JTextField heightField = new JTextField(String.valueOf(oldRecord.getHeight()));

                Object[] fields = {
                        "Имя:", nameField,
                        "Возраст:", ageField,
                        "Рост:", heightField
                };

                int option = JOptionPane.showConfirmDialog(
                        this, fields, "Редактирование записи (ID: " + id + ")", JOptionPane.OK_CANCEL_OPTION
                );

                if (option == JOptionPane.OK_OPTION) {
                    try {
                        String name = nameField.getText().trim();
                        int age = Integer.parseInt(ageField.getText().trim());
                        double height = Double.parseDouble(heightField.getText().trim());

                        // Создаём НОВУЮ запись с тем же ID, но новыми данными
                        Record updatedRecord = new Record(id, name, age, height);

                        boolean success = dbManager.getDb().updateRecord(updatedRecord);
                        if (success) {
                            updateTable(); // обновляем GUI
                            showInfo("Запись успешно обновлена.");
                        } else {
                            showError("Не удалось обновить запись.");
                        }

                    } catch (NumberFormatException ex) {
                        showError("Ошибка: возраст и рост должны быть числами.");
                    }
                }

            } catch (NumberFormatException ex) {
                showError("ID должен быть целым числом.");
            }
        });

        //  Очистить БД
        clearBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this, "Удалить ВСЕ записи?", "Подтверждение", JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                dbManager.clear();
                updateTable();
                showInfo("База данных очищена.");
            }
        });

        //  Импортировать файл excel
        excelBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Выберите Excel (.xlsx)");

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();

                if (dbManager.loadFromExcel(file)) {
                    updateTable();
                    showInfo("Данные успешно загружены из Excel!");
                } else {
                    showError("Ошибка чтения Excel файла.");
                }
            }
        });

        setVisible(true);
    }

    // Обновление таблицы
    private void updateTable() {
        tableModel.setRowCount(0);

        for (Record r : dbManager.getDb().getTable().values()) {
            tableModel.addRow(new Object[]{
                    r.getId(),
                    r.getName(),
                    r.getAge(),
                    r.getHeight()
            });
        }
    }

    // Вспомогательные сообщения
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Инфо", JOptionPane.INFORMATION_MESSAGE);
    }
}




