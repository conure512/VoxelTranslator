package conure.vt.filetypes;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import conure.vt.Global;
import conure.vt.VoxFileInterpreter;
/**Reads and writes to/from .vox files.*/
public final class Vox extends VoxFileInterpreter {
	/**@return ".vox"*/
	@Override
	public String getExtension() {
		return ".vox";
	}
	@Override
	public boolean[][][] load(File source,String[] args) throws IOException {
		int frame=0;
		for(String arg:args) {
			if(arg.startsWith("-f:")) try {
				frame=Integer.parseInt(arg.substring(3));
			} catch(NumberFormatException e) {
				Global.println("Frame index could not be resolved - loading frame 0");
			}
		}
		if(frame<0) {
			Global.println("Frame index cannot be negative - loading frame 0");
			frame=0;
		}
		int current=0;
		DataInputStream reader=new DataInputStream(new FileInputStream(source));
		int[] size=null;
		ArrayList<short[]> voxels=new ArrayList<short[]>();
		short[] vox;
		int num;
		boolean anim=false;
		reader.skip(20);
		while(reader.available()>0)
			switch(readChunkID(reader)) {
				case "PACK":
					reader.skip(8);
					int max=Global.readIntLittleEndian(reader)-1;
					if(frame>max) {
						Global.println("Max frame index is "+max+" - loading frame 0");
						frame=0;
					}
					anim=true;
					break;
				case "SIZE":
					if(!anim&&frame!=0) {
						Global.println("Model contains only 1 frame - loading that one");
						frame=0;
					}
					if(current==frame) {
						reader.skip(8);
						size=new int[3];
						size[0]=Global.readIntLittleEndian(reader);
						size[1]=Global.readIntLittleEndian(reader);
						size[2]=Global.readIntLittleEndian(reader);
					} else
						reader.skip(20);
					break;
				case "XYZI":
					if(current==frame) {
						reader.skip(8);
						num=Global.readIntLittleEndian(reader);
						for(int i=0;i<num;i++) {
							vox=new short[4];
							vox[0]=(short)reader.read();
							vox[1]=(short)reader.read();
							vox[2]=(short)reader.read();
							vox[3]=(short)reader.read();
							voxels.add(vox);
						}
					} else
						reader.skip((long)Global.readIntLittleEndian(reader)+Global.readIntLittleEndian(reader));
					current++;
					break;
				default:
					reader.skip((long)Global.readIntLittleEndian(reader)+Global.readIntLittleEndian(reader));
					break;
			}
		reader.close();
		if(size==null) {
			Global.println("Incorrect file format.");
			return null;
		}
		boolean[][][] model=new boolean[size[0]][size[1]][size[2]];
		for(short[] v:voxels)
			model[v[0]][v[1]][v[2]]=true;
		return model;
	}
	@Override
	public boolean export(boolean[][][] model,File dest,String[] args) throws IOException {
		int voxNum=0;
		for(int x=0;x<model.length;x++)
			for(int y=0;y<model[x].length;y++)
				for(int z=0;z<model[x][y].length;z++)
					if(model[x][y][z])
						voxNum++;
		DataOutputStream writer=new DataOutputStream(new FileOutputStream(dest));
		writer.write("VOX ".getBytes());
		writeInts(writer,150);
		writer.writeBytes("MAIN");
		writeInts(writer,0,4*voxNum+40);
		writer.writeBytes("SIZE");
		writeInts(writer,12,0,model.length,model[0].length,model[0][0].length);
		writer.writeBytes("XYZI");
		writeInts(writer,4+4*voxNum,0,voxNum);
		for(int x=0;x<model.length;x++)
			for(int y=0;y<model[x].length;y++)
				for(int z=0;z<model[x][y].length;z++)
					if(model[x][y][z]) {
						writer.write(x);
						writer.write(y);
						writer.write(z);
						writer.write(1);
					}
		return true;
	}
	private static String readChunkID(DataInputStream reader) throws IOException {
		String id="";
		for(int i=0;i<4;i++)
			id+=(char)reader.read();
		return id;
	}
	private static void writeInts(DataOutputStream writer,int... nums) throws IOException {
		for(int n:nums)
			writer.write(new byte[] {(byte)n,(byte)(n>>8),(byte)(n>>16),(byte)(n>>24)});
	}
}