package conure.vt.filetypes;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import conure.vt.VoxFileInterpreter;
public class Particle_MCFunction extends VoxFileInterpreter {
	@Override
	public String getExtension() {
		return ".particle.mcfunction";
	}
	public boolean export(boolean[][][] model,File dest,String[] args) throws IOException {
		double scale=.125;
		String particle="minecraft:dust 1 1 1 1";
		for(String arg:args) {
			if(arg.startsWith("-s:")) try {
				scale=Double.parseDouble(arg.substring(3));
			} catch(Exception e) {}
			else if(arg.startsWith("-p:")) try {
				particle=arg.substring(3);
				if(!particle.contains(":"))
					particle="minecraft:"+particle;
			} catch(Exception e) {}
		}
		double[] offset=new double[3];
		offset[0]=model.length/2d;
		offset[1]=model[0].length/2d;
		offset[2]=model[0][0].length/2d;
		DataOutputStream writer=new DataOutputStream(new FileOutputStream(dest));
		double y,z;
		for(int j=0;j<model[0].length;j++) {
			y=(j-offset[1])*scale;
			for(int k=0;k<model[0][0].length;k++) {
				z=(k-offset[2])*scale;
				for(int i=0;i<model.length;i++)
					if(model[i][j][k])
						writer.writeBytes("particle "+particle+" ^"+((i-offset[0])*scale)+" ^"+y+" ^"+z+" 0 0 0 0 1 force\n");
			}
		}
		writer.close();
		return true;
	}
}