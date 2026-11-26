package com.example.billing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;

/**
 * 請求書発行サービスクラス。
 */
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    /** インメモリキャッシュ（invoiceId → Invoice） */
    private final Map<String, Invoice> cache = new HashMap<>();
    /** インメモリキャッシュ（clientId → Client） */
    private final Map<String, Client>  clientCache = new HashMap<>();

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * 取引先をキャッシュに登録する。
     * @param client 登録する取引先
     */
    public void registerClient(Client client) {
        clientCache.put(client.getClientId(), client);
    }

    /**
     * 請求書を発行してキャッシュに追加する。
     * @param invoice 発行する請求書
     */
    public void issue(Invoice invoice) {
        cache.put(invoice.getInvoiceId(), invoice);
        //System.out.println("請求書発行: invoiceId=" + invoice.getInvoiceId());
    }

    /**
     * キャッシュ内の請求書を一括 INSERT する。
     */
    public void bulkInsert() {
        System.out.println("[JDBC] 一括INSERT開始:  件" + cache.size());
        for (Invoice inv : cache.values()) {
            System.out.println("[JDBC] INSERT 開始: invoiceId=" + inv.getInvoiceId());
            invoiceRepository.insert(inv);
        }
        System.out.println("[JDBC] 一括INSERT完了:  件" + cache.size());
    }

    /**
     * 月次集計レポートを印字する。
     */
    public void printMonthlyReport() {
        System.out.println("========== 月次集計レポート ==========");
        System.out.printf("%-16s  %-18s  %12s%n",
            "請求書番号", "取引先", "税込合計");
        System.out.println("------------------------------------------------------");

        int grandTotal = 0;
        List<Invoice> sortedInvoices = new ArrayList<>(cache.values());
        sortedInvoices.sort(Comparator.comparing(Invoice::getInvoiceId));

        for (Invoice inv : sortedInvoices) {
            Client client = clientCache.get(inv.getClientId());
            String clientName = (client != null) ? client.getClientName() : "不明";
            System.out.printf("%-16s  %-18s  %,12d円%n",
                inv.getInvoiceId(), clientName, inv.calcTotalWithTax());
            grandTotal += inv.calcTotalWithTax();
        }

        System.out.println("------------------------------------------------------");
        System.out.printf("合計：%,d円%n", grandTotal);
    }

    /**
     * 支払期限の昇順で請求書をソートして印字する。
     */
    public void printSortedByDueDate() {
        List<Invoice> invoices = new ArrayList<>(cache.values());
        Invoice[] arr = invoices.toArray(new Invoice[0]);

        // バブルソート（支払期限の昇順）
        for (int i = 0; i < arr.length - 1; i++) {
            for (int j = 0; j < arr.length - 1 - i; j++) {
                if (arr[j].getDueDate().isAfter(arr[j + 1].getDueDate())) {
                    Invoice tmp = arr[j];
                    arr[j]      = arr[j + 1];
                    arr[j + 1]  = tmp;
                }
            }
        }

        System.out.println("\n========== 支払期限別ソート ==========");
        for (Invoice inv : arr) {
            System.out.printf("%-16s  %-12s  %,12d円%n",
                inv.getInvoiceId(), inv.getDueDate(), inv.calcTotalWithTax());
        }
    }
}