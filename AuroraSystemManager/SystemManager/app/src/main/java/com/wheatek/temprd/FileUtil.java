// Gionee <liuyb> <2013-12-11> add for CR00964937 begin
package com.wheatek.temprd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class FileUtil {
    
    private static final String SAFE_LIST_PATH = "/system/etc/CyeeSafePayConfig.xml";

    public static boolean isExists(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    public static void copyFile(String sourceFilePath, String targetFilePath) throws IOException {
        File sourceFile = new File(sourceFilePath);
        File targetFile = new File(targetFilePath);
        copyFile(sourceFile, targetFile);
    }

    public static void copyFile(File sourceFile, File targetFile) throws IOException {
        FileInputStream fin = null;
        FileOutputStream fout = null;
        try {
            fin = new FileInputStream(sourceFile);
            fout = new FileOutputStream(targetFile);
            int bytesRead;
            byte[] buf = new byte[4 * 1024];
            while ((bytesRead = fin.read(buf)) != -1) {
                fout.write(buf, 0, bytesRead);
            }
            fout.flush();
        } finally {
            if (fout != null) {
                fout.close();
            }
            if (fin != null) {
                fin.close();
            }
        }
    }

    public static void del(String filepath) throws IOException {
        File f = new File(filepath);
        if (f.exists()) {
            f.delete();
        }
    }
    
    public static List<String> getSafeListFromXml() {
        List<String> safeList = new ArrayList<String>();
        DocumentBuilderFactory factory = null;
        DocumentBuilder builder = null;
        Document document = null;
        InputStream inputStream = null;

        factory = DocumentBuilderFactory.newInstance();
        try {
            builder = factory.newDocumentBuilder();
            inputStream = new FileInputStream(new File(SAFE_LIST_PATH));
            document = builder.parse(inputStream);
            Element root = document.getDocumentElement();
            Element safelistNode = (Element) root.getElementsByTagName("safelist").item(0);
            NodeList nodes = safelistNode.getElementsByTagName("pkg");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element pkgElement = (Element) (nodes.item(i));                
                String pkg = pkgElement.getTextContent().trim();
                safeList.add(pkg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return safeList;
    }
}
//Gionee <liuyb> <2013-12-11> add for CR00964937 end