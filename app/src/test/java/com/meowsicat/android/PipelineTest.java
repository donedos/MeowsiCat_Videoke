package com.meowsicat.android;

import com.meowsicat.android.database.MegidxDatabase;
import com.meowsicat.android.database.MegSinger;
import com.meowsicat.android.database.MegSong;
import com.meowsicat.android.model.LyricLine;
import com.meowsicat.android.parser.LineBuilder;
import com.meowsicat.android.parser.MidiParser;
import java.io.File;
import java.nio.file.Files;
import org.junit.Assume;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PipelineTest {
    private static final String ROOT = "/Volumes/xSSD/Projects/SongHub/SongHub FILES_Latest/SongHub";

    @Test
    public void parsesCatalogIndex() throws Exception {
        Assume.assumeTrue("catalog fixture not present", new File(ROOT + "/idx/shubidx").isFile());
        MegidxDatabase db = MegidxDatabase.load(ROOT + "/idx/shubidx");
        assertTrue("expected many songs, got " + db.songs.size(), db.songs.size() > 1000);
        assertFalse(db.singers.isEmpty());
        System.out.println("songs=" + db.songs.size()
                + " singers=" + db.singers.size()
                + " languages=" + db.languages.size()
                + " styles=" + db.styleMap.size()
                + " legacy=" + db.isLegacyHeader);
        for (int i = 0; i < 5 && i < db.songs.size(); i++) {
            MegSong s = db.songs.get(i);
            String singer = (s.singerIdx >= 0 && s.singerIdx < db.singers.size())
                    ? db.singers.get(s.singerIdx).name : "";
            System.out.println("  [" + i + "] code=" + s.code + " " + s.name + " / " + singer
                    + " langId=" + s.langId + " style=" + s.style
                    + " off=" + s.midiByteOffset() + " len=" + s.midiByteLength());
        }
        for (MegSinger sg : db.singers) {
            System.out.println("SINGER " + sg.number + " " + sg.name + " count=" + sg.count + " style=" + sg.style);
        }
    }

    @Test
    public void parsesUserMidiSong() throws Exception {
        File midi = new File(ROOT + "/songs/Malunggay Pandesal - Flo Rida (Low).mid");
        Assume.assumeTrue("user midi fixture not present", midi.exists());
        byte[] bytes = Files.readAllBytes(midi.toPath());
        MidiParser.ParseResult res = MidiParser.parse(bytes, midi.getName());
        assertNotNull(res);
        System.out.println("ticksPerBeat=" + res.ticksPerBeat
                + " lyricEvents=" + res.lyricEventsTicks.size()
                + " chordEvents=" + res.chordEventsTicks.size()
                + " countdownEvents=" + res.countdownEventsTicks.size()
                + " systemMode=" + res.systemMode
                + " metadata=" + res.metadata);
        for (MidiParser.TickMsText t : res.lyricEventsTicks) {
            System.out.println("  @" + t.tick + " ms=" + t.ms + " [" + t.text + "]");
        }
        java.util.List<LyricLine> lines = LineBuilder.build(res.lyricEventsTicks, "", res.ticksPerBeat);
        System.out.println("lyricLines=" + lines.size());
        for (LyricLine ll : lines) {
            System.out.println("  " + ll.startMs + "→" + ll.endMs + " tag=" + ll.speakerTag
                    + " frags=" + ll.fragments.size() + " " + ll.fragments);
        }
        assertFalse("expected at least one lyric line", lines.isEmpty());
    }
}