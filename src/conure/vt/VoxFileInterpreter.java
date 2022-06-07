package conure.vt;
import java.io.File;
import java.io.IOException;
/**Instances of this class transport data between the central program and a 3-D model file. Subclasses of this class should
 * specialize in files of a specific type (ie, extension) and should override one or both of the methods "load" and "export".*/
public abstract class VoxFileInterpreter {
	/**Used by the program to determine what type of file this class interprets.
	 * @return The extension as a String including the dot, for example ".binvox"*/
	public abstract String getExtension();
	/**Loads a 3D file to be passed back to the central program. Designed to be optionally overridden.
	 * @param source - The file to read from. The program will guarantee that "source" exists before calling this method.
	 * @param args - Additional loading specifications. If none apply, this parameter can be ignored. In the window console,
	 * this parameter is the user's input in the "Load Parameters" box. From the command line, all arguments after the
	 * load command are sent here.
	 * @return A 3D array of booleans representing the voxels in the model. Position [x][y][z] is true if a voxel
	 * is present, and false otherwise. If an error occurs while loading, this method should return null.
	 * @throws IOException if an I/O error occurs. This argument is added so that programmers
	 * don't have to catch this exception while implementing this method.*/
	public boolean[][][] load(File source,String[] args) throws IOException {
		return null;
	}
	/**Writes a 3D Voxel file using the program's loaded model. Designed to be optionally overridden.
	 * @param model - A 3D array of booleans representing the voxels in the model. Position [x][y][z] is true if a voxel
	 * is present, and false otherwise. This method should NOT mutate this variable.
	 * @param dest - The file to write to. The program will guarantee that "dest" is writable before calling this method.
	 * @param args - Additional export specifications. If none apply, this parameter can be ignored. In the window console,
	 * this parameter is the user's input in the "Export Parameters" box. From the command line, all arguments after the
	 * export command are sent here.
	 * @return true if the export was a success, false otherwise.
	 * @throws IOException if an I/O error occurs. This argument is added so that programmers
	 * don't have to catch this exception while implementing this method.*/
	public boolean export(boolean[][][] model,File dest,String[] args) throws IOException {
		return false;
	}
	final boolean canLoad() {
		try {
			getClass().getDeclaredMethod("load",File.class,String[].class);
			return true;
		} catch(NoSuchMethodException|SecurityException e) {
			return false;
		}
	}
	final boolean canExport() {
		try {
			getClass().getDeclaredMethod("export",boolean[][][].class,File.class,String[].class);
			return true;
		} catch(NoSuchMethodException|SecurityException e) {
			return false;
		}
	}
}