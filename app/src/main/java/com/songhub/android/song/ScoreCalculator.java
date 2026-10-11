package com.songhub.android.song;

import com.songhub.android.storage.ScoreSettings;
import java.util.Random;

/* JADX INFO: loaded from: classes3.dex */
public class ScoreCalculator {
    private static final Random random = new Random();

    public static String getMessageForScore(int i) {
        if (i <= 60) {
            return "😢 Please try again!";
        }
        if (i <= 74) {
            return "💪 Practice makes perfect!";
        }
        if (i <= 80) {
            return "📈 Needs more improvement!";
        }
        if (i <= 90) {
            return "👍 Good voice!";
        }
        if (i <= 99) {
            return "🎤 Great singer!";
        }
        return i == 100 ? "⭐ You are a super star!" : "";
    }

    public static int generateRandomScore(ScoreSettings.Difficulty difficulty) {
        double dNextDouble = random.nextDouble();
        if (difficulty == ScoreSettings.Difficulty.EASY) {
            if (dNextDouble < 0.2d) {
                return 100;
            }
            if (dNextDouble < 0.7d) {
                return randomInt(90, 99);
            }
            if (dNextDouble < 0.9d) {
                return randomInt(80, 89);
            }
            return randomInt(70, 79);
        }
        if (difficulty == ScoreSettings.Difficulty.MEDIUM) {
            if (dNextDouble < 0.1d) {
                return 100;
            }
            if (dNextDouble < 0.45d) {
                return randomInt(90, 99);
            }
            if (dNextDouble < 0.8d) {
                return randomInt(80, 89);
            }
            if (dNextDouble < 0.92d) {
                return randomInt(70, 79);
            }
            return randomInt(60, 69);
        }
        if (difficulty != ScoreSettings.Difficulty.HARD) {
            return randomInt(60, 80);
        }
        if (dNextDouble < 0.05d) {
            return 100;
        }
        if (dNextDouble < 0.25d) {
            return randomInt(90, 99);
        }
        if (dNextDouble < 0.6d) {
            return randomInt(80, 89);
        }
        if (dNextDouble < 0.85d) {
            return randomInt(70, 79);
        }
        return randomInt(55, 69);
    }

    public static int computeMicScore(ScoreSettings.Difficulty difficulty) {
        int iGenerateRandomScore = generateRandomScore(difficulty);
        if (iGenerateRandomScore == 100) {
            if (random.nextBoolean()) {
                return 100;
            }
            return randomInt(97, 99);
        }
        return Math.min(100, Math.max(50, iGenerateRandomScore));
    }

    private static int randomInt(int i, int i2) {
        return random.nextInt((i2 - i) + 1) + i;
    }
}
