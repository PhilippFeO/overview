package vsue.communication;

import vsue.rmi.VSAuctionClient;

public class VSResponseTime {
    public static long[] responseTimes = new long[130];
    public static long start;
    public static long end;
    public static void main(String[] args) {
        int AUCTIONS = 1;

		checkArguments(args);

        String userName = args[0];
		VSAuctionClient client = new VSAuctionClient(userName);

		String registryHost = args[1];
		int registryPort = Integer.parseInt(args[2]);
		client.init(registryHost, registryPort);

		int k;
		for (k = 0; k < AUCTIONS; k++) {
            client.register("test" + k, 100, 100);
        }

		StringBuilder output = new StringBuilder();
		for (int l = 0; l < 10; l++) {
            int j = 0;
            for(int i=0; i < 120; i++) {
                if(i < 20) {}
                else {
                    start = System.currentTimeMillis();
                    client.list();
                    end = System.currentTimeMillis();
                    responseTimes[j] = end - start;
                    j++;

                }
            }

            double median = mittelwert(responseTimes);
            output.append(median);
            output.append(", ");
        }

        System.out.println("Mit " + k + " Auktionen: ");
        System.out.println(output);
        client.shutdown();
	}

    public static double mittelwert(long[] zahlen) {
        double summe = 0;
        for(double value : zahlen){
           summe += value;
        }
        return summe / zahlen.length;
    }

    private static void checkArguments(String[] args) {
		if (args.length < 3) {
			System.err.println("usage: java " + VSAuctionClient.class.getName() + " <user-name> <registry_host> <registry_port>");
			System.exit(1);
		}
	}
}
