package com.ito.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.ito.order.exception.DuplicateOrderException;
import com.ito.order.model.Order;
import com.ito.order.service.OrderService;

/**
 * 주문 서비스 테스트 (제공 파일 — 수정 금지)
 *
 *   1. 대량 주문 처리 (100,000건 / 10초) + 정확성 · 중복 검증
 *   2. 최소 주문 건수 (DP)
 */
class OrderServiceTest {

    private OrderService orderService;

    /** 테스트 전용 저장 파일 (실제 주문 파일과 분리) */
    private static final String TEST_STORAGE = "data/test_orders.txt";

    /** 대량 처리 입력 파일 */
    private static final String BULK_FILE = "data/input/orders_5_bulk.txt";

    /** 대량 처리 테스트 주문 건수 */
    private static final int BULK_COUNT = 100000;

    /**
     * 대량 처리 테스트용 파일을 전체 테스트 실행 전 한 번만 생성합니다.
     * BULK00001 ~ BULK100000 까지 고유 주문을 기록합니다.
     * (첫 줄 형식: BULK00001|상품2|2|20.0)
     */
    @BeforeAll
    static void generateBulkFile() throws IOException {
        new File("data/input").mkdirs();
        // 서비스가 UTF-8 로 읽으므로 입력 파일도 UTF-8 로 기록한다 (한글 깨짐 방지)
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(BULK_FILE), StandardCharsets.UTF_8))) {
            for (int i = 1; i <= BULK_COUNT; i++) {
                bw.write(String.format("BULK%05d|상품%d|%d|%.1f",
                        i, (i % 50) + 1, (i % 9) + 1, ((i % 100) + 1) * 10.0));
                bw.newLine();
            }
        }
    }

    @BeforeEach
    void setUp() {
        // 매 테스트 전에 저장 파일을 초기화합니다
        orderService = new OrderService(TEST_STORAGE);
        orderService.clearStorage();
    }

    // ==========================================================================
    // 1. 대량 주문 처리 (성능 + 정확성 + 중복)
    //    - 100,000건을 제한 시간(10초) 내에 처리해야 합니다.
    //      주문 1건마다 파일을 열고 닫는 방식으로는 통과가 어렵습니다.
    //      BufferedReader/BufferedWriter 스트리밍 + HashSet 중복 확인을 사용하세요.
    //    - 개수뿐 아니라 각 필드 값이 정확히 파싱·저장·복원되어야 합니다.
    //    - 이미 저장된 주문을 다시 처리하면 DuplicateOrderException 이 발생해야 합니다.
    // ==========================================================================

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    @DisplayName("[1-1] 대량 파일 처리 — 100,000건을 10초 이내에 처리하고, 반환 값이 입력과 일치해야 한다")
    void processBulkFile_completesWithinTimeout() {
        List<Order> result = orderService.processOrderFile(BULK_FILE);

        assertEquals(BULK_COUNT, result.size());

        // 반환된 첫 주문이 입력 파일 첫 줄(BULK00001|상품2|2|20.0)과 정확히 일치하는가 (파싱 정확성)
        Order first = result.get(0);
        assertEquals("BULK00001", first.getOrderId());
        assertEquals("상품2", first.getProductName());
        assertEquals(2, first.getQuantity());
        assertEquals(20.0, first.getPrice());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    @DisplayName("[1-2] 대량 저장 후 전체 조회 — 100,000건이 모두 저장되고, 값이 그대로 복원되어야 한다")
    void processBulkFile_allOrdersRetrievable() {
        orderService.processOrderFile(BULK_FILE);

        List<Order> all = orderService.findAllOrders();

        assertEquals(BULK_COUNT, all.size());

        // 저장 → 재조회 왕복 후에도 필드가 보존되는가 (저장/복원 정확성)
        Order first = all.get(0);
        assertEquals("BULK00001", first.getOrderId());
        assertEquals("상품2", first.getProductName());
        assertEquals(2, first.getQuantity());
        assertEquals(20.0, first.getPrice());
        assertEquals("PENDING", first.getStatus());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    @DisplayName("[1-3] 중복 주문 처리 — 이미 저장된 주문을 다시 처리하면 DuplicateOrderException 이 발생해야 한다")
    void processBulkFile_reprocess_throwsDuplicateOrderException() {
        orderService.processOrderFile(BULK_FILE);   // 1차: 100,000건 저장

        // 2차: 첫 줄(BULK00001)이 이미 존재 → 예외 발생
        assertThrows(DuplicateOrderException.class,
                () -> orderService.processOrderFile(BULK_FILE));
    }

    // ==========================================================================
    // 2. 최소 주문 건수 (DP)
    //    서로 다른 "묶음 수량"의 주문(Order)들을 몇 번이든 반복 주문해
    //    목표 수량을 "정확히" 채울 때 필요한 최소 주문 건수를 구합니다.
    //    무제한 사용 가능 · 불가능하면 -1 반환.
    //    무작정 큰 묶음부터 담으면 최소가 아닐 수 있어 DP 로 풀어야 합니다.
    // ==========================================================================

    /** 지정한 묶음 수량을 담은 Order 를 만든다 (조회용 필드는 테스트에 영향 없음). */
    private Order pack(int quantity) {
        return new Order("PACK", "묶음상품", quantity, 1000.0);
    }

    @Test
    @DisplayName("[2-1] 최소 주문 건수 — 큰 묶음부터 담으면 틀리는 케이스도 최소 건수를 구해야 한다")
    void minOrdersToFulfill_bigPackFirstFails_returnsOptimal() {
        // 묶음 수량 {1,3,4}, 목표 6 → 3+3 = 2건 (4+1+1=3 이 아님)
        List<Order> packs = List.of(pack(1), pack(3), pack(4));
        assertEquals(2, orderService.minOrdersToFulfill(packs, 6));
    }

    @Test
    @DisplayName("[2-2] 최소 주문 건수 — 목표 수량이 0이면 0건이다")
    void minOrdersToFulfill_zeroTarget_returnsZero() {
        List<Order> packs = List.of(pack(1), pack(3), pack(4));
        assertEquals(0, orderService.minOrdersToFulfill(packs, 0));
    }

    @Test
    @DisplayName("[2-3] 최소 주문 건수 — 정확히 채울 수 없으면 -1을 반환한다")
    void minOrdersToFulfill_impossible_returnsMinusOne() {
        // 묶음 수량 {3,5}, 목표 7 → 정확히 만들 수 없음
        List<Order> packs = List.of(pack(3), pack(5));
        assertEquals(-1, orderService.minOrdersToFulfill(packs, 7));
    }

    @Test
    @DisplayName("[2-4] 최소 주문 건수 — 단일 묶음으로 정확히 나눠지는 경우")
    void minOrdersToFulfill_singlePack_returnsCount() {
        // 묶음 수량 {2}, 목표 8 → 4건
        List<Order> packs = List.of(pack(2));
        assertEquals(4, orderService.minOrdersToFulfill(packs, 8));
    }
}
