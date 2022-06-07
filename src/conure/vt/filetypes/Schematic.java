package conure.vt.filetypes;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import conure.vt.Global;
import conure.vt.VoxFileInterpreter;
public class Schematic extends VoxFileInterpreter {
	@Override
	public String getExtension() {
		return ".schematic";
	}
	@Override
	public boolean[][][] load(File source,String[] args) throws IOException {
		short[] size=new short[3];
		byte[] blocks=null;
		DataInputStream reader=new DataInputStream(new GZIPInputStream(new FileInputStream(source)));
		reader.skip(1);
		NBT.skipNextString(reader);
		file: while(reader.available()==1) {
			switch(reader.read()) {
				case NBT.END:
					break file;
				case NBT.BYTE:
					NBT.skipNextString(reader);
					reader.skip(1);
					break;
				case NBT.SHORT:
					switch(NBT.readNextString(reader)) {
						case "Width":
							size[0]=reader.readShort();
							break;
						case "Height":
							size[1]=reader.readShort();
							break;
						case "Length":
							size[2]=reader.readShort();
							break;
						default:
							reader.skip(2);
							break;
					}
					break;
				case NBT.INT:
				case NBT.FLOAT:
					NBT.skipNextString(reader);
					reader.skip(4);
					break;
				case NBT.LONG:
				case NBT.DOUBLE:
					NBT.skipNextString(reader);
					reader.skip(8);
					break;
				case NBT.BYTE_ARRAY:
					switch(NBT.readNextString(reader)) {
						case "Blocks":
							blocks=new byte[reader.readInt()];
							for(int i=0;i<blocks.length;i++)
								blocks[i]=(byte)reader.read();
							break;
						default:
							reader.skip(reader.readInt());
							break;
					}
					break;
				case NBT.STRING:
					NBT.skipNextString(reader);
					NBT.skipNextString(reader);
					break;
				case NBT.LIST:
					NBT.skipNextString(reader);
					NBT.skipNextList(reader);
					break;
				case NBT.COMPOUND:
					NBT.skipNextString(reader);
					NBT.skipNextCompound(reader);
					break;
				case NBT.INT_ARRAY:
					NBT.skipNextString(reader);
					reader.skip(4*reader.readInt());
					break;
				case NBT.LONG_ARRAY:
					NBT.skipNextString(reader);
					reader.skip(8*reader.readInt());
					break;
			}
		}
		if(blocks==null||size[0]==0||size[1]==0||size[2]==0) {
			Global.println("File is missing critical info.");
			return null;
		}
		ArrayList<Byte> selected=new ArrayList<Byte>();
		selected.add((byte)0);
		boolean selectVoxels=false;
		for(String arg:args) {
			if(arg.startsWith("-v:")) try {
				selected=parseByteList(arg.substring(3));
				selectVoxels=true;
			} catch(NumberFormatException e) {
				Global.println("Block IDs could not be resolved - defaulting to space=0");
			} else if(arg.startsWith("-s:")) try {
				selected=parseByteList(arg.substring(3));
				selectVoxels=false;
			} catch(NumberFormatException e) {
				Global.println("Space IDs could not be resolved - defaulting to 0");
			}
		}
		boolean[][][] model=new boolean[size[0]][size[1]][size[2]];
		for(int y=0;y<size[1];y++)
			for(int z=0;z<size[2];z++)
				for(int x=0;x<size[0];x++)
					if(selected.contains(blocks[(y*size[2]+z)*size[0]+x])==selectVoxels)
						model[x][y][z]=true;
		return model;
	}
	@Override
	public boolean export(boolean[][][] model,File dest,String[] args) throws IOException {
		long d=model.length*model[0].length*model[0][0].length;
		if(d>Integer.MAX_VALUE||d<1) {
			Global.println("Model's dimensions are invalid for a schematic file.");
			return false;
		}
		byte blockID=1,spaceID=0;
		for(String arg:args) {
			if(arg.startsWith("-v:")) try {
				blockID=Byte.parseByte(arg.substring(3));
			} catch(NumberFormatException e) {
				Global.println("Block ID could not be resolved - defaulting to 1");
			} else if(arg.startsWith("-s:")) try {
				spaceID=Byte.parseByte(arg.substring(3));
			} catch(NumberFormatException e) {
				Global.println("Space ID could not be resolved - defaulting to 0");
			}
		}
		DataOutputStream writer=new DataOutputStream(new GZIPOutputStream(new FileOutputStream(dest)));
		NBT.openTag(writer,NBT.COMPOUND,"Schematic");
		NBT.writeTagShort(writer,"Width",model.length);
		NBT.writeTagShort(writer,"Length",model[0][0].length);
		NBT.writeTagShort(writer,"Height",model[0].length);
		NBT.writeTagString(writer,"Materials","Alpha");
		NBT.openTag(writer,NBT.BYTE_ARRAY,"Blocks");
		writer.writeInt((int)d);
		for(int y=0;y<model[0].length;y++)
			for(int z=0;z<model[0][y].length;z++)
				for(int x=0;x<model.length;x++) {
					if(model[x][y][z])
						writer.write(blockID);
					else
						writer.write(spaceID);
				}
		NBT.openTag(writer,NBT.BYTE_ARRAY,"Data");
		writer.writeInt((int)d);
		for(int i=0;i<d;i++)
			writer.write(0);
		writer.write(NBT.END);
		writer.close();
		return true;
	}
	private static ArrayList<Byte> parseByteList(String str) throws NumberFormatException {
		ArrayList<Byte> list=new ArrayList<Byte>();
		for(String arg:str.split(","))
			list.add(Byte.parseByte(arg));
		return list;
	}
}