package main.java.com.sudoku.controller;

/**
 * [UC-05] Quản lý trạng thái game tập trung
 * UR-5.1 → startGame() khởi động trạng thái PLAYING
 * UR-5.2 → pauseGame() chuyển sang PAUSED
 * UR-5.3 → resumeGame() chuyển về PLAYING
 * UR-5.4 → recordMistake() đếm lỗi, chống đếm trùng
 * UR-5.5 → isGameLost(), setWon(), setLost()
 */
public class GameController {

    public enum GameState {
        IDLE, PLAYING, PAUSED, WON, LOST
    }

    private GameState currentState = GameState.IDLE;
    private int maxMistakes;
    private int mistakeCount;

    // UR-5.4: Lưu giá trị sai cuối cùng của mỗi ô để tránh đếm trùng
    // Index = row*9 + col, Value = số sai đã bị tính lỗi rồi
    private int[] lastMistakeValuePerCell = new int[81];

    public GameController(int maxMistakes) {
        this.maxMistakes = maxMistakes;
    }

    // ----------------------------------------------------------
    // UR-5.1: Bắt đầu ván mới → PLAYING
    // ----------------------------------------------------------
    public void startGame() {
        mistakeCount = 0;
        for (int i = 0; i < 81; i++) lastMistakeValuePerCell[i] = 0;
        currentState = GameState.PLAYING;
    }

    // ----------------------------------------------------------
    // UR-5.2: Tạm dừng → PAUSED
    // ----------------------------------------------------------
    public boolean pauseGame() {
        if (currentState == GameState.PLAYING) {
            currentState = GameState.PAUSED;
            return true;
        }
        return false;
    }

    // ----------------------------------------------------------
    // UR-5.3: Tiếp tục → PLAYING
    // ----------------------------------------------------------
    public boolean resumeGame() {
        if (currentState == GameState.PAUSED) {
            currentState = GameState.PLAYING;
            return true;
        }
        return false;
    }

    // ----------------------------------------------------------
    // UR-5.4: Ghi nhận lỗi sai — chống đếm trùng
    // Trả về true nếu đây là lỗi MỚI (cần update UI)
    // ----------------------------------------------------------
    public boolean recordMistake(int row, int col, int value) {
        if (currentState != GameState.PLAYING) return false;
        int idx = row * 9 + col;
        if (value == 0) {
            // Người chơi xóa ô → reset để lần sau nhập sai mới tính lỗi
            lastMistakeValuePerCell[idx] = 0;
            return false;
        }
        // Nếu cùng giá trị sai đã bị tính rồi → không tính thêm
        if (lastMistakeValuePerCell[idx] == value) return false;
        lastMistakeValuePerCell[idx] = value;
        mistakeCount++;
        return true;
    }

    // ----------------------------------------------------------
    // UR-5.5: Kiểm tra thua / thắng
    // ----------------------------------------------------------
    public boolean isGameLost()  { return mistakeCount >= maxMistakes; }
    public void setWon()         { currentState = GameState.WON; }
    public void setLost()        { currentState = GameState.LOST; }

    // Getters
    public GameState getCurrentState() { return currentState; }
    public int getMistakeCount()       { return mistakeCount; }
    public int getMaxMistakes()        { return maxMistakes; }
    public boolean isPlaying()         { return currentState == GameState.PLAYING; }
    public boolean isPaused()          { return currentState == GameState.PAUSED; }
    public boolean isGameOver()        { return currentState == GameState.WON || currentState == GameState.LOST; }
}