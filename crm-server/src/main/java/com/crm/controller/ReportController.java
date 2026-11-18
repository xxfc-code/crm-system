package com.crm.controller;

import com.crm.common.DataScope;
import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.mapper.ReportMapper;
import com.crm.service.DataScopeService;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Resource
    private ReportMapper reportMapper;
    @Resource
    private DataScopeService dataScopeService;

    /** 销售统计：订单和回款按月合并 */
    @GetMapping("/sales")
    @SuppressWarnings("unchecked")
    public Result<Map<String, Object>> sales() {
        DataScope ds = dataScopeService.current();
        Map<String, Object> data = new HashMap<>();
        // 订单和回款先各自补齐 12 个月，再按月份合并成一条
        // （两条 SQL 查的列不一样：订单侧没有 payAmount，回款侧没有 orderAmount，
        //   合并后前端只读一个数组就能同时拿到订单量和回款额）
        List<Map<String, Object>> orders = fillMonths(reportMapper.salesByMonth(ds));
        List<Map<String, Object>> payments = fillMonths(reportMapper.paymentByMonth(ds));
        Map<String, Map<String, Object>> payByMonth = new HashMap<>();
        for (Map<String, Object> p : payments) {
            payByMonth.put(String.valueOf(p.get("month")), p);
        }
        for (Map<String, Object> o : orders) {
            Map<String, Object> p = payByMonth.get(String.valueOf(o.get("month")));
            o.put("payAmount", p == null ? 0 : p.get("payAmount"));
        }
        data.put("orders", orders);
        data.put("payments", payments);
        return Result.ok(data);
    }

    /**
     * 补齐近 12 个月：没数据的月份补 0，按时间正序
     * 不补的话趋势图只有几个点，看不出「哪个月没成交」
     *
     * 注意：两条 SQL 查出来的列不一样（订单侧只有 orderCount/orderAmount，
     * 回款侧只有 payAmount），所以缺失的列一律补 0 而不是 null ——
     * null 传到前端会变成 "null" 文本或让图表断线。
     */
    private List<Map<String, Object>> fillMonths(List<Map<String, Object>> rows) {
        Map<String, Map<String, Object>> byMonth = new HashMap<>();
        for (Map<String, Object> r : rows) {
            byMonth.put(String.valueOf(r.get("month")), r);
        }
        List<Map<String, Object>> list = new ArrayList<>();
        YearMonth cur = YearMonth.now();
        for (int i = 11; i >= 0; i--) {
            String key = cur.minusMonths(i).toString();
            Map<String, Object> row = byMonth.get(key);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", key);
            item.put("orderCount", num(row, "orderCount"));
            item.put("orderAmount", num(row, "orderAmount"));
            item.put("payAmount", num(row, "payAmount"));
            list.add(item);
        }
        return list;
    }

    /** 取数值列，缺失或 null 一律当 0 */
    private Object num(Map<String, Object> row, String key) {
        if (row == null) {
            return 0;
        }
        Object v = row.get(key);
        return v == null ? 0 : v;
    }

    @GetMapping("/customer")
    public Result<List<Map<String, Object>>> customer() {
        return Result.ok(reportMapper.customerPurchase(dataScopeService.current()));
    }

    @GetMapping("/product")
    public Result<List<Map<String, Object>>> product() {
        return Result.ok(reportMapper.productSales(dataScopeService.current()));
    }

    /** 应收账款，分页 */
    @GetMapping("/receivable")
    public Result<PageResult<Map<String, Object>>> receivable(@RequestParam(defaultValue = "1") int pageNum,
                                                              @RequestParam(defaultValue = "10") int pageSize) {
        DataScope ds = dataScopeService.current();
        PageHelper.startPage(pageNum, pageSize);
        return Result.ok(PageResult.of(reportMapper.receivable(ds)));
    }
}
