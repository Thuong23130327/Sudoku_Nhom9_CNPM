package com.sudoku.test;

import com.sudoku.model.GameMatch;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class SudokuGameMatchTest {
    private final String TEST_FILE_NAME = "history_test.json";
    private Gson gson;

    // JUnit 4 dùng @Before (thay vì @BeforeEach của JUnit 5)
    @Before
    public void setUp() {
        gson = new Gson();
        // Dọn dẹp môi trường trước khi chạy mỗi ca kiểm thử
        File file = new File(TEST_FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
    }

    // UT-01: Kiểm tra việc khởi tạo đối tượng GameMatch
    @Test
    public void testGameMatchConstructor() {
        GameMatch match = new GameMatch("06/06/2026 15:30:00", "trung bình", 120, "Chiến thắng");

        // Cú pháp Assert của JUnit 4 giữ nguyên giá trị mong đợi trước, thực tế sau
        assertEquals("06/06/2026 15:30:00", match.getDate());
        assertEquals("trung bình", match.getLevel());
        assertEquals(120, match.getDuration());
        assertEquals("Chiến thắng", match.getOutcome());
    }

    // UT-02: Kiểm tra chức năng ghi và đọc dữ liệu trận đấu ra file JSON
    @Test
    public void testSaveAndReadHistoryJson() throws Exception {
        List<GameMatch> testList = new ArrayList<GameMatch>();
        testList.add(new GameMatch("06/06/2026 15:30:00", "dễ", 90, "Chiến thắng"));
        testList.add(new GameMatch("06/06/2026 16:00:00", "asian", 250, "Thua (Bỏ cuộc)"));

        // Ghi dữ liệu test vào file
        FileWriter writer = null;
        try {
            writer = new FileWriter(TEST_FILE_NAME);
            gson.toJson(testList, writer);
        } finally {
            if (writer != null) writer.close();
        }

        // Thẩm định file có tồn tại không
        File file = new File(TEST_FILE_NAME);
        assertTrue("File dữ liệu lịch sử phải được tạo ra thành công.", file.exists());

        // Đọc lại từ file test
        List<GameMatch> readList;
        FileReader reader = null;
        try {
            reader = new FileReader(TEST_FILE_NAME);
            java.lang.reflect.Type listType = new TypeToken<ArrayList<GameMatch>>(){}.getType();
            readList = gson.fromJson(reader, listType);
        } finally {
            if (reader != null) reader.close();
        }

        // Thẩm định dữ liệu đầu ra khớp đầu vào
        assertNotNull(readList);
        assertEquals(2, readList.size());
        assertEquals("dễ", readList.get(0).getLevel());
        assertEquals("Thua (Bỏ cuộc)", readList.get(1).getOutcome());
    }
}
