package com.currency;

import java.awt.Font;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;

public class Currency {

    public static void main(String[] args) throws Exception {
        String[][] rates = getRates();
        for (int i = 0; i < rates.length; i++) {
            System.out.println(Arrays.toString(rates[i]));
        }
        //создаем объект окна для вывода инф.
        JFrame frame = new JFrame();
        frame.setTitle("Курс валют");// заголовок
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//закрытие программы на крестик

        String[] columnNames = {"Валюта", "Код", "Цена"};
        JTable table = new JTable(rates, columnNames);//массив и название всех столбцов

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 16));// берём заголовок header
        table.setFont(new Font("Arial", Font.PLAIN, 14));// берём table всю таблицу
        table.getRowHeight(table.getRowHeight() + 7);//добавляет к каждой строке пиксели в высоту

        //для выравнивание содержимого таблицы по центру имп. доп класс DefaultTableCellRenderer
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        //table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);//применяем к таблице
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);//применяем к таблице
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);//применяем к таблице

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane);

        frame.pack();//важен порядок сначала pack затем setLocationRelativeTo и они должны быть последними
        frame.setLocationRelativeTo(null);//по центру экрана
        frame.setVisible(true);
    }

    private static String[][] getRates() throws Exception {
        HashMap<String, NodeList> result = new HashMap<>();//здесь хранится ключ и узел
        String[][] rates = null;
        SimpleDateFormat dataFormat = new SimpleDateFormat("dd/MM/yyyy");//формат даты
        Date date = new Date();
        String url = "http://www.cbr.ru/scripts/XML_daily.asp?date_req=" + dataFormat.format(date);
        Document doc = loadDocument(url);
        System.out.println(doc.getXmlVersion());//получаем XML версию документа
        NodeList nl = doc.getElementsByTagName("Valute");//все элементы содержатся в массиве (ищет узлы)
        for (int i = 0; i < nl.getLength(); i++) {//перебираем полученный массив
            Node c = nl.item(i);// хранение самого узла Node, чтобы получить узел исп. метод item();
            NodeList nlChilds = c.getChildNodes();//получаем дочерние узлы
            for (int j = 0; j < nlChilds.getLength(); j++) {
                if (nlChilds.item(j).getNodeName().equals("CharCode")) {
                    //ключ - (его внутренний текст)CharCode , значение - дочерний узел
                    result.put(nlChilds.item(j).getTextContent(), nlChilds);

                }
            }
        }
        //преобразуем HashMap к виду String[][]
        int k = 0; //индекс массива
        rates = new String[result.size()][3];//3 элемента - "Валюта", "Код", "Цена"
        //перебираем HashMap
        for (Map.Entry<String, NodeList> entry : result.entrySet()) {
            NodeList temp = entry.getValue();// получаем значение которое имеет NodeList
            double value = 0;
            int nominal = 0;
            String name = "";
            for (int i = 0; i < temp.getLength(); i++) {
                switch (temp.item(i).getNodeName()) {
                    case "Value":
                        //значение преобр. в Double, заменив запят. на точку
                        value = Double.parseDouble(temp.item(i).getTextContent().replace(",", "."));
                        break;
                    case "Nominal":
                        //значение преобр. в Double, заменив запят. на точку
                        nominal = Integer.parseInt(temp.item(i).getTextContent());
                        break;
                    case "Name":
                        //значение преобр. в Double, заменив запят. на точку
                        name = temp.item(i).getTextContent();
                        break;
                    default:
                        break;
                }
            }
            double amount = value / nominal;
            rates[k][0] = name;
            rates[k][1] = entry.getKey();
            rates[k][2] = (((double) Math.round(amount * 10000) / 10000)) + " рублей.";
            k++;
        }
        return rates;
    }

    //загрузка документа
    private static Document loadDocument(String url) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        return factory.newDocumentBuilder().parse(new URL(url).openStream());
    }
}
