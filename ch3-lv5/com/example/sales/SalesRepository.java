package com.example.sales;

import java.util.ArrayList;
import java.util.List;

/**
 * 売上レコードのリポジトリクラス。
 */
public class SalesRepository {

    private static final java.util.logging.Logger log =
        java.util.logging.Logger.getLogger(SalesRepository.class.getName());

    private final List<SalesRecord> records = new ArrayList<>();

    public void add(SalesRecord record) {
        records.add(record);
    }

    public List<SalesRecord> findAll() {
        return new ArrayList<>(records);
    }
}