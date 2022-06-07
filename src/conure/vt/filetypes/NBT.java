package conure.vt.filetypes;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import conure.vt.Global;
import conure.vt.VoxFileInterpreter;
/**Reads and writes to/from Minecraft NBT (Structure Block) files.*/
public final class NBT extends VoxFileInterpreter {
	static final byte END=0,BYTE=1,SHORT=2,INT=3,LONG=4,FLOAT=5,DOUBLE=6,
			BYTE_ARRAY=7,STRING=8,LIST=9,COMPOUND=10,INT_ARRAY=11,LONG_ARRAY=12;
	/**@return ".nbt"*/
	@Override
	public String getExtension() {
		return ".nbt";
	}
	@Override
	public boolean[][][] load(File source,String[] args) throws IOException {
		int[] size=new int[3];
		ArrayList<String> palette=new ArrayList<String>();
		HashMap<Integer,Integer> blocks=new HashMap<Integer,Integer>();
		int len,state=0,pos=0;
		DataInputStream reader=new DataInputStream(new GZIPInputStream(new FileInputStream(source)));
		reader.skip(1);
		skipNextString(reader);
		file: while(reader.available()==1)
			switch(reader.read()) {
				case END:
					break file;
				case INT:
					skipNextString(reader);
					reader.skip(4);
					break;
				case STRING:
					skipNextString(reader);
					skipNextString(reader);
					break;
				case LIST:
					switch(readNextString(reader)) {
						case "size":
							reader.skip(5);
							for(int i=0;i<3;i++)
								size[i]=reader.readInt();
							break;
						case "palette":
							reader.skip(1);
							len=reader.readInt();
							for(int i=0;i<len;i++) {
								palette_element: while(reader.available()==1)
									switch(reader.read()) {
										case END:
											break palette_element;
										case STRING:
											skipNextString(reader);
											palette.add(readNextString(reader));
											break;
										case COMPOUND:
											skipNextString(reader);
											skipNextCompound(reader);
											break;
									}
							}
							break;
						case "blocks":
							reader.skip(1);
							len=reader.readInt();
							for(int i=0;i<len;i++) {
								block: while(reader.available()==1)
									switch(reader.read()) {
										case END:
											break block;
										case INT:
											skipNextString(reader);
											state=reader.readInt();
											break;
										case LIST:
											skipNextString(reader);
											reader.skip(5);
											pos=(reader.readInt()<<16)+(reader.readInt()<<8)+reader.readInt();
											break;
										case COMPOUND:
											skipNextString(reader);
											skipNextCompound(reader);
											break;
									}
								blocks.put(pos,state);
							}
							break;
						default:
							skipNextList(reader);
							break;
					}
					break;
				default:
					Global.println("Incorrect file format");
					reader.close();
					return null;
			}
		reader.close();
		String[] selected=new String[] {
			"minecraft:air",
			"minecraft:cave_air",
			"minecraft:water",
			"minecraft:lava"
		};
		boolean selectVoxels=false;
		for(String arg:args) {
			if(arg.startsWith("-v:")) {
				selectVoxels=true;
				selected=parseBlockTypes(arg.substring(3));
			} else if(arg.startsWith("-s:")) {
				selectVoxels=false;
				selected=parseBlockTypes(arg.substring(3));
			}
		}
		ArrayList<Integer> indices=new ArrayList<Integer>();
		int index;
		for(String s:selected) {
			index=palette.indexOf(s);
			if(index!=-1)
				indices.add(index);
		}
		boolean[][][] model=new boolean[size[0]][size[1]][size[2]];
		for(int x=0;x<size[0];x++)
			for(int y=0;y<size[1];y++)
				for(int z=0;z<size[2];z++)
					if(blocks.containsKey((x<<16)+(y<<8)+z)&&(indices.contains(blocks.get((x<<16)+(y<<8)+z))==selectVoxels))
						model[x][y][z]=true;
		return model;
	}
	@Override
	public boolean export(boolean[][][] model,File dest,String[] args) throws IOException {
		int[] d= {model.length,model[0].length,model[0][0].length};
		for(int i=0;i<3;i++)
			if(d[i]>48) {
				Global.println("Minecraft cannot load models over 48 blocks in length");
				return false;
			}
		String voxel="minecraft:stone",space=null;
		for(String arg:args) {
			if(arg.startsWith("-v:")) {
				voxel=arg.substring(3);
				if(voxel.indexOf(':')==-1)
					voxel="minecraft:"+voxel;
			} else if(arg.startsWith("-s:")) {
				space=arg.substring(3);
				if(space.indexOf(':')==-1)
					space="minecraft:"+space;
			}
		}
		int voxNum;
		if(space==null) {
			voxNum=0;
			for(int x=0;x<model.length;x++)
				for(int y=0;y<model[x].length;y++)
					for(int z=0;z<model[x][y].length;z++)
						if(model[x][y][z])
							voxNum++;
		} else
			voxNum=d[0]*d[1]*d[2];
		DataOutputStream writer=new DataOutputStream(new GZIPOutputStream(new FileOutputStream(dest)));
		openTag(writer,COMPOUND,"");
		openTagList(writer,"size",INT,3);
		writer.writeInt(d[0]);
		writer.writeInt(d[1]);
		writer.writeInt(d[2]);
		openTagList(writer,"palette",COMPOUND,space==null?1:2);
		writeTagString(writer,"Name",voxel);
		writer.write(END);
		if(space!=null) {
			writeTagString(writer,"Name",space);
			writer.write(END);
		}
		openTagList(writer,"blocks",COMPOUND,voxNum);
		for(int x=0;x<d[0];x++)
			for(int y=0;y<d[1];y++)
				for(int z=0;z<d[2];z++) {
					if(model[x][y][z]) {
						writeTagInt(writer,"state",0);
						openTagList(writer,"pos",INT,3);
						writer.writeInt(x);
						writer.writeInt(y);
						writer.writeInt(z);
						writer.write(END);
					} else if(space!=null) {
						writeTagInt(writer,"state",1);
						openTagList(writer,"pos",INT,3);
						writer.writeInt(x);
						writer.writeInt(y);
						writer.writeInt(z);
						writer.write(END);
					}
				}
		writer.write(END);
		writer.close();
		return true;
	}
	private static String[] parseBlockTypes(String str) {
		String[] args=str.split(",");
		for(int i=0;i<args.length;i++)
			if(!args[i].contains(":"))
				args[i]="minecraft:"+args[i];
		return args;
	}
	static String readNextString(DataInputStream reader) throws IOException {
		short len=reader.readShort();
		String name="";
		for(int i=0;i<len;i++)
			name+=(char)reader.read();
		return name;
	}
	static void skipNextString(DataInputStream reader) throws IOException {
		reader.skip(reader.readShort());
	}
	static void skipNextList(DataInputStream reader) throws IOException {
		byte tagId=(byte)reader.read();
		int len=reader.readInt();
		switch(tagId) {
			case BYTE:
				reader.skip(len);
				return;
			case SHORT:
				reader.skip(2*len);
				return;
			case INT:
			case FLOAT:
				reader.skip(4*len);
				return;
			case LONG:
			case DOUBLE:
				reader.skip(8*len);
				return;
			case BYTE_ARRAY:
				for(int i=0;i<len;i++)
					reader.skip(reader.readInt());
				return;
			case STRING:
				for(int i=0;i<len;i++)
					skipNextString(reader);
				return;
			case LIST:
				for(int i=0;i<len;i++)
					skipNextList(reader);
				return;
			case COMPOUND:
				for(int i=0;i<len;i++)
					skipNextCompound(reader);
				return;
			case INT_ARRAY:
				for(int i=0;i<len;i++)
					reader.skip(4*reader.readInt());
				return;
			case LONG_ARRAY:
				for(int i=0;i<len;i++)
					reader.skip(8*reader.readInt());
				return;
		}
	}
	static void skipNextCompound(DataInputStream reader) throws IOException {
		byte id;
		while(reader.available()==1) {
			id=(byte)reader.read();
			if(id==END)
				return;
			skipNextString(reader);
			switch(id) {
				case BYTE:
					reader.skip(1);
					break;
				case SHORT:
					reader.skip(2);
					break;
				case INT:
				case FLOAT:
					reader.skip(4);
					break;
				case LONG:
				case DOUBLE:
					reader.skip(8);
					break;
				case BYTE_ARRAY:
					reader.skip(reader.readInt());
					break;
				case STRING:
					skipNextString(reader);
					break;
				case LIST:
					skipNextList(reader);
					break;
				case COMPOUND:
					skipNextCompound(reader);
					break;
				case INT_ARRAY:
					reader.skip(4*reader.readInt());
					break;
				case LONG_ARRAY:
					reader.skip(8*reader.readInt());
					break;
			}
		}
	}
	static void writeTagShort(DataOutputStream writer,String name,int n) throws IOException {
		openTag(writer,SHORT,name);
		writer.writeShort(n);
	}
	static void writeTagInt(DataOutputStream writer,String name,int n) throws IOException {
		openTag(writer,INT,name);
		writer.writeInt(n);
	}
	static void writeTagString(DataOutputStream writer,String name,String data) throws IOException {
		openTag(writer,STRING,name);
		int len=data.length();
		writer.write(new byte[] {(byte)(len>>8),(byte)len});
		writer.writeBytes(data);
	}
	static void openTagList(DataOutputStream writer,String name,byte tagId,int size) throws IOException {
		openTag(writer,LIST,name);
		writer.write(tagId);
		writer.writeInt(size);
	}
	static void openTag(DataOutputStream writer,byte id,String name) throws IOException {
		int len=name.length();
		writer.write(new byte[] {id,(byte)(len>>8),(byte)len});
		writer.writeBytes(name);
	}
}