package conure.vt;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
/**A Swing window that displays a 3D rendered version of the loaded model.
 * NOTE - This class is a work in progress and still contains a major bug.*/
public class RenderWindow extends JFrame {
	private static final long serialVersionUID=1L;
	private static final int SIZE=512,BACKGROUND=0xff000000,FACE=0xff505050,DELTA=0x101010;
	private static final double SMALL_STEP=.000001;
	private final JLabel imgLabel,viewLabel;
	private final BufferedImage img;
	private final boolean[][][] model;
	private final int[] offHigh,offLow;
	private double th=0.0,phi=0.0,arc=1.5,dist;
	private int[] raster;
	public RenderWindow(String modelname) {
		super("Viewing Model "+modelname);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setBounds(150,150,SIZE+14,SIZE+37);
		JPanel panel=new JPanel(null);
		img=new BufferedImage(SIZE,SIZE,BufferedImage.TYPE_INT_ARGB);
		raster=((DataBufferInt)img.getRaster().getDataBuffer()).getData();
		imgLabel=new JLabel(new ImageIcon(img));
		imgLabel.setBounds(0,0,SIZE,SIZE);
		panel.add(imgLabel);
		viewLabel=new JLabel();
		viewLabel.setBounds(0,SIZE,300,15);
		panel.add(viewLabel);
		add(panel);
		model=Voxels.array;
		offHigh=new int[] {model.length/2,model[0].length/2,model[0][0].length/2};
		offLow=new int[] {offHigh[0]-model.length,offHigh[1]-model[0].length,offHigh[2]-model[0][0].length};
		dist=(offHigh[0]+offHigh[1]+offHigh[2]);
		imgLabel.addMouseWheelListener(e -> {
			if(e.getWheelRotation()>0)
				dist*=1.2;
			else
				dist/=1.2;
			renderViewRayTrace();
		});
		imgLabel.addMouseMotionListener(new MouseMotionListener() {
			private int[] pos=new int[2];
			@Override
			public void mouseMoved(MouseEvent e) {
				pos[0]=e.getX();
				pos[1]=e.getY();
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				th+=.01*(e.getX()-pos[0]);
				phi+=.01*(e.getY()-pos[1]);
				renderViewRayTrace();
				mouseMoved(e);
			}
		});
		renderViewRayTrace();
	}
	private void renderViewRayTrace() {
		final double[] view=spherToCart(dist,th,phi);
		for(int i=0;i<3;i++)
			view[i]+=(offLow[i]+offHigh[i])/2.0;
		viewLabel.setText(Math.floor(view[0])+" "+Math.floor(view[1])+" "+Math.floor(view[2]));
		for(int y=0;y<SIZE;y++) {
			final int yF=y;
			//new Thread(() -> {
				for(int x=0;x<SIZE;x++) {
					final double[] vec=spherToCart(-1d,th+arc*(((double)x-SIZE)/SIZE+.5),phi+arc*(((double)yF-SIZE)/SIZE+.5));
					raster[yF*SIZE+x]=trace(snapToModelEdge(view,vec),vec);
				}
			 //}).start();
		}
		imgLabel.setIcon(new ImageIcon(img));
	}
	private double[] snapToModelEdge(double[] pos,double[] vec) {
		if(blockAt(pos)!=-1)
			return pos;
		double t,tMin=-1.0;
		for(int i=0;i<3;i++) {
			if(vec[i]==0)
				continue;
			t=(offLow[i]-pos[i])/vec[i];
			if(t>0&&(tMin==-1||t<tMin)&&isInBounds(pos,vec,t+SMALL_STEP))
				tMin=t;
			t=(offHigh[i]-pos[i])/vec[i];
			if(t>0&&(tMin==-1||t<tMin)&&isInBounds(pos,vec,t+SMALL_STEP))
				tMin=t;
		}
		if(tMin==-1)
			return null;
		double[] newPos=new double[3];
		for(int i=0;i<3;i++)
			newPos[i]=pos[i]+(tMin-2*SMALL_STEP)*vec[i];
		return newPos;
	}
	private boolean isInBounds(double[] pos,double[] vec,double t) {
		double coord;
		for(int i=0;i<3;i++) {
			coord=pos[i]+t*vec[i];
			if(coord<offLow[i]||coord>=offHigh[i])
				return false;
		}
		return true;
	}
	private int trace(double[] pos,double[] vec) {
		if(pos==null)
			return BACKGROUND;
		double[] next=new double[3];
		int flipped;
		for(int i=0;i<3;i++)
			next[i]=pos[i];
		while(true) {
			flipped=snapNextIntFlip(next,vec);
			switch(blockAt(next)) {
				case 0:
					continue;
				case 1:
					switch(flipped) {
						case 0:
							return FACE;
						case 1:
							return FACE+DELTA;
						case 2:
							return FACE-DELTA;
					}
				case -1:
					return BACKGROUND;
			}
		}
	}
	private int blockAt(double[] pos) {
		int[] coords=new int[3];
		for(int i=0;i<3;i++) {
			coords[i]=(int)Math.floor(pos[i]);
			if(coords[i]<offLow[i]||coords[i]>=offHigh[i])
				return -1;
			coords[i]-=offLow[i];
		}
		return model[coords[0]][coords[1]][coords[2]]?1:0;
	}
	private static int snapNextIntFlip(double[] pos,double[] vec) {
		int plane,toFlip=-1;
		double t,tMin=-1.0;
		for(int i=0;i<3;i++) {
			if(vec[i]==0)
				continue;
			plane=(int)Math.floor(pos[i]);
			if(vec[i]>0)
				plane++;
			t=(plane-pos[i])/vec[i];
			if(tMin==-1||t<tMin) {
				toFlip=i;
				tMin=t;
			}
		}
		for(int i=0;i<3;i++)
			pos[i]+=(tMin+SMALL_STEP)*vec[i];
		return toFlip;
	}
	private static double[] spherToCart(double r,double th,double phi) {
		double cyl=Math.cos(phi);
		return new double[] {r*cyl*Math.cos(th),r*Math.sin(phi),r*cyl*Math.sin(th)};
	}
}