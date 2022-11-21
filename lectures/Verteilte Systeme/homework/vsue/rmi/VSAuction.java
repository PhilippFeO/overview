package vsue.rmi;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class VSAuction implements Externalizable{

	/* The auction name. */
	private String name;

	/* The currently highest bid for this auction. */
	private int price;


	public VSAuction(String name, int startingPrice) {
		this.name = name;
		this.price = startingPrice;
	}

	public VSAuction(){}


	public String getName() {
		return name;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(final int newPrice){
		this.price = newPrice;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		if(name.length() <= Integer.MAX_VALUE){
			out.writeUTF(name);
		}
		out.writeInt(price);
	}


	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.name = in.readUTF();
		this.price = in.readInt();
	}

}
