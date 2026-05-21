package com.ito.order.service;

import com.ito.order.exception.DuplicateOrderException;
import com.ito.order.exception.InvalidOrderException;
import com.ito.order.model.Order;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 주문 비즈니스 로직을 처리하는 서비스 클래스
 *
 * ※ 이 파일의 [구현 N] 메서드 3개만 완성하면 됩니다.
 *   "[제공] 수정 금지" 로 표시된 메서드는 이미 구현되어 있으니 그대로 사용하세요.
 *
 * 저장 파일 포맷 (storagePath):
 *   한 줄 = 주문 1건, 필드 구분자 "|"
 *   예) ORD001|사과|3|1500.0|PENDING|2024-06-01T09:30:00
 *
 * 입력 파일 포맷 (processOrderFile 인자):
 *   한 줄 = 주문 요청 1건, 필드 구분자 "|"
 *   예) ORD001|사과|3|1500.0
 */
public class OrderService {

    public static final String DEFAULT_STORAGE_PATH = "data/orders.txt";

    private final String storagePath;

    public OrderService() {
        this(DEFAULT_STORAGE_PATH);
    }

    /** [제공] 수정 금지 — 저장 파일의 상위 디렉토리·파일이 없으면 생성합니다. */
    public OrderService(String storagePath) {
        this.storagePath = storagePath;
        try {
            File file = new File(storagePath);
            File parent = file.getParentFile();
            if (parent != null) parent.mkdirs();
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("저장 파일 초기화 실패: " + storagePath, e);
        }
    }

    // =========================================================================
    // [구현 1] 입력 파일 처리
    // =========================================================================

    /**
     * 입력 TXT 파일을 읽어 주문을 처리합니다.
     * 흐름: 파일 읽기 → parseInputLine 파싱 → validateOrder 검증
     *       → 중복 ID 확인(loadExistingIds 로 미리 로드) → 저장 → 저장된 목록 반환
     *
     * ※ 대량(10만 건) 처리 테스트가 있습니다. 주문 1건마다 파일을 열고 닫으면
     *   제한 시간(10초) 내 통과가 어렵습니다. BufferedReader/BufferedWriter 로
     *   한 번에 읽고 쓰도록 구현하세요.
     *
     * @param inputFilePath 읽을 입력 TXT 파일 경로
     * @return 저장된 Order 목록
     * @throws InvalidOrderException   유효하지 않은 데이터가 있을 때
     * @throws DuplicateOrderException 중복 주문 ID 가 있을 때
     */
    public List<Order> processOrderFile(String inputFilePath) {
        // TODO: 구현하세요
        throw new UnsupportedOperationException("TODO: processOrderFile 을 구현하세요");
    }

    // =========================================================================
    // [구현 2] 전체 주문 조회
    // =========================================================================

    /**
     * 저장된 모든 주문을 반환합니다. (저장된 주문이 없으면 빈 리스트)
     * 저장 파일을 읽어 각 줄을 parseStoredLine 으로 복원하세요.
     */
    public List<Order> findAllOrders() {
        // TODO: 구현하세요
        throw new UnsupportedOperationException("TODO: findAllOrders 를 구현하세요");
    }

    // =========================================================================
    // [구현 3] 최소 주문 건수 (DP)
    // =========================================================================

