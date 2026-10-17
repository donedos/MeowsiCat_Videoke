package com.meowsicat.android.database;

import com.meowsicat.android.parser.LanguageEncoding;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MegidxDatabase {
    public static final int ALIGNMENT_SIZE = 2048;
    private static final String DEFAULT_ENCODING = "ISO-8859-1";
    private static final int EXTENDED_FIXED_SIZE = 60;
    private static final int SINGER_IDX_PACKED_SENTINEL = 65535;
    private static final int SIZE_SENTINEL = 65534;
    private static final Map<String, String> stringPool = new HashMap();
    public boolean isLegacyHeader;
    public long pIndexStart;
    public long pLanguageType;
    public long pNumIndexPos;
    public long pSingerCode;
    public long pSingerPos;
    public long pSingerType;
    public long pSingerWord;
    public long pSongInfo;
    public long pSongPos;
    public long pSongType;
    public long pSongWord;
    public int singerNameNumFlag;
    public long singerNum;
    public long singerWordNum;
    public int songNameNumFlag;
    public long songNum;
    public long songWordNum;
    public List<MegSong> songs = new ArrayList();
    public List<MegSinger> singers = new ArrayList();
    public List<String> languages = new ArrayList();
    public Map<Integer, String> styleMap = new LinkedHashMap();

    private static String intern(String str) {
        if (str == null) {
            return null;
        }
        Map<String, String> map = stringPool;
        String str2 = map.get(str);
        if (str2 != null) {
            return str2;
        }
        map.put(str, str);
        return str;
    }

    public static MegidxDatabase load(String str) throws IOException {
        MegidxDatabase megidxDatabase = new MegidxDatabase();
        RandomAccessFile randomAccessFile = new RandomAccessFile(str, "r");
        try {
            FileChannel channel = randomAccessFile.getChannel();
            try {
                long size = channel.size();
                if (size < 56) {
                    throw new IOException("File too short");
                }
                MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_ONLY, 0L, size);
                map.order(ByteOrder.LITTLE_ENDIAN);
                megidxDatabase.parse(map);
                if (channel != null) {
                    channel.close();
                }
                randomAccessFile.close();
                return megidxDatabase;
            } catch (Throwable th) {
                if (channel != null) {
                    try {
                        channel.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        } catch (Throwable th3) {
            try {
                randomAccessFile.close();
            } catch (Throwable th4) {
                th3.addSuppressed(th4);
            }
            throw th3;
        }
    }

    private static boolean isValidIndexStart(ByteBuffer byteBuffer, long j, int i) {
        if (j <= 0 || j >= i) {
            return false;
        }
        int i2 = byteBuffer.get((int) j) & 255;
        return i2 == 1 || i2 == 2 || i2 == 3 || i2 == 4 || i2 == 5 || i2 == 6 || i2 == 7 || i2 == 9;
    }

    private void parse(ByteBuffer byteBuffer) {
        LinkedHashMap linkedHashMap;
        char c;
        int i;
        int i2;
        int i3;
        int iLimit = byteBuffer.limit();
        long j = iLimit >= 28 ? ((long) byteBuffer.getInt(24)) & 4294967295L : 0L;
        long j2 = iLimit >= 56 ? ((long) byteBuffer.getInt(52)) & 4294967295L : 0L;
        long j3 = iLimit >= 60 ? 4294967295L & ((long) byteBuffer.getInt(56)) : 0L;
        boolean z = ((j2 > j ? 1 : (j2 == j ? 0 : -1)) > 0 && isValidIndexStart(byteBuffer, j2, iLimit)) && !((j3 > j ? 1 : (j3 == j ? 0 : -1)) > 0 && isValidIndexStart(byteBuffer, j3, iLimit));
        this.isLegacyHeader = z;
        if (z) {
            parseLegacyHeader(byteBuffer);
        } else if (iLimit < 60) {
            return;
        } else {
            parseExtendedHeader(byteBuffer);
        }
        long j4 = this.pIndexStart;
        if (j4 <= 0 || j4 >= iLimit) {
            return;
        }
        int i4 = (int) j4;
        while (i4 < iLimit) {
            int i5 = byteBuffer.get(i4) & 255;
            if (i5 != 0) {
                int i6 = i4 + 2;
                if (i6 > iLimit || (i2 = byteBuffer.get(i4 + 1) & 255) < 2 || (i3 = i4 + i2) > iLimit) {
                    break;
                }
                if (i5 == 7) {
                    this.languages.add(intern(decodeWithFallback(byteBuffer, i4 + 4, i2 - 4, DEFAULT_ENCODING)));
                } else if (i5 == 4) {
                    this.styleMap.put(Integer.valueOf(byteBuffer.get(i6) & 255), intern(decodeWithFallback(byteBuffer, i4 + 5, i2 - 5, DEFAULT_ENCODING)));
                }
                i4 = i3;
            } else {
                i4++;
            }
        }
        ArrayList arrayList = new ArrayList();
        Iterator<String> it = this.languages.iterator();
        while (it.hasNext()) {
            arrayList.add(LanguageEncoding.getEncodingForLanguage(it.next()));
        }
        LinkedHashMap linkedHashMap2 = new LinkedHashMap();
        int i7 = (int) this.pIndexStart;
        while (i7 < iLimit) {
            int i8 = byteBuffer.get(i7) & 255;
            if (i8 == 0) {
                i7++;
            } else if (i8 == 9) {
                i7 += 4;
            } else {
                int i9 = byteBuffer.get(i7 + 1) & 255;
                if (i9 < 2 || (i = i7 + i9) > iLimit) {
                    break;
                }
                if (i8 == 5) {
                    MegSinger megSinger = new MegSinger();
                    short s = byteBuffer.getShort(i7 + 2);
                    megSinger.number = ((65535 & s) >> 12) & 15;
                    megSinger.count = s & 4095;
                    megSinger.style = byteBuffer.get(i7 + 4) & 255;
                    megSinger.nameBytes = extractBytes(byteBuffer, i7 + 5, i9 - 5);
                    this.singers.add(megSinger);
                } else if (i8 == 1) {
                    MegSong songBlock = parseSongBlock(byteBuffer, i7, i9, arrayList);
                    this.songs.add(songBlock);
                    linkedHashMap2.put(Integer.valueOf(songBlock.singerIdx), (songBlock.langId < 0 || songBlock.langId >= arrayList.size()) ? DEFAULT_ENCODING : arrayList.get(songBlock.langId));
                }
                i7 = i;
            }
        }
        ArrayList arrayList2 = new ArrayList();
        int i10 = (int) this.pIndexStart;
        while (i10 < iLimit) {
            if ((byteBuffer.get(i10) & 255) == 9) {
                linkedHashMap = linkedHashMap2;
                long j5 = (((long) (byteBuffer.get(i10 + 2) & 255)) << 16) | (((long) (byteBuffer.get(i10 + 1) & 255)) << 8) | 9 | (((long) (byteBuffer.get(i10 + 3) & 255)) << 24);
                arrayList2.add(Integer.valueOf((int) (((j5 >> 16) & 65535) | ((65280 & j5) << 8))));
                i10 += 4;
                c = 2;
            } else {
                linkedHashMap = linkedHashMap2;
                int i11 = i10 + 1;
                int i12 = byteBuffer.get(i11) & 255;
                c = 2;
                i10 = i12 < 2 ? i11 : i10 + i12;
            }
            linkedHashMap2 = linkedHashMap;
        }
        LinkedHashMap linkedHashMap3 = linkedHashMap2;
        for (int i13 = 0; i13 < Math.min(this.songs.size(), arrayList2.size()); i13++) {
            this.songs.get(i13).code = ((Integer) arrayList2.get(i13)).intValue();
        }
        int i14 = 0;
        while (i14 < this.singers.size()) {
            MegSinger megSinger2 = this.singers.get(i14);
            LinkedHashMap linkedHashMap4 = linkedHashMap3;
            String str = (String) linkedHashMap4.get(Integer.valueOf(i14));
            if (str == null) {
                if (arrayList.isEmpty()) {
                    str = DEFAULT_ENCODING;
                } else {
                    str = (String) arrayList.get(0);
                }
            }
            megSinger2.name = decodeBytes(megSinger2.nameBytes, str);
            megSinger2.nameBytes = null;
            i14++;
            linkedHashMap3 = linkedHashMap4;
        }
    }

    private void parseLegacyHeader(ByteBuffer byteBuffer) {
        this.pSingerType = ((long) byteBuffer.getInt(0)) & 4294967295L;
        this.pLanguageType = ((long) byteBuffer.getInt(4)) & 4294967295L;
        this.pSongType = ((long) byteBuffer.getInt(8)) & 4294967295L;
        this.pSingerCode = ((long) byteBuffer.getInt(12)) & 4294967295L;
        this.pSongInfo = ((long) byteBuffer.getInt(16)) & 4294967295L;
        this.pSongWord = ((long) byteBuffer.getInt(20)) & 4294967295L;
        this.pSingerWord = ((long) byteBuffer.getInt(24)) & 4294967295L;
        this.singerNum = byteBuffer.getShort(28) & 65535;
        this.songNum = byteBuffer.getShort(30) & 65535;
        this.songWordNum = byteBuffer.getShort(32) & 65535;
        this.singerWordNum = byteBuffer.getShort(34) & 65535;
        this.songNameNumFlag = byteBuffer.getShort(36) & 65535;
        this.singerNameNumFlag = byteBuffer.getShort(38) & 65535;
        this.pSingerPos = ((long) byteBuffer.getInt(40)) & 4294967295L;
        this.pSongPos = ((long) byteBuffer.getInt(44)) & 4294967295L;
        this.pNumIndexPos = ((long) byteBuffer.getInt(48)) & 4294967295L;
        this.pIndexStart = ((long) byteBuffer.getInt(52)) & 4294967295L;
    }

    private void parseExtendedHeader(ByteBuffer byteBuffer) {
        this.pSingerType = ((long) byteBuffer.getInt(0)) & 4294967295L;
        this.pLanguageType = ((long) byteBuffer.getInt(4)) & 4294967295L;
        this.pSongType = ((long) byteBuffer.getInt(8)) & 4294967295L;
        this.pSingerCode = ((long) byteBuffer.getInt(12)) & 4294967295L;
        this.pSongInfo = ((long) byteBuffer.getInt(16)) & 4294967295L;
        this.pSongWord = ((long) byteBuffer.getInt(20)) & 4294967295L;
        this.pSingerWord = ((long) byteBuffer.getInt(24)) & 4294967295L;
        this.singerNum = ((long) byteBuffer.getInt(28)) & 4294967295L;
        this.songNum = ((long) byteBuffer.getInt(32)) & 4294967295L;
        this.songWordNum = byteBuffer.getShort(36) & 65535;
        this.singerWordNum = byteBuffer.getShort(38) & 65535;
        this.songNameNumFlag = byteBuffer.getShort(40) & 65535;
        this.singerNameNumFlag = byteBuffer.getShort(42) & 65535;
        this.pSingerPos = ((long) byteBuffer.getInt(44)) & 4294967295L;
        this.pSongPos = ((long) byteBuffer.getInt(48)) & 4294967295L;
        this.pNumIndexPos = ((long) byteBuffer.getInt(52)) & 4294967295L;
        this.pIndexStart = ((long) byteBuffer.getInt(56)) & 4294967295L;
    }

    private MegSong parseSongBlock(ByteBuffer byteBuffer, int i, int i2, List<String> list) {
        int i3;
        int i4;
        MegSong megSong = new MegSong();
        megSong.style = byteBuffer.get(i + 2) & 255;
        byte b = byteBuffer.get(i + 3);
        megSong.langType = (b & 128) != 0 ? 2 : 1;
        megSong.langId = b & 127;
        short s = byteBuffer.getShort(i + 4);
        int i5 = s & 65535;
        megSong.singerIdx = s & 16383;
        boolean z = i5 == 65535;
        int i6 = byteBuffer.getShort(i + 6) & 65535;
        megSong.midiPosSectors = byteBuffer.getInt(i + 8);
        int i7 = i + 12;
        if (megSong.langType == 2) {
            long j = byteBuffer.getInt(i7);
            megSong.mp3Vol = (int) (((4294967295L & j) >> 24) & 15);
            megSong.mp3PosSectors = (int) (j & 16777215);
            i4 = 65535 & byteBuffer.getShort(i + 16);
            megSong.isMp3 = true;
            i7 = i + 18;
            i3 = 16;
        } else {
            i3 = 10;
            i4 = 0;
        }
        int i8 = (i2 - 2) - i3;
        boolean z2 = i6 == 65534;
        boolean z3 = i4 == 65534;
        int i9 = z2 ? 4 : 0;
        if (z3) {
            i9 += 4;
        }
        if (z) {
            i9 += 4;
        }
        int iMax = Math.max(0, i8 - i9);
        megSong.name = decodeWithFallback(byteBuffer, i7, iMax, (megSong.langId < 0 || megSong.langId >= list.size()) ? DEFAULT_ENCODING : list.get(megSong.langId));
        int i10 = i7 + iMax;
        if (z) {
            megSong.singerIdx = byteBuffer.getInt(i10);
            i10 += 4;
        }
        if (z2) {
            i6 = byteBuffer.getInt(i10);
        }
        megSong.midiSizeSectors = i6;
        if (z2) {
            i10 += 4;
        }
        if (megSong.isMp3) {
            if (z3) {
                i4 = byteBuffer.getInt(i10);
            }
            megSong.mp3SizeSectors = i4;
        }
        return megSong;
    }

    private static byte[] extractBytes(ByteBuffer byteBuffer, int i, int i2) {
        if (i2 <= 0 || i < 0 || i + i2 > byteBuffer.limit()) {
            return new byte[0];
        }
        byte[] bArr = new byte[i2];
        int iPosition = byteBuffer.position();
        byteBuffer.position(i);
        byteBuffer.get(bArr);
        byteBuffer.position(iPosition);
        return bArr;
    }

    private static String decodeBytes(byte[] bArr, String str) {
        if (bArr == null || bArr.length == 0) {
            return "";
        }
        try {
            return new String(bArr, str).replace("\u0000", "").trim();
        } catch (Exception unused) {
            return new String(bArr).replace("\u0000", "").trim();
        }
    }

    private static String decodeWithFallback(ByteBuffer byteBuffer, int i, int i2, String str) {
        return decodeBytes(extractBytes(byteBuffer, i, i2), str);
    }
}
