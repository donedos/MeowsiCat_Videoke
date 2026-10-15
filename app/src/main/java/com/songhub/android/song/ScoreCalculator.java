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

    /** Legacy simulated mic score — used as a fallback when no microphone audio was captured. */
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

    /**
     * Scores a performance from real microphone capture statistics.
     *
     * @param voiceActivityRatio   fraction of frames above the ambient noise floor
     * @param loudnessConsistency  0..1 steadiness of volume while singing
     * @param avgLevelDb           average level of the active frames (dBFS)
     */
    public static int computeMicScore(double voiceActivityRatio,
                                      double loudnessConsistency,
                                      double avgLevelDb,
                                      ScoreSettings.Difficulty difficulty) {
        if (voiceActivityRatio <= 0.0) {
            // Nothing above the noise floor was picked up.
            return randomInt(35, 50);
        }
        // Voice activity: triangular preference peaking at 0.65 — sustained singing
        // with natural rests scores highest; constant blare and silence both score low.
        double activityScore;
        if (voiceActivityRatio <= 0.65d) {
            activityScore = Math.max(0.0d, voiceActivityRatio / 0.65d);
        } else {
            activityScore = Math.max(0.0d, 1.0d - (voiceActivityRatio - 0.65d) / 0.35d);
        }
        // Level: comfortable singing band rewards, clipping/blaring and whispering penalize.
        double levelScore;
        if (avgLevelDb < -35.0d) {
            levelScore = 0.2d;
        } else if (avgLevelDb < -25.0d) {
            levelScore = 0.5d;
        } else if (avgLevelDb < -6.0d) {
            levelScore = 1.0d;
        } else if (avgLevelDb < -2.0d) {
            levelScore = 0.6d;
        } else {
            levelScore = 0.3d;
        }
        double base = 35.0d
                + 40.0d * activityScore
                + 15.0d * Math.max(0.0d, Math.min(1.0d, loudnessConsistency))
                + 10.0d * levelScore;
        double multiplier;
        if (difficulty == ScoreSettings.Difficulty.EASY) {
            multiplier = 1.08d;
        } else if (difficulty == ScoreSettings.Difficulty.HARD) {
            multiplier = 0.94d;
        } else {
            multiplier = 1.0d;
        }
        int score = (int) Math.round(base * multiplier) + randomInt(-2, 2);
        return Math.min(100, Math.max(35, score));
    }

    private static int randomInt(int i, int i2) {
        return random.nextInt((i2 - i) + 1) + i;
    }
}
