package conure.vt;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
/**Provides various utility methods for programmers to use when writing extensions.*/
public final class Global {
	/**Keeps track of the current app version.*/
	public static final String VERSION="v1.0";
	/**Prints object(s) to the current console - the on-screen text area
	 * if using the window console, or System.out if using command prompt.
	 * @param o - The object(s) to be printed. Calls o.toString().*/
	public static void print(Object... o) {
		for(Object obj:o) {
			if(Main.window!=null)
				Main.window.print(obj.toString());
			else
				System.out.print(obj.toString());
		}
	}
	/**Prints object(s) and a new line to the current console - the on-screen text area
	 * if using the window console, or System.out if using command prompt.
	 * @param o - The object(s) to be printed. Calls o.toString().*/
	public static void println(Object... o) {
		print(o);
		if(Main.window!=null)
			Main.window.println();
		else
			System.out.println();
	}
	/**Some file formats use little-endian formatting. This method is similar to
	 * DataInputStream.readInt(), but the lowest-order bytes are read first.
	 * @param reader - The InputStream object to read from. reader.read() will be called 4 times.
	 * @return The integer represented by the next 4 bytes, in little-endian form.
	 * @throws IOException Thrown by the reader.*/
	public static int readIntLittleEndian(InputStream reader) throws IOException {
		return reader.read()+(reader.read()<<8)+(reader.read()<<16)+(reader.read()<<24);
	}
	/**Voxelizes a 3-D polygon-based model.
	 * @param polygons - A collection of lists of double-arrays. Each double-array contains 3 elements
	 * representing the coordinates of one vertex. Each list represents a polygon formed by the vertices
	 * it contains. Multiple lists may reference the same vertex. A recommended form for this parameter is
	 * LinkedList&lt;ArrayList&lt;double[]&gt;&gt; - LinkedLists are easier on internal memory (especially
	 * when they're particularly large), and ArrayLists are very efficient when calling the method "get".
	 * @param edgelength - The maximum length (in voxels) of any dimension of the final voxelized model.
	 * @return A voxel model stored in a boolean[][][], or null if the polygons are incorrectly formatted.
	 * If this method is called from VoxFileInterpreter.load(..), the returned value can safely be
	 * returned from that method.*/
	public static boolean[][][] voxelizePolygons(Iterable<? extends List<double[]>> polygons,int edgelength) {
		edgelength--;
		if(edgelength<1) {
			println("Voxelization failed - dimension must be >1.");
			return null;
		}
		double[] min=null,max=null;
		try {
			for(List<double[]> f:polygons)
				for(double[] v:f) {
					if(min==null)
						min=new double[] {v[0],v[1],v[2]};
					else for(int i=0;i<3;i++)
						if(min[i]>v[i])
							min[i]=v[i];
					if(max==null)
						max=new double[] {v[0],v[1],v[2]};
					else for(int i=0;i<3;i++)
						if(max[i]<v[i])
							max[i]=v[i];
				}
		} catch(IndexOutOfBoundsException e) {
			println("Voxelization failed - double arrays must be 3 elements in length.");
			return null;
		}
		double absmax=0.0;
		try {
			for(int i=0;i<3;i++) {
				max[i]-=min[i];
				if(absmax<max[i])
					absmax=max[i];
			}
		} catch(NullPointerException e) {
			println("Voxelization failed - Polygons are missing, or lack vertices.");
			return null;
		}
		double scale=edgelength/absmax;
		int[] c=new int[3];
		for(int i=0;i<3;i++)
			c[i]=(int)Math.ceil(scale*max[i]+1);
		boolean[][][] model=new boolean[c[0]][c[1]][c[2]];
		double xstep,ystep;
		Plane plane;
		double[] params;
		for(List<double[]> f:polygons)
			for(int i=2;i<f.size();i++) {
				plane=new Plane(f.get(0),f.get(i-1),f.get(i));
				xstep=1/(plane.getXScale()*scale);
				ystep=1/(plane.getYScale()*scale);
				for(double x=0;x<1+xstep;x+=xstep)
					for(double y=0;y<1-x+ystep;y+=ystep) {
						params=plane.getRealCoords(x,y);
						for(int j=0;j<3;j++)
							c[j]=(int)((params[j]-min[j])*scale);
						model[c[0]][c[1]][c[2]]=true;
					}
			}
		return model;
	}
}
