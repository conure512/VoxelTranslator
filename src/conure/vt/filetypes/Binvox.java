package conure.vt.filetypes;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;
import conure.vt.Global;
import conure.vt.VoxFileInterpreter;
/**Reads and writes to/from binvox files.*/
public final class Binvox extends VoxFileInterpreter {
	/**@return ".binvox"*/
	@Override
	public String getExtension() {
		return ".binvox";
	}
	@Override
	public boolean[][][] load(File source,String[] args) throws IOException {
		DataInputStream reader=new DataInputStream(new FileInputStream(source));
		String header="";
		int bitNum;
		while(!header.endsWith("data\n")) {
			bitNum=reader.read();
			if(bitNum==-1) {
				Global.println("Header parsing failed - End of file reached");
				reader.close();
				return null;
			}
			header+=(char)bitNum;
		}
		int[] dim=new int[3];
		Scanner lineReader=new Scanner(header),iter;
		String line;
		while(lineReader.hasNextLine()) {
			line=lineReader.nextLine();
			iter=new Scanner(line);
			switch(iter.next()) {
				case "dim":
					dim[0]=iter.nextInt();
					dim[1]=iter.nextInt();
					dim[2]=iter.nextInt();
					break;
			}
			iter.close();
		}
		lineReader.close();
		if(dim[0]==0||dim[1]==0||dim[2]==0) {
			Global.println("File specifies invalid dimensions");
			reader.close();
			return null;
		}
		line="";
		bitNum=0;
		boolean state=false;
		int count=0;
		boolean[][][] model=new boolean[dim[0]][dim[2]][dim[1]];
		load: for(int x=0;x<dim[0];x++)
			for(int z=0;z<dim[1];z++)
				for(int y=0;y<dim[2];y++) {
					count--;
					if(count<=0) {
						state=reader.read()>0;
						count=reader.read();
						if(count==-1)
							break load;
						count&=0xff;
					}
					if(state) {
						model[x][y][z]=true;
					}
				}
		reader.close();
		return model;
	}
	@Override
	public boolean export(boolean[][][] model,File dest,String[] args) throws IOException {
		boolean fit=false;
		int[] max=new int[3];
		max[0]=model.length;
		max[1]=model[0][0].length;
		max[2]=model[0].length;
		int absmax=max[0];
		if(absmax<max[1])
			absmax=max[1];
		if(absmax<max[2])
			absmax=max[2];
		double[] trans=new double[3];
		for(String arg:args)
			switch(arg.toLowerCase()) {
				case "-fit":
					fit=true;
					break;
				case "-center":
					for(int i=0;i<3;i++)
						trans[i]=max[i]/(-2.0*absmax);
					break;
				case "-centermass":
					int num=0;
					int[] c=new int[3];
					for(;c[0]<max[0];c[0]++)
						for(c[1]=0;c[1]<max[1];c[1]++)
							for(c[2]=0;c[2]<max[2];c[2]++)
								if(model[c[0]][c[2]][c[1]]) {
									num++;
									for(int i=0;i<3;i++)
										trans[i]+=c[i]+.5;
								}
					if(num>0)
						for(int i=0;i<3;i++)
							trans[i]/=-num*absmax;
					break;
				default:
					if(arg.startsWith("-t:")) {
						String[] coords=arg.split(",");
						try {
							trans[0]=Double.parseDouble(coords[0]);
							trans[1]=Double.parseDouble(coords[2]);
							trans[2]=Double.parseDouble(coords[1]);
						} catch(NumberFormatException|IndexOutOfBoundsException e) {
							Global.println("Translation must be 3 numeric values separated by commas.");
							trans[0]=trans[1]=trans[2]=0.0;
						}
					}
					break;
			}
		if(!fit)
			max[0]=max[1]=max[2]=absmax;
		DataOutputStream writer=new DataOutputStream(new FileOutputStream(dest));
		writer.writeBytes("#binvox 1\ndim "+max[0]+' '+max[1]+' '+max[2]+
				"\ntranslate "+trans[0]+' '+trans[1]+' '+trans[2]+"\nscale 1.0\ndata\n");
		int state=model[0][0][0]?1:0,count=0;
		boolean voxPresent=false;
		for(int x=0;x<max[0];x++)
			for(int z=0;z<max[1];z++)
				for(int y=0;y<max[2];y++) {
					try {
						voxPresent=model[x][y][z];
					} catch(IndexOutOfBoundsException e) {
						voxPresent=false;
					}
					if((state==1)==voxPresent&&count<255)
						count++;
					else {
						writer.write(state);
						writer.write(count);
						state=voxPresent?1:0;
						count=1;
					}
				}
		if(count>0) {
			writer.write(state);
			writer.write(count);
		}
		writer.close();
		return true;
	}
}