package com.songhub.android;

import com.songhub.android.database.MegidxDatabase;
import com.songhub.android.database.MegSong;
import java.io.File;
import java.io.RandomAccessFile;
import org.junit.Test;

public class MappingDumpTest {
    @Test
    public void dumpMapping() throws Exception {
        String root = "/Volumes/xSSD/Projects/SongHub/SongHub FILES_Latest/SongHub";
        MegidxDatabase db = MegidxDatabase.load(new File(root, "idx/shubidx").getAbsolutePath());
        System.out.println("DBG songs=" + db.songs.size() + " legacy=" + db.isLegacyHeader);

        RandomAccessFile raf = new RandomAccessFile(new File(root, "idx/shubblob"), "r");
        long size = raf.length();
        java.util.ArrayList<Long> list = new java.util.ArrayList<>();
        byte[] hdr = new byte[4];
        for (long i = 0; i + 4 <= size; i += 2048) {
            raf.seek(i);
            raf.readFully(hdr);
            if (hdr[0] == 'M' && hdr[1] == 'K' && hdr[2] == 'M' && hdr[3] == 'D') {
                list.add(i);
            }
        }
        System.out.println("DBG blob_records=" + list.size());
        System.out.println("DBG first_records=" + list.subList(0, Math.min(15, list.size())));
        System.out.println("DBG last_records=" + list.subList(Math.max(0, list.size() - 5), list.size()));

        for (int i = 0; i < Math.min(15, db.songs.size()); i++) {
            MegSong s = db.songs.get(i);
            System.out.println("DBG song[" + i + "] code=" + s.code + " off=" + s.midiByteOffset() + " len=" + s.midiByteLength());
        }

        for (int i = 0; i < db.songs.size(); i++) {
            MegSong s = db.songs.get(i);
            if (s.midiByteOffset() < 200000) {
                System.out.println("DBG lowoff song[" + i + "] code=" + s.code + " off=" + s.midiByteOffset() + " len=" + s.midiByteLength());
            }
        }
        raf.close();
    }
}