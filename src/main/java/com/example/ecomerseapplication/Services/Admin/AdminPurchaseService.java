package com.example.ecomerseapplication.Services.Admin;

import com.example.ecomerseapplication.DTOs.requests.DateRangeRequest;
import com.example.ecomerseapplication.DTOs.requests.PurchaseActionRequest;
import com.example.ecomerseapplication.DTOs.responses.CompactAdminPurchaseResponse;
import com.example.ecomerseapplication.DTOs.responses.PageResponse;
import com.example.ecomerseapplication.DTOs.responses.ReportResponses;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.PurchaseProductProjection;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.PurchaseProjection;
import com.example.ecomerseapplication.Entities.Purchase;
import com.example.ecomerseapplication.Entities.PurchaseCart;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.InvalidEnumNameException;
import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.PessimisticLockOrTimeoutPurchaseException;
import com.example.ecomerseapplication.Mappers.PurchaseMapper;
import com.example.ecomerseapplication.Others.PageContentLimit;
import com.example.ecomerseapplication.Repositories.PurchaseCartRepository;
import com.example.ecomerseapplication.Repositories.PurchaseRepository;
import com.example.ecomerseapplication.Services.PurchaseCartService;
import com.example.ecomerseapplication.Services.PurchaseService;
import com.example.ecomerseapplication.enums.DeliveryStatus;
import com.example.ecomerseapplication.enums.PurchaseStatusAction;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import jakarta.validation.Valid;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminPurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseService purchaseService;
    private final AdminProductService adminProductService;
    private final PurchaseCartService purchaseCartService;
    private final PurchaseCartRepository purchaseCartRepository;

    public AdminPurchaseService(PurchaseRepository purchaseRepository, PurchaseService purchaseService, AdminProductService adminProductService, PurchaseCartService purchaseCartService, PurchaseCartRepository purchaseCartRepository) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseService = purchaseService;
        this.adminProductService = adminProductService;
        this.purchaseCartService = purchaseCartService;
        this.purchaseCartRepository = purchaseCartRepository;
    }

    public PageResponse<CompactAdminPurchaseResponse> getPagedPurchasesCompact(int page) {
        PageRequest pageRequest = PageRequest.of(page, PageContentLimit.limit);

        Page<PurchaseProjection> purchaseProjectionsPage = purchaseRepository.getAllForAdminPaged(pageRequest);

        List<PurchaseProjection> purchaseProjectionsList = purchaseProjectionsPage.getContent();

        List<CompactAdminPurchaseResponse> responseList = PurchaseMapper.purchaseProjectionListToCompactAdminResponseList(purchaseProjectionsList);

        Page<CompactAdminPurchaseResponse> oldPage = new PageImpl<>(responseList,
                purchaseProjectionsPage.getPageable(),
                purchaseProjectionsPage.getTotalElements());

        return PageResponse.from(oldPage);
    }

    public Integer getRefundPendingCount() {
        return purchaseRepository.refundPendingCount(DeliveryStatus.REFUND_REQUESTED);
    }

    @Transactional
    public void executePurchaseStatusAction(PurchaseActionRequest request) {

        Purchase purchase;
        try {
            purchase = purchaseService.getByIdWithLock(request.purchaseId());
        } catch (PessimisticLockException | LockTimeoutException e) {
            throw new PessimisticLockOrTimeoutPurchaseException("Purchase with id: " + request.purchaseId() + " currently locked updates",
                    "Заключена поръчка",
                    "Поръчката беше временно недостъпна за промени, опитайте отново");
        }

        String action = request.action();

        purchaseActionResolver(action, purchase);

    }

    private void purchaseActionResolver(String action, Purchase purchase) {

        PurchaseStatusAction newAction;
        try {
            newAction = PurchaseStatusAction.valueOf(action.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidEnumNameException("Invalid PurchaseStatusAction enum name: " + action,
                    "Невалидна действие",
                    "Зададеното действие за поръчката е невалидно");
        }

        switch (newAction) {
            case DELIVER -> purchase.deliverPurchase();
            case CANCEL -> {
                purchase.cancelPurchase();
                List<PurchaseCart> purchaseCarts = purchaseCartService.getByPurchaseId(purchase.getId());
                purchaseService.restockProductsOfCancelledPurchase(purchaseCarts);
            }
            case SHIP -> purchase.shipPurchase();
            case APPROVE_REFUND -> {
                purchase.approveRefund();
                List<PurchaseCart> purchaseCarts = purchaseCartService.getByPurchaseId(purchase.getId());

                adminProductService.refreshStockOfPurchaseProducts(purchaseCarts);
            }
            case REJECT_REFUND -> purchase.rejectRefund();
        }
    }


    public ReportResponses.ReportResponse revenueReport(DateRangeRequest request) {

        List<PurchaseProjection> projections = purchaseRepository.purchasesOfPeriod(
                request.startDate(),
                request.endDate(),
                DeliveryStatus.DELIVERED);

        List<MonthDataResponse> months = getMonthsFromRequest(request);

        Map<String, MonthDataResponse> monthMap = months
                .stream()
                .collect(Collectors.toMap(mdr -> mdr.monthKey, mdr -> mdr));

        ZoneId zoneId = ZoneId.of(request.timezone());

        for (PurchaseProjection projection : projections) {

            if (projection == null) {
                continue;
            }

            ZonedDateTime zonedDelivery = projection.getDeliveryDate().atZone(zoneId);

            String mapKey = YearMonth.from(zonedDelivery).toString();

            MonthDataResponse monthResponse = monthMap
                    .get(mapKey);

            if (monthResponse != null) {
                monthResponse.revenueCents += projection.getTotalCost();
                monthResponse.purchasesCount++;
            }

        }

        List<ReportResponses.MetricDto> metrics = getMetricDtos(monthMap, projections);

        ReportResponses.ChartDto chartDto = new ReportResponses.ChartDto(ReportResponses.ChartType.LINE,
                "month",
                "revenue",
                "Приходи (€)",
                buildChartDataMapList(months),
                ReportResponses.ValueType.CURRENCY);

        return ReportResponses.buildMixedReport("Приходи за периода ",
                metrics,
                chartDto,
                List.of("Месец", "Приходи (€)", "Поръчки"),
                getTableRowMapListForRevenueReport(months),
                List.of(request.startDate().toString(), request.endDate().toString())
        );
    }

    @NotNull
    private static List<ReportResponses.MetricDto> getMetricDtos(Map<String, MonthDataResponse> monthMap, List<PurchaseProjection> projections) {
        int total = 0;

        for (MonthDataResponse monthResponse : monthMap.values()) {
            total += monthResponse.revenueCents;
        }

        int averagePerPurchase = total / projections.size();

        return List.of(new ReportResponses.MetricDto("Общи приходи", total+"" , ReportResponses.ValueType.CURRENCY),
                new ReportResponses.MetricDto("Брой поръчки", projections.size() + "", ReportResponses.ValueType.NUMBER),
                new ReportResponses.MetricDto("Средна стойност", averagePerPurchase + "", ReportResponses.ValueType.CURRENCY));
    }

    @NotNull
    private static List<Map<String, ReportResponses.TableColumnRow>> getTableRowMapListForRevenueReport(List<MonthDataResponse> months) {

        String mon = "Месец";
        String cash = "Приходи (€)";
        String purch = "Поръчки";

        List<Map<String, ReportResponses.TableColumnRow>> mapList = new ArrayList<>();

        for (MonthDataResponse month : months) {
            Map<String, ReportResponses.TableColumnRow> monthMap = Map.of(
                    mon, new ReportResponses.TableColumnRow(month.displayName, ReportResponses.ValueType.TEXT),
                    cash, new ReportResponses.TableColumnRow(month.revenueCents + "", ReportResponses.ValueType.CURRENCY),
                    purch, new ReportResponses.TableColumnRow(month.purchasesCount + "", ReportResponses.ValueType.NUMBER));

            mapList.add(monthMap);
        }

        return mapList;
    }

    @NotNull
    private static List<Map<String, String>> buildChartDataMapList(List<MonthDataResponse> months) {

        List<Map<String, String>> responseList = new ArrayList<>();

        for (MonthDataResponse month : months) {
            Map<String, String> monthMap = Map.of("month", month.displayName, "revenue", month.revenueCents + "");
            responseList.add(monthMap);
        }
        return responseList;
    }


    public List<MonthDataResponse> getMonthsFromRequest(DateRangeRequest request) {
        ZoneId zoneId = ZoneId.of(request.timezone());
        ZonedDateTime localStart = request.startDate().atZone(zoneId);
        ZonedDateTime localEnd = request.endDate().atZone(zoneId);

        List<MonthDataResponse> responseList = new ArrayList<>();

        YearMonth startMonth = YearMonth.from(localStart);
        YearMonth endMonth = YearMonth.from(localEnd);

        Locale bgLocale = Locale.of("bg", "BG");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", bgLocale);

        YearMonth current = startMonth;
        while (!current.isAfter(endMonth)) {
            MonthDataResponse item = new MonthDataResponse();

            item.monthKey = current.toString();

            item.displayName = current.format(formatter);

            responseList.add(item);
            current = current.plusMonths(1);
        }

        return responseList;
    }

    public ReportResponses.ReportResponse getPurchaseStatusStatistic(@Valid DateRangeRequest request) {

        List<PurchaseProjection> projections = purchaseRepository.getCountsForDeliveryStatuses(request.startDate(), request.endDate());

        Map<DeliveryStatus, Integer> statusCountMap = projections
                .stream()
                .collect(Collectors.toMap(pr -> DeliveryStatus.valueOf(pr.getDeliveryStatus()), PurchaseProjection::getStatusCount));

        ReportResponses.ChartDto chartDto = new ReportResponses.ChartDto(ReportResponses.ChartType.BAR,
                "status",
                "count",
                "Брой поръчки на статус",
                buildChartDataMapListForPurchaseStatusReport(statusCountMap),
                ReportResponses.ValueType.NUMBER);

        return ReportResponses.buildMixedReportNoMetrics("Състоянието на вискчи поръчки за периода ",
                chartDto,
                List.of("Статус", "Брой", "Дял"),
                getTableRowMapListForPurchaseStatusReport(statusCountMap),
                List.of(request.startDate().toString(), request.endDate().toString()));
    }

    public ReportResponses.ReportResponse purchaseStatusReportWithBgTableNames(ReportResponses.ReportResponse response) {

        List<Map<String, ReportResponses.TableColumnRow>> tableRowMap = response.rows();

        List<Map<String, ReportResponses.TableColumnRow>> newMapList = new ArrayList<>();

        for (Map<String, ReportResponses.TableColumnRow> map : tableRowMap) {

            Map<String, ReportResponses.TableColumnRow> mutableCopy =
                    new LinkedHashMap<>(map);

            ReportResponses.TableColumnRow tableNameColumnRow = mutableCopy.get("Статус");

            ReportResponses.TableColumnRow newStatusName = new ReportResponses.TableColumnRow(DeliveryStatus.valueOf(tableNameColumnRow.value()).getBgLabel(),
                    ReportResponses.ValueType.TEXT);

            mutableCopy.remove("Статус");

            Map<String, ReportResponses.TableColumnRow> newRowMap = new HashMap<>();
            newRowMap.put("Статус", newStatusName);
            newRowMap.putAll(mutableCopy);

            newMapList.add(newRowMap);
        }
        return ReportResponses.buildMixedReportNoMetrics(response.title(),
                response.chart(),
                List.of("Статус", "Брой", "Дял"),
                newMapList,
               response.dates());
    }

    private static int totalPurchaseStatusCount(Map<DeliveryStatus, Integer> statusCountMap) {
        int total = 0;
        for (Integer count : statusCountMap.values()) {
            total += count;
        }
        return total;
    }

    @NotNull
    private static List<Map<String, ReportResponses.TableColumnRow>> getTableRowMapListForPurchaseStatusReport(Map<DeliveryStatus, Integer> statusCountMap) {

        int totalCount = totalPurchaseStatusCount(statusCountMap);

        String name = "Статус";
        String statCount = "Брой";
        String portion = "Дял";

        List<Map<String, ReportResponses.TableColumnRow>> mapList = new ArrayList<>();

        for (DeliveryStatus status : DeliveryStatus.values()) {
            Integer count = statusCountMap.get(status);

            double portionValue = count!=null ?((double) count / totalCount) :0;

            Map<String, ReportResponses.TableColumnRow> monthMap = Map.of(
                    name, new ReportResponses.TableColumnRow(status.name(), ReportResponses.ValueType.TEXT),
                    statCount, new ReportResponses.TableColumnRow(count!=null ?count+ "" :"0" , ReportResponses.ValueType.NUMBER),
                    portion, new ReportResponses.TableColumnRow( portionValue+ "", ReportResponses.ValueType.PERCENTAGE));

            mapList.add(monthMap);
        }

        return mapList;
    }

    @NotNull
    private static List<Map<String, String>> buildChartDataMapListForPurchaseStatusReport(Map<DeliveryStatus, Integer> statusCountMap) {

        List<Map<String, String>> responseList = new ArrayList<>();

        for (DeliveryStatus status : DeliveryStatus.values()) {
            Integer count = statusCountMap.get(status);

            Map<String, String> monthMap = Map.of("status", status.name(), "count", count!=null ?count+ "" :"0");
            responseList.add(monthMap);
        }
        return responseList;
    }

    @ToString
    public static class MonthDataResponse {
        public String monthKey;
        public String displayName;
        public int revenueCents = 0;
        public int purchasesCount = 0;
    }

    public ReportResponses.ReportResponse getTopSellingProductsForPeriod(DateRangeRequest request, Integer limit) {

        PageRequest pageRequest = PageRequest.of(0, limit);

        Instant startDate = request.startDate();
        Instant endDate = request.endDate();


        List<PurchaseProductProjection> productPurchaseProjections = purchaseCartRepository.getTopSellingOfPeriod(startDate,
                endDate,
                pageRequest,
                DeliveryStatus.DELIVERED);

        List<String> columns = List.of("Код на продукт", "Име на продукт", "Продадени бройки", "Приходи (€)");

       return ReportResponses.buildTableReport("Най-продавани продукти за периода ",
                columns,
                getTableRowMapListForTopProductsReport(productPurchaseProjections, columns),
                List.of(request.startDate().toString(), request.endDate().toString()));
    }

    @NotNull
    private static List<Map<String, ReportResponses.TableColumnRow>> getTableRowMapListForTopProductsReport(List<PurchaseProductProjection> productProjections,
                                                                                                            List<String> columns) {
        List<Map<String, ReportResponses.TableColumnRow>> mapList = new ArrayList<>();

        for (PurchaseProductProjection product : productProjections) {
            Map<String, ReportResponses.TableColumnRow> monthMap = Map.of(
                    columns.getFirst(), new ReportResponses.TableColumnRow(product.getProductCode(), ReportResponses.ValueType.TEXT),
                    columns.get(1), new ReportResponses.TableColumnRow(product.getProductName(), ReportResponses.ValueType.TEXT),
                    columns.get(2), new ReportResponses.TableColumnRow(product.getUnitsSold() + "", ReportResponses.ValueType.NUMBER),
                    columns.get(3), new ReportResponses.TableColumnRow(product.getRevenueGained() + "", ReportResponses.ValueType.CURRENCY));

            mapList.add(monthMap);
        }

        return mapList;
    }

}
