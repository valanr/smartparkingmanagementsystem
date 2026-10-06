package com.example.smartparkingmanagamentsystem;

import android.graphics.Bitmap;
import android.graphics.Color;

public class QrGenerator {

    public static Bitmap generateQrBitmap(String content, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        int margin = 16;
        int activeWidth = width - (2 * margin);
        int activeHeight = height - (2 * margin);

        // Simple pattern rendering
        int hash = content.hashCode();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x < margin || x >= width - margin || y < margin || y >= height - margin) {
                    bitmap.setPixel(x, y, Color.WHITE);
                    continue;
                }

                int relX = (x - margin) * 21 / activeWidth;
                int relY = (y - margin) * 21 / activeHeight;

                // Corner position detection patterns
                boolean isTopLeftPattern = (relX < 7 && relY < 7);
                boolean isTopRightPattern = (relX >= 14 && relY < 7);
                boolean isBottomLeftPattern = (relX < 7 && relY >= 14);

                if (isTopLeftPattern) {
                    bitmap.setPixel(x, y, isFinderPattern(relX, relY) ? Color.BLACK : Color.WHITE);
                } else if (isTopRightPattern) {
                    bitmap.setPixel(x, y, isFinderPattern(relX - 14, relY) ? Color.BLACK : Color.WHITE);
                } else if (isBottomLeftPattern) {
                    bitmap.setPixel(x, y, isFinderPattern(relX, relY - 14) ? Color.BLACK : Color.WHITE);
                } else {
                    boolean bit = ((relX * 31 + relY * 17 + hash) % 3) == 0;
                    bitmap.setPixel(x, y, bit ? Color.BLACK : Color.WHITE);
                }
            }
        }
        return bitmap;
    }

    private static boolean isFinderPattern(int x, int y) {
        if (x == 0 || x == 6 || y == 0 || y == 6) return true;
        if (x >= 2 && x <= 4 && y >= 2 && y <= 4) return true;
        return false;
    }
}
