package conure.vt.filetypes;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import conure.vt.Global;
import conure.vt.VoxFileInterpreter;
/**Voxelizes 3-D OBJ files.*/
public final class OBJ extends VoxFileInterpreter {
	/**@return ".obj"*/
	@Override
	public String getExtension() {
		return ".obj";
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
		Scanner reader=new Scanner(source),parser;
		ArrayList<double[]> vertices=new ArrayList<double[]>(),face;
		LinkedList<ArrayList<double[]>> faces=new LinkedList<ArrayList<double[]>>();
		double[] v;
		String next;
		int index;
		while(reader.hasNextLine()) {
			parser=new Scanner(reader.nextLine());
			if(parser.hasNext()) switch(parser.next()) {
				case "v":
					try {
						v=new double[] {Double.parseDouble(parser.next()),
								Double.parseDouble(parser.next()),
								Double.parseDouble(parser.next())};
					} catch(NoSuchElementException|NumberFormatException e) {
						Global.println("File formatting not recognized");
						return null;
					}
					vertices.add(v);
					break;
				case "f":
					face=new ArrayList<double[]>();
					while(parser.hasNext()) {
						next=parser.next();
						index=next.indexOf('/');
						if(index!=-1)
							next=next.substring(0,index);
						index=Integer.parseInt(next);
						if(index>0)
							index--;
						else
							index+=vertices.size();
						try {
							face.add(vertices.get(index));
						} catch(IndexOutOfBoundsException e) {
							Global.println("File formatting not recognized");
							return null;
						}
					}
					faces.add(face);
					break;
			}
			parser.close();
		}
		reader.close();
		return Global.voxelizePolygons(faces,edgelength);
	}
}