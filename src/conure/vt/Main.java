package conure.vt;
import java.io.File;
import java.util.Scanner;
final class Main {
	static String directory;
	static WindowConsole window=null;
	static {
		try {
			directory=new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent()+File.separatorChar;
		} catch(Exception e) {
			directory="";
		}
	}
	public static void main(String[] args) {
		try {
			Voxels.loadExtensions();
		} catch(Exception e) {
			System.out.println("Failed to fully load extensions");
		}
		if(args.length>0&&args[0].equals("-nogui"))
			runCmdConsole();
		else {
			window=new WindowConsole();
			window.setVisible(true);
		}
	}
	private static void runCmdConsole() {
		Scanner input=new Scanner(System.in),parser;
		File file;
		System.out.print("VOXEL TRANSLATOR "+Global.VERSION+"\n----\n"+directory+"\nEnter command, or \"help\" for a list of commands >");
		String cmd=input.nextLine(),arg1,extra;
		while(!cmd.toLowerCase().equals("exit")) {
			parser=new Scanner(cmd);
			if(parser.hasNext()) switch(parser.next().toLowerCase()) {
				case "cd":
					if(parser.hasNext()) {
						arg1=parser.next();
						file=new File(arg1);
						if(file.isDirectory())
							directory=file.getPath()+File.separatorChar;
						else
							System.out.println("Invalid directory path - file must be a folder.");
					} else
						System.out.println("Usage: cd <path>");
					break;
				case "load":
					if(parser.hasNext()) {
						arg1=parser.next();
						extra="";
						while(parser.hasNext())
							extra+=parser.next()+' ';
						try {
							Voxels.attemptLoad(arg1,directory,arg1.substring(arg1.lastIndexOf('.')),extra);
						} catch(Exception e) {
							System.out.println("Invalid file name.");
						}
					} else
						System.out.println("Usage: load <filename/path>");
					break;
				case "export":
					if(Voxels.array==null) {
						System.out.println("Error - no model loaded");
						break;
					}
					if(parser.hasNext()) {
						arg1=parser.next();
						extra="";
						while(parser.hasNext())
							extra+=parser.next()+' ';
						try {
							Voxels.attemptExport(arg1,directory,arg1.substring(arg1.lastIndexOf('.')),extra);
						} catch(Exception e) {
							System.out.println("Invalid file name.");
						}
					} else
						System.out.println("Usage: export <filename/path>");
					break;
				case "view":
					if(Voxels.array==null) {
						System.out.println("Error - no model loaded");
						break;
					}
					if(parser.hasNext()) switch(parser.next().toLowerCase()) {
						case "x":
							printView(0);
							break;
						case "y":
							printView(1);
							break;
						case "z":
							printView(2);
							break;
						default:
							System.out.println("Unknown dimension type.");
							break;
					} else
						System.out.println("Usage: view <x/y/z>");
					break;
				case "layer":
					if(Voxels.array==null) {
						System.out.println("Error - no model loaded");
						break;
					}
					if(parser.hasNext()) {
						arg1=parser.next();
						int d;
						try {
							d=Integer.parseInt(parser.next());
						} catch(NumberFormatException e) {
							System.out.println("Depth must be an integer.");
							break;
						} catch(Exception e) {
							System.out.println("Usage: layer <x/y/z> <depth>");
							break;
						}
						switch(arg1) {
							case "x":
								printLayer(0,d);
								break;
							case "y":
								printLayer(1,d);
								break;
							case "z":
								printLayer(2,d);
								break;
							default:
								System.out.println("Unknown dimension type.");
								break;
						}
					} else
						System.out.println("Usage: layer <x/y/z> <depth>");
					break;
				case "help": case "?":
					if(parser.hasNext()) switch(parser.next()) {
						case "cd":
							System.out.println("\ncd <path>\n"
									+"- Specifies the default directory that this program reads and writes to.\n"
									+"- Initially set as the folder containing this .jar file.\n"
									+"- When using \"load\" or \"export\", the program will first search this folder for the specified file or path.\n"
									+"- If the file is not found, the machine's default directory is used instead.\n"
									+"Example: cd "+directory);
							break;
						case "load":
							System.out.println("\nload <filename/path> [parameters]\n"
									+"- Loads a 3D model into the console from the specified file.\n"
									+"- Load format is determined by the file's extension.\n"
									+"- Type \"help cd\" for more info on how the program locates this file.\n"
									+"- Additional parameters may be added after the file name.\n"
									+"- This command must successfully run before calling export, view, or layer.\n"
									+"- If an object has already been loaded, this command will overwrite it.\n"
									+"Example: load Object1.obj -d:512");
							break;
						case "export":
							System.out.println("\nexport <filename/path> [parameters]\n"
									+"- Exports the loaded model into the specified file.\n"
									+"- Export format is determined by the specified extension.\n"
									+"- If the file already exists, it will be overwritten - otherwise, a new one will be created.\n"
									+"- Type \"help cd\" for more info on how the program locates this file.\n"
									+"- Additional parameters may be added after the file name.\n"
									+"Example: export NewObject.binvox -fit");
							break;
						case "view":
							System.out.println("\nview <x/y/z>\n"
									+"- Prints a flattened 2D rendering of the loaded model.\n"
									+"- Model is rendered from the perspective of a viewer on the specified axis.\n"
									+"Example: view x");
							break;
						case "layer":
							System.out.println("\nlayer <x/y/z> <depth>\n"
									+"- Prints one 2D layer of the loaded model at the specified depth.\n"
									+"- Cross section is normal to the specified axis.\n"
									+"Example: layer x 10");
							break;
						case "help": case "?":
							System.out.println("\nYou seem to already know how this command works.");
							break;
						case "exit":
							System.out.println("\nexit\n"
									+"- Ends the program.\n"
									+"- All external files will remain intact, but any internally loaded model will be discarded.");
							break; 
						default:
							System.out.println("Unknown command name.");
							break;
					} else
						System.out.println("Commands\n\n"
								+"cd <path>\n- Changes default directory.\n\n"
								+"load <filename/path> [parameters]\n- Loads a 3D model into the console.\n\n"
								+"export <filename/path> [parameters]\n- Exports the loaded model into a file.\n\n"
								+"view <x/y/z>\n- Prints an outline of the loaded object, viewed from the x, y, or z direction.\n\n"
								+"layer <x/y/z> <depth>\n- Prints a layer of the loaded object, viewed from the specified direction.\n\n"
								+"help <commandname>\n- Shows more detailed information on the specified command.\n\n"
								+"exit\n- Ends the program.");
					break;
				default:
					System.out.println("Unknown command. Type \"help\" for a list of commands.");
					break;
			}
			System.out.print('\n'+directory+"\nEnter command >");
			cmd=input.nextLine();
		}
		System.out.println("Program ended.");
		input.close();
	}
	private static void printView(int axisKey) {
		int w,h,d;
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
				System.out.println("Invalid axis key");
				return;
		}
		boolean line=false,vox=false;
		for(int j=0;j<h;j++) {
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
					System.out.print('0');
				else
					System.out.print(' ');
			}
			System.out.println();
		}
		System.out.println();
	}
	private static void printLayer(int axisKey,int layer) {
		int w,h,d;
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
				System.out.println("Invalid axis key");
				return;
		}
		if(layer<0||layer>=d) {
			System.out.println("Render failed - Layer is out of bounds.");
			return;
		}
		boolean vox=false;
		for(int j=0;j<h;j++) {
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
					System.out.print('0');
				else
					System.out.print(' ');
			}
			System.out.println();
		}
		System.out.println();
	}
}
