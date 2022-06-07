package conure.vt;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import conure.vt.filetypes.Binvox;
import conure.vt.filetypes.NBT;
import conure.vt.filetypes.OBJ;
import conure.vt.filetypes.STL;
import conure.vt.filetypes.Schematic;
import conure.vt.filetypes.Vox;
final class Voxels {
	static boolean[][][] array=null;
	static final ArrayList<VoxFileInterpreter> interpreters;
	static {
		interpreters=new ArrayList<VoxFileInterpreter>();
		interpreters.add(new Binvox());
		interpreters.add(new Vox());
		interpreters.add(new OBJ());
		interpreters.add(new STL());
		interpreters.add(new NBT());
		interpreters.add(new Schematic());
	}
	static void loadExtensions() throws Exception {
		File extFolder=new File("vt-extensions");
		if(!extFolder.isDirectory())
			return;
		JarFile jar;
		Class<?> c;
		String next;
		URLClassLoader loader;
		for(File file:extFolder.listFiles()) if(file.getName().endsWith(".jar")) {
			jar=new JarFile(file);
			loader=new URLClassLoader(new URL[] {new URL("jar:file:"+file.getAbsolutePath()+"!/")});
			for(Enumeration<JarEntry> entries=jar.entries();entries.hasMoreElements();) {
				next=entries.nextElement().getName();
				if(next.endsWith(".class")) {
					next=next.replace('/','.').substring(0,next.length()-6);
					try {
						c=loader.loadClass(next);
						if(VoxFileInterpreter.class.isAssignableFrom(c)) {
							try {
								VoxFileInterpreter vfi=(VoxFileInterpreter)c.getConstructor().newInstance();
								Voxels.interpreters.add(vfi);
								System.out.println("Loaded extension "+vfi.getExtension());
							} catch(IllegalAccessException e) {
								System.out.println("Load failed for class "+c.getSimpleName()+" - Constructor is inaccessible.");
							} catch(NoSuchMethodException|IllegalArgumentException e) {
								System.out.println("Load failed for class "+c.getSimpleName()+" - Default constructor not found.");
							} catch(InvocationTargetException e) {
								System.out.println("Load failed for class "+c.getSimpleName()+" - Constructor failed to execute.");
							} catch(InstantiationException|SecurityException e) {}
						}
					} catch(ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
			jar.close();
		}
	}
	static boolean attemptLoad(String path,String dir,String ext,String argString) {
		if(!dir.endsWith("/")&&!dir.endsWith("\\"))
			dir+=File.separatorChar;
		File file=new File(dir+path);
		if(!file.canRead()) {
			file=new File(path);
			if(!file.canRead()) {
				Global.println("That file could not be found, or it was unreadable.");
				return false;
			}
		}
		boolean[][][] temp;
		String[] args=argString.split(" ");
		for(VoxFileInterpreter vfi:interpreters) if(vfi.getExtension().equals(ext)&&vfi.canLoad()) {
			try {
				temp=vfi.load(file,args);
			} catch(IOException e) {
				Global.println("An I/O error has occurred. Try restarting the program.");
				return false;
			}
			if(temp==null) {
				Global.println("Load failed.");
				return false;
			}
			temp=reduce(temp);
			if(temp!=null) {
				array=temp;
				Global.println("Model loaded.");
				return true;
			} else {
				Global.println("Model empty - load stopped.");
				return false;
			}
		}
		Global.println("Unrecognized file type.");
		return false;
	}
	static boolean attemptExport(String path,String dir,String ext,String argString) {
		if(!dir.endsWith("/")&&!dir.endsWith("\\"))
			dir+=File.separatorChar;
		File file=new File(dir+path);
		if(!createWritablePath(file)) {
			file=new File(path);
			if(!createWritablePath(file)) {
				Global.println("That file location appears to be unwritable.");
				return false;
			}
		}
		String[] args=argString.split(" ");
		for(VoxFileInterpreter vfi:interpreters) if(vfi.getExtension().equals(ext)&&vfi.canExport()) {
			try {
				if(vfi.export(array,file,args)) {
					Global.println("File created.");
					return true;
				} else {
					Global.println("Export failed.");
					return false;
				}
			} catch(IOException e) {
				Global.println("I/O error: ",e.getMessage());
				return false;
			}
		}
		Global.println("Unrecognized file type.");
		return false;
	}
	private static boolean createWritablePath(File file) {
		File parent=file.getParentFile();
		if(parent==null||(file.exists()&&!file.canWrite()))
			return false;
		if(parent.exists())
			return parent.isDirectory();
		return parent.mkdirs();
	}
	private static boolean[][][] reduce(boolean[][][] model) {
		int[] c=new int[3],max=new int[3],min=new int[] {model.length,model[0].length,model[0][0].length};
		for(;c[0]<model.length;c[0]++)
			for(c[1]=0;c[1]<model[c[0]].length;c[1]++)
				for(c[2]=0;c[2]<model[c[0]][c[1]].length;c[2]++)
					if(model[c[0]][c[1]][c[2]]) for(int i=0;i<3;i++) {
						if(max[i]<c[i])
							max[i]=c[i];
						if(min[i]>c[i])
							min[i]=c[i];
					}
		if(min[0]==model.length)
			return null;
		int[] r=new int[3];
		for(int i=0;i<3;i++)
			r[i]=max[i]-min[i]+1;
		if(r[0]==model.length&&r[1]==model[0].length&&r[2]==model[0][0].length)
			return model;
		boolean[][][] temp=new boolean[r[0]][r[1]][r[2]];
		for(c[0]=0;c[0]<r[0];c[0]++)
			for(c[1]=0;c[1]<r[1];c[1]++)
				for(c[2]=0;c[2]<r[2];c[2]++)
					if(model[min[0]+c[0]][min[1]+c[1]][min[2]+c[2]])
						temp[c[0]][c[1]][c[2]]=true;
		return temp;
	}
	static void flip(int axisKey) {
		if(array==null) {
			Global.println("Error - No object loaded");
			return;
		}
		switch(axisKey) {
			case 0: {
				boolean[][] temp;
				int d=array.length;
				for(int k=0;k<d/2;k++) {
					temp=array[d-k-1];
					array[d-k-1]=array[k];
					array[k]=temp;
				}
				break;
			}
			case 1: {
				boolean[] temp;
				int h=array.length,
					d=array[0].length;
				for(int i=0;i<h;i++)
					for(int k=0;k<d/2;k++) {
						temp=array[i][d-k-1];
						array[i][d-k-1]=array[i][k];
						array[i][k]=temp;
					}
				break;
			}
			case 2: {
				boolean temp;
				int w=array.length,
					h=array[0].length,
					d=array[0][0].length;
				for(int i=0;i<w;i++)
					for(int j=0;j<h;j++)
						for(int k=0;k<d/2;k++) {
							temp=array[i][j][d-k-1];
							array[i][j][d-k-1]=array[i][j][k];
							array[i][j][k]=temp;
						}
				break;
			}
			default:
				Global.println("Invalid axis key.");
				return;
		}
	}
	static void rotate(int axisKey,boolean left) {
		if(array==null) {
			Global.println("Error - No object loaded");
			return;
		}
		boolean[][][] temp;
		int w,h,d;
		switch(axisKey) {
			case 0:
				w=array[0].length;
				h=array[0][0].length;
				d=array.length;
				temp=new boolean[d][h][w];
				for(int i=0;i<w;i++)
					for(int j=0;j<h;j++)
						for(int k=0;k<d;k++) {
							if(left)
								temp[k][h-j-1][i]=array[k][i][j];
							else
								temp[k][j][w-i-1]=array[k][i][j];
						}
				break;
			case 1:
				w=array[0][0].length;
				h=array.length;
				d=array[0].length;
				temp=new boolean[w][d][h];
				for(int i=0;i<w;i++)
					for(int j=0;j<h;j++)
						for(int k=0;k<d;k++) {
							if(left)
								temp[i][k][h-j-1]=array[j][k][i];
							else
								temp[w-i-1][k][j]=array[j][k][i];
						}
				break;
			case 2:
				w=array.length;
				h=array[0].length;
				temp=new boolean[h][w][array[0][0].length];
				for(int i=0;i<w;i++)
					for(int j=0;j<h;j++) {
						if(left)
							temp[h-j-1][i]=array[i][j];
						else
							temp[j][w-i-1]=array[i][j];
					}
				break;
			default:
				Global.println("Invalid axis key.");
				return;
		}
		array=temp;
	}
}