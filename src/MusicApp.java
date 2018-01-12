import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.*;

public class MusicApp {
	JFrame frame;
	JPanel mainPanel;
	ArrayList<JCheckBox> checkBoxList;
	public Sequencer sequencer;
	Sequence sequence;
	Track track;

	String[] instrumentName = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", "Hand Clap", "High Tom", 
			"Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga"};
	
	int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};
	
	public static void main(String[] args) {
		new MusicApp().userInterface();
	}
	
	public void userInterface() {
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		BorderLayout layout = new BorderLayout();
		JPanel panel = new JPanel(layout);
		
		checkBoxList = new ArrayList<JCheckBox>();
		Box buttonArea = new Box(BoxLayout.Y_AXIS);
		
		JButton start = new JButton("Start");
		start.addActionListener(new StartButtonListener());
		buttonArea.add(start);
		
		JButton stop = new JButton("Stop");
		stop.addActionListener(new StopButtonListener());
		buttonArea.add(stop);
		
		JButton fast = new JButton("Faster");
		fast.addActionListener(new FastButtonListener());
		buttonArea.add(fast);
		
		JButton slow = new JButton("Slower");
		slow.addActionListener(new SlowButtonListener());
		buttonArea.add(slow);
		
		JButton save = new JButton("Save");
		save.addActionListener(new SaveButtonListener());
		buttonArea.add(save);
		
		JButton play = new JButton("PlayMyMusic");
		play.addActionListener(new PlayMyMusicButtonListener());
		buttonArea.add(play);
		
		Box nameArea = new Box(BoxLayout.Y_AXIS);
		for (int i = 0; i < 16; i++) {
			nameArea.add(new Label(instrumentName[i]));
		}
		
		panel.add(BorderLayout.WEST, nameArea);
		panel.add(BorderLayout.EAST, buttonArea);
		
		frame.getContentPane().add(panel);
		
		GridLayout checkBoxes = new GridLayout(16, 16);
		checkBoxes.setVgap(1);
		checkBoxes.setHgap(2);
		mainPanel = new JPanel(checkBoxes);
		panel.add(BorderLayout.CENTER, mainPanel);
		
		for (int i = 0; i < 256; i++) {
			JCheckBox c = new JCheckBox();
			c.setSelected(false);
			checkBoxList.add(c);
			mainPanel.add(c);
		}
		
		midiConfig();
		
		frame.setBounds(50, 50, 300, 300);
		frame.pack();
		frame.setVisible(true);
	}
	
	public void midiConfig() {
		try {																// Typowa konfiguracja midi
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
			sequence = new Sequence(Sequence.PPQ, 4);
			track = sequence.createTrack();
			sequencer.setTempoInBPM(120);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void createTrackAndPlay() {
		int[] trackList = null;
		
		//track = sequence.createTrack();
		sequence.deleteTrack(track);
		track = sequence.createTrack();
		
		for (int i = 0; i< 16; i++) {				// sprawdza 16 checkbox dla ka¿dego z instruments i w którym takcie ma graæ ten instrument
			trackList = new int[16];										
			int key = instruments[i];
			
			for (int j = 0; j < 16; j++) {			 //mamy 16 instruments i dlatego robimy pêtle, bêdzie sprawdzaæ
				JCheckBox jc = (JCheckBox) checkBoxList.get(j + (16*i));	//które instruments s¹ zaznaczone w checkbox
				if (jc.isSelected()) {
					trackList[j] = key;
				} else {
					trackList[j] = 0;
				}
			}
			
			createTrack(trackList);
			track.add(createEvent(176, 1, 127, 0, 16));
		}
		track.add(createEvent(192, 9, 1, 0, 15));
		try {
			sequencer.setSequence(sequence);
			sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
			sequencer.start();
			sequencer.setTempoInBPM(120);
		} catch (Exception ex) { 
			ex.printStackTrace();
			}
	}
	
	public class StartButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			//start.setText("dziala");
			createTrackAndPlay();
		}
	}
	
	public class StopButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			sequencer.stop();
		}
	}
	
	public class FastButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			float tempo = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float) (tempo * 1.05));
		}
	}
	
	public class SlowButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			float tempo = sequencer.getTempoFactor();
			sequencer.setTempoFactor((float) (tempo * 0.95));
		}
	}
	
	public class SaveButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			JFileChooser dataFile = new JFileChooser();
			dataFile.showSaveDialog(frame);
			saveFile(dataFile.getSelectedFile());
		}
	}
	
	private void saveFile(File file) {
		boolean[] boxState = new boolean[256];
		
		for (int i = 0; i < boxState.length; i++) {
			JCheckBox box = (JCheckBox) checkBoxList.get(i);
			if(box.isSelected()) {
				boxState[i] = true;
			}
			
			try {
				FileOutputStream fileStream = new FileOutputStream(file);
				ObjectOutputStream os = new ObjectOutputStream(fileStream);
				os.writeObject(boxState);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}	
	}
	
	public class PlayMyMusicButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			JFileChooser dataFile = new JFileChooser();
			dataFile.showOpenDialog(frame);
			openFile(dataFile.getSelectedFile());
		}
	}

	private void openFile(File file) {
			boolean[] boxState = null;
			
			try {
				FileInputStream inputFile = new FileInputStream(file);
				ObjectInputStream inputStream = new ObjectInputStream(inputFile);
				boxState = (boolean[]) inputStream.readObject();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			for (int i = 0; i < boxState.length; i++) {
				JCheckBox box = (JCheckBox) checkBoxList.get(i);
				if(boxState[i]) {
					box.setSelected(true);
				} else {
					box.setSelected(false);
				}
			}
			sequencer.stop();
			createTrackAndPlay();
	}

	public void createTrack(int[] list) {
		for (int i = 0; i < 16; i++) {
			int key = list[i];
			if (key != 0) {
				track.add(createEvent(144, 9, key, 100, i));
				track.add(createEvent(128, 9, key, 100, i+1));
			}
		}
	}
	
	public static MidiEvent createEvent(int plc, int canal, int one, int two, int takt) {
		MidiEvent event = null;
		
		try {
			ShortMessage a = new ShortMessage();
			a.setMessage(plc, canal, one, two);
			event = new MidiEvent(a, takt);
		} catch (Exception ex) {ex.printStackTrace(); }	
		return event;
	}
}
