package conure.vt.filetypes;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import conure.vt.Global;
import conure.vt.VoxFileInterpreter;
/**Voxelizes 3-D STL files.*/
public final class STL extends VoxFileInterpreter {
	/**@return ".stl"*/
	@Override
	public String getExtension() {
		return ".stl";
	}
	@Override
	public boolean[][][] load(File source,String[] args) throws IOException {
		int edgelength=64;
		for(String arg:args) {
			if(arg.startsWith("-d:")) try {
				edgelength=Integer.parseInt(arg.substring(3));
			} catch(NumberFormatException e) {
				Global.println("Dimension could not be resolved - defaulting to 64");
			}
		}
		Scanner reader=new Scanner(source);
		String header=reader.hasNext()?reader.next():"";
		if(!header.equals("solid")) {
			reader.close();
			return loadBinary(source,edgelength);
		}
		LinkedList<ArrayList<double[]>> faces=new LinkedList<ArrayList<double[]>>();
		ArrayList<double[]> face;
		double[] v;
		while(reader.hasNext())
			if(reader.next().equals("outer")) {
				reader.nextLine();
				face=new ArrayList<double[]>();
				for(int i=0;i<3;i++) {
					reader.next();
					v=new double[3];
					v[0]=Double.parseDouble(reader.next());
					v[2]=Double.parseDouble(reader.next());
					v[1]=Double.parseDouble(reader.next());
					reader.nextLine();
					face.add(v);
				}
				faces.add(face);
			}
		reader.close();
		return Global.voxelizePolygons(faces,edgelength);
	}
	private static boolean[][][] loadBinary(File source,int edgelength) throws IOException {
		DataInputStream reader=new DataInputStream(new FileInputStream(source));
		LinkedList<ArrayList<double[]>> faces=new LinkedList<ArrayList<double[]>>();
		ArrayList<double[]> face;
		reader.skip(80);
		int lower=reader.read()+(reader.read()<<8)+(reader.read()<<16);
		long len=reader.read();
		len=(len<<24)+lower;
		float[] v=new float[3];
		for(long i=0;i<len;i++) {
			reader.skip(12);
			face=new ArrayList<double[]>();
			for(int j=0;j<3;j++) {
				v[0]=readNextFloat(reader);
				v[2]=readNextFloat(reader);
				v[1]=readNextFloat(reader);
				face.add(new double[] {v[0],v[1],v[2]});
			}
			reader.skip(2);
			faces.add(face);
		}
		reader.close();
		return Global.voxelizePolygons(faces,edgelength);
	}
	private static float readNextFloat(DataInputStream reader) throws IOException {
		return Float.intBitsToFloat(reader.read()+(reader.read()<<8)+(reader.read()<<16)+(reader.read()<<24));
	}
}