import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by rtmkrlv on 05.08.2015.
 */
public class Main {

    private static final String[] inputHeader = {"Наименование №1", "Артикул производителя", "SEO ссылка на товар (ЧПУ)", "Цена, RUB", "Валюта",
            "Тег title для товара", "Тег description для товара", "Тег keywords для товара", "Наличие", "Категория товара (полная)"};
    private static final String[] outputHeader = {"Наименование", "Артикул", "Ссылка на витрину", "Цена", "Валюта",
            "Заголовок", "META Description", "META Keywords", "В наличии", "Доступен", "Статус"};
    private static final String inputInStock = "Есть";
    private static final String inputNotInStock = "Нет";
    private static final String[] outputInStock = {"10", "1", "1"};
    private static final String[] outputNotInStock = {"0", "0", "0"};
    private static final String[] outputTemp = {"Temp", "", "", "", "", "", "", "", "", "", ""};

    public static void main(String[] args) {

        // принимаем аргументы
        String inputCSV = args[0]; // первый аргумент - имя файла
        boolean rrp = Boolean.parseBoolean(args[1]); // второй аргумент - необходимость установки РРЦ
        int numberOfParts = 1; // по умолчанию файл не разбивается на части
        boolean temp = false; // по умолчанию не добавляется категория Temp
        if (args.length > 2) { // если аргументов больше двух, то третий - необходимость создания категории Temp, либо количество частей, на которое нужно разбить файл
            if ("temp".equals(args[2].toLowerCase())) {
                temp = true;
            } else {
                numberOfParts = Integer.parseInt(args[2]);
            }
        }

        // считываем файл
        List<List<String>> csv = new ArrayList<>();
        boolean converted;
        try (PLiTokenizer tokenizer = new PLiTokenizer(new FileReader(inputCSV), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
             CsvListReader reader = new CsvListReader(tokenizer, CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE)) {

            // считываем и проверяем заголовок
            System.out.print("Reading & checking header... ");
            String[] header = reader.getHeader(true);
            if (Arrays.equals(header, inputHeader)) {
                converted = false;
                System.out.println("OK (PLi header detected).");
            } else if (Arrays.equals(header, outputHeader)) {
                converted = true;
                System.out.println("OK (SS header detected).");
            } else {
                System.out.println("Unknown header! Stopped!");
                throw new Error();
            }

            // считываем, проверяем и, если необходимо, конвертируем строки
            System.out.print("Reading");
            if (converted) {
                System.out.print(" & checking");
            } else {
                System.out.print(", checking & converting");
            }
            System.out.print(" row(s)... ");
            List<String> row;
            String[] outputCells = new String[3];
            int skipped = 0;
            boolean skip = false;
            while ((row = reader.read()) != null) {
                if (!converted & row.size() == inputHeader.length) {
                    if (row.get(8).equals(inputInStock)) {
                        outputCells = outputInStock;
                    } else if (row.get(8).equals(inputNotInStock)) {
                        outputCells = outputNotInStock;
                    } else {
                        skip = true;
                    }
                    if (!skip) {
                        row.set(8, outputCells[0]);
                        row.set(9, outputCells[1]);
                        row.add(outputCells[2]);
                    }
                } else if (converted & row.size() == outputHeader.length) {
                    String[] cells = {row.get(8), row.get(9), row.get(10)};
                    if (!(Arrays.equals(cells, outputInStock) || Arrays.equals(cells, outputNotInStock))) {
                        skip = true;
                    }
                } else {
                    skip = true;
                }
                if (skip) {
                    skipped++;
                    skip = false;
                } else {
                    csv.add(row);
                }
            }
            if (csv.size() == 0) {
                System.out.println("File is empty! Stopped!");
            }
            System.out.print("OK (readed");
            if (converted) {
                System.out.print(" & checked ");
            } else {
                System.out.print(", checked & converted ");
            }
            System.out.println(csv.size() + " row(s), skipped " + skipped + " row(s)).");
        } catch (Exception e) {
            System.out.println("Exception!");
            return;
        }

        // выставляем фиксированные цены
        System.out.print("\nSetting of fixed price(s)... ");

        List<String[]> fixedPrice = new ArrayList<>();
        fixedPrice.add(new String[]{"Сервиз LUMINARC AIME COUNTRY FLOWER", "G4029", "1531.00"});

        System.out.println("OK (setted " + setPrice(csv, fixedPrice, 2) + " of " + fixedPrice.size() + " fixed price(s)).");

        // выставляем цены на акционные товары
        System.out.print("Setting of promotional price(s)... ");

        List<String[]> promotionalPrice = new ArrayList<>();
        promotionalPrice.add(new String[]{"Epson L120 (C11CD76302)", "C11CD76302", "UAH", "2499.00", "SALE2"});
        promotionalPrice.add(new String[]{"Epson L350 (C11CC26301)", "C11CC26301", "UAH", "3999.00", "SALE2"});
        promotionalPrice.add(new String[]{"HTC Desire 210 (White)", "99HABF021-00", "UAH", "1875.00", ""});
        promotionalPrice.add(new String[]{"HTC Desire 210 (Black)", "99HABF022-00", "UAH", "1875.00", ""});
        promotionalPrice.add(new String[]{"Philips MCM2005", "4C30NX9ZS", "UAH", "1999.00", ""});
        promotionalPrice.add(new String[]{"Dell Alienware TactX (570-11554)", "570-11554", "UAH", "1099.00", ""});

        System.out.println("OK (setted " + setPrice(csv, promotionalPrice, 3) + " of " + promotionalPrice.size() + " promotional price(s)).");

        // если нужно, выставляем рекомендованные розничные цены
        System.out.print("Setting of recommended retail price(s)... ");

        if (rrp) {

            List<String[]> recommendedRetailPrice = new ArrayList<>();
            recommendedRetailPrice.add(new String[]{"Delonghi ESAM 2200 S", "ESAM2200.S", "7499.00"});
            recommendedRetailPrice.add(new String[]{"Delonghi ESAM 2600", "ESAM2600", "7499.00"});
            recommendedRetailPrice.add(new String[]{"Delonghi ESAM 3000 B", "ESAM3000.B", "7499.00"});
            recommendedRetailPrice.add(new String[]{"Delonghi ESAM 3200 S", "ESAM3200.S", "7499.00"});
            recommendedRetailPrice.add(new String[]{"Delonghi ESAM 4000 B", "ESAM4000.B", "7499.00"});
            recommendedRetailPrice.add(new String[]{"Delonghi ESAM 4200 S", "ESAM4200.S", "7499.00"});

            System.out.println("OK (setted " + setPrice(csv, recommendedRetailPrice, 2) + " of " + recommendedRetailPrice.size() + " recommended retail price(s)).");
        } else {
            System.out.println("Skipped.");
        }

        // перезаписываем файл
        String outputCSV = inputCSV;
        try (CsvListWriter writer = new CsvListWriter(new FileWriter(outputCSV), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE)) {
            System.out.print("\nWriting header");
            if (temp) {
                System.out.print(", category");
            }
            System.out.print(" & row(s)... ");
            writer.writeHeader(outputHeader);
            if (temp) {
                writer.write(outputTemp);
            }
            for (List<String> row : csv) writer.write(row);
            System.out.print("OK (written header");
            if (temp) {
                System.out.print(", category");
            }
            System.out.println(" & " + csv.size() + " row(s)).");
        } catch (Exception e) {
            System.out.println("Exception!");
            return;
        }

        // если необходимо, разбиваем дополнительно на части
        if (numberOfParts > 1) {
            int partSize = (int)Math.ceil((double)csv.size() / numberOfParts);
            int count = 0;

            for (int i = 0; i < numberOfParts; i++) {
                outputCSV = new StringBuilder(inputCSV).insert(inputCSV.length() - 4, " (" + (i + 1) + ")").toString();

                try (CsvListWriter writer = new CsvListWriter(new FileWriter(outputCSV), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE)) {
                    System.out.print("Writing header & row(s) part " + (i + 1) + " of " + numberOfParts + "... ");
                    writer.writeHeader(outputHeader);
                    for (int j = 0; j < partSize & count < csv.size(); j++) {
                        List<String> row = csv.get(count++);
                        writer.write(row);
                    }
                    System.out.println("OK (written header & " + (count - i * partSize) + " row(s)).");
                } catch (Exception e) {
                    System.out.println("Exception!");
                    return;
                }
            }
        }

        // ищем дубли
        System.out.print("\nSearching duplicates... ");

        // считаем количество повторений каждого товара
        Map<String, Integer> duplicates = new HashMap<>();
        String product;
        for (List<String> row : csv) {
            product = row.get(0);
            if (duplicates.containsKey(product)) {
                duplicates.put(product, duplicates.get(product) + 1);
            } else {
                duplicates.put(product, 1);
            }
        }

        // убираем товары, встречающиеся по одному разу
        Iterator it = duplicates.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if ((Integer)pair.getValue() == 1) {
                it.remove();
            }
        }

        // выводим результат
        if (duplicates.size() == 0) {
            System.out.println("OK (duplicates not found).");
        } else {
            System.out.println("OK (found " + duplicates.size() + " duplicate(s)).\n\nDuplicates:\n");
            it = duplicates.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                System.out.println(pair.getKey() + " - " + pair.getValue());
            }
        }

    }

    // метод, выставляющий в прайсе цены из передаваемой коллекции массивов
    static int setPrice(List<List<String>> csv, List<String[]> price, int priceColumn) {
        int count = 0;
        for (int i = 0; i < csv.size(); i++) {
            List<String> row = csv.get(i);
            for (String[] priceRow : price) {
                if (row.get(0).equals(priceRow[0]) && row.get(1).equals(priceRow[1])) {
                    row.set(3, priceRow[priceColumn]);
                    csv.set(i, row);
                    count++;
                    break;
                }
            }
        }
        return count;
    }
}