    /**
     * 서로 다른 "묶음 수량"을 가진 주문 목록 packs 를 사용해
     * 목표 수량 targetQuantity 를 "정확히" 채울 때 필요한 최소 주문 건수를 반환합니다.
     *
     *  - 각 주문(묶음)은 몇 번이든 반복 주문할 수 있습니다. (무제한)
     *    각 묶음이 채우는 수량은 order.getQuantity() 입니다.
     *  - targetQuantity 를 정확히 채울 수 없으면 -1 을 반환합니다.
     *  - targetQuantity 가 0 이면 0 을 반환합니다.
     *
     * ※ 무작정 큰 묶음부터 담으면 최소가 아닐 수 있습니다.
     *    예) 묶음 수량 {1, 3, 4} 로 목표 6 을 채운다면
     *        4 + 1 + 1 = 3건
     *        3 + 3     = 2건  ← 최소(정답)
     *    그래서 모든 조합을 따지는 DP 로 풀어야 합니다.
     *    dp[a] = "수량 a 를 정확히 채우는 최소 주문 건수" 로 정의하고
     *    dp[0]=0 에서 시작해 dp[targetQuantity] 까지 채워 나가세요.
     *
     * @param packs          사용 가능한 묶음 주문 목록 (각 order.getQuantity() 가 묶음 수량)
     * @param targetQuantity 채워야 할 목표 수량 (0 이상)
     * @return 최소 주문 건수, 정확히 채울 수 없으면 -1
     */
    public int minOrdersToFulfill(List<Order> packs, int targetQuantity) {
        // TODO: 구현하세요
        throw new UnsupportedOperationException("TODO: minOrdersToFulfill 을 구현하세요");
    }

    // =========================================================================
    // 아래는 [제공] 수정 금지 — 이미 구현되어 있습니다. 그대로 사용하세요.
    // =========================================================================

    /** [제공] 입력 파일 한 줄(4필드: orderId|productName|quantity|price) → Order */
    private Order parseInputLine(String line) {
        String[] f = line.split("\\|", -1);
        if (f.length != 4) throw new InvalidOrderException("입력 형식 오류");
        try {
            return new Order(f[0].trim(), f[1].trim(),
                    Integer.parseInt(f[2].trim()), Double.parseDouble(f[3].trim()));
        } catch (NumberFormatException e) {
            throw new InvalidOrderException("숫자 형식 오류");
        }
    }

    /** [제공] 입력값 정합성 검사 (orderId·productName 빈값, quantity&lt;1, price&le;0 → 예외) */
    private void validateOrder(Order order) {
        if (order.getOrderId() == null || order.getOrderId().trim().isEmpty())
            throw new InvalidOrderException("주문 ID 오류");
        if (order.getProductName() == null || order.getProductName().trim().isEmpty())
            throw new InvalidOrderException("상품명 오류");
        if (order.getQuantity() < 1)
            throw new InvalidOrderException("수량 오류");
        if (order.getPrice() <= 0)
            throw new InvalidOrderException("가격 오류");
    }

    /** [제공] 저장 파일 한 줄(6필드) → Order 복원 */
    private Order parseStoredLine(String line) {
        String[] f = line.split("\\|", -1);
        if (f.length != 6) throw new InvalidOrderException("저장 형식 오류");
        try {
            Order order = new Order();
            order.setOrderId(f[0].trim());
            order.setProductName(f[1].trim());
            order.setQuantity(Integer.parseInt(f[2].trim()));
            order.setPrice(Double.parseDouble(f[3].trim()));
            order.setStatus(f[4].trim());
            order.setCreatedAt(f[5].trim());
            return order;
        } catch (NumberFormatException e) {
            throw new InvalidOrderException("저장 데이터 숫자 형식 오류");
        }
    }

    /** [제공] 저장 파일의 기존 orderId 전체를 Set 으로 로드 (중복 확인용) */
    private Set<String> loadExistingIds() {
        Set<String> ids = new HashSet<>();
        try {
            for (String line : Files.readAllLines(Path.of(storagePath))) {
                if (line == null || line.trim().isEmpty()) continue;
                ids.add(parseStoredLine(line).getOrderId());
            }
        } catch (IOException e) {
            throw new RuntimeException("저장 파일 읽기 실패", e);
        }
        return ids;
    }

    /** [제공] 저장 파일 내용 비우기 (테스트 초기화용) */
    public void clearStorage() {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(storagePath, false), StandardCharsets.UTF_8))) {
            // overwrite 모드로 열고 닫으면 내용이 비워짐
        } catch (IOException e) {
            throw new RuntimeException("저장 파일 초기화 실패", e);
        }
    }
}
