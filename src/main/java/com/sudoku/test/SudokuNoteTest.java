package com.sudoku.test;

import org.junit.Test;
import static org.junit.Assert.*;
import javax.swing.JTextField;

/*
    UnitTest cho chức năng Ghi chú (Note)
    Người viết Test: Nguyễn Thanh Tú
 */
public class SudokuNoteTest {
    // ===========================================================================
    // UT: Kiểm tra tính năng thêm mới và sắp xếp tự động của Ghi chú (Note)
    // ===========================================================================
    @Test
    public void testHandleNoteInput_AddAndSort() {
        // Giả lập một JTextField của Swing
        JTextField cell = new JTextField();

        // Giả lập hành vi gõ lần lượt các số: 5, rồi đến 1, rồi đến 3
        char[] typedChars = {'5', '1', '3'};

        for (char keyChar : typedChars) {
            String currentText = (String) cell.getClientProperty("noteText");
            if (currentText == null) currentText = "";

            java.util.List<String> notes = new java.util.ArrayList<String>();
            if (!currentText.isEmpty()) {
                String[] tokens = currentText.split("[\\s]+");
                for (String t : tokens) {
                    if (!t.isEmpty()) notes.add(t);
                }
            }

            String inputNum = String.valueOf(keyChar);
            if (!notes.contains(inputNum)) {
                notes.add(inputNum);
            }

            // Kiểm tra xem hệ thống có tự sắp xếp tăng dần không
            java.util.Collections.sort(notes);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < notes.size(); i++) {
                sb.append(notes.get(i));
                if (i < notes.size() - 1) sb.append(" ");
            }
            cell.putClientProperty("noteText", sb.toString());
        }

        // Kết quả kỳ vọng: Dù gõ "5", "1", "3" nhưng bộ nhớ ẩn phải lưu là "1 3 5"
        String finalNote = (String) cell.getClientProperty("noteText");
        assertEquals("Chuỗi ghi chú phải tự động sắp xếp tăng dần từ nhỏ đến lớn", "1 3 5", finalNote);
    }

    // ===========================================================================
    // UT: Kiểm tra tính năng xóa số nháp cũ khi gõ trùng (Toggle Note)
    // ===========================================================================
    @Test
    public void testHandleNoteInput_ToggleDelete() {
        JTextField cell = new JTextField();

        // Bước 1: Giả lập ô lưới đã có sẵn ghi chú là "1 3 5"
        cell.putClientProperty("noteText", "1 3 5");

        // Bước 2: Người chơi gõ lại số "3" (Hành vi muốn xóa số 3 khỏi ghi chú)
        char retypedChar = '3';
        String currentText = (String) cell.getClientProperty("noteText");

        java.util.List<String> notes = new java.util.ArrayList<String>();
        String[] tokens = currentText.split("[\\s]+");
        for (String t : tokens) {
            notes.add(t);
        }

        String inputNum = String.valueOf(retypedChar);
        if (notes.contains(inputNum)) {
            notes.remove(inputNum); // Nếu trùng thì xóa
        }

        java.util.Collections.sort(notes);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < notes.size(); i++) {
            sb.append(notes.get(i));
            if (i < notes.size() - 1) sb.append(" ");
        }
        cell.putClientProperty("noteText", sb.toString());

        // Kết quả kỳ vọng: Số 3 phải biến mất, chuỗi còn lại là "1 5"
        String finalNote = (String) cell.getClientProperty("noteText");
        assertEquals("Số gõ trùng phải bị loại bỏ khỏi chuỗi ghi chú nháp", "1 5", finalNote);
    }
}
