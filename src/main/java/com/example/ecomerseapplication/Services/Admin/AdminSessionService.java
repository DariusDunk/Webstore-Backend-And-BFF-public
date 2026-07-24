package com.example.ecomerseapplication.Services.Admin;

import com.example.ecomerseapplication.DTOs.responses.ReportResponses;
import com.example.ecomerseapplication.DTOs.serverDtos.projectionInterfaces.SessionActivityProjection;
import com.example.ecomerseapplication.Repositories.SessionRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class AdminSessionService {
    private final SessionRepository sessionRepository;

    public AdminSessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public ReportResponses.ReportResponse getActiveSessions() {

        Instant now = Instant.now();
        Instant fourMinutesAgo = now.minus(4, ChronoUnit.MINUTES);
        Instant truncated = now.truncatedTo(ChronoUnit.MINUTES);

        List<String> dateList = List.of(truncated.toString());

        SessionActivityProjection projection = sessionRepository.getActiveSessionCount(fourMinutesAgo);

        return getReportResponse(projection.getTotal()!=null ?projection.getTotal().toString(): "0",
                projection.getAuth()!=null ?projection.getAuth().toString() :"0",
                projection.getGuest()!=null ?projection.getGuest().toString():"0",
                dateList);
    }

    @NotNull
    private static ReportResponses.ReportResponse getReportResponse(String totalCount, String authCount, String guestCount, List<String> dateList) {
        List<Map<String, ReportResponses.TableColumnRow>> rows = getTableRowMapList(
                totalCount,
                authCount,
                guestCount);

        List<Map<String, String>> chartData = buildChartDataMapList(
                totalCount,
                authCount,
                guestCount);

        ReportResponses.ChartDto chartDto = ReportResponses.buildBarChart("sessionType",
                "count",
                "Брой потребители",
                chartData,
                ReportResponses.ValueType.NUMBER);

        ReportResponses.ReportResponse response = ReportResponses.buildMixedReport("Потребителска активност към ",
                null,
                chartDto,
                List.of("Тип потребител", "Брой"),
                rows,
                dateList
        );

        System.out.println("response: " + response);

        return response;
    }

    @NotNull
    private static List<Map<String, ReportResponses.TableColumnRow>> getTableRowMapList(String totalCount, String authCount, String guestCount) {
        String total = totalCount != null ? totalCount : "0";
        String auth = authCount != null ? authCount : "0";
        String guest = guestCount != null ? guestCount : "0";

        Map<String, ReportResponses.TableColumnRow> totalMap = Map.of(
                "Тип потребител", new ReportResponses.TableColumnRow("Общо", ReportResponses.ValueType.TEXT),
                "Брой", new ReportResponses.TableColumnRow(total, ReportResponses.ValueType.NUMBER));
        Map<String, ReportResponses.TableColumnRow> authMap = Map.of(
                "Тип потребител", new ReportResponses.TableColumnRow("Потребители", ReportResponses.ValueType.TEXT),
                "Брой", new ReportResponses.TableColumnRow(auth, ReportResponses.ValueType.NUMBER));
        Map<String, ReportResponses.TableColumnRow> guestMap = Map.of("Тип потребител", new ReportResponses.TableColumnRow("Гост потребители", ReportResponses.ValueType.TEXT),
                "Брой", new ReportResponses.TableColumnRow(guest, ReportResponses.ValueType.NUMBER));

        return List.of(totalMap, authMap, guestMap);
    }

    @NotNull
    private static List<Map<String, String>> buildChartDataMapList(String totalCount, String authCount, String guestCount) {
        String total = totalCount != null ? totalCount : "0";
        String auth = authCount != null ? authCount : "0";
        String guest = guestCount != null ? guestCount : "0";

        Map<String, String> totalMap = Map.of("sessionType", "Общо", "count", total);
        Map<String, String> authMap = Map.of("sessionType", "Потребители", "count", auth);
        Map<String, String> guestMap = Map.of("sessionType", "Гост потребители", "count", guest);

        return List.of(totalMap, authMap, guestMap);
    }
}
