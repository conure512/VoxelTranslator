package conure.vt;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
/**The main Java Swing window that appears when running the application normally.*/
class WindowConsole extends JFrame {
	private static final long serialVersionUID=1L;
	private static final int BACKGROUND=0,LAYER=0xff00c000,FULL_VIEW=0xff505050;
	private final JTextArea textConsole;
	private final JTextField directory,loadPath,exportPath,loadExtra,exportExtra;
	private final JButton loadButton,exportButton,flipV,flipH,rotL,rotR,viewModelButton;
	private final JComboBox<String> loadFormat,exportFormat,viewAxis;
	private final JLabel modelName,imgLabel,layerLabel,axes;
	private int layer=-1;
	WindowConsole() {
		super("Voxel Translator "+Global.VERSION);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setBounds(100,100,700,375);
		JPanel panel=new JPanel(null);
		JLabel label=new JLabel("Current Directory");
		label.setBounds(2,0,100,15);
		panel.add(label);
		directory=new JTextField(Main.directory);
		directory.setBounds(2,15,250,20);
		panel.add(directory);
		label=new JLabel("File to Load");
		label.setBounds(2,40,69,15);
		panel.add(label);
		loadPath=new JTextField();
		loadPath.setBounds(2,55,170,20);
		panel.add(loadPath);
		loadButton=new JButton("Load");
		loadButton.setBounds(172,55,80,20);
		loadButton.setEnabled(false);
		panel.add(loadButton);
		label=new JLabel("Export Destination");
		label.setBounds(2,80,110,15);
		panel.add(label);
		exportPath=new JTextField();
		exportPath.setBounds(2,95,170,20);
		panel.add(exportPath);
		exportButton=new JButton("Export");
		exportButton.setBounds(172,95,80,20);
		panel.add(exportButton);
		label=new JLabel("Console");
		label.setBounds(2,120,100,15);
		panel.add(label);
		textConsole=new JTextArea();
		textConsole.setLineWrap(true);
		textConsole.setWrapStyleWord(true);
		textConsole.setEditable(false);
		JScrollPane scroll=new JScrollPane(textConsole);
		scroll.setBounds(2,135,250,203);
		panel.add(scroll);
		label=new JLabel("LOAD SETTINGS");
		label.setBounds(300,0,100,15);
		panel.add(label);
		label=new JLabel("Format:");
		label.setBounds(300,22,45,15);
		panel.add(label);
		loadFormat=new JComboBox<String>();
		loadFormat.addItem("");
		loadFormat.setBounds(347,20,90,20);
		panel.add(loadFormat);
		label=new JLabel("Parameters");
		label.setBounds(300,37,100,20);
		panel.add(label);
		loadExtra=new JTextField();
		loadExtra.setBounds(300,54,139,20);
		panel.add(loadExtra);
		label=new JLabel("EXPORT SETTINGS");
		label.setBounds(470,0,120,15);
		panel.add(label);
		label=new JLabel("Format:");
		label.setBounds(470,22,45,15);
		panel.add(label);
		exportFormat=new JComboBox<String>();
		exportFormat.addItem("");
		exportFormat.setBounds(517,20,90,20);
		panel.add(exportFormat);
		label=new JLabel("Parameters");
		label.setBounds(470,37,108,20);
		panel.add(label);
		exportExtra=new JTextField();
		exportExtra.setBounds(470,54,139,20);
		panel.add(exportExtra);
		for(VoxFileInterpreter vfi:Voxels.interpreters) {
			if(vfi.canLoad())
				loadFormat.addItem(vfi.getExtension());
			if(vfi.canExport())
				exportFormat.addItem(vfi.getExtension());
		}
		modelName=new JLabel();
		modelName.setBounds(260,120,440,15);
		panel.add(modelName);
		label=new JLabel("Perspective Axis:");
		label.setBounds(260,137,98,15);
		panel.add(label);
		viewAxis=new JComboBox<String>();
		viewAxis.addItem("x");
		viewAxis.addItem("y");
		viewAxis.addItem("z");
		viewAxis.setBounds(364,135,50,20);
		panel.add(viewAxis);
		imgLabel=new JLabel();
		imgLabel.setBounds(310,160,0,0);
		panel.add(imgLabel);
		layerLabel=new JLabel();
		layerLabel.setBounds(420,137,270,15);
		panel.add(layerLabel);
		label=new JLabel("FLIP");
		label.setBounds(262,160,30,15);
		panel.add(label);
		flipV=new JButton("v^");
		flipV.setBounds(252,175,48,20);
		panel.add(flipV);
		flipH=new JButton("<>");
		flipH.setBounds(252,195,48,20);
		panel.add(flipH);
		label=new JLabel("ROTATE");
		label.setBounds(252,220,50,15);
		panel.add(label);
		rotL=new JButton("<<");
		rotL.setBounds(252,235,48,20);
		panel.add(rotL);
		rotR=new JButton(">>");
		rotR.setBounds(252,255,48,20);
		panel.add(rotR);
		axes=new JLabel("<html>z^<br/>+ y ></html>");
		axes.setBounds(262,280,40,32);
		panel.add(axes);
		exportButton.setEnabled(false);
		modelName.setText("No model loaded.");
		layerLabel.setText("");
		viewModelButton=new JButton("View Model");
		viewModelButton.setBounds(405,85,99,20);
		panel.add(viewModelButton);
		viewAxis.setEnabled(false);
		flipV.setEnabled(false);
		flipH.setEnabled(false);
		rotL.setEnabled(false);
		rotR.setEnabled(false);
		viewModelButton.setEnabled(false);
		loadButton.addActionListener(e -> attemptLoad());
		loadPath.addActionListener(e -> {
			if(loadButton.isEnabled())
				attemptLoad();
		});
		exportButton.addActionListener(e -> attemptExport());
		exportPath.addActionListener(e -> {
			if(exportButton.isEnabled())
				attemptExport();
		});
		loadPath.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {}
			@Override
			public void insertUpdate(DocumentEvent e) {
				update();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				update();
			}
			private void update() {
				String ext=loadPath.getText();
				try {
					ext=ext.substring(ext.indexOf('.'));
				} catch(Exception e) {
					loadFormat.setSelectedIndex(0);
					return;
				}
				for(int i=1;i<loadFormat.getItemCount();i++)
					if(loadFormat.getItemAt(i).equals(ext)) {
						loadFormat.setSelectedIndex(i);
						return;
					}
				loadFormat.setSelectedIndex(0);
			}
		});
		exportPath.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {}
			@Override
			public void insertUpdate(DocumentEvent e) {
				update();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				update();
			}
			private void update() {
				String ext=exportPath.getText();
				try {
					ext=ext.substring(ext.indexOf('.'));
				} catch(Exception e) {
					exportFormat.setSelectedIndex(0);
					return;
				}
				for(int i=1;i<exportFormat.getItemCount();i++)
					if(exportFormat.getItemAt(i).equals(ext)) {
						exportFormat.setSelectedIndex(i);
						return;
					}
				exportFormat.setSelectedIndex(0);
			}
		});
		loadFormat.addActionListener(e -> loadButton.setEnabled(loadFormat.getSelectedIndex()!=0));
		exportFormat.addActionListener(e -> exportButton.setEnabled(Voxels.array!=null&&exportFormat.getSelectedIndex()!=0));
		viewAxis.addActionListener(e -> {
			switch(viewAxis.getSelectedIndex()) {
				case 0:
					axes.setText("<html>z^<br/>+ y ></html>");
					break;
				case 1:
					axes.setText("<html>x^<br/>+ z ></html>");
					break;
				case 2:
					axes.setText("<html>y^<br/>+ x ></html>");
					break;
			}
			renderSilhouette();
		});
		imgLabel.addMouseWheelListener(e -> {
			if(Voxels.array==null)
				return;
			layer+=e.getWheelRotation();
			renderLayer();
		});
		flipV.addActionListener(e -> {
			Voxels.flip((viewAxis.getSelectedIndex()+2)%3);
			renderSilhouette();
		});
		flipH.addActionListener(e -> {
			Voxels.flip((viewAxis.getSelectedIndex()+1)%3);
			renderSilhouette();
		});
		rotL.addActionListener(e -> {
			Voxels.rotate(viewAxis.getSelectedIndex(),true);
			renderSilhouette();
		});
		rotR.addActionListener(e -> {
			Voxels.rotate(viewAxis.getSelectedIndex(),false);
			renderSilhouette();
		});
		viewModelButton.addActionListener(e -> new RenderWindow(modelName.getText().substring(14)).setVisible(true));
		add(panel);
	}
	private void attemptLoad() {
		String path=loadPath.getText();
		if(Voxels.attemptLoad(path,directory.getText(),(String)loadFormat.getSelectedItem(),loadExtra.getText()))
			onLoadSuccess(path);
	}
	private void attemptExport() {
		Voxels.attemptExport(exportPath.getText(),directory.getText(),(String)exportFormat.getSelectedItem(),exportExtra.getText());
	}
	void onLoadSuccess(String fileName) {
		if(!(exportButton.isEnabled()||exportPath.getText().equals("")))
			exportButton.setEnabled(true);
		modelName.setText("Loaded Model: "+fileName);
		viewAxis.setEnabled(true);
		flipV.setEnabled(true);
		flipH.setEnabled(true);
		rotL.setEnabled(true);
		rotR.setEnabled(true);
		viewModelButton.setEnabled(true);
		renderSilhouette();
	}
	private void renderSilhouette() {
		layer=-1;
		layerLabel.setText("No layer selected - Scroll over image to select");
		int axisKey=viewAxis.getSelectedIndex(),w,h,d;
		switch(axisKey) {
			case 0:
				w=Voxels.array[0].length;
				h=Voxels.array[0][0].length;
				d=Voxels.array.length;
				break;
			case 1:
				w=Voxels.array[0][0].length;
				h=Voxels.array.length;
				d=Voxels.array[0].length;
				break;
			case 2:
				w=Voxels.array.length;
				h=Voxels.array[0].length;
				d=Voxels.array[0][0].length;
				break;
			default:
				println("Invalid axis key");
				return;
		}
		BufferedImage image=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
		int[] raster=((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		imgLabel.setSize(w,h);
		imgLabel.setIcon(new ImageIcon(image));
		boolean line=false,vox=false;
		for(int j=0;j<h;j++)
			for(int i=0;i<w;i++) {
				line=false;
				for(int k=0;k<d;k++) {
					switch(axisKey) {
						case 0:
							vox=Voxels.array[k][i][h-j-1];
							break;
						case 1:
							vox=Voxels.array[h-j-1][k][i];
							break;
						case 2:
							vox=Voxels.array[i][h-j-1][k];
							break;
					}
					if(vox) {
						line=true;
						break;
					}
				}
				if(line)
					raster[j*w+i]=FULL_VIEW;
				else
					raster[j*w+i]=BACKGROUND;
			}
	}
	private void renderLayer() {
		int axisKey=viewAxis.getSelectedIndex(),d;
		switch(axisKey) {
			case 0:
				d=Voxels.array.length;
				break;
			case 1:
				d=Voxels.array[0].length;
				break;
			case 2:
				d=Voxels.array[0][0].length;
				break;
			default:
				return;
		}
		if(layer>d)
			layer=-1;
		else if(layer<-1)
			layer=d;
		BufferedImage image=(BufferedImage)((ImageIcon)imgLabel.getIcon()).getImage();
		int[] raster=((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		int w=image.getWidth(),h=image.getHeight();
		imgLabel.setIcon(new ImageIcon(image));
		if(layer==-1||layer==d) {
			layerLabel.setText("No layer selected");
			for(int j=0;j<h;j++)
				for(int i=0;i<w;i++)
					if(raster[j*w+i]==LAYER)
						raster[j*w+i]=FULL_VIEW;
			return;
		}
		layerLabel.setText("Layer "+layer);
		boolean vox=false;
		for(int j=0;j<h;j++)
			for(int i=0;i<w;i++) {
				switch(axisKey) {
					case 0:
						vox=Voxels.array[layer][i][h-j-1];
						break;
					case 1:
						vox=Voxels.array[h-j-1][layer][i];
						break;
					case 2:
						vox=Voxels.array[i][h-j-1][layer];
						break;
				}
				if(vox)
					raster[j*w+i]=LAYER;
				else if(raster[j*w+i]==LAYER)
					raster[j*w+i]=FULL_VIEW;
			}
	}
	void print(String s) {
		textConsole.append(s);
	}
	void println() {
		textConsole.append("\n");
	}
	void println(String s) {
		textConsole.append(s+'\n');
	}
}