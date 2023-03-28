import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class BeatBox {
    private JPanel mainPanel, checkBoxesPanel;
    private JButton startButton, stopButton, tempoUpButton, tempoDownButton, clearButton;
    private final static JMenuBar menuBar = new JMenuBar();
    private Sequencer sequencer;
    private Sequence sequence;
    private Track track;
    private ArrayList<JCheckBox> checkBoxesList = new ArrayList<>();
    private final int[] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public BeatBox() {
        checkBoxesPanel.setLayout(new GridLayout(16, 16));
        createCheckBoxes();
        setUpMidi();

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        JMenuItem saveCompositionMenuItem = new JMenuItem("Save"), loadCompositionMenuItem = new JMenuItem("Load");
        fileMenu.add(saveCompositionMenuItem);
        fileMenu.add(loadCompositionMenuItem);

        saveCompositionMenuItem.addActionListener(e -> saveComposition());
        loadCompositionMenuItem.addActionListener(e -> loadComposition());
        startButton.addActionListener(e -> buildTrackAndStart());
        stopButton.addActionListener(e -> sequencer.stop());
        tempoUpButton.addActionListener(e -> sequencer.setTempoFactor((float)(sequencer.getTempoFactor() * 1.3)));
        tempoDownButton.addActionListener(e -> sequencer.setTempoFactor((float)(sequencer.getTempoFactor() * 0.97)));
        clearButton.addActionListener(e -> clearComposition());
    }

    private void createCheckBoxes() {
        for(int i = 0; i < 256; i++) {
            JCheckBox chBox = new JCheckBox();
            chBox.setSelected(false);
            checkBoxesList.add(chBox);
            checkBoxesPanel.add(chBox);
        }
    }

    private void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void clearComposition() {
        for (JCheckBox chBox : checkBoxesList)
            chBox.setSelected(false);
    }

    public void saveComposition() {
        JFileChooser fileChooser = new JFileChooser();
        int ret = fileChooser.showDialog(null, "Save");
        if(ret == JFileChooser.APPROVE_OPTION) {
            try(FileOutputStream fOut = new FileOutputStream(fileChooser.getSelectedFile().getAbsolutePath());
                ObjectOutputStream oOut = new ObjectOutputStream(fOut)) {
                for(JCheckBox chBox : checkBoxesList) {
                    oOut.writeObject(chBox.isSelected());
                }
            }  catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadComposition() {
        JFileChooser fileChooser = new JFileChooser();
        int ret = fileChooser.showDialog(null, "Open file");
        if (ret == JFileChooser.APPROVE_OPTION) {
            try(FileInputStream fIn = new FileInputStream(fileChooser.getSelectedFile().getAbsolutePath());
                   ObjectInputStream oIn = new ObjectInputStream(fIn)) {
                for(JCheckBox chBox : checkBoxesList) {
                    chBox.setSelected((boolean)oIn.readObject());
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    private void buildTrackAndStart() {
        int[] trackList;

        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for(int i = 0; i < 16; i++) {
            trackList = new int[16];

            int key = instruments[i];

            for (int j = 0; j < 16; j++) {
                JCheckBox chBox = checkBoxesList.get(j + (16 * i));
                if (chBox.isSelected())
                    trackList[j] = key;
                else
                    trackList[j] = 0;
            }

            makeTracks(trackList);
            track.add(makeEvent(176, 1, 127, 0, 16));
        }
        track.add(makeEvent(192, 9, 1, 0, 15));

        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void makeTracks(int[] keyList) {
        for(int i = 0; i < 16; i++) {
            int key = keyList[i];
            if(key != 0) {
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i+1));
            }
        }
    }

    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage msg = new ShortMessage();
            msg.setMessage(comd, chan, one, two);
            event = new MidiEvent(msg, tick);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return event;
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("BeatBox");
        frame.setContentPane(new BeatBox().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        frame.setJMenuBar(menuBar);
    }
}
