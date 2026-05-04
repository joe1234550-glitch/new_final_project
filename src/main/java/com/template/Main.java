package main.java.com.template;

import main.java.com.template.config.DatabaseConfig;
import com.template.view.MainView;
import main.java.com.template.view.MainView2;

import java.util.Scanner;

/**
 * 程式入口
 *
 * 📝 同學只要改 Banner 和 package 名稱就好，
 *    這個 Main 結構可以直接沿用。
 *
 * 編譯指令（Mac/Linux）：
 *   javac -cp "lib/*" -d out -encoding UTF-8 src/main/java/com/template/**\/*.java
 *
 * 執行指令：
 *   java -cp "out:lib/*" com.template.Main         (Mac/Linux)
 *   java -cp "out;lib\*" com.template.Main          (Windows)
 *
 * 或直接用：
 *   ./run.sh       (Mac/Linux)
 *   run.bat        (Windows)
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // Step 1: 測試資料庫連線
            System.out.print("正在連線資料庫...");
            if (DatabaseConfig.testConnection()) {
                System.out.println(" ✅ 成功！\n");
            } else {
                System.out.println(" ❌ 失敗！");
                System.out.println("請確認：");
                System.out.println("  1. PostgreSQL 是否啟動");
                System.out.println("  2. DatabaseConfig.java 中的帳密是否正確");
                System.out.println("  3. 資料庫 myproject 是否已建立");
                return;
            }

            // Step 2: 啟動主畫面
//            MainView mainView = new MainView(scanner);
//            mainView.start();
            MainView2 mainView2 = new MainView2(scanner);
            mainView2.start();

        } catch (Exception e) {
            System.err.println("❌ 發生未預期的錯誤: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
            System.out.println("\n程式結束。");
        }
    }
}
